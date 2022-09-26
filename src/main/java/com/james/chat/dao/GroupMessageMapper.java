package com.james.chat.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupMessageMapper {

    @Select("select name from friendgroup where userId = (select userId from user where userName=#{username})")
    public List<String> getUserGroups(String username);

    @Select("select userName from user where userId in (select userId from friendGroup where name = #{name})")
    public List<String> getGroupUsernames(String name);
}
