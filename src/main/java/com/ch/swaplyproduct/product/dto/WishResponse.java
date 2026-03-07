package com.ch.swaplyproduct.product.dto;

import com.ch.swaplyproduct.product.entity.Product;
import com.ch.swaplyproduct.product.entity.ProductImage;
import com.ch.swaplyproduct.product.entity.Wish;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class WishResponse {

    private Long wishId;
    private Long productId;
    private String productTitle;
    private BigDecimal productPrice;
    private String productThumbnail;  // 썸네일 imageUrl
    private String productStatus;     // SALE / RESERVED / SOLD_OUT
    private String tradeType;
    private LocalDateTime createdAt;  // 찜 등록 시간

    public static WishResponse of(Wish wish, Product product, ProductImage thumbnail) {
        return WishResponse.builder()
                .wishId(wish.getWishId())
                .productId(product.getProductId())
                .productTitle(product.getTitle())
                .productPrice(product.getPrice())
                .productThumbnail(thumbnail != null ? thumbnail.getImageUrl() : null)
                .productStatus(product.getStatus().name())
                .tradeType(product.getTradeType())
                .createdAt(wish.getCreatedAt())
                .build();
    }
}
