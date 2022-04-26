package com.smalldogg.jpashop.service;

import com.smalldogg.jpashop.domain.Delivery;
import com.smalldogg.jpashop.domain.Member;
import com.smalldogg.jpashop.domain.Order;
import com.smalldogg.jpashop.domain.OrderItem;
import com.smalldogg.jpashop.domain.item.Item;
import com.smalldogg.jpashop.repository.ItemRepository;
import com.smalldogg.jpashop.repository.MemberRepository;
import com.smalldogg.jpashop.repository.OrderRepository;
import com.smalldogg.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final MemberRepository memberRepository;
  private final ItemRepository itemRepository;

  /**
   *주문
   */
  @Transactional
  public Long order(Long memberId, Long itemId, int count) {

    //엔티티 조회
    Member member = memberRepository.findOne(memberId);
    Item item = itemRepository.findOne(itemId);

    //배송정보 생성
    Delivery delivery = new Delivery();
    delivery.setAddress(member.getAddress());

    //주문상품 생성
    OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

    //주문 생성
    Order order = Order.createOrder(member, delivery, orderItem);

    //주문 저장
    orderRepository.save(order); //cascade의 범위 : 라이프사이클에 대해서 동일하게 관리하는 관계를 가지는 대상일 때가 적합

    return order.getId();
  }


  /**
   * 주문 취소
   */
  @Transactional
  public void cancelOrder(Long orderId) {
    //주문 엔티티 조회
    Order order = orderRepository.findOne(orderId);
    //주문 취소
    order.cancel();
    // * transaction script pattern에 비해 코드가 많이 줄어듬. JPA의 장점
  }

  /**
   * 주문 검색
   */
  public List<Order> findOrders(OrderSearch orderSearch) {
    return orderRepository.findAllByString(orderSearch);
  }
}
