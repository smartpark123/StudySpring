package com.StudySpring.modules.study.event;

import com.StudySpring.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;

@Getter
@RequiredArgsConstructor
public class StudyUpdateEvent {

    private final Study study;

    private final String message;

}
