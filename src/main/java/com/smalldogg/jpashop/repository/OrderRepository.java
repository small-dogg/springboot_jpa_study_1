package com.smalldogg.jpashop.repository;

import com.smalldogg.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * String으로 조건에 대한 동적 쿼리(권장x)
     */

    public List<Order> findAllByString(OrderSearch orderSearch) {
        //동적 쿼리 처리 방법 1

        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition = true;

        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (orderSearch.getMemberName() != null) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * JPA Criteria를 사용한 동적 쿼리(권장x)
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                        "select o from Order o" +
                                " join fetch o.member m" +
                                " join fetch o.delivery d", Order.class)
                .getResultList();
    }

    //여기서 distinct는 결과 값이 동일한 Row에 대해서 DB에서의 중복제거도 있겠지만,
    // Entity Manager에서도 보유한 Root Entity 대상의 중복을 제거하여 총 2개의 결과만 반환한다.
    //이 예제에서는 중복 제거를 하지 않으면, 결과는 4개!
    //근데... 얘는 절대로 Paging을 처리할 수 없어...ㅠㅠ
    //[WARN] firstResult/maxResults specified with collection fetch; applying in memory!
    // 원래 4개짜리 쿼리 결과를 들고와서 중복제거를하고 Paging 처리까지하라고? 그러면 Paging할 대상의 Row는 무엇을 기준으로 해야하는데?
    // JPA도 미쳐버리는거지... Join으로 뻥튀기가 되어버렸으니깐.. 그래서 메모리에서 해버리겠다는 그런 결정..
    // OutOfMemoryException 각!🌟🌟
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i", Order.class)
                .getResultList();
    }

//    public List<OrderSimpleQueryDto> findOrderDtos() {
//        return em.createQuery(
//                "select new com.smalldogg.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
//                        " from Order o" +
//                        " join o.member m" +
//                        " join o.delivery d", OrderSimpleQueryDto.class)
//                .getResultList();
//    }
}
