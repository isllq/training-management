package com.training.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScoreImportResult {
    private int totalRows;
    private int successRows;
    private int failedRows;
    private List<String> errors = new ArrayList<>();
}
