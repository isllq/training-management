package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.TrainScoreFinalMapper;
import com.training.model.entity.TrainScoreFinal;
import com.training.service.ScoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {
    private final TrainScoreFinalMapper scoreFinalMapper;

    public ScoreServiceImpl(TrainScoreFinalMapper scoreFinalMapper) {
        this.scoreFinalMapper = scoreFinalMapper;
    }

    @Override
    public List<TrainScoreFinal> list(Long publishId) {
        return scoreFinalMapper.list(publishId);
    }

    @Override
    public TrainScoreFinal create(TrainScoreFinal scoreFinal) {
        scoreFinalMapper.insert(scoreFinal);
        return scoreFinal;
    }

    @Override
    public void update(TrainScoreFinal scoreFinal) {
        int affected = scoreFinalMapper.update(scoreFinal);
        if (affected <= 0) {
            throw new BizException("成绩记录不存在或已删除");
        }
    }

    @Override
    public void delete(Long id) {
        int affected = scoreFinalMapper.softDelete(id);
        if (affected <= 0) {
            throw new BizException("成绩记录不存在或已删除");
        }
    }
}
