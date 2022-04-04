package com.StudySpring.modules.event;


import com.StudySpring.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account); //있는지 없는지 확인

    Enrollment findByEventAndAccount(Event event, Account account);

    @EntityGraph ("Enrollment.withEventAndStudy")
    List<Enrollment> findByAccountAndAcceptedOrderByEnrolledAtDesc(Account account, boolean accepted);
}
