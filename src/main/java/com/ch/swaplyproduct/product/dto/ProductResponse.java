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
    private String thumbnailUrl;          // ✅ 목록용 썸네일 URL (게이트웨이 경유 경로)
    private java.util.List<ImageDto> images; // ✅ 상세용 전체 이미지

    @lombok.Getter
    @lombok.Builder
    public static class ImageDto {
        private Long imageId;
        private String imageUrl;
        private boolean isThumbnail;
        private int sortOrder;
    }

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

    // ✅ 상세 조회: 이미지 목록 포함
    public static ProductResponse fromWithImages(Product product, java.util.List<com.ch.swaplyproduct.product.entity.ProductImage> imageList) {
        java.util.List<ImageDto> dtos = imageList.stream()
                .map(img -> ImageDto.builder()
                        .imageId(img.getImageId())
                        .imageUrl(img.getImageUrl())
                        .isThumbnail(img.isThumbnail())
                        .sortOrder(img.getSortOrder())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        String thumbnail = imageList.stream()
                .filter(com.ch.swaplyproduct.product.entity.ProductImage::isThumbnail)
                .map(com.ch.swaplyproduct.product.entity.ProductImage::getImageUrl)
                .findFirst()
                .orElse(imageList.isEmpty() ? null : imageList.get(0).getImageUrl());

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
                .thumbnailUrl(thumbnail)
                .images(dtos)
                .build();
    }
}