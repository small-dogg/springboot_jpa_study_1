package com.smalldogg.jpashop.service;

import com.smalldogg.jpashop.domain.Member;
import com.smalldogg.jpashop.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)//readOnly 옵션을 작성하면 조회하는데 좀 더 성능이 좋음.
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;

  /**
   * 회원 가입
   */
  @Transactional//readonly 기본값이 false
  public Long join(Member member){
    validateDuplicateMember(member);// 중복 회원 검증
    memberRepository.save(member);
    return member.getId();
  }

  /**
   * 이름 중복 방지
   */
  private void validateDuplicateMember(Member member) {
    //EXCEPTION
    List<Member> findMembers = memberRepository.findByName(member.getName());//이런 경우, memeber의 이름을 유니크 제약조건으로 걸어준다.
    if(!findMembers.isEmpty()){
      throw new IllegalStateException("이미 존재하는 회원입니다.");
    }
  }

  /**
   * 회원 전체 조회
   */
  public List<Member> findMembers(){
    return memberRepository.findAll();
  }

  /**
   * 회원 단일 조회
   */
  public Member findOne(Long memberId){
    return memberRepository.findOne(memberId);
  }
}
