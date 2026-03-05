package com.ch.swaplyproduct.product.dto;

import com.ch.swaplyproduct.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductResponse {

    private Long productId;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer categoryId;
    private Integer brandId;
    private Long sellerId;
    private String status;
    private Long viewCount;
    private Long wishCount;
    private LocalDateTime createdAt;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
                .sellerId(product.getSellerId())
                .status(product.getStatus().name())
                .viewCount(product.getViewCount())
                .wishCount(product.getWishCount())
                .createdAt(product.getCreatedAt())
                .build();
    }
}