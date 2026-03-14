package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.Order;
import com.d4rksoul.base.ecommerce.dto.response.OrderResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, PaymentMapper.class})
public interface OrderMapper {
    OrderResponseDto toDto(Order order);
}
