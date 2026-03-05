package com.ch.swaplyproduct.product.repository;


import com.ch.swaplyproduct.product.entity.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishRepository extends JpaRepository<Wish, Long> {

    // ✅ 내가 찜했는지 확인
    Optional<Wish> findByMemberIdAndProductId(Long memberId, Long productId);

    // ✅ 내가 찜한 상품 목록
    Page<Wish> findByMemberId(Long memberId, Pageable pageable);

    // ✅ 특정 상품 찜 개수
    long countByProductId(Long productId);

    // ✅ 찜 취소
    void deleteByMemberIdAndProductId(Long memberId, Long productId);
}
