package com.d4rksoul.base.ecommerce.spring.react.mapper.admin;

import com.d4rksoul.base.ecommerce.spring.react.dto.request.admin.AdminCreateDiscountCodeRequest;
import com.d4rksoul.base.ecommerce.spring.react.dto.response.admin.AdminDiscountCodeResponse;
import com.d4rksoul.base.ecommerce.spring.react.entity.DiscountCode;
import org.springframework.stereotype.Component;

@Component
public class AdminDiscountCodeMapper {

    public DiscountCode toEntity(AdminCreateDiscountCodeRequest request) {
        return DiscountCode.builder()
            .code(request.code())
            .discountPercentage(request.discountPercentage())
            .active(true)
            .build();
    }

    public AdminDiscountCodeResponse toDto(DiscountCode discountCode, long orders) {
        return AdminDiscountCodeResponse.builder()
            .id(discountCode.getId())
            .code(discountCode.getCode())
            .discountPercentage(discountCode.getDiscountPercentage())
            .active(discountCode.isActive())
            .orders(orders)
            .createdAt(discountCode.getCreatedAt())
            .build();
    }
}
