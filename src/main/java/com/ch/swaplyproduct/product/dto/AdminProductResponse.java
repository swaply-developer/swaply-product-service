package com.ch.swaplyproduct.product.dto;

import com.ch.swaplyproduct.product.entity.Product;
import com.ch.swaplyproduct.product.entity.ProductImage;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 관리자 상품 목록/상세 응답 DTO.
 *
 * 프론트 ProductsPage.jsx 가 읽는 필드:
 *   productId, title, sellerNickname, category, price,
 *   status(한글), reportCount, wishCount, createdAt
 */
@Getter
@Builder
public class AdminProductResponse {

    private Long productId;
    private String title;
    private Long sellerId;
    private String sellerNickname;   // member-service 조회 후 주입
    private String category;         // 카테고리 이름
    private BigDecimal price;
    private String status;           // ProductStatus.description (한글: "판매중","숨김","삭제" 등)
    private Long wishCount;
    private int reportCount;         // 현재는 0 (report-service 연동 시 교체)
    private LocalDateTime createdAt;
    private String thumbnailUrl;

    public static AdminProductResponse from(Product product, List<ProductImage> images, String sellerNickname) {
        String thumbnail = images.stream()
                .filter(ProductImage::isThumbnail)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0).getImageUrl());

        String categoryName = product.getCategory() != null
                ? product.getCategory().getName()
                : "-";

        return AdminProductResponse.builder()
                .productId(product.getProductId())
                .title(product.getTitle())
                .sellerId(product.getSellerId())
                .sellerNickname(sellerNickname != null ? sellerNickname : "-")
                .category(categoryName)
                .price(product.getPrice())
                .status(product.getStatus().getDescription())   // 한글 반환
                .wishCount(product.getWishCount())
                .reportCount(0)  // TODO: report-service 연동 시 교체
                .createdAt(product.getCreatedAt())
                .thumbnailUrl(thumbnail)
                .build();
    }
}
