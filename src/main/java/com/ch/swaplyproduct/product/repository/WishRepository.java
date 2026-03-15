package com.ch.swaplyproduct.product.repository;

import com.ch.swaplyproduct.product.entity.Wish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishRepository extends JpaRepository<Wish, Long> {

    // 기존: 내가 찜했는지 확인
    Optional<Wish> findByMemberIdAndProductId(Long memberId, Long productId);

    // 기존: 내가 찜한 상품 목록
    Page<Wish> findByMemberId(Long memberId, Pageable pageable);

    // 기존: 특정 상품 찜 개수
    long countByProductId(Long productId);

    // 기존: 찜 취소
    void deleteByMemberIdAndProductId(Long memberId, Long productId);

    // 신규: 특정 상품을 찜한 모든 회원 ID 조회 (가격 변동 알림용)
    @Query("select w.memberId from Wish w where w.productId = :productId")
    List<Long> findAllMemberIdsByProductId(@Param("productId") Long productId);
}
