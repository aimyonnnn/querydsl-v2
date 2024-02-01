package com.example.controller;

import com.example.dto.MemberSearchCondition;
import com.example.dto.MemberTeamDto;
import com.example.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private  final MemberJpaRepository memberJpaRepository;

    /**
     * 예제 실행(postman)
     * http://localhost:8080/v1/members?teamName=teamA&ageGoe=35&ageLoe=40
     *
     * @param condition
     * @return
     */
    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }






}
