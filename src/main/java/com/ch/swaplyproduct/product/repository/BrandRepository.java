package com.ch.swaplyproduct.product.repository;

import com.ch.swaplyproduct.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

    // category.categoryId 기준으로 브랜드 조회
    List<Brand> findByCategoryCategoryId(Integer categoryId);
}
