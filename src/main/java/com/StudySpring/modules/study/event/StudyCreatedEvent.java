package com.StudySpring.modules.study.event;

import com.StudySpring.modules.study.Study;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Data
@Getter
public class StudyCreatedEvent {

    private Study study;

    public StudyCreatedEvent(Study study) {
        this.study = study;
    }


}
