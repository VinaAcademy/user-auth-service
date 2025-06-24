package vn.vinaacademy.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.vinaacademy.user.dto.UpdateUserInfoRequest;
import vn.vinaacademy.user.dto.UserDto;
import vn.vinaacademy.user.dto.UserViewDto;
import vn.vinaacademy.user.service.UserService;
import vn.vinaacademy.common.response.ApiResponse;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserDto> getCurrentUser() {
        return ApiResponse.success("Get current user successfully",
                userService.getCurrentUser());
    }
    
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhập thông tin user")
    @PutMapping("/update-info")
    public ApiResponse<UserDto> updateUserInfo(@RequestBody @Valid UpdateUserInfoRequest request) {
        UserDto updatedUser = userService.updateUserInfo(request);
        log.debug("update info for user "+updatedUser.getId());
        return ApiResponse.success(updatedUser);
    }    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem thông tin user từ id")
    @GetMapping("/view/{userId}")
    public ApiResponse<UserViewDto> viewUserInfo(@PathVariable UUID userId) {
        UserViewDto viewUser = userService.viewUser(userId);
        log.debug("get view info for user "+viewUser.getId());
        return ApiResponse.success(viewUser);
    }
}
