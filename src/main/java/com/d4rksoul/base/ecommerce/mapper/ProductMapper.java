package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.Product;
import com.d4rksoul.base.ecommerce.dto.request.ProductRequestDto;
import com.d4rksoul.base.ecommerce.dto.response.ProductResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, DiscountMapper.class, ReviewMapper.class})
public interface ProductMapper {
    ProductResponseDto toDto(Product product);
    Product toEntity(ProductRequestDto dto);

}
