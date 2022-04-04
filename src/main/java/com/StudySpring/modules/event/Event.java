package com.StudySpring.modules.event;

import com.StudySpring.modules.account.Account;
import com.StudySpring.modules.account.UserAccount;
import com.StudySpring.modules.study.Study;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//쿼리 개수를 줄이고  select n+1 문제 해결을 위해
@NamedEntityGraph(
        name = "Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments")
)
@Entity
@Getter @Setter @EqualsAndHashCode(of ="id")
public class Event {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Study study;

    @ManyToOne
    private Account createdBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = true)
    private Integer limitOfEnrollments;

    @OneToMany(mappedBy = "event") //이 연관관계의 주인은 Enrollment의 event 이다
    private List<Enrollment> enrollments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    //view.html 에서 현재 모임가입이 가능한 상황이면 참가 신청 버튼을
    public boolean isEnrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAttended(userAccount) && !isAlreadyEnrolled(userAccount);
    }

    // 가입이 된 상태면
    public boolean isDisenrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAttended(userAccount) && isAlreadyEnrolled(userAccount);
    }

    //이벤트가 종료 되지 않은 상태
    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    //참석이 완료된 상태면
    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }

        return false;
    }

    public int numberOfRemainSpots() { //지정 수강 인원중 현재 수강 인원을 뺌
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }


    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    //현재 유저 수 조회
    public long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public boolean canAccept(Enrollment enrollment){//수락
        return this.eventType == EventType.CONFIRMATIVE     //관리자 선택 타입이고
                && this.enrollments.contains(enrollment)    //enrollments 안에 들어있고
                && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments()
                && !enrollment.isAttended()                 //아직 참석하지 않았고
                && !enrollment.isAccepted();                //아직 수락하지 않았으면
    }

    public boolean canReject(Enrollment enrollment){//거절
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }
    // 추가할 수 있는 인원 여부를 확인하는 역할
    public boolean isAbleToAcceptWaitingEnrollment() {
        //선착순이면서 제한인원을 넘지 않았으면 true 값 리턴
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
    }

    //event와 enrollment 양방향 관계를 맺어주는 역할
    public void addEnrollment(Enrollment enrollment) {
        //event가 연관관계의 주인이기 때문에 enrollment에서 관계를 맺어준다
        this.enrollments.add(enrollment);
        enrollment.setEvent(this);
    }
    //event와 enrollment 양방향 관계를 끊어주는 역할
    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        enrollment.setEvent(null);
    }

    // 확정인원이 빠지고 공석이 발생하면 추가인원을 받을 수 있는지 확인하는 역할
    public void acceptNextWaitingEnrollment() {
        if (this.isAbleToAcceptWaitingEnrollment()) { //만약 추가인원을 받을 수 있는 상태면
            Enrollment enrollmentToAccept = this.getTheFirstWaitingEnrollment(); //대기중인 유저 조회
            if (enrollmentToAccept != null) {
                enrollmentToAccept.setAccepted(true);
            }
        }
    }

    //대기중인 Enrollment를 조회하는 역할
    private Enrollment getTheFirstWaitingEnrollment() {
        for (Enrollment e : this.enrollments) {
            if (!e.isAccepted()) {
                return e;
            }
        }

        return null;
    }

    //모임인원이 늘어나면 waitingList 크기를 늘어난 만큼 늘려주는 역할
    public void acceptWaitingList() {
        if (this.isAbleToAcceptWaitingEnrollment()) {
            var waitingList = getWaitingList();
            int numberToAccept = (int) Math.min(this.limitOfEnrollments - this.getNumberOfAcceptedEnrollments(), waitingList.size());
            waitingList.subList(0, numberToAccept).forEach(e -> e.setAccepted(true));
        }
    }

    private List<Enrollment> getWaitingList() {
        return this.enrollments.stream().filter(enrollment -> !enrollment.isAccepted()).collect(Collectors.toList());
    }

    public void accept(Enrollment enrollment) {
        if (this.eventType == EventType.CONFIRMATIVE
                && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments()) {
            enrollment.setAccepted(true);
        }
    }

    public void reject(Enrollment enrollment) {
        if (this.eventType == EventType.CONFIRMATIVE) {
            enrollment.setAccepted(false);
        }
    }
}
