package com.StudySpring.modules.study;

import com.StudySpring.modules.account.CurrentAccount;
import com.StudySpring.modules.account.Account;
import com.StudySpring.modules.study.form.StudyForm;
import com.StudySpring.modules.study.vaildator.StudyFormVaildator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyRepository studyRepository;
    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final StudyFormVaildator studyFormVaildator;

    @InitBinder("studyForm")
    public void studyFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(studyFormVaildator);
    }



    @GetMapping("/new-study")
    public String newStudyForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentAccount Account account, @Valid StudyForm studyForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "study/form";
        }

        Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class), account);
        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}")
    public String viewStudySubmit(@CurrentAccount Account account, @PathVariable String path, Model model){
        studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(studyRepository.findByPath(path));
        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentAccount Account account, @PathVariable String path, Model model){
        studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(studyRepository.findByPath(path));
        return "study/members";
    }

    @GetMapping("/study/{path}/join")
    public String joinStudy(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.addMember(study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }

    @GetMapping("/study/{path}/leave")
    public String leaveStudy(@CurrentAccount Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.removeMember(study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }

    @GetMapping("/study/data")
    public String generateTestData(@CurrentAccount Account account){
        studyService.generateTestStudies(account);
        return "redirect:/";
    }

}
