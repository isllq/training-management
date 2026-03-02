package com.training.mapper;

import com.training.model.entity.TrainQaReply;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainQaReplyMapper {
    @Select("SELECT r.*, u.real_name AS creator_name FROM train_qa_reply r " +
            "LEFT JOIN sys_user u ON r.creator_id = u.id " +
            "WHERE r.thread_id=#{threadId} AND r.is_deleted=0 ORDER BY r.id ASC")
    List<TrainQaReply> listByThreadId(@Param("threadId") Long threadId);

    @Select("SELECT r.*, u.real_name AS creator_name FROM train_qa_reply r " +
            "LEFT JOIN sys_user u ON r.creator_id = u.id " +
            "WHERE r.id=#{id} AND r.is_deleted=0")
    TrainQaReply selectById(@Param("id") Long id);

    @Insert("INSERT INTO train_qa_reply(thread_id, content, creator_id, created_at, is_deleted) " +
            "VALUES(#{threadId}, #{content}, #{creatorId}, NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainQaReply reply);

    @Update("UPDATE train_qa_reply SET is_deleted=1 WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);
}
