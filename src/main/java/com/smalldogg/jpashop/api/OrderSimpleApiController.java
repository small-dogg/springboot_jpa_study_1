package com.smalldogg.jpashop.api;

import com.smalldogg.jpashop.domain.Address;
import com.smalldogg.jpashop.domain.Order;
import com.smalldogg.jpashop.domain.OrderStatus;
import com.smalldogg.jpashop.repository.OrderRepository;
import com.smalldogg.jpashop.repository.OrderSearch;
import com.smalldogg.jpashop.repository.OrderSimpleQueryDto;
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

    // v3 와의 trade-off
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderRepository.findOrderDtos();
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
