package com.ch.swaplyproduct.product.repository;

import com.ch.swaplyproduct.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

    // 카테고리 ID 기준 브랜드 목록 (기존)
    List<Brand> findByCategoryCategoryId(Integer categoryId);

    // brandName fallback 처리용: 카테고리 + 이름으로 브랜드 조회
    Optional<Brand> findByCategoryCategoryIdAndName(Integer categoryId, String name);
}
