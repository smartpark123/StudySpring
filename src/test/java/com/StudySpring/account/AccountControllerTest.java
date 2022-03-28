package com.StudySpring.account;

import com.StudySpring.domain.Account;
import com.StudySpring.mail.EmailMessage;
import com.StudySpring.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    EmailService emailService;

    @DisplayName("인증 메일 확인 ~ 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception{
        mockMvc.perform(get("/check-email-token")
                .param("token", "spdlqj")
                .param("email", "pusisi1@naver.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated()); // 인증이 안되었는지 확인
    }

    @DisplayName("인증 메일 확인 ~ 입력값 정상")
    @Test
    void checkEmailToken() throws Exception{

        Account account = Account.builder()
                .email("pusisi1@nvaer.com")
                .password("12345678")
                .nickname("pjs")
                .build();
        Account newAccount = accountRepository.save(account); //account 저장
        newAccount.generateEmailCheckToken(); //토큰 생성

        mockMvc.perform(get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("pjs")); //pjs로 인증이 되었는지 확인
    }


    @DisplayName("회원 가입 화면 보이는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @DisplayName("회원 가입 처리 ~ 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception{
        mockMvc.perform(post("/sign-up")
                .param("nickname","pjs")
                .param("email","email...")
                .param("password","12345")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());

    }

    @DisplayName("회원 가입 처리 ~ 입력값 정상")
    @Test
    void signUpSubmit_with_correct_input() throws Exception{
        mockMvc.perform(post("/sign-up")
                .param("nickname","pjs")
                .param("email","pusisi1@naver.com")
                .param("password","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("pjs"));

        Account account = accountRepository.findByEmail("pusisi1@naver.com");
        assertNotNull(account); //null이 아닌지 확인
        assertNotEquals(account.getPassword(),"12345678"); //account의 pw가 입력된 값이랑 다른지 확인
        assertNotNull(account.getEmailCheckToken()); //  토큰 값이 널인지 아닌지 확인
        then(emailService).should().sendEmail(any(EmailMessage.class)); //send 호출 확인

    }



}