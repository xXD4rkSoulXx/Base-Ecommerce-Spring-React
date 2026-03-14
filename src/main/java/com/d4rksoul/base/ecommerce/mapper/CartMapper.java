package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.Cart;
import com.d4rksoul.base.ecommerce.dto.request.AddToCartDto;
import com.d4rksoul.base.ecommerce.dto.response.CartResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {
    CartResponseDto toDto(Cart cart);
    Cart toEntity(AddToCartDto dto);
}
