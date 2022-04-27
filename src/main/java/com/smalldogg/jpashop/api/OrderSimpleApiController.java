package com.smalldogg.jpashop.api;

import com.smalldogg.jpashop.domain.Order;
import com.smalldogg.jpashop.repository.OrderRepository;
import com.smalldogg.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * XtoOne 관계(ManyToOne, OneToOne) 안에서의 성능 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        //양방향 연관관계의 문제점이 발생
        //양방향 연관관계에서 서로 참조하는 부분 중 한군데를 JsonIgnore 애너테이션으로 호출하지않도록 처리
        //하지만,

        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }
}
