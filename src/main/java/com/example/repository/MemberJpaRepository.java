package com.example.repository;

import com.example.dto.MemberSearchCondition;
import com.example.dto.MemberTeamDto;
import com.example.dto.QMemberTeamDto;
import com.example.entitiy.Member;
import com.example.entitiy.QMember;
import com.example.entitiy.QTeam;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.example.entitiy.QMember.member;
import static com.example.entitiy.QTeam.team;
import static org.springframework.util.StringUtils.*;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findAll_Querydsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery(
                "select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    /**
     * 동적 쿼리와 성능 최적화 - Builder & @QueryProjection 사용
     *
     * QMemberTeamDto는 생성자를 사용하기 때문에 필드 이름을 맞추지 않아도 된다
     *
     * hasText()는 주어진 문자열이 실제 텍스트를 포함하고 있는지 확인합니다.
     * 즉, 문자열이 null이 아니고, 길이가 0보다 크며, 공백 문자만으로 이루어져 있지 않을 때 true를 반환합니다.
     * 그 외의 경우에는 false를 반환합니다.
     *
     * @param condition
     * @return
     */

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

    /**
     * 동적 쿼리와 성능 최적화 - Where절 파라미터 & @QueryProjection 사용
     *
     * @param condition
     * @return
     */
    public List<MemberTeamDto> search(MemberSearchCondition condition) {

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoeEq(condition.getAgeGoe()),
                        ageLoeEq(condition.getAgeLoe())
                )
                .fetch();

    }

    private Predicate usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private Predicate teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private Predicate ageGoeEq(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private Predicate ageLoeEq(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }


}
