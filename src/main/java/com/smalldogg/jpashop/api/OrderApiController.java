package com.smalldogg.jpashop.api;

import com.smalldogg.jpashop.domain.Address;
import com.smalldogg.jpashop.domain.Order;
import com.smalldogg.jpashop.domain.OrderItem;
import com.smalldogg.jpashop.domain.OrderStatus;
import com.smalldogg.jpashop.repository.OrderRepository;
import com.smalldogg.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();//Lazy 강제 초기화
            order.getDelivery().getAddress();//Lazy 강제 초기화

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o->o.getItem().getName());//stream을 사용하여 하위에 존재하는 Lazy 대상들 강제 초기화
        }
        return all;
    }

    //🌟겉에 들어나있는 엔티티 뿐만 아니라 그 엔티티의 연관 관계가 존재하는 엔티티 조차도 노출해서는 안돼!
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());
    }

    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;
        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

            //Dto 안에 Entity가 존재. Wrapping 조차도 허용할 수 없음. OrderItemDto도 있어야함.
            orderItems = order.getOrderItems().stream()
                    .map(o-> new OrderItemDto(o))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto{
        private String itemName;//상품 명
        private int orderPrice;// 주문 가격
        private int count;// 주문 수량

        public OrderItemDto(OrderItem o) {
            itemName = o.getItem().getName();
            orderPrice = o.getOrderPrice();
            count = o.getCount();
        }
    }


}
