package com.StudySpring.modules.event;


import com.StudySpring.infra.MockMvcTest;
import com.StudySpring.modules.account.AccountFactory;
import com.StudySpring.modules.account.WithAccount;
import com.StudySpring.modules.account.AccountRepository;
import com.StudySpring.modules.account.Account;
import com.StudySpring.modules.study.Study;
import com.StudySpring.modules.study.StudyControllerTest;
import com.StudySpring.modules.study.StudyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class EventControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;
    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    @DisplayName ("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount ("pjs123")
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account whiteship = accountFactory.createAccount("whiteship");
        Study study = studyFactory.createStudy("test-study", whiteship);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, whiteship);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account pjs123 = accountRepository.findByNickname("pjs123");
        isAccepted(pjs123, event);
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 (이미 인원이 꽉차서)")
    @WithAccount("pjs123")
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account whiteship = accountFactory.createAccount("whiteship");
        Study study = studyFactory.createStudy("test-study", whiteship);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, whiteship);

        Account may = accountFactory.createAccount("may");
        Account june = accountFactory.createAccount("june");
        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, june);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account pjs123 = accountRepository.findByNickname("pjs123");
        isNotAccepted(pjs123, event);
    }

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithAccount("pjs123")
    void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account pjs123 = accountRepository.findByNickname("pjs123");
        Account whiteship = accountFactory.createAccount("whiteship");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", whiteship);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, whiteship);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, pjs123);
        eventService.newEnrollment(event, whiteship);

        isAccepted(may, event);
        isAccepted(pjs123, event);
        isNotAccepted(whiteship, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(whiteship, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, pjs123));
    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithAccount("pjs123")
    void not_accepterd_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account pjs123 = accountRepository.findByNickname("pjs123");
        Account whiteship = accountFactory.createAccount("whiteship");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", whiteship);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, whiteship);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, whiteship);
        eventService.newEnrollment(event, pjs123);

        isAccepted(may, event);
        isAccepted(whiteship, event);
        isNotAccepted(pjs123, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(whiteship, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, pjs123));
    }

    private void isNotAccepted(Account whiteship, Event event) {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, whiteship).isAccepted());
    }

    private void isAccepted(Account account, Event event) {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("pjs123")
    void newEnrollment_to_CONFIMATIVE_event_not_accepted() throws Exception {
        Account whiteship = accountFactory.createAccount("whiteship");
        Study study = studyFactory.createStudy("test-study", whiteship);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, whiteship);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account pjs123 = accountRepository.findByNickname("pjs123");
        isNotAccepted(pjs123, event);
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setTitle(eventTitle);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(study, event, account);
    }

}