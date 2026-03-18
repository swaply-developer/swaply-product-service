package com.ch.swaplyproduct.product.dto;

import com.ch.swaplyproduct.product.entity.Brand;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BrandResponse {

    private Integer brandId;
    private String  name;
    private String  nameEn;
    private String  logoUrl;
    private Integer categoryId;
    private String  categoryName;    // 카테고리 이름 (헤더 탭 필터용)
    private long    followCount;     // 팔로워 수
    private boolean followed;        // 현재 로그인 유저의 팔로우 여부
    private LocalDateTime createdAt;

    /** 팔로우 정보 없이 생성 (비로그인 또는 followCount만 필요할 때) */
    public static BrandResponse from(Brand brand) {
        return BrandResponse.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .nameEn(brand.getNameEn())
                .logoUrl(brand.getLogoUrl())
                .categoryId(brand.getCategory() != null ? brand.getCategory().getCategoryId() : null)
                .categoryName(brand.getCategory() != null ? brand.getCategory().getName() : null)
                .followCount(0L)
                .followed(false)
                .createdAt(brand.getCreatedAt())
                .build();
    }

    /** followCount + followed 포함 생성 */
    public static BrandResponse from(Brand brand, long followCount, boolean followed) {
        return BrandResponse.builder()
                .brandId(brand.getBrandId())
                .name(brand.getName())
                .nameEn(brand.getNameEn())
                .logoUrl(brand.getLogoUrl())
                .categoryId(brand.getCategory() != null ? brand.getCategory().getCategoryId() : null)
                .categoryName(brand.getCategory() != null ? brand.getCategory().getName() : null)
                .followCount(followCount)
                .followed(followed)
                .createdAt(brand.getCreatedAt())
                .build();
    }
}
