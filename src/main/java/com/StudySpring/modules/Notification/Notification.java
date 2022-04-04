package com.StudySpring.modules.Notification;

import com.StudySpring.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.tomcat.jni.Local;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Notification {

    @Id @GeneratedValue
    private Long id;

    private String title;

    private String link;

    private String message;

    private boolean checked;

    @ManyToOne
    private Account account;  //유저 알림설정 조회

    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;



}
