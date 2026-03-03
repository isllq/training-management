package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.TrainScoreFinalMapper;
import com.training.model.dto.ScoreFormulaConfig;
import com.training.model.entity.TrainScoreFinal;
import com.training.service.ScoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {
    private static final BigDecimal SCORE_MIN = BigDecimal.ZERO;
    private static final BigDecimal SCORE_MAX = new BigDecimal("100");
    private static final BigDecimal WEIGHT_SUM_TARGET = BigDecimal.ONE;
    private static final BigDecimal WEIGHT_SUM_TOLERANCE = new BigDecimal("0.0001");
    private static final int SCORE_SCALE = 2;

    private final TrainScoreFinalMapper scoreFinalMapper;
    @Value("${training.score.alpha:0.4}")
    private BigDecimal alpha;
    @Value("${training.score.beta:0.3}")
    private BigDecimal beta;
    @Value("${training.score.gamma:0.3}")
    private BigDecimal gamma;

    public ScoreServiceImpl(TrainScoreFinalMapper scoreFinalMapper) {
        this.scoreFinalMapper = scoreFinalMapper;
    }

    @PostConstruct
    public void init() {
        validateWeights();
    }

    @Override
    public List<TrainScoreFinal> list(Long publishId) {
        validateWeights();
        List<TrainScoreFinal> list = scoreFinalMapper.list(publishId);
        list.forEach(item -> item.setFinalScore(calculateTotalScore(item.getUsualScore(), item.getTaskScore(), item.getReportScore())));
        return list;
    }

    @Override
    public ScoreFormulaConfig formula() {
        validateWeights();
        ScoreFormulaConfig config = new ScoreFormulaConfig();
        config.setAlpha(alpha);
        config.setBeta(beta);
        config.setGamma(gamma);
        config.setExpression("S_total = alpha*S_process + beta*C_team + gamma*S_final, 且 alpha+beta+gamma=1");
        return config;
    }

    @Override
    public TrainScoreFinal create(TrainScoreFinal scoreFinal) {
        validatePayload(scoreFinal);
        scoreFinal.setFinalScore(calculateTotalScore(scoreFinal.getUsualScore(), scoreFinal.getTaskScore(), scoreFinal.getReportScore()));
        scoreFinalMapper.insert(scoreFinal);
        return scoreFinal;
    }

    @Override
    public void update(TrainScoreFinal scoreFinal) {
        validatePayload(scoreFinal);
        scoreFinal.setFinalScore(calculateTotalScore(scoreFinal.getUsualScore(), scoreFinal.getTaskScore(), scoreFinal.getReportScore()));
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

    private void validatePayload(TrainScoreFinal scoreFinal) {
        validateWeights();
        if (scoreFinal == null) {
            throw new BizException("成绩参数不能为空");
        }
        if (scoreFinal.getPublishId() == null) {
            throw new BizException("开设计划不能为空");
        }
        if (scoreFinal.getStudentId() == null) {
            throw new BizException("学生不能为空");
        }
        scoreFinal.setUsualScore(normalizeComponent(scoreFinal.getUsualScore(), "过程得分"));
        scoreFinal.setTaskScore(normalizeComponent(scoreFinal.getTaskScore(), "团队协作得分"));
        scoreFinal.setReportScore(normalizeComponent(scoreFinal.getReportScore(), "答辩得分"));
    }

    private void validateWeights() {
        if (alpha == null || beta == null || gamma == null) {
            throw new BizException("成绩权重配置缺失，请检查training.score配置");
        }
        if (!isWeightInRange(alpha) || !isWeightInRange(beta) || !isWeightInRange(gamma)) {
            throw new BizException("成绩权重配置错误：alpha、beta、gamma必须在0到1之间");
        }
        BigDecimal sum = alpha.add(beta).add(gamma);
        BigDecimal delta = sum.subtract(WEIGHT_SUM_TARGET).abs();
        if (delta.compareTo(WEIGHT_SUM_TOLERANCE) > 0) {
            throw new BizException("成绩权重配置错误：alpha+beta+gamma必须等于1");
        }
    }

    private boolean isWeightInRange(BigDecimal weight) {
        return weight.compareTo(BigDecimal.ZERO) >= 0 && weight.compareTo(BigDecimal.ONE) <= 0;
    }

    private BigDecimal normalizeComponent(BigDecimal value, String name) {
        if (value == null) {
            throw new BizException(name + "不能为空");
        }
        if (value.compareTo(SCORE_MIN) < 0 || value.compareTo(SCORE_MAX) > 0) {
            throw new BizException(name + "必须在0到100之间");
        }
        return value.setScale(SCORE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalScore(BigDecimal processScore, BigDecimal teamScore, BigDecimal finalScore) {
        BigDecimal p = processScore == null ? SCORE_MIN : processScore;
        BigDecimal t = teamScore == null ? SCORE_MIN : teamScore;
        BigDecimal f = finalScore == null ? SCORE_MIN : finalScore;
        return p.multiply(alpha)
                .add(t.multiply(beta))
                .add(f.multiply(gamma))
                .setScale(SCORE_SCALE, RoundingMode.HALF_UP);
    }
}
