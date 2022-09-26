package com.james.chat.dao;

import com.james.chat.vo.ListFriendVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ListFriendVoMapper {

    public List<ListFriendVo> getFriendVoByUsername(String username);

    @Select("select avatarUrl from user where userName=#{username}")
    public String getAvatarUrlByUsername(String username);
}
