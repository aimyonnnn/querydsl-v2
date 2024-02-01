package com.example.controller;

import com.example.dto.MemberSearchCondition;
import com.example.dto.MemberTeamDto;
import com.example.entitiy.Member;
import com.example.repository.MemberJpaRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public Result<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return new Result(memberJpaRepository.search(condition));
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }











}
