package com.ch.swaplyproduct.product.repository;

import com.ch.swaplyproduct.product.entity.Product;
import com.ch.swaplyproduct.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // ✅ 판매중 상품 목록
    Page<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);

    // ✅ 특정 판매자 상품 목록
    Page<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    // ✅ 카테고리별 상품 (메서드명 그대로 유지)
    @Query("select p from Product p where p.category.categoryId = :categoryId and p.status = :status")
    Page<Product> findByCategoryIdAndStatus(@Param("categoryId") Integer categoryId, @Param("status") ProductStatus status, Pageable pageable);

    // ✅ 제목 검색
    Page<Product> findByTitleContainingAndStatus(String keyword, ProductStatus status, Pageable pageable);

    // 🔥 조회수 증가 (동시성 안전)
    @Modifying
    @Query("update Product p set p.viewCount = p.viewCount + 1 where p.productId = :productId")
    int increaseViewCount(@Param("productId") Long productId);

    // 🔥 상태 변경 (현재 상태 조건 포함)
    @Modifying
    @Query("""
        update Product p
        set p.status = :newStatus
        where p.productId = :productId
        and p.status = :currentStatus
    """)
    int updateStatusIfMatch(
            @Param("productId") Long productId,
            @Param("currentStatus") ProductStatus currentStatus,
            @Param("newStatus") ProductStatus newStatus
    );

    /*-------------------------------------------------------------------
    *   View
    * -------------------------------------------------------------------*/
    @Modifying
    @Query("""
    update Product p
    set p.viewCount = p.viewCount + :count
    where p.productId = :productId
""")
    int bulkIncreaseViewCount(@Param("productId") Long productId,
                              @Param("count") Long count);

    /*-------------------------------------------------------------------
     *   Wish
     * -------------------------------------------------------------------*/
    @Modifying
    @Query("""
    update Product p
    set p.wishCount = p.wishCount + :count
    where p.productId = :productId
""")
    int bulkIncreaseWishCount(@Param("productId") Long productId,
                              @Param("count") Long count);





}

