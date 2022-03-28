package com.StudySpring.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) //런타임까지 유지 설정
@Target(ElementType.PARAMETER) //파라미터에서 붙일 수 있도록 타켓 설정
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account") //현재 객체가 익명 사용자 라면 널
public @interface CurrentAccount {
}


//스프링 시큐리티로 접근가능하도록 설정해놓은 url에 로그인 없이 접근한 유저는 anonymousUser로 간주