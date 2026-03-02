package com.training.service;

import com.training.model.entity.TrainScoreFinal;

import java.util.List;

public interface ScoreService {
    List<TrainScoreFinal> list(Long publishId);

    TrainScoreFinal create(TrainScoreFinal scoreFinal);

    void update(TrainScoreFinal scoreFinal);

    void delete(Long id);
}
