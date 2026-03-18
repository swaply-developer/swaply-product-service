package com.ch.swaplyproduct.product.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Integer categoryId;

    /** DB에 등록된 브랜드 ID (apiBrands에서 선택 시) */
    private Integer brandId;

    /**
     * DB에 없는 브랜드명 직접 입력 시 (brandId가 없을 때 fallback).
     * 백엔드에서 해당 카테고리에 동일 이름 브랜드가 없으면 신규 생성.
     */
    private String brandName;

    private Long sellerId;

    private String tradeType;

    private Integer thumbnailIndex;
}
