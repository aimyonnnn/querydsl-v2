package com.example.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    private String username;
    private int age;

    @QueryProjection // Querydsl에서 DTO로 바로 조회하기 위해 사용
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
