package com.StudySpring.modules.event.validator;

import com.StudySpring.modules.event.Event;
import com.StudySpring.modules.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm)target;

        if(isNotValidEndEnrollmentDatetime(eventForm)) { //오늘 날짜 전 시간이면
            errors.rejectValue("endEnrollmentDateTime", "wrong.datetime", "모임 접수 종료 일시를 정확히 입력하세요.");
        }

        //종료 시간이 스타드 시간 이전이거나 접수 종료 시간보다 이전이면
        if(isNotValidEndDateTime(eventForm)){
            errors.rejectValue("endDateTime", "wrong.datetime", "모임 종료 일시를 정확히 입력하세요.");
        }
        //접수가 끝나기 전에 시작하면
        if(isNotValidStartDateTime(eventForm)){
            errors.rejectValue("startDateTime", "wrong.datetime", "모임 시작 일시를 정확히 입력하세요.");
        }

    }

    private boolean isNotValidStartDateTime(EventForm eventForm) {
        LocalDateTime startDateTime = eventForm.getStartDateTime();
        return startDateTime.isBefore(eventForm.getEndEnrollmentDateTime());
    }

    private boolean isNotValidEndEnrollmentDatetime(EventForm eventForm) {
        return eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }

    private boolean isNotValidEndDateTime(EventForm eventForm) {
        return eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime()) || eventForm.getEndDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        if (eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()) {
            errors.rejectValue("limitOfEnrollments", "wrong.value", "확인된 참기 신청보다 모집 인원 수가 커야 합니다.");
        }
    }
}
