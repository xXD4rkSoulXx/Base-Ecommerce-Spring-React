/*
 *  A pasta Mapper serve para interligar o Domain ao DTO
 * */

package com.d4rksoul.base.ecommerce.mapper;

import com.d4rksoul.base.ecommerce.domain.Review;
import com.d4rksoul.base.ecommerce.dto.request.ReviewRequestDto;
import com.d4rksoul.base.ecommerce.dto.response.ReviewResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    ReviewResponseDto toDto(Review review);
    Review toEntity(ReviewRequestDto dto);
}
