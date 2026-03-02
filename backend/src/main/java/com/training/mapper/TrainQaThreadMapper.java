package com.training.mapper;

import com.training.model.entity.TrainQaThread;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainQaThreadMapper {
    @Select("<script>" +
            "SELECT q.*, u.real_name AS creator_name, " +
            "(SELECT COUNT(1) FROM train_qa_reply r WHERE r.thread_id = q.id AND r.is_deleted = 0) AS reply_count " +
            "FROM train_qa_thread q " +
            "LEFT JOIN sys_user u ON q.creator_id = u.id " +
            "WHERE q.is_deleted=0 " +
            "<if test='publishId != null'> AND q.publish_id=#{publishId} </if>" +
            "ORDER BY CASE WHEN reply_count = 0 THEN 0 ELSE 1 END, q.id DESC" +
            "</script>")
    List<TrainQaThread> list(@Param("publishId") Long publishId);

    @Select("<script>" +
            "SELECT q.*, u.real_name AS creator_name, " +
            "(SELECT COUNT(1) FROM train_qa_reply r WHERE r.thread_id = q.id AND r.is_deleted = 0) AS reply_count " +
            "FROM train_qa_thread q " +
            "JOIN train_project_publish p ON q.publish_id = p.id AND p.is_deleted = 0 " +
            "LEFT JOIN sys_user u ON q.creator_id = u.id " +
            "WHERE q.is_deleted=0 " +
            "AND (" +
            "p.class_name = #{className} " +
            "OR FIND_IN_SET(#{className}, " +
            "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.class_name, '，', ','), '；', ','), ';', ','), '/', ','), ' ', ''), '、', ',')" +
            ") > 0 " +
            "OR p.class_name LIKE CONCAT('%', #{className}, '%')" +
            ") " +
            "<if test='publishId != null'> AND q.publish_id=#{publishId} </if>" +
            "ORDER BY CASE WHEN reply_count = 0 THEN 0 ELSE 1 END, q.id DESC" +
            "</script>")
    List<TrainQaThread> listByClassName(@Param("publishId") Long publishId, @Param("className") String className);

    @Select("SELECT q.*, u.real_name AS creator_name, " +
            "(SELECT COUNT(1) FROM train_qa_reply r WHERE r.thread_id = q.id AND r.is_deleted = 0) AS reply_count " +
            "FROM train_qa_thread q " +
            "LEFT JOIN sys_user u ON q.creator_id = u.id " +
            "WHERE q.id=#{id} AND q.is_deleted=0")
    TrainQaThread selectById(@Param("id") Long id);

    @Insert("INSERT INTO train_qa_thread(publish_id, title, content, creator_id, status, created_at, updated_at, is_deleted) " +
            "VALUES(#{publishId}, #{title}, #{content}, #{creatorId}, #{status}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainQaThread thread);

    @Update("UPDATE train_qa_thread SET title=#{title}, content=#{content}, status=#{status}, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int update(TrainQaThread thread);

    @Update("UPDATE train_qa_thread SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM train_qa_thread WHERE is_deleted=0")
    Long countAll();
}
