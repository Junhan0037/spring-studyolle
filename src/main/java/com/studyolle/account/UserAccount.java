package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Getter
public class UserAccount extends User { // Spring security에 account 프로퍼티를 적용

    private final Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }

}
