package com.ch.swaplyproduct.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberSummaryDto {
    private Long memberId;
    private String nickname;
    private String phone;
}
