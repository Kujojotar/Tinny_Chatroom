package com.james.chat.config;

import com.james.chat.authentication.UserInfoMapper;
import com.james.chat.dao.AppUserMapper;
import com.james.chat.authentication.SessionAuthenticationFilter;
import com.james.chat.authentication.UsernamePasswordAuthenticationFilter;
import com.james.chat.redis.RedisService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final AppUserMapper appUserMapper;

    private final RedisService redisService;

    public WebSecurityConfig(AppUserMapper appUserMapper, RedisService redisService) {
        this.appUserMapper = appUserMapper;
        this.redisService = redisService;
    }

    @Override
    protected UserDetailsService userDetailsService() {
        return new UserInfoMapper(appUserMapper);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable();
        http.antMatcher("/user/b/**").authorizeRequests().anyRequest().hasRole("USER")
                .and()
                .formLogin().loginPage("/login/user").usernameParameter("username").passwordParameter("password").loginProcessingUrl("/user/b/login")
                .failureForwardUrl("/login")
                .and().addFilter(new SessionAuthenticationFilter(new AuthenticationManager() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),authentication.getCredentials());
            }
        }, redisService)).addFilter(new UsernamePasswordAuthenticationFilter(authenticationManager(), redisService)).
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER).and().addFilterBefore(new HttpFilter() {
            @Override
            protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type");
                    response.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,UPDATE");
                    response.setHeader("Access-Control-Allow-Origin", "*");
                    response.setHeader("Access-Allow-Control-Credentials","*");
                    response.setHeader("Access-Allow-Max-Age","36000");
                } else {
                    chain.doFilter(request, response);
                }
            }
        }, WebAsyncManagerIntegrationFilter.class);
    }
}
