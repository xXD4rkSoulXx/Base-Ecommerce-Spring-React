package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.CartItem;
import com.d4rksoul.base.ecommerce.dto.response.CartItemResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface CartItemMapper {
    CartItemResponseDto toDto(CartItem cartItem);
}
