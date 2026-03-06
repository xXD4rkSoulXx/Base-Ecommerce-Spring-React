/*
 *  A pasta Mapper serve para interligar o Domain ao DTO
 * */

package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.Order;
import com.d4rksoul.base.ecommerce.dto.request.AddToCartDto;
import com.d4rksoul.base.ecommerce.dto.response.OrderResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponseDto toDto(Order order);
    Order toEntity(AddToCartDto dto);
}
