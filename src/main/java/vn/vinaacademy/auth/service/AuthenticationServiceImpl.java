package vn.vinaacademy.auth.service;

import lombok.extern.slf4j.Slf4j;
import vn.vinaacademy.common.config.UrlBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vinaacademy.auth.dto.mapper.UserMapper;
import vn.vinaacademy.auth.repository.UserRepository;
import vn.vinaacademy.auth.dto.*;
import vn.vinaacademy.auth.entity.ActionToken;
import vn.vinaacademy.auth.entity.RefreshToken;
import vn.vinaacademy.auth.enums.ActionTokenType;
import vn.vinaacademy.auth.helpers.SecurityHelper;
import vn.vinaacademy.auth.repository.ActionTokenRepository;
import vn.vinaacademy.auth.repository.RefreshTokenRepository;
import vn.vinaacademy.common.utils.JwtUtils;
import vn.vinaacademy.auth.entity.User;
import vn.vinaacademy.auth.repository.RoleRepository;
import vn.vinaacademy.common.constant.AuthConstants;
import vn.vinaacademy.common.exception.BadRequestException;
import vn.vinaacademy.common.exception.RetryableException;
import vn.vinaacademy.common.utils.RandomUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    //    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActionTokenRepository actionTokenRepository;

    private final UrlBuilder urlBuilder;
    //    private final LogService logService;
    private final HttpServletRequest httpServletRequest;
    private final RoleRepository roleRepository;
    private final SecurityHelper securityHelper;


    /**
     * Registers a new user in the system.
     *
     * @param registerRequest The registration request containing user details including email, password, retyped password and full name
     * @throws BadRequestException if passwords don't match or if email already exists in the system.
     *                             The user account is initially disabled and requires email verification to be activated.
     */
    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (!StringUtils.equals(registerRequest.getPassword(), registerRequest.getRetypedPassword())) {
            throw BadRequestException.message("Mật khẩu không khớp");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw BadRequestException.message("Email đã tồn tại");
        }
        User user = UserMapper.INSTANCE.toUser(registerRequest);
        String username = generateUsername(registerRequest.getFullName());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(false);
        user.setRoles(Set.of(roleRepository.findByCode(AuthConstants.STUDENT_ROLE)));
//        user.setRoles(List.of(roleRepository.findByCode(AuthConstants.STUDENT_ROLE)));
        user = userRepository.save(user);

        String token = RandomUtils.generateUUID();

        ActionToken actionToken = ActionToken.builder()
                .user(user)
                .type(ActionTokenType.VERIFY_ACCOUNT)
                .token(token)
                .expiredAt(LocalDateTime.now().plusHours(AuthConstants.ACTION_TOKEN_EXPIRED_HOURS))
                .build();
        actionTokenRepository.save(actionToken);

        log.info("Đăng ký thành công cho người dùng: {}", user.getEmail());
//        emailService.sendVerificationEmail(user.getEmail(), actionToken.getToken());
    }

    @Retryable(retryFor = {RetryableException.class}, maxAttempts = 3)
    private String generateUsername(String fullName) {
        String username = fullName.toLowerCase().replaceAll("\\s+", "");
        username = username.substring(0, Math.min(username.length(), 10))
                + RandomUtils.generateRandomString(5);
        if (userRepository.existsByUsername(username)) {
            throw RetryableException.message("Tên đăng nhập đã tồn tại");
        }
        return username;
    }

    /**
     * Authenticates a user and generates access and refresh tokens.
     *
     * @param loginRequest The login request containing the user's email and password.
     * @return An AuthenticationResponse containing access and refresh tokens.
     * @throws BadRequestException if the user is not found, not enabled, or locked.
     */
    public AuthenticationResponse login(AuthenticationRequest loginRequest) {
        Authentication authentication = authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
        if (userDetails == null) {
            throw BadRequestException.message("Không tìm thấy người dùng: " + loginRequest.getEmail());
        }

        if (!userDetails.isEnabled()) {
            throw BadRequestException.message("Tài khoản chưa được xác thực");
        }

        if (!userDetails.isAccountNonLocked()) {
            throw BadRequestException.message("Tài khoản đã bị khóa");
        }


        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);


        RefreshToken token = RefreshToken.builder()
                .token(refreshToken)
                .username(userDetails.getUsername())
                .expireTime(jwtService.getExpirationTime(refreshToken))
                .build();
        refreshTokenRepository.save(token);

        log.info("Đăng nhập thành công cho người dùng: {}", userDetails.getUsername());
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    private Authentication authenticateUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication;

        } catch (AuthenticationException ex) {
            String message = authenticationExceptionMessage(ex);
            if (ex instanceof DisabledException) {
                handleDisabledException(email);
            }
            throw BadRequestException.message(message);
        }
    }

    private void handleDisabledException(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy người dùng"));

        ActionToken token = actionTokenRepository.findForUpdate(user, ActionTokenType.VERIFY_ACCOUNT)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy token"));

        LocalDateTime now = LocalDateTime.now();
        if (token.getExpiredAt().isBefore(now)) {
            token.setExpiredAt(now.plusHours(AuthConstants.ACTION_TOKEN_EXPIRED_HOURS));
            actionTokenRepository.save(token);
//            emailService.sendVerificationEmail(email, token.getToken());
        }
    }


    private static String authenticationExceptionMessage(AuthenticationException ex) {
        String message = "Lỗi xác thực không xác định";
        if (ex instanceof BadCredentialsException) {
            message = "Sai tên đăng nhập hoặc mật khẩu";
        } else if (ex instanceof DisabledException) {
            message = "Tài khoản chưa được xác thực, vui lòng kiểm tra email";
        } else if (ex instanceof LockedException) {
            message = "Tài khoản đã bị khóa";
        } else if (ex instanceof AccountExpiredException) {
            message = "Tài khoản đã hết hạn";
        }
        return message;
    }

    /**
     * Resends the verification email to the specified user.
     *
     * @param email The email address of the user.
     * @throws BadRequestException if the user or verification token is not found.
     */
    @Override
    @Transactional
    public void resendNewVerificationEmail(String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy người dùng: " + email));

        ActionToken actionToken = actionTokenRepository.findByUserAndType(user, ActionTokenType.VERIFY_ACCOUNT)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy token xác thực"));

        actionToken.setExpiredAt(LocalDateTime.now().plusHours(AuthConstants.ACTION_TOKEN_EXPIRED_HOURS));

        actionTokenRepository.save(actionToken);

        log.info("Gửi lại email xác thực cho người dùng: {}", user.getEmail());

//        emailService.sendVerificationEmail(user.getEmail(), actionToken.getToken());
    }

    /**
     * Logs out a user by invalidating the provided refresh token.
     *
     * @param refreshToken The refresh token request to invalidate.
     * @throws BadRequestException if the token is invalid.
     */
    public void logout(RefreshTokenRequest refreshToken) {
        String username = jwtService.extractUsername(JwtUtils.getJwtToken(httpServletRequest));

        RefreshToken token = refreshTokenRepository.findByTokenAndUsername(refreshToken.getRefreshToken(),
                        username)
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        refreshTokenRepository.delete(token);

        log.info("Đăng xuất thành công cho người dùng: {}", username);
    }

    /**
     * Refreshes the access token using the provided refresh token.
     *
     * @param refreshToken The refresh token request.
     * @return An AuthenticationResponse containing a new access token and the provided refresh token.
     * @throws BadRequestException if the token is invalid or expired.
     */
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken.getRefreshToken())
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        if (token.getExpireTime().isBefore(LocalDateTime.now())) {
            throw BadRequestException.message("Token đã hết hạn");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(token.getUsername());

        String accessToken = jwtService.generateAccessToken(userDetails);
        return new AuthenticationResponse(accessToken, refreshToken.getRefreshToken());
    }

    /**
     * Verifies a user account using a verification token and signature.
     *
     * @param token     The verification token.
     * @param signature The signature to validate the token.
     * @throws BadRequestException if the signature or token is invalid.
     */
    @Transactional
    @Override
    public void verifyAccount(String token, String signature) {
        if (!urlBuilder.isSignatureValid(token, signature)) {
            throw BadRequestException.message("Chữ ký không hợp lệ");
        }

        ActionToken actionToken = actionTokenRepository.findByTokenAndType(token, ActionTokenType.VERIFY_ACCOUNT)
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        User user = actionToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        actionTokenRepository.delete(actionToken);

//        emailService.sendWelcomeEmail(user);

        log.info("Xác thực tài khoản thành công cho người dùng: {}", user.getEmail());
    }

    /**
     * Initiates the password reset process for a given user's email.
     *
     * @param email The email address of the user.
     * @throws BadRequestException if the user is not found.
     */
    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy người dùng: " + email));

        String token = RandomUtils.generateUUID();

        ActionToken actionToken = ActionToken.builder()
                .user(user)
                .type(ActionTokenType.RESET_PASSWORD)
                .expiredAt(LocalDateTime.now().plusHours(AuthConstants.ACTION_TOKEN_EXPIRED_HOURS))
                .token(token)
                .build();
        actionTokenRepository.save(actionToken);

//        emailService.sendPasswordResetEmail(user, actionToken.getToken());

        log.info("Yêu cầu đặt lại mật khẩu thành công cho người dùng: {}", user.getEmail());
    }

    /**
     * Validates a password reset token.
     *
     * @param request The reset password request containing the token and signature.
     * @return true if the token is valid and not expired; false otherwise.
     * @throws BadRequestException if the token is invalid.
     */
    @Override
    public boolean checkResetPasswordToken(ResetPasswordRequest request) {
        if (!urlBuilder.isSignatureValid(request.getToken(), request.getSignature())) {
            return false;
        }
        ActionToken actionToken = actionTokenRepository.findByTokenAndType(request.getToken(), ActionTokenType.RESET_PASSWORD)
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        return actionToken.getExpiredAt().isAfter(LocalDateTime.now());
    }

    /**
     * Resets the user's password using a valid token and signature.
     *
     * @param request The reset password request containing the new password, token, and signature.
     * @throws BadRequestException if the signature is invalid, or the token is invalid or expired.
     */
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!urlBuilder.isSignatureValid(request.getToken(), request.getSignature())) {
            throw BadRequestException.message("Chữ ký không hợp lệ");
        }

        ActionToken actionToken = actionTokenRepository.findByTokenAndType(request.getToken(), ActionTokenType.RESET_PASSWORD)
                .orElseThrow(() -> BadRequestException.message("Token không hợp lệ"));

        if (actionToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw BadRequestException.message("Token đã hết hạn");
        }

        if (!StringUtils.equals(request.getPassword(), request.getRetypedPassword())) {
            throw BadRequestException.message("Mật khẩu không khớp");
        }

        User user = actionToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        actionTokenRepository.delete(actionToken);

        log.info("Đặt lại mật khẩu thành công cho người dùng: {}", user.getEmail());
    }

    @Override
    @Transactional
    public boolean changePassword(ChangePasswordRequest request) {
        User user = securityHelper.getCurrentUser();
        if (!StringUtils.equals(request.getNewPassword(), request.getRetypedPassword())) {
            throw BadRequestException.message("Mật khẩu mới không khớp");
        }

        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw BadRequestException.message("Mật khẩu hiện tại không chính xác");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Đổi mật khẩu thành công cho người dùng: {}", user.getEmail());
        return true;
    }
}
