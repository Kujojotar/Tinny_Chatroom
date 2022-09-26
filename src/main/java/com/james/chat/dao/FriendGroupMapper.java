package com.james.chat.dao;

import com.james.chat.entity.FriendGroup;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FriendGroupMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FriendGroup record);

    FriendGroup selectByPrimaryKey(Long id);

    List<FriendGroup> selectAll();

    int updateByPrimaryKey(FriendGroup record);

    List<FriendGroup> searchGroupsWithKeyword(String key,int limit);

    List<FriendGroup> searchGroupsWithKeywordExplicit(String key,int limit,String username);

    List<FriendGroup> searchGroupsWithName(String key,int limit);

    @Select("select MAX(id) from friends")
    Long tmpMaxId();

    @Insert("insert into friends(id,userid,friendid) values(#{arg0},(select userId from user where userName=#{arg1}),(select userId from user where userName=#{arg2}))")
    boolean insertIntoFriendList(Long id, String fromId, String toId);
}
