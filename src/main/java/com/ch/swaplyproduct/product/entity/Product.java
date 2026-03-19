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
    private Brand brand;

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

    @Column(nullable = false)
    private String tradeType;

    // ── 팩토리 메서드 ──────────────────────────────────────────────────────────
    public static Product create(Long sellerId, Category category, Brand brand,
                                 String title, String description, BigDecimal price,
                                 String tradeType) {
        Product product = new Product();
        product.sellerId    = sellerId;
        product.category    = category;
        product.brand       = brand;
        product.title       = title;
        product.description = description;
        product.price       = price;
        product.tradeType   = tradeType;
        product.status      = ProductStatus.SALE;
        product.viewCount   = 0L;
        product.wishCount   = 0L;
        return product;
    }

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images = new ArrayList<>();

    // ── 비즈니스 메서드 ────────────────────────────────────────────────────────

    public void updateStatus(ProductStatus newStatus) {
        this.status = newStatus;
    }

    /** 상품 정보 수정 (판매자 본인만 호출 가능) */
    public void update(Category category, Brand brand,
                       String title, String description,
                       BigDecimal price, String tradeType) {
        this.category    = category;
        this.brand       = brand;
        this.title       = title;
        this.description = description;
        this.price       = price;
        this.tradeType   = tradeType;
    }

    /**
     * 가격 변경.
     * 변경 전 가격은 서비스 레이어에서 별도로 캡처해 알림 메시지에 담는다.
     */
    public void updatePrice(BigDecimal newPrice) {
        this.price = newPrice;
    }

    /**
     * 신고 승인으로 인한 강제 삭제.
     * 별도 컬럼 없이 기존 status = DELETED 를 사용한다.
     */
    public void markDeleted() {
        this.status = ProductStatus.DELETED;
    }
}
