package vn.vinaacademy.auth.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import vn.vinaacademy.auth.entity.Role;
import vn.vinaacademy.auth.entity.User;
import vn.vinaacademy.common.constant.AppConstants;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    @Value("${application.jwt.accessToken.expiration:3600}")
    private int accessTokenExpirationTime;
    @Value("${application.jwt.refreshToken.expiration:86400}")
    private int refreshTokenExpirationTime;

    public String generateAccessToken(UserDetails userDetails) {
        return jwtEncoder.encode(JwtEncoderParameters.from(createClaims(userDetails, accessTokenExpirationTime)))
                .getTokenValue();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return jwtEncoder.encode(JwtEncoderParameters.from(createClaims(userDetails, refreshTokenExpirationTime)))
                .getTokenValue();
    }

    public LocalDateTime getExpirationTime(String token) {
        return LocalDateTime.ofInstant((Instant) extractClaims(token).get("exp"), ZoneId.of(AppConstants.TIME_ZONE));
    }

    public boolean isValidToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        Instant expired = (Instant) extractClaims(token).get("exp");
        return expired.isBefore(Instant.now());
    }

    public String extractUsername(String token) {
        return (String) extractClaims(token).get("sub");
    }

    public String extractUserId(String token) {
        return (String) extractClaims(token).get("userId");
    }

    public String extractEmail(String token) {
        return (String) extractClaims(token).get("email");
    }

    public String extractRoles(String token) {
        List<String> rolesList = (List<String>) extractClaims(token).get("roles");
        String[] roles = rolesList.toArray(new String[0]);
        return String.join(",", roles);
    }

    private Map<String, Object> extractClaims(String token) {
        return jwtDecoder.decode(token).getClaims();
    }

    public int getAccessTokenExpirationSeconds() {
        return accessTokenExpirationTime; // or whatever your configured JWT expiration time is
    }

    private JwtClaimsSet createClaims(UserDetails userDetails, int expiredTime) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails cannot be null");
        }
        if (expiredTime <= 0) {
            throw new IllegalArgumentException("Expiration time must be greater than zero");
        }

        JwtClaimsSet.Builder claimsSet = JwtClaimsSet.builder()
                .issuer(userDetails.getUsername())
                .subject(userDetails.getUsername())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(expiredTime))
                .claim("scope", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toArray(String[]::new))
                .claim("sub", userDetails.getUsername()); // Subject claim

        if (userDetails instanceof User user) {
            claimsSet.claim("userId", user.getId());
            claimsSet.claim("email", user.getEmail());
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                claimsSet.claim("roles", user.getRoles().stream()
                        .map(Role::getCode)
                        .toArray(String[]::new));
            }
        } else {
            throw new IllegalArgumentException("UserDetails must be an instance of User");
        }

        return claimsSet.build();
    }

    public boolean isValidToken(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
