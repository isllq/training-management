package com.training.controller;

import com.training.common.ApiResponse;
import com.training.common.BizException;
import com.training.mapper.TrainAnnouncementMapper;
import com.training.mapper.TrainSubmissionMapper;
import com.training.model.entity.TrainAnnouncement;
import com.training.model.entity.TrainFileAsset;
import com.training.model.entity.TrainSubmission;
import com.training.model.entity.TrainTask;
import com.training.security.AuthContext;
import com.training.security.RoleGuard;
import com.training.mapper.TrainTaskMapper;
import com.training.service.FileAssetService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileAssetService fileAssetService;
    private final TrainSubmissionMapper submissionMapper;
    private final TrainAnnouncementMapper announcementMapper;
    private final TrainTaskMapper taskMapper;

    public FileController(FileAssetService fileAssetService, TrainSubmissionMapper submissionMapper,
                          TrainAnnouncementMapper announcementMapper, TrainTaskMapper taskMapper) {
        this.fileAssetService = fileAssetService;
        this.submissionMapper = submissionMapper;
        this.announcementMapper = announcementMapper;
        this.taskMapper = taskMapper;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ApiResponse<TrainFileAsset> upload(@RequestPart("file") MultipartFile file,
                                              @RequestParam("bizType") String bizType,
                                              @RequestParam(value = "bizId", required = false) Long bizId) {
        String normalizedType = normalizeBizType(bizType);
        checkBizPermission(normalizedType, bizId, true, null);
        return ApiResponse.success(fileAssetService.upload(file, normalizedType, bizId, AuthContext.getUserId()));
    }

    @GetMapping
    public ApiResponse<List<TrainFileAsset>> list(@RequestParam("bizType") String bizType,
                                                  @RequestParam(value = "bizId", required = false) Long bizId) {
        String normalizedType = normalizeBizType(bizType);
        checkBizPermission(normalizedType, bizId, false, null);
        return ApiResponse.success(fileAssetService.list(normalizedType, bizId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        TrainFileAsset asset = fileAssetService.getById(id);
        checkBizPermission(asset.getBizType(), asset.getBizId(), true, asset);
        fileAssetService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable("id") Long id) throws Exception {
        TrainFileAsset asset = fileAssetService.getById(id);
        checkBizPermission(asset.getBizType(), asset.getBizId(), false, asset);
        Path path = Paths.get(asset.getFilePath());
        if (!Files.exists(path)) {
            throw new BizException("文件不存在或已失效");
        }
        String fileName = URLEncoder.encode(asset.getOriginalName(), "UTF-8").replace("+", "%20");
        String contentType = StringUtils.hasText(asset.getContentType())
                ? asset.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        long contentLength = asset.getFileSize() == null ? Files.size(path) : asset.getFileSize();
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(contentLength)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .body(resource);
    }

    private void checkBizPermission(String bizType, Long bizId, boolean write, TrainFileAsset asset) {
        String type = normalizeBizType(bizType);
        Long uid = AuthContext.getUserId();

        if ("ANNOUNCEMENT".equals(type)) {
            if (bizId == null) {
                throw new BizException("公告附件必须关联公告");
            }
            TrainAnnouncement announcement = announcementMapper.selectById(bizId);
            if (announcement == null) {
                throw new BizException("公告不存在或已删除");
            }
            if (write) {
                if (RoleGuard.isAdmin()) {
                    return;
                }
                RoleGuard.requireTeacherOrAdmin();
                if (uid == null || !uid.equals(announcement.getAuthorId())) {
                    throw new BizException(4030, "仅发布者或管理员可维护公告附件");
                }
            } else if (RoleGuard.isStudent()) {
                if (announcement.getStatus() == null || announcement.getStatus() != 1) {
                    throw new BizException(4030, "无权限访问该附件");
                }
            }
            return;
        }

        if ("TASK".equals(type)) {
            if (bizId == null) {
                throw new BizException("任务附件必须关联任务记录");
            }
            TrainTask task = taskMapper.selectById(bizId);
            if (task == null) {
                throw new BizException("任务不存在或已删除");
            }
            if (write) {
                RoleGuard.requireTeacherOrAdmin();
            }
            return;
        }

        if ("SUBMISSION".equals(type)) {
            if (bizId == null) {
                throw new BizException("提交附件必须关联提交记录");
            }
            TrainSubmission submission = submissionMapper.selectById(bizId);
            if (submission == null) {
                throw new BizException("提交记录不存在或已删除");
            }
            if (RoleGuard.isStudent()) {
                if (uid == null || !uid.equals(submission.getStudentId())) {
                    throw new BizException(4030, "无权限操作");
                }
                if (write && asset != null && asset.getUploaderId() != null && !uid.equals(asset.getUploaderId())) {
                    throw new BizException(4030, "无权限操作");
                }
            }
            return;
        }

        if ("MATERIAL".equals(type)) {
            if (write) {
                RoleGuard.requireTeacherOrAdmin();
            }
            return;
        }

        if (RoleGuard.isStudent() && write) {
            throw new BizException(4030, "无权限操作");
        }
    }

    private String normalizeBizType(String bizType) {
        if (!StringUtils.hasText(bizType)) {
            throw new BizException("bizType不能为空");
        }
        return bizType.trim().toUpperCase();
    }
}
