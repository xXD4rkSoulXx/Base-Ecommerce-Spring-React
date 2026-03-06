/*
 *  A pasta Mapper serve para interligar o Domain ao DTO
 * */

package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.Cart;
import com.d4rksoul.base.ecommerce.dto.response.CartItemResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartItemResponseDto toDto(Cart cart);
}
