/*
 *  A pasta Mapper serve para interligar o Domain ao DTO
 * */

package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.OrderItem;
import com.d4rksoul.base.ecommerce.dto.response.OrderItemResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemResponseDto toDto(OrderItem orderItem);
}
