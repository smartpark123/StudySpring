package com.StudySpring.modules.study;

import com.StudySpring.infra.MockMvcTest;
import com.StudySpring.modules.account.AccountFactory;
import com.StudySpring.modules.account.AccountRepository;
import com.StudySpring.modules.account.WithAccount;
import com.StudySpring.modules.account.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@MockMvcTest
class studySettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyRepository studyRepository;

    @Test
    @WithAccount("pjs123")
    @DisplayName ("스터디 소개 수정 폼 조회 - 실패 (권한 없는 유저)")
    void updateDescriptionForm_fail() throws Exception {
        Account whiteship = accountFactory.createAccount("whiteship");
        Study study = studyFactory.createStudy("test-study", whiteship);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isForbidden());
    }



    @Test
    @WithAccount("pjs123")
    @DisplayName("스터디 소개 수정 폼 조회 - 성공")
    void updateDescriptionForm_success() throws Exception {
        Account pjs123 = accountRepository.findByNickname("pjs123");
        Study study = studyFactory.createStudy("test-study", pjs123);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithAccount("pjs123")
    @DisplayName("스터디 소개 수정 - 성공")
    void updateDescription_success() throws Exception {
        Account pjs123 = accountRepository.findByNickname("pjs123");
        Study study = studyFactory.createStudy("test-study", pjs123);

        String settingsDescriptionUrl = "/study/" + study.getPath() + "/settings/description";
        mockMvc.perform(post(settingsDescriptionUrl)
                .param("shortDescription", "short description")
                .param("fullDescription", "full description")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(settingsDescriptionUrl))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("pjs123")
    @DisplayName("스터디 소개 수정 - 실패")
    void updateDescription_fail() throws Exception {
        Account pjs123 = accountRepository.findByNickname("pjs123");
        Study study = studyFactory.createStudy("test-study", pjs123);

        String settingsDescriptionUrl = "/study/" + study.getPath() + "/settings/description";
        mockMvc.perform(post(settingsDescriptionUrl)
                .param("shortDescription", "")
                .param("fullDescription", "full description")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("study"))
                .andExpect(model().attributeExists("account"));
    }

}
