package com.StudySpring.modules.Notification;

import com.StudySpring.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional (readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByAccountAndChecked(Account account, boolean checked);

    @Transactional
    List<Notification> findByAccountAndCheckedOrderByCreatedDateTimeDesc(Account account, boolean b);

    @Transactional
    void deleteByAccountAndChecked(Account account, boolean checked);
}
