package com.training.controller;

import com.training.common.ApiResponse;
import com.training.model.entity.EduClass;
import com.training.model.entity.EduCollege;
import com.training.model.entity.EduMajor;
import com.training.security.RoleGuard;
import com.training.service.BaseDataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base")
public class BaseDataController {
    private final BaseDataService baseDataService;

    public BaseDataController(BaseDataService baseDataService) {
        this.baseDataService = baseDataService;
    }

    @GetMapping("/colleges")
    public ApiResponse<List<EduCollege>> listColleges() {
        return ApiResponse.success(baseDataService.listColleges());
    }

    @PostMapping("/colleges")
    public ApiResponse<EduCollege> createCollege(@RequestBody EduCollege college) {
        RoleGuard.requireAdmin();
        return ApiResponse.success(baseDataService.createCollege(college));
    }

    @PutMapping("/colleges/{id}")
    public ApiResponse<Void> updateCollege(@PathVariable("id") Long id, @RequestBody EduCollege college) {
        RoleGuard.requireAdmin();
        college.setId(id);
        baseDataService.updateCollege(college);
        return ApiResponse.success();
    }

    @DeleteMapping("/colleges/{id}")
    public ApiResponse<Void> deleteCollege(@PathVariable("id") Long id) {
        RoleGuard.requireAdmin();
        baseDataService.deleteCollege(id);
        return ApiResponse.success();
    }

    @GetMapping("/majors")
    public ApiResponse<List<EduMajor>> listMajors(@RequestParam(value = "collegeId", required = false) Long collegeId) {
        return ApiResponse.success(baseDataService.listMajors(collegeId));
    }

    @PostMapping("/majors")
    public ApiResponse<EduMajor> createMajor(@RequestBody EduMajor major) {
        RoleGuard.requireAdmin();
        return ApiResponse.success(baseDataService.createMajor(major));
    }

    @PutMapping("/majors/{id}")
    public ApiResponse<Void> updateMajor(@PathVariable("id") Long id, @RequestBody EduMajor major) {
        RoleGuard.requireAdmin();
        major.setId(id);
        baseDataService.updateMajor(major);
        return ApiResponse.success();
    }

    @DeleteMapping("/majors/{id}")
    public ApiResponse<Void> deleteMajor(@PathVariable("id") Long id) {
        RoleGuard.requireAdmin();
        baseDataService.deleteMajor(id);
        return ApiResponse.success();
    }

    @GetMapping("/classes")
    public ApiResponse<List<EduClass>> listClasses(@RequestParam(value = "majorId", required = false) Long majorId) {
        return ApiResponse.success(baseDataService.listClasses(majorId));
    }

    @PostMapping("/classes")
    public ApiResponse<EduClass> createClass(@RequestBody EduClass eduClass) {
        RoleGuard.requireAdmin();
        return ApiResponse.success(baseDataService.createClass(eduClass));
    }

    @PutMapping("/classes/{id}")
    public ApiResponse<Void> updateClass(@PathVariable("id") Long id, @RequestBody EduClass eduClass) {
        RoleGuard.requireAdmin();
        eduClass.setId(id);
        baseDataService.updateClass(eduClass);
        return ApiResponse.success();
    }

    @DeleteMapping("/classes/{id}")
    public ApiResponse<Void> deleteClass(@PathVariable("id") Long id) {
        RoleGuard.requireAdmin();
        baseDataService.deleteClass(id);
        return ApiResponse.success();
    }
}
