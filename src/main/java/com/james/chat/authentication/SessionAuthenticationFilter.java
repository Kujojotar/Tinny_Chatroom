package com.james.chat.authentication;

import com.james.chat.config.UserRoles;
import com.james.chat.redis.RedisService;
import com.james.chat.redis.RedisUserInfo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class SessionAuthenticationFilter extends BasicAuthenticationFilter {

    private final RedisService redisService;

    public SessionAuthenticationFilter(AuthenticationManager authenticationManager, RedisService redisService) {
        super(authenticationManager);
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        if (validateSession(header)) {
            SecurityContextHolder.getContext().setAuthentication(getAuthentication(header));
        }
        response.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,UPDATE");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Allow-Control-Credentials","*");
        response.setHeader("Access-Allow-Max-Age","36000");
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String sessionId) {
        String key = sessionId;
        RedisUserInfo info = redisService.get(key, RedisUserInfo.class);
        return new UsernamePasswordAuthenticationToken(info.getUsername(), null, Collections.singleton(new SimpleGrantedAuthority(UserRoles.ROLE_USER)));
    }

    private boolean validateSession(String cookie) {
        if (cookie == null) {
            return false;
        }
        String sessionId = cookie;
        if (sessionId == null) {
            return false;
        }
        boolean sessionExists = redisService.exists(sessionId);
        return sessionExists;
    }
}
