package com.james.chat.dao;

import com.james.chat.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppUserMapper {
    int deleteByPrimaryKey(Long userid);

    int insert(AppUser record);

    AppUser selectByPrimaryKey(Long userid);

    AppUser selectByUsername(String username);

    List<AppUser> selectAll();

    int updateByPrimaryKey(AppUser record);

    List<AppUser> searchUsersWithKeyWord(String key, int limit);

    List<AppUser> searchUsersWithKeyWordExplicit(String key, int limit, String username);
}
