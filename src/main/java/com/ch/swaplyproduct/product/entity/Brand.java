package com.ch.swaplyproduct.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer brandId;

    private String name;
    private String nameEn;
    private String logoUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /*-----------------------------------------------------------------------
    *   정적 생성 메서드
    * -----------------------------------------------------------------------*/

    public static Brand create(String name,
                               String nameEn,
                               String logoUrl,
                               Category category) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("브랜드명은 필수입니다.");
        }

        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }

        Brand brand = new Brand();
        brand.name = name;
        brand.nameEn = nameEn;
        brand.logoUrl = logoUrl;
        brand.category = category;

        return brand;
    }
}
