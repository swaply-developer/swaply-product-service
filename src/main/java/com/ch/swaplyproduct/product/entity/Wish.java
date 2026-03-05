package com.ch.swaplyproduct.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"memberId", "productId"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Wish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishId;

    // 🔥 Member 연관관계 제거
    @Column(nullable = false)
    private Long memberId;

    // 🔥 Product는 같은 서비스면 연관관계 가능
    @Column(nullable = false)
    private Long productId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}