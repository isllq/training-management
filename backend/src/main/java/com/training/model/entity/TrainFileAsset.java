package com.training.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainFileAsset {
    private Long id;
    private String bizType;
    private Long bizId;
    private String originalName;
    private String storageName;
    private String contentType;
    private Long fileSize;
    private String filePath;
    private Long uploaderId;
    private LocalDateTime uploadedAt;
    private Integer isDeleted;

    private String downloadUrl;
}
