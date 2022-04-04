package com.StudySpring.modules.study;

import com.StudySpring.modules.account.QAccount;
import com.StudySpring.modules.tag.QTag;
import com.StudySpring.modules.tag.Tag;
import com.StudySpring.modules.zone.QZone;
import com.StudySpring.modules.zone.Zone;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.querydsl.QuerydslRepositoryInvokerAdapter;

import java.util.List;
import java.util.Set;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension{

    public StudyRepositoryExtensionImpl() {
        super(Study.class);
    }

    @Override
    public Page<Study> findByKeyword(String keyword, Pageable pageable) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword))
                .or(study.tags.any().title.containsIgnoreCase(keyword))
                .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(study.tags, QTag.tag).fetchJoin() //study 에 tags 는 QTag 에 조인
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .leftJoin(study.members, QAccount.account).fetchJoin()
                .distinct();
        //페이징 적용
        JPQLQuery<Study> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Study> fetchResults = pageableQuery.fetchResults();// 페이징 정보 포함 데이터 가져옴
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal()); //컨텐츠 리스트, pagealbe, 전체 데이터 수
    }

    @Override
    public List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.closed.isFalse())
                .and(study.tags.any().in(tags))
                .and(study.zones.any().in(zones)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .orderBy(study.publishedDateTime.desc())
                .distinct()
                .limit(9);
        return query.fetch();
    }
}
