package com.StudySpring.account;


import com.StudySpring.domain.Account;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UserAccount extends User { //User 는 시큐리티에서 오는 정보

    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account=account;
    }
}

//시큐리티에서 사용하는 유저정보와 도메인에서 사용하는 유저정보의 어댑터 역할