package com.ch.swaplyproduct.product.repository;

import com.ch.swaplyproduct.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
}