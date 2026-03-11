package com.ch.swaplyproduct.product.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 파일: src/main/java/com/ch/swaplyproduct/product/entity/Product.java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand; // nullable 허용

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.SALE;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Long wishCount = 0L;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Product.java 내 추가 및 수정

    // Product.java 필드 추가
    @Column(nullable = false)
    private String tradeType;

    // 정적 생성 메서드 업데이트
    public static Product create(Long sellerId, Category category, Brand brand,
                                 String title, String description, BigDecimal price,
                                 String tradeType) { // 👈 파라미터 추가
        Product product = new Product();
        product.sellerId = sellerId;
        product.category = category;
        product.brand = brand;
        product.title = title;
        product.description = description;
        product.price = price;
        product.tradeType = tradeType; // 👈 저장
        product.status = ProductStatus.SALE;
        product.viewCount = 0L;
        product.wishCount = 0L;
        return product;
    }

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images = new ArrayList<>();

    public void updateStatus(ProductStatus newStatus) {
        this.status = newStatus;
    }
}

