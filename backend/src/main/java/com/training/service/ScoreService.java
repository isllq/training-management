package com.training.service;

import com.training.model.dto.ScoreImportResult;
import com.training.model.dto.ScoreFormulaConfig;
import com.training.model.entity.TrainScoreFinal;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ScoreService {
    List<TrainScoreFinal> list(Long publishId);
    ScoreFormulaConfig formula();

    TrainScoreFinal create(TrainScoreFinal scoreFinal);

    void update(TrainScoreFinal scoreFinal);

    void delete(Long id);

    ScoreImportResult importFromFile(MultipartFile file, Long defaultPublishId);
}
