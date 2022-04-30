package com.smalldogg.jpashop.service.query;

import com.smalldogg.jpashop.domain.Order;
import com.smalldogg.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;


// OSIV Disable 시, 발생하는 지연 로딩 문제를 해결하기 위해 쿼리용 서비스를 별도로 분리

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }
}
