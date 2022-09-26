package com.james.chat.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.james.chat.dao")
public class MybatisConfiguration {
}
