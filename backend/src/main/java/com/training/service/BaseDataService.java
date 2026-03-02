package com.training.service;

import com.training.model.entity.EduClass;
import com.training.model.entity.EduCollege;
import com.training.model.entity.EduMajor;

import java.util.List;

public interface BaseDataService {
    List<EduCollege> listColleges();

    EduCollege createCollege(EduCollege college);

    void updateCollege(EduCollege college);

    void deleteCollege(Long id);

    List<EduMajor> listMajors(Long collegeId);

    EduMajor createMajor(EduMajor major);

    void updateMajor(EduMajor major);

    void deleteMajor(Long id);

    List<EduClass> listClasses(Long majorId);

    EduClass createClass(EduClass eduClass);

    void updateClass(EduClass eduClass);

    void deleteClass(Long id);
}
