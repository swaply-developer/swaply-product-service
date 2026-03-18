package com.ch.swaplyproduct.product.repository;

import com.ch.swaplyproduct.product.entity.BrandFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BrandFollowRepository extends JpaRepository<BrandFollow, Long> {

    // 특정 회원이 특정 브랜드를 팔로우했는지
    Optional<BrandFollow> findByMemberIdAndBrandId(Long memberId, Integer brandId);

    boolean existsByMemberIdAndBrandId(Long memberId, Integer brandId);

    // 팔로우 취소
    void deleteByMemberIdAndBrandId(Long memberId, Integer brandId);

    // 회원의 팔로우 브랜드 ID 목록
    @Query("select f.brandId from BrandFollow f where f.memberId = :memberId")
    List<Integer> findBrandIdsByMemberId(@Param("memberId") Long memberId);

    // 브랜드별 팔로워 수
    long countByBrandId(Integer brandId);
}
