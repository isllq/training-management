package com.training.service;

import com.training.model.entity.TrainQaReply;
import com.training.model.entity.TrainQaThread;

import java.util.List;

public interface QaService {
    List<TrainQaThread> listThreads(Long publishId);

    List<TrainQaThread> listThreadsByClass(Long publishId, String className);

    TrainQaThread createThread(TrainQaThread thread);

    void updateThread(TrainQaThread thread);

    void deleteThread(Long id);

    List<TrainQaReply> listReplies(Long threadId);

    TrainQaReply createReply(TrainQaReply reply);

    void deleteReply(Long id);
}
