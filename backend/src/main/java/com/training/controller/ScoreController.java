package com.training.controller;

import com.training.common.ApiResponse;
import com.training.common.BizException;
import com.training.mapper.TrainScoreFinalMapper;
import com.training.model.dto.ScoreFormulaConfig;
import com.training.model.dto.ScoreImportResult;
import com.training.model.entity.TrainScoreFinal;
import com.training.security.AuthContext;
import com.training.security.RoleGuard;
import com.training.security.UserScopeService;
import com.training.service.ScoreService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {
    private final ScoreService scoreService;
    private final UserScopeService userScopeService;
    private final TrainScoreFinalMapper scoreFinalMapper;

    public ScoreController(ScoreService scoreService,
                           UserScopeService userScopeService,
                           TrainScoreFinalMapper scoreFinalMapper) {
        this.scoreService = scoreService;
        this.userScopeService = userScopeService;
        this.scoreFinalMapper = scoreFinalMapper;
    }

    @GetMapping
    public ApiResponse<List<TrainScoreFinal>> list(@RequestParam(value = "publishId", required = false) Long publishId) {
        if (RoleGuard.isTeacher() && publishId != null) {
            userScopeService.requireManagePublish(publishId);
        }
        List<TrainScoreFinal> list = scoreService.list(publishId);
        if (RoleGuard.isStudent()) {
            Long uid = AuthContext.getUserId();
            list = list.stream().filter(item -> uid != null && uid.equals(item.getStudentId())).collect(Collectors.toList());
        } else if (RoleGuard.isTeacher() && publishId == null) {
            list = list.stream()
                    .filter(item -> userScopeService.canManagePublish(item.getPublishId()))
                    .collect(Collectors.toList());
        }
        return ApiResponse.success(list);
    }

    @GetMapping("/formula")
    public ApiResponse<ScoreFormulaConfig> formula() {
        return ApiResponse.success(scoreService.formula());
    }

    @PostMapping
    public ApiResponse<TrainScoreFinal> create(@RequestBody TrainScoreFinal scoreFinal) {
        RoleGuard.requireTeacherOrAdmin();
        userScopeService.requireManagePublish(scoreFinal.getPublishId());
        return ApiResponse.success(scoreService.create(scoreFinal));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable("id") Long id, @RequestBody TrainScoreFinal scoreFinal) {
        RoleGuard.requireTeacherOrAdmin();
        TrainScoreFinal existing = scoreFinalMapper.selectById(id);
        if (existing == null) {
            throw new BizException("成绩记录不存在或已删除");
        }
        userScopeService.requireManagePublish(existing.getPublishId());
        if (scoreFinal.getPublishId() != null && !scoreFinal.getPublishId().equals(existing.getPublishId())) {
            userScopeService.requireManagePublish(scoreFinal.getPublishId());
        }
        scoreFinal.setId(id);
        scoreService.update(scoreFinal);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        RoleGuard.requireTeacherOrAdmin();
        TrainScoreFinal existing = scoreFinalMapper.selectById(id);
        if (existing == null) {
            throw new BizException("成绩记录不存在或已删除");
        }
        userScopeService.requireManagePublish(existing.getPublishId());
        scoreService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/export")
    public void export(@RequestParam(value = "publishId", required = false) Long publishId, HttpServletResponse response) throws Exception {
        RoleGuard.requireTeacherOrAdmin();
        if (RoleGuard.isTeacher()) {
            if (publishId == null) {
                throw new BizException("教师导出成绩请先选择实训科目");
            }
            userScopeService.requireManagePublish(publishId);
        }
        List<TrainScoreFinal> list = scoreService.list(publishId);
        String fileName = URLEncoder.encode("score_export.csv", "UTF-8");
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
        writer.println("ID,学期,班级,项目名称,成绩类型,学生ID,学生姓名,过程得分,团队协作得分,答辩得分,总评");
        for (TrainScoreFinal score : list) {
            writer.println(score.getId() + "," + safeCsv(score.getTermName()) + "," + safeCsv(score.getClassName()) + ","
                    + safeCsv(score.getProjectName()) + ",多元综合评价（过程+协作+答辩）," + score.getStudentId() + ","
                    + safeCsv(score.getStudentName()) + "," + score.getUsualScore() + "," + score.getTaskScore() + ","
                    + score.getReportScore() + "," + score.getFinalScore());
        }
        writer.flush();
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ApiResponse<ScoreImportResult> importScores(@RequestPart("file") MultipartFile file,
                                                       @RequestParam(value = "publishId", required = false) Long publishId) {
        RoleGuard.requireTeacherOrAdmin();
        if (RoleGuard.isTeacher()) {
            if (publishId == null) {
                throw new BizException("教师导入成绩请先选择实训科目");
            }
            userScopeService.requireManagePublish(publishId);
        }
        return ApiResponse.success(scoreService.importFromFile(file, publishId));
    }

    private String safeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
