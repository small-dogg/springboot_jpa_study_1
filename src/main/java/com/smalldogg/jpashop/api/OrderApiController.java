package com.smalldogg.jpashop.api;

import com.smalldogg.jpashop.domain.Address;
import com.smalldogg.jpashop.domain.Order;
import com.smalldogg.jpashop.domain.OrderItem;
import com.smalldogg.jpashop.domain.OrderStatus;
import com.smalldogg.jpashop.repository.OrderRepository;
import com.smalldogg.jpashop.repository.OrderSearch;
import com.smalldogg.jpashop.repository.order.query.OrderFlatDto;
import com.smalldogg.jpashop.repository.order.query.OrderItemQueryDto;
import com.smalldogg.jpashop.repository.order.query.OrderQueryDto;
import com.smalldogg.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

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
                .collect(toList());
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    // Global Batch Size를 작성하였다. 연관관계상에 존재하는 toMany 엔티티들을 자동으로 지연로딩하였다.
    // hibernate.default_batch_fetch_size는 100~1000 사이의 값을 사용하는 것을 권장하는데,
    // WAS와 DB가 버틸 수 있다면 높게설정하면되고, 사실 메모리 최적화의 관점을 잘 고려해야함..
    // in절의 파라미터를 여러개를 한꺼번에 넘겨 여러개의 데이터를 한번에 팍팍 받을 것이냐.
    // 아니면 나눠서 적당한수준의 양으로 여러번 오래 받을 것이냐
    // 시간과 부하 간의 트레이드-오프한 부분인 것 같다.. 시스템이 감당할 수 있는 수준을 의사결정하는 것도 결국엔 의사결정자의 몫.
    // 넌지스레 던지는 말로 대충 500놓고 쓰면 되지 않겠냐... XD
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100")int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // toOne 관계에 걸리는 대상들 fetch join(한방쿼리)으로 해결.
        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    //ToOne 관계는 조인해서 바로 해결하고, ToMany 관계는 별도의 메서드를 작성하고, DTO를 만들어서 반환해줌
    //단점은 N+1 문제. 추가 ToMany 관계의 메서드에서 수행하는 쿼리가 실행된다는 점이다.
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    //쿼리를 1번만 날림
    //중복데이터이 추가되므로 상황에 따라 V5보다 느릴 수있음
    //애플리케이셔에서 추가 작업이 큼
    //페이징이 안됨(중복데이터 때문에)
    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        //개발자가 어떻게든 지지고 볶아가지고, 싹다 조인해서 분해하고 조립하여, 원하는 결과를 생성
//        return flats.stream()
//                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),o.getName(),))
//                mapping(o -> new OrderItemQueryDto(필드맵핑)
//                )).entrySet().stream()
//                .map(e -> new OrderQueryDto(필드맵핑)
//                        .collect(toList()));
        return new ArrayList<>();
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
                    .collect(toList());
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
