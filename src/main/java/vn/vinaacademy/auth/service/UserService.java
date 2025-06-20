package vn.vinaacademy.auth.service;


import vn.vinaacademy.auth.dto.UpdateUserInfoRequest;
import vn.vinaacademy.auth.dto.UserDto;
import vn.vinaacademy.auth.dto.UserViewDto;

import java.util.UUID;

public interface UserService {
    void createTestingData();

    UserDto getCurrentUser();
    
    UserDto updateUserInfo(UpdateUserInfoRequest request);
    
    UserViewDto viewUser(UUID userId);
}
