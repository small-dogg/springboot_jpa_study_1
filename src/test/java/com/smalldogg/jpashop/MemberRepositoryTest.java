package com.smalldogg.jpashop;

import com.smalldogg.jpashop.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MemberRepositoryTest {

  @Autowired MemberRepository memberRepository;

  @Test
  @Transactional //Transactional 애노테이션이 Test에 있으면, 수행 후 롤백을 수행함.
  @Rollback(value = false) // Rollback을 false로 수정하면, Test에서 수행해도 Rollback 하지 않음.
  public void testMember() throws Exception {
    //given
    Member member = new Member();
    member.setUsername("memberA");

    //when
    Long savedId = memberRepository.save(member);
    Member findMember = memberRepository.find(savedId);

    //then
    Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
    Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
    //영속성 컨텍스트의 기본 개념
    Assertions.assertThat(findMember).isEqualTo(member);
  }
}