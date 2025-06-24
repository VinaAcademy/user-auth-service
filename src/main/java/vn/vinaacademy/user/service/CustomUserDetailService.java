package vn.vinaacademy.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vinaacademy.user.repository.UserRepository;
import vn.vinaacademy.user.entity.User;
import vn.vinaacademy.common.exception.BadRequestException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 15; // 15 minutes

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(username).orElseThrow(() -> BadRequestException.message("User is invalid."));
        if (!user.isEnabled() && isLockTimeExpired(user)) {
            unlockAccount(user);
        }

        if (!user.isEnabled()) {
            throw BadRequestException.message("Account is locked. Please try again later.");
        }
        return user;
    }

    @Transactional
    public void increaseFailedAttempts(String username) {
        User user = userRepository.findByEmailWithRoles(username).orElse(null);
        if (user == null) {
            return;
        }
        int newFailedAttempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(newFailedAttempts);

        if (newFailedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(user);
        } else {
            userRepository.save(user);
        }
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        User user = userRepository.findByEmailWithRoles(username).orElse(null);
        if (user == null) {
            return;
        }
        user.setFailedAttempts(0);
        userRepository.save(user);
    }

    private void lockAccount(User user) {
        user.setEnabled(false);
        user.setLockTime(LocalDateTime.now().plusMinutes(LOCK_TIME_DURATION));
        userRepository.save(user);
    }

    private boolean isLockTimeExpired(User user) {
        LocalDateTime lockTime = user.getLockTime();
        return lockTime != null && lockTime.isBefore(LocalDateTime.now());
    }

    private void unlockAccount(User user) {
        user.setEnabled(true);
        user.setFailedAttempts(0);
        user.setLockTime(null);
        userRepository.save(user);
    }
}
