package com.ch.swaplyproduct.consumer;

import com.ch.swaplyproduct.message.OrderCreatedMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreatedConsumer {

    // 주의: 현재 시점 우리의 재고관리 앱에서는 주문 완료 처리에 대해 웹 상의 요청으로
    //          즉 클라이언트의 요청에 의해 주문완료 여부를 전달받는 방식이 아니라, RabbitMQ의
    //          메시지를 청취 하는 방식으로 전달받아야 한다.       따라서 현재!! 로써는 컨트롤러가 필요없음
    //          또한 requestId도 헤더값으로 전달될 일도 없다.
    //      오해히자 말기!! 재고관리 팀으로 주문 이외의 다른요청에 대해서는 충분히 컨트롤러에서 처리 가능함
    //          예) 배송관리팀이 재고관리 팀에게 현재 재고량을 요청했다면??
    //          Controller 에 요청받아 처리해야 함.. 이때는 헤더로 requestId 로 넘어옴..


    @RabbitListener(queues = "order.created.queue") // Listener 에는 json 문자열을 자바객체로 컨버터가 내장되있음
    public void onMessage(OrderCreatedMessage message){
        log.debug("주문 생성 감지 ");

        // RabbitMQ 서버의 리슨을 담당하는 쓰레드는 MDC 의 존재를 모르는 리스너 전용 별도 쓰레드 이므로,
        // 반드시 MDC를 재저장 해야함!!
        MDC.put("requestId", message.getRequestId());   // ** Thread 기반이기에 order 에서 레빗으로 넘어갈 때 쓰레드가 죽음
                                                                                // 따라서 레빗에 있는 MDC 를 .put으로 꺼내서 다시 저장해라
        try{
            // 재고 처리 업무
            log.debug("재고 업무 시작");
            log.debug(MDC.get("requestId"));
        }finally {
            MDC.remove("requestId");
        }

    }


}
