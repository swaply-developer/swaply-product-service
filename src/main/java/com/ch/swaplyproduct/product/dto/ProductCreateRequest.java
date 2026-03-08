package com.ch.swaplyproduct.product.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter; // 👈 FormData 바인딩을 위해 필수
import java.math.BigDecimal;

@Getter
@Setter // 👈 추가
public class ProductCreateRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Integer categoryId;

    private Integer brandId;

    private Long sellerId; // 프론트에서 안 보내면 컨트롤러에서 강제 주입 예정

    private String tradeType; // 👈 프론트의 'BOTH', 'DELIVERY' 등을 받기 위해 추가
    private Integer thumbnailIndex;
}