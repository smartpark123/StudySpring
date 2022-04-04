package com.StudySpring.modules.study.vaildator;

import com.StudySpring.modules.study.StudyRepository;
import com.StudySpring.modules.study.form.StudyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


@Component
@Transactional
@RequiredArgsConstructor
public class StudyFormVaildator implements Validator {

    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> clazz){
        return StudyForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm)target;
        if( studyRepository.existsByPath(studyForm.getPath())){ //패스 유무 확인
            errors.rejectValue("path", "wrong.path","해당 스터디 경로를 사용할 수 없습니다.");
        }


    }



}

