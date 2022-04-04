package com.StudySpring.modules.event;

import com.StudySpring.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;


@NamedEntityGraph(
        name = "Enrollment.withEventAndStudy",
        attributeNodes = {
                @NamedAttributeNode(value = "event", subgraph = "study")
        },
        subgraphs = @NamedSubgraph(name = "study", attributeNodes = @NamedAttributeNode("study"))
)
@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne  //여기서 값이 변경되야 DB에 적용이 된다
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;  // 정렬 역할

    private boolean accepted;  //참가 확정

    private boolean attended; // 실제 참가 여부
}
