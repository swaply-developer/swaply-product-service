package com.ch.swaplyproduct.product.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CategoryResponse {

    private Integer categoryId;
    private String categoryName;
    private Integer parentId;
    private List<CategoryResponse> sub;
}