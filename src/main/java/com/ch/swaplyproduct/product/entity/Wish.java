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

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long productId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // ✅ 정적 팩토리 메서드 추가
    public static Wish create(Long memberId, Long productId) {
        Wish wish = new Wish();
        wish.memberId = memberId;
        wish.productId = productId;
        return wish;
    }
}
