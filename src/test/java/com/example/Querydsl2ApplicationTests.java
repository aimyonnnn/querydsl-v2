package com.example;

import com.example.entitiy.Hello;
import com.example.entitiy.QHello;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class Querydsl2ApplicationTests {

    @Autowired
    EntityManager em;

    @Test
    void contextLoads() {

        Hello hello = new Hello();
        em.persist(hello); // 영속성 컨텍스트에 저장

        JPAQueryFactory query = new JPAQueryFactory(em); // 쿼리팩토리 생성
        QHello qHello = QHello.hello; // Q타입 동작 확인

        Hello result = query
                .selectFrom(qHello)
                .fetchOne();

        assertThat(result).isEqualTo(hello); // 검증
        assertThat(result.getId()).isEqualTo(hello.getId());

    }




}
