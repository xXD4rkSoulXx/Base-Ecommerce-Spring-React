/*
 *  A pasta Mapper serve para interligar o Domain ao DTO
 * */

package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.User;
import com.d4rksoul.base.ecommerce.dto.request.AdminSignUpDto;
import com.d4rksoul.base.ecommerce.dto.request.SignInDto;
import com.d4rksoul.base.ecommerce.dto.request.SignUpDto;
import com.d4rksoul.base.ecommerce.dto.response.UserResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toDto(User user);
    User toEntity(SignUpDto dto);
    User toEntity(AdminSignUpDto dto);
    User toEntity(SignInDto dto);
}
