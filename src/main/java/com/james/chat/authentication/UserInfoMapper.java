package com.james.chat.authentication;

import com.james.chat.dao.AppUserMapper;
import com.james.chat.entity.AppUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserInfoMapper implements UserDetailsService {

    private final AppUserMapper mapper;

    public UserInfoMapper(AppUserMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AppUser user = mapper.selectByUsername(s);
        return user == null? null:new UserInfoWrapper(user);
    }
}
