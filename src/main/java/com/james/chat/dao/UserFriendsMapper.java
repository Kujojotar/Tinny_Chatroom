package com.james.chat.dao;

import com.james.chat.entity.UserFriends;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserFriendsMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserFriends record);

    UserFriends selectByPrimaryKey(Integer id);

    List<UserFriends> selectAll();

    int updateByPrimaryKey(UserFriends record);


}
