package com.training.service;

import com.training.model.entity.TrainSubmission;

import java.util.List;

public interface SubmissionService {
    List<TrainSubmission> list(Long taskId, Long teamId);

    TrainSubmission create(TrainSubmission submission);

    void update(TrainSubmission submission);

    void delete(Long id);
}
