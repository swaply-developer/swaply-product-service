package com.ch.swaplyproduct.controller;

import com.ch.swaplyproduct.config.AmqpConfig;
import com.ch.swaplyproduct.util.ExcelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ExcelParser excelParser;

    // 1. 아주 쉬운 GET 방식 테스트 (브라우저 주소창에 바로 입력 가능!)
    @GetMapping("/hello")
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "게이트웨이-프로덕트 연결 성공!"
        ));
    }


        private final RabbitTemplate rabbitTemplate;

        @PostMapping("/mq-test")
        public ResponseEntity<?> sendTestMessage() {
            // 1. 보낼 목업 데이터 생성
            Map<String, Object> orderEvent = new HashMap<>();
            orderEvent.put("orderId", 12345);
            orderEvent.put("userId", "user_admin");
            orderEvent.put("productName", "테스트 상품 A");
            orderEvent.put("amount", 2);
            orderEvent.put("status", "CREATED");

            // 2. RabbitMQ로 전송 (설정하신 이름 그대로 사용)
            log.info("RabbitMQ 메시지 발행 시도: {}", orderEvent);
            rabbitTemplate.convertAndSend(
                    AmqpConfig.ORDER_CREATED_EXCHANGE,
                    AmqpConfig.ORDER_CREATED_ROUTING_KEY,
                    orderEvent
            );

            return ResponseEntity.ok(Map.of("msg", "메시지가 큐로 성공적으로 발송되었습니다!"));
        }


}
