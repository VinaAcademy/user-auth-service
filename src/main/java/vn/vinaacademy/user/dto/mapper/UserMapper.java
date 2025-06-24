package vn.vinaacademy.user.dto.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import vn.vinaacademy.user.dto.RegisterRequest;
import vn.vinaacademy.user.dto.UserDto;
import vn.vinaacademy.user.dto.UserViewDto;
import vn.vinaacademy.user.dto.ViewMappingDto;
import vn.vinaacademy.user.entity.User;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toUser(RegisterRequest registerRequest);

    UserDto toDto(User user);
    
    @Mapping(expression = "java(viewMappingDto.countCourseCreate)", target = "countCourseCreate")
    @Mapping(expression = "java(viewMappingDto.countCourseEnroll)", target = "countCourseEnroll")
    @Mapping(expression = "java(viewMappingDto.countCourseEnrollComplete)", target = "countCourseEnrollComplete")
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isCollaborator", ignore = true)
    UserViewDto toViewDto(User user, @Context ViewMappingDto viewMappingDto);
}
