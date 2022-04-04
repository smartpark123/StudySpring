package com.StudySpring.modules.event.form;


import com.StudySpring.modules.event.EventType;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class EventForm {

    @NotBlank
    @Length(max = 50)
    private String title;

    private String description;

    private EventType eventType = EventType.FCFS; //선입선출 지정

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)  //datetype format  yyyy-MM-dd HH:mm:ss
    private LocalDateTime endEnrollmentDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;

    @Min(2) // 최소 2 이상
    private Integer limitOfEnrollments = 2;

}
