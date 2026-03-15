package com.ch.swaplyproduct.message;

import com.ch.swaplyproduct.config.AmqpConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 찜 관련 알림 이벤트를 RabbitMQ 로 발행하는 퍼블리셔.
 *
 * payment-service 의 TradeMessagePublisher 와 동일한 패턴.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WishNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 누군가 상품을 찜했을 때 → 판매자 알림
     *
     * @param message 찜 추가 이벤트 메시지
     */
    public void publishWishAdded(WishAddedMessage message) {
        log.info("[MQ-WishAdded] 발행: productId={}, wisher={}, seller={}",
                message.productId(), message.wishMemberId(), message.sellerId());
        rabbitTemplate.convertAndSend(
                AmqpConfig.WISH_ADDED_EXCHANGE,
                AmqpConfig.WISH_ADDED_KEY,
                message
        );
    }

    /**
     * 상품 가격이 변경됐을 때 → 이 상품을 찜한 모든 회원 알림
     *
     * @param message 가격 변동 이벤트 메시지
     */
    public void publishWishPrice(WishPriceMessage message) {
        log.info("[MQ-WishPrice] 발행: productId={}, 찜한 유저 {}명, {}→{}",
                message.productId(), message.wishedMemberIds().size(),
                message.oldPrice(), message.newPrice());
        rabbitTemplate.convertAndSend(
                AmqpConfig.WISH_PRICE_EXCHANGE,
                AmqpConfig.WISH_PRICE_KEY,
                message
        );
    }
}
