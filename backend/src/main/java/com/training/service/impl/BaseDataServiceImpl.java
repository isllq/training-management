package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.EduClassMapper;
import com.training.mapper.EduCollegeMapper;
import com.training.mapper.EduMajorMapper;
import com.training.model.entity.EduClass;
import com.training.model.entity.EduCollege;
import com.training.model.entity.EduMajor;
import com.training.service.BaseDataService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseDataServiceImpl implements BaseDataService {
    private final EduCollegeMapper collegeMapper;
    private final EduMajorMapper majorMapper;
    private final EduClassMapper classMapper;

    public BaseDataServiceImpl(EduCollegeMapper collegeMapper, EduMajorMapper majorMapper, EduClassMapper classMapper) {
        this.collegeMapper = collegeMapper;
        this.majorMapper = majorMapper;
        this.classMapper = classMapper;
    }

    @Override
    public List<EduCollege> listColleges() {
        return collegeMapper.list();
    }

    @Override
    public EduCollege createCollege(EduCollege college) {
        if (college.getStatus() == null) {
            college.setStatus(1);
        }
        collegeMapper.insert(college);
        return college;
    }

    @Override
    public void updateCollege(EduCollege college) {
        if (collegeMapper.update(college) <= 0) {
            throw new BizException("学院不存在或已删除");
        }
    }

    @Override
    public void deleteCollege(Long id) {
        if (collegeMapper.softDelete(id) <= 0) {
            throw new BizException("学院不存在或已删除");
        }
    }

    @Override
    public List<EduMajor> listMajors(Long collegeId) {
        return majorMapper.list(collegeId);
    }

    @Override
    public EduMajor createMajor(EduMajor major) {
        if (major.getStatus() == null) {
            major.setStatus(1);
        }
        majorMapper.insert(major);
        return major;
    }

    @Override
    public void updateMajor(EduMajor major) {
        if (majorMapper.update(major) <= 0) {
            throw new BizException("专业不存在或已删除");
        }
    }

    @Override
    public void deleteMajor(Long id) {
        if (majorMapper.softDelete(id) <= 0) {
            throw new BizException("专业不存在或已删除");
        }
    }

    @Override
    public List<EduClass> listClasses(Long majorId) {
        return classMapper.list(majorId);
    }

    @Override
    public EduClass createClass(EduClass eduClass) {
        if (eduClass.getStatus() == null) {
            eduClass.setStatus(1);
        }
        classMapper.insert(eduClass);
        return eduClass;
    }

    @Override
    public void updateClass(EduClass eduClass) {
        if (classMapper.update(eduClass) <= 0) {
            throw new BizException("班级不存在或已删除");
        }
    }

    @Override
    public void deleteClass(Long id) {
        if (classMapper.softDelete(id) <= 0) {
            throw new BizException("班级不存在或已删除");
        }
    }
}
