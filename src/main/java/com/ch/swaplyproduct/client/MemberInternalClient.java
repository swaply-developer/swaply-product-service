package com.ch.swaplyproduct.client;

import com.ch.swaplyproduct.product.dto.MemberSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * member-service 내부 호출 클라이언트.
 *
 * payment-service 의 TradeExternalClient 와 동일한 패턴.
 * 게이트웨이 PUBLIC_PREFIXES 에 "/api/members/summary/bulk" 가 이미 허용돼 있다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberInternalClient {

    private final RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    /**
     * memberId 단건으로 닉네임 조회.
     * 내부적으로 bulk API 를 1건으로 호출한다.
     *
     * @return 닉네임, 조회 실패 시 "알 수 없음" fallback
     */
    public String getNickname(Long memberId) {
        Map<Long, MemberSummaryDto> result = getMemberSummaries(List.of(memberId));
        MemberSummaryDto dto = result.get(memberId);
        if (dto == null) {
            log.warn("[MemberClient] 회원 정보 조회 실패: memberId={}", memberId);
            return "알 수 없음";
        }
        return dto.getNickname();
    }

    /**
     * 여러 memberId 를 한 번에 조회해 Map 으로 반환.
     */
    public Map<Long, MemberSummaryDto> getMemberSummaries(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        String url = gatewayUrl + "/api/members/summary/bulk";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<Long>> request = new HttpEntity<>(memberIds, headers);

        try {
            ResponseEntity<List<MemberSummaryDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {}
            );

            List<MemberSummaryDto> body = response.getBody();
            if (body == null || body.isEmpty()) return Map.of();

            return body.stream()
                    .collect(Collectors.toMap(MemberSummaryDto::getMemberId, m -> m));

        } catch (Exception e) {
            log.error("[MemberClient] bulk 조회 실패: memberIds={}, error={}", memberIds, e.getMessage());
            return Map.of();
        }
    }
}
