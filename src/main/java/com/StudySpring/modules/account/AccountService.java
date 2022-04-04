package com.StudySpring.modules.account;

import com.StudySpring.modules.account.form.SignUpForm;
import com.StudySpring.infra.config.AppProperties;
import com.StudySpring.modules.tag.Tag;
import com.StudySpring.modules.zone.Zone;
import com.StudySpring.infra.mail.EmailMessage;
import com.StudySpring.infra.mail.EmailService;
import com.StudySpring.modules.account.form.Notifications;
import com.StudySpring.modules.account.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @Transactional //newAccount 객체는 준영속 상태 객체임 따라서 DB에 저장하기 위해 트랜잭션 범위로 지정하여 persist 상태로 유지되도록 함
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm); //account 객체에 값 저장
        //newAccount.generateEmailCheckToken(); //email 토큰 생성성
        sendSignUpConfirmEmail(newAccount); //email 생성
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) { //Account 값 저장
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken(); //email 토큰 생성

//        Account account = Account.builder()
//                .email(signUpForm.getEmail())
//                .nickname(signUpForm.getNickname())
//                .password(passwordEncoder.encode(signUpForm.getPassword())) //pw encoding
//                .studyCreatedByWeb(true)
//                .studyEnrollmentResultByWeb(true)
//                .studyUpdatedByWeb(true)
//                .build();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) { //메일 생성 후 전송
        //HTML 메시지 생성 및 전송
        Context context = new Context(); //타입리프 모델
        context.setVariable("link","/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname",newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디그룹 서비스를 사용하시려면 링크를 클릭해주세요");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        //콘솔용용
        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디그룹, 회원 가입 인증")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }


    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))); //권한 설정 (권한이 있는 사용자다)
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    // 로그인을 하기 위해 DB 에 있는 정보를 조회하기 위해 구현
    // 시큐리티를 통해 요청을 처리하는 핸들러는 안만들어도 됨
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션  성능에 이점
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp(); //회원가입 성공 처리
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account);

//        account.setUrl(profile.getUrl());
//        account.setOccupation(profile.getOccupation());
//        account.setLocation(profile.getLocation());
//        account.setBio(profile.getBio());
//        account.setProfileImage(profile.getProfileImage());
        accountRepository.save(account);
    }


    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account); //merge
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);

//        account.setStudyCreatedByWeb(notifications.isStudyCreatedByWeb());
//        account.setStudyCreatedByEmail(notifications.isStudyCreatedByEmail());
//        account.setStudyUpdatedByWeb(notifications.isStudyUpdatedByWeb());
//        account.setStudyUpdatedByEmail(notifications.isStudyUpdatedByEmail());
//        account.setStudyEnrollmentResultByEmail(notifications.isStudyEnrollmentResultByEmail());
//        account.setStudyEnrollmentResultByWeb(notifications.isStudyEnrollmentResultByWeb());
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account); //account 객체는 준영속 상태 따라서 save를 해주어야 반영됨됨
        login(account); //변경된 내용을 적용하기 위해 로그인 진행
    }

    public void sendLoginLink(Account account) {
        //HTML 메시지 생성 및 전송
        Context context = new Context(); //타입리프 모델
        context.setVariable("link","/check-email-token?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname",account.getNickname());
        context.setVariable("linkName", "이메일 로그인");
        context.setVariable("message", "스터디그룹 로그인을 하시려면 링크를 클릭해주세요");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);


        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디그룹, 로그인 링크")
                .subject("/login-by-email?token=" + account.getEmailCheckToken() +
                        "&email=" +account.getEmail())
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId()); //account를 읽어오고
        byId.ifPresent(a -> a.getTags().add(tag));                            //만약 있으면 추가
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags(); //없으면 에러정보 있으면 태그정보
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }

    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if(account == null){
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        return account;
    }
}

