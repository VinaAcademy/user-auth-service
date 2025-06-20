package vn.vinaacademy.auth.dto.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import vn.vinaacademy.auth.dto.RegisterRequest;
import vn.vinaacademy.auth.dto.UserDto;
import vn.vinaacademy.auth.dto.UserViewDto;
import vn.vinaacademy.auth.dto.ViewMappingDto;
import vn.vinaacademy.auth.entity.User;

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
