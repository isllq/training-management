package com.training.mapper;

import com.training.model.dto.AnnouncementView;
import com.training.model.entity.TrainAnnouncement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainAnnouncementMapper {
    @Select("<script>" +
            "SELECT a.*, u.real_name AS author_name, " +
            "CASE WHEN EXISTS (" +
            "SELECT 1 FROM train_announcement_read ar " +
            "WHERE ar.announcement_id = a.id AND ar.user_id = #{userId} AND ar.is_deleted = 0" +
            ") THEN 1 ELSE 0 END AS read_flag, " +
            "(SELECT COUNT(1) FROM train_announcement_read ar " +
            "JOIN sys_user su ON ar.user_id = su.id " +
            "WHERE ar.announcement_id = a.id AND ar.is_deleted = 0 " +
            "AND su.is_deleted = 0 AND su.status = 1 AND su.user_type = 'STUDENT' " +
            "AND (a.publish_id IS NULL OR su.class_name = p.class_name " +
            "OR FIND_IN_SET(su.class_name, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.class_name, '，', ','), '；', ','), ';', ','), '/', ','), ' ', ''), '、', ',')) > 0 " +
            "OR su.class_name LIKE CONCAT('%', p.class_name, '%') " +
            "OR p.class_name LIKE CONCAT('%', su.class_name, '%'))) AS read_count, " +
            "(SELECT COUNT(1) FROM sys_user su " +
            "WHERE su.is_deleted = 0 AND su.status = 1 AND su.user_type = 'STUDENT' " +
            "AND (a.publish_id IS NULL OR su.class_name = p.class_name " +
            "OR FIND_IN_SET(su.class_name, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.class_name, '，', ','), '；', ','), ';', ','), '/', ','), ' ', ''), '、', ',')) > 0 " +
            "OR su.class_name LIKE CONCAT('%', p.class_name, '%') " +
            "OR p.class_name LIKE CONCAT('%', su.class_name, '%'))) AS target_count, " +
            "GREATEST((" +
            "SELECT COUNT(1) FROM sys_user su " +
            "WHERE su.is_deleted = 0 AND su.status = 1 AND su.user_type = 'STUDENT' " +
            "AND (a.publish_id IS NULL OR su.class_name = p.class_name " +
            "OR FIND_IN_SET(su.class_name, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.class_name, '，', ','), '；', ','), ';', ','), '/', ','), ' ', ''), '、', ',')) > 0 " +
            "OR su.class_name LIKE CONCAT('%', p.class_name, '%') " +
            "OR p.class_name LIKE CONCAT('%', su.class_name, '%'))) - (" +
            "SELECT COUNT(1) FROM train_announcement_read ar " +
            "JOIN sys_user su ON ar.user_id = su.id " +
            "WHERE ar.announcement_id = a.id AND ar.is_deleted = 0 " +
            "AND su.is_deleted = 0 AND su.status = 1 AND su.user_type = 'STUDENT' " +
            "AND (a.publish_id IS NULL OR su.class_name = p.class_name " +
            "OR FIND_IN_SET(su.class_name, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.class_name, '，', ','), '；', ','), ';', ','), '/', ','), ' ', ''), '、', ',')) > 0 " +
            "OR su.class_name LIKE CONCAT('%', p.class_name, '%') " +
            "OR p.class_name LIKE CONCAT('%', su.class_name, '%'))), 0) AS unread_count " +
            "FROM train_announcement a " +
            "LEFT JOIN train_project_publish p ON a.publish_id = p.id AND p.is_deleted = 0 " +
            "LEFT JOIN sys_user u ON a.author_id = u.id " +
            "WHERE a.is_deleted = 0 " +
            "<if test='publishId != null'> AND (a.publish_id = #{publishId} OR a.publish_id IS NULL) </if>" +
            "<if test='className != null and className != \"\"'> " +
            "AND (a.publish_id IS NULL OR p.class_name = #{className} " +
            "OR FIND_IN_SET(#{className}, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.class_name, '，', ','), '；', ','), ';', ','), '/', ','), ' ', ''), '、', ',')) > 0 " +
            "OR #{className} LIKE CONCAT('%', p.class_name, '%') " +
            "OR p.class_name LIKE CONCAT('%', #{className}, '%')) " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (a.title LIKE CONCAT('%', #{keyword}, '%') OR a.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='onlyPublished != null and onlyPublished'> " +
            "AND a.status = 1 " +
            "AND (a.publish_time IS NULL OR a.publish_time &lt;= NOW()) " +
            "AND (a.expire_time IS NULL OR a.expire_time &gt;= NOW()) " +
            "</if>" +
            "ORDER BY a.priority DESC, a.publish_time DESC, a.id DESC" +
            "</script>")
    List<AnnouncementView> list(@Param("publishId") Long publishId,
                                @Param("keyword") String keyword,
                                @Param("userId") Long userId,
                                @Param("className") String className,
                                @Param("onlyPublished") Boolean onlyPublished);

    @Select("SELECT * FROM train_announcement WHERE id = #{id} AND is_deleted = 0")
    TrainAnnouncement selectById(@Param("id") Long id);

    @Insert("INSERT INTO train_announcement(publish_id, title, content, author_id, priority, status, publish_time, expire_time, created_at, updated_at, is_deleted) " +
            "VALUES(#{publishId}, #{title}, #{content}, #{authorId}, #{priority}, #{status}, #{publishTime}, #{expireTime}, NOW(), NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainAnnouncement announcement);

    @Update("UPDATE train_announcement SET publish_id=#{publishId}, title=#{title}, content=#{content}, priority=#{priority}, " +
            "status=#{status}, publish_time=#{publishTime}, expire_time=#{expireTime}, updated_at=NOW() " +
            "WHERE id=#{id} AND is_deleted=0")
    int update(TrainAnnouncement announcement);

    @Update("UPDATE train_announcement SET is_deleted=1, updated_at=NOW() WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);

    @Select("SELECT COUNT(1) FROM train_announcement a " +
            "WHERE a.is_deleted = 0 AND a.status = 1 " +
            "AND (a.publish_time IS NULL OR a.publish_time <= NOW()) " +
            "AND (a.expire_time IS NULL OR a.expire_time >= NOW()) " +
            "AND NOT EXISTS (" +
            "SELECT 1 FROM train_announcement_read ar " +
            "WHERE ar.announcement_id = a.id AND ar.user_id = #{userId} AND ar.is_deleted = 0)")
    Long countUnread(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM train_announcement a " +
            "LEFT JOIN train_project_publish p ON a.publish_id = p.id AND p.is_deleted = 0 " +
            "WHERE a.is_deleted = 0 AND a.status = 1 " +
            "AND (a.publish_time IS NULL OR a.publish_time <= NOW()) " +
            "AND (a.expire_time IS NULL OR a.expire_time >= NOW()) " +
            "AND (a.publish_id IS NULL OR p.class_name = #{className} " +
            "OR FIND_IN_SET(#{className}, REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(p.class_name, '，', ','), '；', ','), ';', ','), '/', ','), ' ', ''), '、', ',')) > 0 " +
            "OR #{className} LIKE CONCAT('%', p.class_name, '%') " +
            "OR p.class_name LIKE CONCAT('%', #{className}, '%')) " +
            "AND NOT EXISTS (" +
            "SELECT 1 FROM train_announcement_read ar " +
            "WHERE ar.announcement_id = a.id AND ar.user_id = #{userId} AND ar.is_deleted = 0)")
    Long countUnreadByClass(@Param("userId") Long userId, @Param("className") String className);
}
