package vn.vinaacademy.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vinaacademy.auth.entity.Role;
import vn.vinaacademy.auth.entity.User;
import vn.vinaacademy.auth.service.JwtService;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class TestController {
    @Autowired
    private JwtService jwtService;

    // Example endpoint to test JWT generation
    @RequestMapping("/test")
    public String testJwtGeneration() {
        // Generate a JWT token for testing purposes
        String token = jwtService.generateRefreshToken(User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@gmail.com")
                .roles(Set.of(Role.builder().code("USER").build()))
                .build());
        return "Generated JWT Token: " + token;
    }
}
