package com.ch.swaplyproduct.product.dto;

import com.ch.swaplyproduct.product.entity.Brand;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BrandResponse {

    private Integer brandId;
    private String name;
    private String nameEn;
    private String logoUrl;
    private Integer categoryId;
    private LocalDateTime createdAt;

    public static BrandResponse from(Brand brand) {
        return BrandResponse.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .nameEn(brand.getNameEn())
                .logoUrl(brand.getLogoUrl())
                .categoryId(brand.getCategory().getCategoryId())
                .createdAt(brand.getCreatedAt())
                .build();
    }
}