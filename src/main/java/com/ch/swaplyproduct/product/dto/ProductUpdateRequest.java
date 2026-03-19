package com.ch.swaplyproduct.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductUpdateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Integer categoryId;

    /** DB에 등록된 브랜드 ID (선택) */
    private Integer brandId;

    /** 직접 입력 브랜드명 (brandId 없을 때 fallback) */
    private String brandName;

    private String tradeType;

    private Integer thumbnailIndex;

    /** 게이트웨이가 주입하는 요청자 ID — 판매자 본인 검증용 */
    private Long sellerId;
}