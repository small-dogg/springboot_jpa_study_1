package com.smalldogg.jpashop;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

  @PersistenceContext
  private EntityManager em;

  public Long save(Member member) {
    em.persist(member);
    return member.getId(); // 커맨드와 쿼리를 분리해라.
  }

  public Member find(Long id) {
    return em.find(Member.class, id);
  }
}
