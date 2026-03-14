package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.Address;
import com.d4rksoul.base.ecommerce.dto.request.AddressRequestDto;
import com.d4rksoul.base.ecommerce.dto.response.AddressResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressResponseDto toDto(Address address);
    Address toEntity(AddressRequestDto dto);
}
