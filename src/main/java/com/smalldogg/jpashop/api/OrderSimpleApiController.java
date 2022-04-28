package com.smalldogg.jpashop.api;

import com.smalldogg.jpashop.domain.Address;
import com.smalldogg.jpashop.domain.Order;
import com.smalldogg.jpashop.domain.OrderStatus;
import com.smalldogg.jpashop.repository.OrderRepository;
import com.smalldogg.jpashop.repository.OrderSearch;
import com.smalldogg.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import com.smalldogg.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

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

    // Order 2개
    // N + 1 문제가 발생 -> 1+ 회원 N + 배송 N
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {

        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    //Order 2개
    // fetch join으로 한방 쿼리로 처리가 되었다.
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    // v3 와의 trade-off(v3: 코드 퀄리티, v4: 성능) -> 양자 택일
    // Repository 재사용성이 떨어짐. 엔티티에 대한 객체 그래프를 탐색하는 등에 이유로 사용되어져야하는 Repository가
    // API의 스펙이 의존하여 짜여져있다보니, findOrderDtos라는 Repository의 메서드를 다른용도로 활용할 수 있는 방법이 없음.
    // 논리적인 계층이 깨져있음. Repository로 화면을 의존함. API 스펙이 바뀌면 Repository의 내용을 고쳐야한다는...
    // v4보다는 v3을 선호.
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }



    @Data
    @AllArgsConstructor
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); //LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화
        }
    }
}
