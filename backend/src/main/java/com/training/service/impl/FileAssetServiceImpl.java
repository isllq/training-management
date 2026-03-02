package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.TrainFileAssetMapper;
import com.training.model.entity.TrainFileAsset;
import com.training.service.FileAssetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileAssetServiceImpl implements FileAssetService {
    private final TrainFileAssetMapper fileAssetMapper;

    @Value("${training.file.upload-dir:uploads}")
    private String uploadDir;

    public FileAssetServiceImpl(TrainFileAssetMapper fileAssetMapper) {
        this.fileAssetMapper = fileAssetMapper;
    }

    @Override
    public TrainFileAsset upload(MultipartFile file, String bizType, Long bizId, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = getFileExtension(originalName);
        String storageName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path targetDir = Paths.get(uploadDir, bizType.toLowerCase()).toAbsolutePath();
        Path targetPath = targetDir.resolve(storageName);

        try {
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BizException("文件保存失败: " + ex.getMessage());
        }

        TrainFileAsset asset = new TrainFileAsset();
        asset.setBizType(bizType);
        asset.setBizId(bizId);
        asset.setOriginalName(StringUtils.hasText(originalName) ? originalName : storageName);
        asset.setStorageName(storageName);
        asset.setContentType(file.getContentType());
        asset.setFileSize(file.getSize());
        asset.setFilePath(targetPath.toString());
        asset.setUploaderId(uploaderId);
        fileAssetMapper.insert(asset);
        asset.setDownloadUrl("/api/files/" + asset.getId() + "/download");
        return asset;
    }

    @Override
    public List<TrainFileAsset> list(String bizType, Long bizId) {
        List<TrainFileAsset> list = fileAssetMapper.list(bizType, bizId);
        for (TrainFileAsset asset : list) {
            asset.setDownloadUrl("/api/files/" + asset.getId() + "/download");
        }
        return list;
    }

    @Override
    public TrainFileAsset getById(Long id) {
        TrainFileAsset asset = fileAssetMapper.selectById(id);
        if (asset == null) {
            throw new BizException("文件不存在或已删除");
        }
        asset.setDownloadUrl("/api/files/" + asset.getId() + "/download");
        return asset;
    }

    @Override
    public void delete(Long id) {
        TrainFileAsset asset = fileAssetMapper.selectById(id);
        if (asset == null) {
            throw new BizException("文件不存在或已删除");
        }
        int affected = fileAssetMapper.softDelete(id);
        if (affected <= 0) {
            throw new BizException("文件不存在或已删除");
        }
        try {
            Files.deleteIfExists(Paths.get(asset.getFilePath()));
        } catch (Exception ignored) {
        }
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(idx);
    }
}
