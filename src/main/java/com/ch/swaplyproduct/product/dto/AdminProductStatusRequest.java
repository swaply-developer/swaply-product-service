package com.ch.swaplyproduct.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 상품 상태 변경 요청 DTO.
 * PATCH /api/admin/products/{productId}/status
 * body: { "status": "숨김" }  (ProductStatus.description 한글값)
 */
@Getter
@NoArgsConstructor
public class AdminProductStatusRequest {

    @NotBlank(message = "status는 필수입니다.")
    private String status;
}
