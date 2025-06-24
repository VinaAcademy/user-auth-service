package vn.vinaacademy.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vinaacademy.common.constant.AuthConstants;
import vn.vinaacademy.user.entity.Role;
import vn.vinaacademy.user.entity.User;
import vn.vinaacademy.user.service.JwtService;

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
                .roles(Set.of(Role.builder().id(1L).code(AuthConstants.ADMIN_ROLE).build(),
                        Role.builder().id(2L).code(AuthConstants.STAFF_ROLE).build(),
                        Role.builder().id(3L).code(AuthConstants.INSTRUCTOR_ROLE).build(),
                        Role.builder().id(4L).code(AuthConstants.STUDENT_ROLE).build()))
                .build());
        return "Generated JWT Token: " + token;
    }
}
