package com.ch.swaplyproduct.product.repository;

import com.ch.swaplyproduct.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}