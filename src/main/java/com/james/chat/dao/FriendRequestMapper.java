package com.james.chat.dao;

import com.james.chat.entity.FriendRequest;
import com.james.chat.entity.RequestVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FriendRequestMapper {
    int insert(FriendRequest record);

    List<FriendRequest> selectAll();

    @Select("select exists(select request_user from friend_request where request_user=(select userId from user where userName=#{arg0}) and " +
            "accept_user=(select userId from user where userName=#{arg1}))")
    boolean existsRequest(String arg0, String arg1);

    @Select("select exists(select id from friends where userid=(select userId from user where userName=#{arg0}) and " +
            "friendid=(select userId from user where userName=#{arg1}))")
    boolean existsFriendship(String arg0,String arg1);

    @Insert("insert into friend_request values((select userId from user where userName=#{arg0}),(select userId from user where userName=#{arg1}), #{arg2})")
    int insertWithNames(String arg0, String arg1,String arg2);

    @Delete("delete from friend_request where request_user=(select userId from user where userName=#{arg0}) and accept_user=(select userId from user where userName=#{arg1})")
    int deleteRequestTarget(String arg0, String arg1);

    List<FriendRequest> getUserRequests(String username);

    @Select("select userName from user where userId=#{arg0}")
    String searchName(Long id);
}
