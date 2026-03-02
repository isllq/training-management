package com.training.mapper;

import com.training.model.entity.TrainFileAsset;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TrainFileAssetMapper {
    @Select("<script>" +
            "SELECT * FROM train_file_asset WHERE is_deleted = 0 " +
            "<if test='bizType != null and bizType != \"\"'> AND biz_type = #{bizType} </if>" +
            "<if test='bizId != null'> AND biz_id = #{bizId} </if>" +
            "ORDER BY id DESC" +
            "</script>")
    List<TrainFileAsset> list(@Param("bizType") String bizType, @Param("bizId") Long bizId);

    @Select("SELECT * FROM train_file_asset WHERE id = #{id} AND is_deleted = 0")
    TrainFileAsset selectById(@Param("id") Long id);

    @Insert("INSERT INTO train_file_asset(biz_type, biz_id, original_name, storage_name, content_type, file_size, file_path, uploader_id, uploaded_at, is_deleted) " +
            "VALUES(#{bizType}, #{bizId}, #{originalName}, #{storageName}, #{contentType}, #{fileSize}, #{filePath}, #{uploaderId}, NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TrainFileAsset asset);

    @Update("UPDATE train_file_asset SET is_deleted=1 WHERE id=#{id} AND is_deleted=0")
    int softDelete(@Param("id") Long id);
}
