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
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "brand_id"})
        }
)
public class BrandFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followId;

    @Column(nullable = false, name = "member_id")
    private Long memberId;

    @Column(nullable = false, name = "brand_id")
    private Integer brandId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public static BrandFollow create(Long memberId, Integer brandId) {
        BrandFollow f = new BrandFollow();
        f.memberId = memberId;
        f.brandId  = brandId;
        return f;
    }
}
