package com.james.chat.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.james.chat.entity.LoginUser;
import com.james.chat.redis.RedisService;
import com.james.chat.redis.RedisUserInfo;
import com.james.chat.util.JacksonUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

public class UsernamePasswordAuthenticationFilter extends org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final RedisService redisService;

    private final String OPTION_METHOD = "OPTIONS";

    public UsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager, RedisService redisService) {
        this.authenticationManager = authenticationManager;
        this.redisService = redisService;
        super.setFilterProcessesUrl("/user/b/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (OPTION_METHOD.equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Access-Control-Allow-Headers", "Authorization");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,UPDATE");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Allow-Control-Credentials","*");
            response.setHeader("Access-Allow-Max-Age","36000");
            return null;
        } else {
            response.setHeader("Access-Control-Allow-Headers", "Authorization");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,UPDATE");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Allow-Control-Credentials","*");
            response.setHeader("Access-Allow-Max-Age","36000");
        }
        LoginUser user = null;
        StringBuilder body = new StringBuilder();
        try {
            BufferedReader reader = request.getReader();
            String tmp;
            while((tmp = reader.readLine())!=null) {
                body.append(tmp);
            }
            tmp = body.toString();
            ObjectMapper mapper = new ObjectMapper();
            user = mapper.<LoginUser>readValue(tmp, LoginUser.class);
        } catch (IOException e) {
            try {
                response.getWriter().write("HTTP/1.2 500 \n\r\n\rSystem Error");
            }catch (IOException e2) {
                user = null;
            }
        }
        if (user == null) {
            return null;
        }
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            if (authentication!=null && authentication.isAuthenticated()) {
                RedisUserInfo redisUserInfo = new RedisUserInfo();
                redisUserInfo.setUsername(user.getUsername());
                try {
                    String encodedKey = user.getUsername();
                    redisService.set(encodedKey, JacksonUtil.writeObject(redisUserInfo));
                    request.setAttribute("Authorization", encodedKey);
                    Cookie cookie = new Cookie("Authorization", encodedKey);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (InternalAuthenticationServiceException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\n\t\"code\": 500,\n\t\"success\": false,\n\t\"message\": \"登陆失败\",\n\t\"data\": \"用户名或密码错误\"\n}");
            }catch (IOException ee){
            }
        }
        return authentication;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        response.setHeader("Content-Encoding", "UTF-8");
        response.getWriter().write("{\n\t\"code\": 200,\n\t\"success\": true,\n\t\"message\": \"登陆成功\",\n\t\"data\": \""+ request.getAttribute("Authorization")+"\"\n}");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        if (!request.getMethod().equals(OPTION_METHOD)) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\n\t\"code\": 500,\n\t\"success\": false,\n\t\"message\": \"登陆失败\",\n\t\"data\": \"用户名或密码错误\"\n}");
        }
    }
}
