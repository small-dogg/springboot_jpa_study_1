package com.smalldogg.jpashop.api;

import com.smalldogg.jpashop.domain.Member;
import com.smalldogg.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    //Entity를 직접 파라미터로 전달받으면, API 스펙이 변경되었을 때 문제가 발생함
    //그 밖에도, side effect가 무수히 발생함.
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //Data Transfer Object를 별도로 작성하면, 필요한 필드가 무엇인지를 명확히 이해할 수 있음
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //PUT은 멱등하다. 같은것을 여러번 호출해도 결과가 똑같다.
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid UpdateMemberRequest request) {
        //커맨드와 쿼리를 명확히 분리한다. update메서드는 업데이트를 위한 것임. 결과를 전달하기 위해, 조회성 서비스 호출을 따로 해줘.
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }

    // 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
    // 기본적으로 엔티티의 모든 값이 노출된다.
    // 응답 스펙을 맞추기 위해 로직이 추가된다(@JsonIgnore, 별도의 뷰 로직 등)
    // 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
    // 엔티티가 변경되면 API 스펙이 변한다.
    // 추가로 컬렉션을 직접 반환하면 향후 API 스펙을 변경하기도 어렵다.(별도의 REsult 클래스 생성으로 해결)
    // => API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
    @GetMapping("/api/v1/members")
    public List<Member> getMemberV1() {
        return memberService.findMembers();
    }

    // v1 문제점 해결
    @GetMapping("/api/v2/members")
    public Result getMemberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;

        private String name;
    }

    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }
}
