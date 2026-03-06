/*
 *  A pasta Mapper serve para interligar o Domain ao DTO
 * */

package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.Discount;
import com.d4rksoul.base.ecommerce.dto.request.AddToCartDto;
import com.d4rksoul.base.ecommerce.dto.response.DiscountResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DiscountMapper {
    DiscountResponseDto toDto(Discount discount);
    Discount toEntity(AddToCartDto discount);
}
