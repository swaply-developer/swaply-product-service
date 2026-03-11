package com.ch.swaplyproduct.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String imageUrl;
    private boolean isThumbnail;
    private int sortOrder;


    public static ProductImage create(Product product, String imageUrl, boolean isThumbnail, int sortOrder) {
        ProductImage pi = new ProductImage();
        pi.product = product;
        pi.imageUrl = imageUrl;
        pi.isThumbnail = isThumbnail;
        pi.sortOrder = sortOrder;
        return pi;
    }

}