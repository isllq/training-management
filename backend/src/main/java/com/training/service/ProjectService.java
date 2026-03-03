package com.training.service;

import com.training.model.entity.TrainProject;
import com.training.model.entity.TrainProjectPublish;

import java.util.List;

public interface ProjectService {
    List<TrainProject> listProjects(String keyword);
    List<TrainProject> listProjectsByTeacher(Long teacherId, String keyword);

    TrainProject createProject(TrainProject project);

    void updateProject(TrainProject project);

    void deleteProject(Long id);

    List<TrainProjectPublish> listPublishes(String className, Long teacherId);

    TrainProjectPublish createPublish(TrainProjectPublish publish);

    void updatePublish(TrainProjectPublish publish);

    void deletePublish(Long id);
}
