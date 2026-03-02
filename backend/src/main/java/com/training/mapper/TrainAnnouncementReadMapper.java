package com.training.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrainAnnouncementReadMapper {
    @Insert("INSERT INTO train_announcement_read(announcement_id, user_id, read_time, is_deleted) " +
            "VALUES(#{announcementId}, #{userId}, NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE read_time=NOW(), is_deleted=0")
    int markRead(@Param("announcementId") Long announcementId, @Param("userId") Long userId);
}
