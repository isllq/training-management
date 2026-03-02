package com.training.service;

import com.training.model.entity.TrainTask;

import java.util.List;

public interface TaskService {
    List<TrainTask> list(Long publishId);

    List<TrainTask> listByClassName(Long publishId, String className);

    TrainTask create(TrainTask task);

    void update(TrainTask task);

    void delete(Long id);
}
