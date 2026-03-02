package com.training.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreFormulaConfig {
    private BigDecimal alpha;
    private BigDecimal beta;
    private BigDecimal gamma;
    private String expression;
}

