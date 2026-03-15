package com.ch.swaplyproduct.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 상품 가격 변경 요청 DTO.
 * PATCH /api/products/{productId}/price
 */
@Getter
@NoArgsConstructor
public class ProductUpdatePriceRequest {

    @NotNull(message = "변경할 가격을 입력해주세요.")
    @DecimalMin(value = "0", inclusive = false, message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;
}
