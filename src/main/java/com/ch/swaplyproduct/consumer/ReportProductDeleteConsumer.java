package com.ch.swaplyproduct.consumer;

import com.ch.swaplyproduct.config.AmqpConfig;
import com.ch.swaplyproduct.product.entity.ProductStatus;
import com.ch.swaplyproduct.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * report-service 가 신고 승인 시 발행하는 상품 삭제 이벤트를 소비합니다.
 *
 * Exchange : report.product.delete.exchange
 * Queue    : report.product.delete.queue
 *
 * 별도 is_deleted 컬럼 없이 기존 ProductStatus.DELETED 를 사용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportProductDeleteConsumer {

    private final ProductRepository productRepository;

    @Transactional
    @RabbitListener(queues = AmqpConfig.REPORT_PRODUCT_DELETE_QUEUE)
    public void consume(DeleteTargetMessage message) {
        log.info("[MQ-ReportDelete] 상품 삭제 처리 수신: targetId={}", message.targetId());
        try {
            Long productId = Long.parseLong(message.targetId());
            productRepository.findById(productId).ifPresentOrElse(
                product -> {
                    if (product.getStatus() == ProductStatus.DELETED) {
                        log.info("[MQ-ReportDelete] 이미 삭제된 상품: productId={}", productId);
                        return;
                    }
                    product.markDeleted();
                    log.info("[MQ-ReportDelete] 상품 status=DELETED 처리 완료: productId={}", productId);
                },
                () -> log.warn("[MQ-ReportDelete] 상품 없음 (무시): productId={}", productId)
            );
        } catch (Exception e) {
            log.error("[MQ-ReportDelete] 상품 삭제 처리 실패: targetId={}, error={}",
                    message.targetId(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public record DeleteTargetMessage(String targetType, String targetId) {}
}
