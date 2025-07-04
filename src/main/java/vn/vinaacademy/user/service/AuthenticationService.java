package vn.vinaacademy.user.service;

import vn.vinaacademy.user.dto.*;

public interface AuthenticationService {
    void register(RegisterRequest registerRequest);

    AuthenticationResponse login(AuthenticationRequest loginRequest);

    void resendNewVerificationEmail(String email);

    void verifyAccount(String token, String signature);

    void logout(RefreshTokenRequest refreshToken);

    AuthenticationResponse refreshToken(RefreshTokenRequest refreshToken);


    void forgotPassword(String email);

    boolean checkResetPasswordToken(ResetPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
    
    boolean changePassword(ChangePasswordRequest request);
}
