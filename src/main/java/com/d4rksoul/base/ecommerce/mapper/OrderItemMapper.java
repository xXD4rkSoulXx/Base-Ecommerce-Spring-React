package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.OrderItem;
import com.d4rksoul.base.ecommerce.dto.response.OrderItemResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses =  {ProductMapper.class})
public interface OrderItemMapper {
    OrderItemResponseDto toDto(OrderItem orderItem);
}
