package vn.vinaacademy.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vinaacademy.user.dto.UserDto;
import vn.vinaacademy.user.service.UserService;
import vn.vinaacademy.common.response.ApiResponse;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/users")
@Slf4j
public class InternalUserController {
    private final UserService userService;
    
    @Operation(summary = "Lấy thông tin user từ id - Internal use only")
    @GetMapping("/{userId}")
    public ApiResponse<UserDto> getUserById(@PathVariable UUID userId) {
        UserDto user = userService.getUserById(userId);
        log.debug("Internal call: get user info for user "+user.getId());
        return ApiResponse.success(user);
    }
}
    