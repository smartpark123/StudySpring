package com.StudySpring.study;

import com.StudySpring.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional (readOnly = true)
public interface StudyRepository extends JpaRepository<Study,Long> {
    boolean existsByPath(String path);

    //조인 그래프 사용   발생하는 쿼리 개수를 줄이기 위해 사용
    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);
}
