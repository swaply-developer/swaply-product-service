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

    // 🔥 Member 제거
    @Column(nullable = false)
    private Long memberId;

    // Brand는 같은 서비스라면 유지 가능
    @Column(nullable = false)
    private Integer brandId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
