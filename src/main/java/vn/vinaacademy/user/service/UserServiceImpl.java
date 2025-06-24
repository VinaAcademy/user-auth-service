package vn.vinaacademy.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vinaacademy.user.dto.mapper.UserMapper;
import vn.vinaacademy.user.repository.UserRepository;
import vn.vinaacademy.user.helpers.SecurityHelper;
import vn.vinaacademy.user.dto.UpdateUserInfoRequest;
import vn.vinaacademy.user.dto.UserDto;
import vn.vinaacademy.user.dto.UserViewDto;
import vn.vinaacademy.user.entity.User;
import vn.vinaacademy.user.entity.Role;
import vn.vinaacademy.user.repository.RoleRepository;
import vn.vinaacademy.common.constant.AuthConstants;
import vn.vinaacademy.common.exception.BadRequestException;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
//	private final CourseInstructorRepository courseInstructorRepository;
//	private final EnrollmentRepository enrollmentRepository;

    private final PasswordEncoder passwordEncoder;
    private final SecurityHelper securityHelper;

    @Override
    @Transactional
    public void createTestingData() {
        String[] roles = {AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE, AuthConstants.INSTRUCTOR_ROLE,
                AuthConstants.STUDENT_ROLE};
        if (roleRepository.count() > 0) {
            return;
        }
        for (String role : roles) {
            roleRepository.save(Role.builder().name(role).code(role).build());
        }

        User admin = User.builder().username("admin").password("admin").email("locn562836@gmail.com").enabled(true)
                .roles(Set.of(roleRepository.findByCode(AuthConstants.ADMIN_ROLE))).build();
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));

        User staff = User.builder().username("staff").password("staff").email("huuloc2155@gmail.com").enabled(true)
                .roles(Set.of(roleRepository.findByCode(AuthConstants.STAFF_ROLE))).build();
        staff.setPassword(passwordEncoder.encode(staff.getPassword()));

        User instructor = User.builder().username("instructor").password("instructor")
                .email("linhpht263@outlook.com.vn").enabled(true)
                .roles(Set.of(roleRepository.findByCode(AuthConstants.INSTRUCTOR_ROLE))).build();
        instructor.setPassword(passwordEncoder.encode(instructor.getPassword()));

        User student = User.builder().username("student").password("student").email("trihung987@gmail.com")
                .enabled(true).roles(Set.of(roleRepository.findByCode(AuthConstants.STUDENT_ROLE))).build();
        student.setPassword(passwordEncoder.encode(student.getPassword()));

        userRepository.save(admin);
        userRepository.save(staff);
        userRepository.save(instructor);
        userRepository.save(student);

    }

    @Override
    public UserDto getCurrentUser() {
        User user = securityHelper.getCurrentUser();
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return UserMapper.INSTANCE.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUserInfo(UpdateUserInfoRequest request) {
        User user = securityHelper.getCurrentUser();
        updateIfPresent(user::setFullName, request.getFullName());
        updateIfPresent(user::setDescription, request.getDescription());
        updateIfPresent(user::setAvatarUrl, request.getAvatarUrl());
        updateIfPresent(user::setBirthday, request.getBirthday());
        updateIfPresent(user::setPhone, request.getPhone());

        User savedUser = userRepository.save(user);
        return UserMapper.INSTANCE.toDto(savedUser);
    }

    private <T> void updateIfPresent(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }    @Override
    public UserViewDto viewUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy user"));
//		long create = user.getRoles().stream().anyMatch(role -> role.getName().contains("instructor"))
//				? courseInstructorRepository.countByInstructorAndIsOwnerTrue(user)
//				: 0;
//		long enroll = user.getRoles().stream().anyMatch(role -> role.getName().contains("student"))
//				? enrollmentRepository.countByUser(user)
//				: 0;
//		long erollComplete = user.getRoles().stream().anyMatch(role -> role.getName().contains("student"))
//				? enrollmentRepository.countByUserAndStatus(user, ProgressStatus.COMPLETED)
//				: 0;
//		ViewMappingDto viewDto = ViewMappingDto.builder().countCourseCreate(create).countCourseEnroll(enroll)
//				.countCourseEnrollComplete(erollComplete).build();
//		UserViewDto userViewDto = UserMapper.INSTANCE.toViewDto(user, viewDto);
//		return userViewDto;
        return null;
    }

    @Override
    public UserDto getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy user"));
        return UserMapper.INSTANCE.toDto(user);
    }
}
