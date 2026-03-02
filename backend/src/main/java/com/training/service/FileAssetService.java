package com.training.service;

import com.training.model.entity.TrainFileAsset;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileAssetService {
    TrainFileAsset upload(MultipartFile file, String bizType, Long bizId, Long uploaderId);

    List<TrainFileAsset> list(String bizType, Long bizId);

    TrainFileAsset getById(Long id);

    void delete(Long id);
}
