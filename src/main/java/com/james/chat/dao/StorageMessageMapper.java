package com.james.chat.dao;

import com.james.chat.entity.StorageMessage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface StorageMessageMapper {
    int insert(StorageMessage record);

    List<StorageMessage> selectAll();

    @Select("select MAX(msgId) from storage_message where fromUserName=#{fromId} and toUserName=#{toId};")
    Long getTmpMessageId(@Param("fromId") String fromUserId, @Param("toId") String toUserId);

    List<StorageMessage> selectAllMessagesByToIdBefore(String toUserId,Date time);

    List<StorageMessage> selectAllMessagesBefore(String fromUserId, String toUserId,Date time);

    int deleteAllRecordsBefore(String fromUserId, String toUserId, Date time);

    int deleteAllRecordsByToIdBefore(String toUserId, Date time);
}
