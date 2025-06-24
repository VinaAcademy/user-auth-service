package vn.vinaacademy.user.service;


import vn.vinaacademy.user.dto.UpdateUserInfoRequest;
import vn.vinaacademy.user.dto.UserDto;
import vn.vinaacademy.user.dto.UserViewDto;

import java.util.UUID;

public interface UserService {
    void createTestingData();

    UserDto getCurrentUser();
    
    UserDto updateUserInfo(UpdateUserInfoRequest request);
    
    UserViewDto viewUser(UUID userId);
    
    UserDto getUserById(UUID userId);
}
