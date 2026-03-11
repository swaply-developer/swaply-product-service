package com.ch.swaplyproduct.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSummaryDto {
    private Long productId;
    private Long sellerId;
    private String title;
    private Long price;
    private String thumbnailUrl;
    private String status;
}