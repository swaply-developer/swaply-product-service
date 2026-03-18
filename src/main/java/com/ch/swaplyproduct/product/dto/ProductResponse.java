package com.ch.swaplyproduct.product.dto;

import com.ch.swaplyproduct.product.entity.Category;
import com.ch.swaplyproduct.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProductResponse {

    private Long productId;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer categoryId;
    private Integer rootCategoryId;
    private Integer brandId;
    private String brandName;   // 추가
    private Long sellerId;
    private String status;
    private Long viewCount;
    private Long wishCount;
    private LocalDateTime createdAt;
    private String thumbnailUrl;
    private List<ImageDto> images;

    @Getter
    @Builder
    public static class ImageDto {
        private Long imageId;
        private String imageUrl;
        private boolean isThumbnail;
        private int sortOrder;
    }

    private static Integer findRootCategoryId(Category category) {
        if (category == null) return null;

        Category current = category;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current.getCategoryId();
    }

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .rootCategoryId(findRootCategoryId(product.getCategory()))
                .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)   // 추가
                .sellerId(product.getSellerId())
                .status(product.getStatus().name())
                .viewCount(product.getViewCount())
                .wishCount(product.getWishCount())
                .createdAt(product.getCreatedAt())
                .build();
    }

    public static ProductResponse fromWithImages(Product product, List<com.ch.swaplyproduct.product.entity.ProductImage> imageList) {
        List<ImageDto> dtos = imageList.stream()
                .map(img -> ImageDto.builder()
                        .imageId(img.getImageId())
                        .imageUrl(img.getImageUrl())
                        .isThumbnail(img.isThumbnail())
                        .sortOrder(img.getSortOrder())
                        .build())
                .collect(Collectors.toList());

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
                .rootCategoryId(findRootCategoryId(product.getCategory()))
                .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)   // 추가
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