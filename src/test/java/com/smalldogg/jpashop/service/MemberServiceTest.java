package com.smalldogg.jpashop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.smalldogg.jpashop.domain.Member;
import com.smalldogg.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {

  @Autowired MemberService memberService;
  @Autowired MemberRepository memberRepository;

  @Test
  public void 회원가입() throws Exception {
    //given
    Member member = new Member();
    member.setName("Kim");

    //when
    Long savedId = memberService.join(member);

    //then
    assertEquals(member,memberRepository.findOne(savedId));
  }

  @Test
  public void 중복_회원_예외() throws Exception {
    //given
    Member member1 = new Member();
    member1.setName("kim");

    Member member2 = new Member();
    member2.setName("kim");

    //when
    memberService.join(member1);

    //then
    assertThrows(IllegalStateException.class,() ->{
      memberService.join(member2);
    });
  }
}