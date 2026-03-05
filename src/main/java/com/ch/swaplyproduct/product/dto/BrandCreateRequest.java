package com.ch.swaplyproduct.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class BrandCreateRequest {

    @NotBlank
    private String name;

    private String nameEn;

    private String logoUrl;

    @NotNull
    private Integer categoryId;
}