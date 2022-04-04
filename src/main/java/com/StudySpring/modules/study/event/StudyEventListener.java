package com.StudySpring.modules.study.event;

import com.StudySpring.infra.config.AppProperties;
import com.StudySpring.infra.mail.EmailMessage;
import com.StudySpring.infra.mail.EmailService;
import com.StudySpring.modules.Notification.Notification;
import com.StudySpring.modules.Notification.NotificationRepository;
import com.StudySpring.modules.Notification.NotificationType;
import com.StudySpring.modules.account.Account;
import com.StudySpring.modules.account.AccountPredicates;
import com.StudySpring.modules.account.AccountRepository;
import com.StudySpring.modules.study.Study;
import com.StudySpring.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Slf4j
@Async
@Transactional
@Component
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent){
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        //tag와 zone에 매핑되는 account 찾음
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));
        accounts.forEach(account -> {
            if(account.isStudyCreatedByEmail()){ //email 알림
                //이메일 생성
                sendStudyCreatedEmail(study, account, "새로운 스터디가 생겼습니다",
                        "스터디올래, '" + study.getTitle() + "' 스터디가 생겼습니다.");
            }
            if(account.isStudyCreatedByWeb()){
                //웹 알림 notification create
                createNotification(study, account, study.getShortDescription(), NotificationType.STUDY_CREATED);
            }
        });

        // 이메일을 보내거나 DB에 Notification 정보 저장 - 이메일 알림, 웹 알림 목적

    }

    @EventListener
    public void handleStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent){
        Study study = studyRepository.findStudyWithManagersAndMembersById(studyUpdateEvent.getStudy().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getManagers());
        accounts.addAll(study.getMembers());

        accounts.forEach(account -> {
            if(account.isStudyUpdatedByEmail()){
                //메일 전송
                sendStudyCreatedEmail(study, account, studyUpdateEvent.getMessage(),
                        "스터디그룹, '" + study.getTitle() + "' 스터디에 새소식이 있습니다.");
            }
            if(account.isStudyUpdatedByWeb()){
                //웹 알림
                createNotification(study, account, studyUpdateEvent.getMessage(), NotificationType.STUDY_UPDATED);
            }
        });
    }

    //알람 생성
    private void createNotification(Study study, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/"+ study.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    //이메일 생성
    private void sendStudyCreatedEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("link", "/study/" + study.getEncodedPath());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
