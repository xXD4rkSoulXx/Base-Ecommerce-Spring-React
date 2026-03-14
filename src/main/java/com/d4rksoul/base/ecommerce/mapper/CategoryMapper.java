package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.Category;
import com.d4rksoul.base.ecommerce.dto.request.CategoryRequestDto;
import com.d4rksoul.base.ecommerce.dto.response.CategoryResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponseDto toDto(Category category);
    Category toEntity(CategoryRequestDto dto);
}
