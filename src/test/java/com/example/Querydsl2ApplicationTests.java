package com.example;

import com.example.dto.MemberDto;
import com.example.dto.QMemberDto;
import com.example.entitiy.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.entitiy.QMember.*;
import static com.example.entitiy.QTeam.team;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class Querydsl2ApplicationTests {

    @Autowired EntityManager em;
    JPAQueryFactory queryFactory;

    /**
     * 테스트 데이터 생성
     */
    @BeforeEach
    public void before() {

        queryFactory = new JPAQueryFactory(em); // 쿼리팩토리 생성

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

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

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 페치 조인
     */
    @PersistenceUnit EntityManagerFactory emf;
    @Test // 페치 조인 미적용
    public void fetchJoinNo() {
        em.flush(); // 영속성 컨텍스트에 있는 쿼리를 DB에 반영
        em.clear(); // 영속성 컨텍스트를 초기화
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test // 페치 조인 적용
    public void fetchJoinUse() {
        em.flush();
        em.clear();
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**
     * 서브쿼리
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() {
        // 서브쿼리의 별칭을 지정해주어야 한다.
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 프로젝트 결과 반환 - DTO조회
     */
    @Test // Setter를 이용한 DTO 조회
    public void findDtoBySetter() {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test // 필드에 값을 매핑할 때는 프로퍼티 접근 방식이 아닌 필드 직접 접근 방식으로 해야 한다.
    public void findDtoByField() {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test // 생성자를 이용한 DTO 조회
    public void findDtoByConstructor() {
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * @QueryProjection 어노테이션을 이용한 DTO 조회
     */
    @Test
    public void findDtoByQueryProjection() {
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 동적 쿼리 - BooleanBuilder 사용
     */
    @Test
    public void dynamicQuery_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;
        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    /**
     *
     * @param usernameCond
     * @param ageCond
     * @return
     */
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if(ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    /**
     * 동적 쿼리 - Where 다중 파라미터 사용
     */
    @Test
    public void dynamicQuery_WhereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;
        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    // Ctrl + Shift + 위아래: 순서 변경
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    /**
     *
     * @param usernameCond
     * @return
     */
    private Predicate usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    /**
     *
     * @param ageCond
     * @return
     */
    private Predicate ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    /**
     * 벌크 연산
     * 주의: 벌크 연산을 먼저 실행하고 영속성 컨텍스트를 초기화 한 후에 조회하는 것이 안전하다.
     */
    @Test
    public void bulkUpdate() {
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();
        // 벌크 연산 후 영속성 컨텍스트 초기화
        em.flush(); // 영속성 컨텍스트에 있는 쿼리를 DB에 반영
        em.clear(); // 영속성 컨텍스트를 초기화
    }














































}
