package com.smalldogg.jpashop.service.query;

import com.smalldogg.jpashop.domain.OrderItem;
import lombok.Getter;

@Getter
class OrderItemDto {
    private String itemName;//상품 명
    private int orderPrice;// 주문 가격
    private int count;// 주문 수량

    public OrderItemDto(OrderItem o) {
        itemName = o.getItem().getName();
        orderPrice = o.getOrderPrice();
        count = o.getCount();
    }
}