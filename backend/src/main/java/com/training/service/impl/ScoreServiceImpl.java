package com.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.training.common.BizException;
import com.training.mapper.SysUserMapper;
import com.training.mapper.TrainProjectPublishMapper;
import com.training.mapper.TrainScoreFinalMapper;
import com.training.model.dto.ScoreImportResult;
import com.training.model.dto.ScoreFormulaConfig;
import com.training.model.entity.SysUser;
import com.training.model.entity.TrainProjectPublish;
import com.training.model.entity.TrainScoreFinal;
import com.training.service.ScoreService;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ScoreServiceImpl implements ScoreService {
    private static final BigDecimal SCORE_MIN = BigDecimal.ZERO;
    private static final BigDecimal SCORE_MAX = new BigDecimal("100");
    private static final BigDecimal WEIGHT_SUM_TARGET = BigDecimal.ONE;
    private static final BigDecimal WEIGHT_SUM_TOLERANCE = new BigDecimal("0.0001");
    private static final int SCORE_SCALE = 2;
    private static final int MAX_IMPORT_ERRORS = 30;
    private static final Pattern CLASS_SPLIT_PATTERN = Pattern.compile("[,，;；/\\s、]+");
    private static final Pattern ASSESS_ITEM_PATTERN = Pattern.compile("([\\u4e00-\\u9fa5A-Za-z]+)\\s*([0-9]+(?:\\.[0-9]+)?)\\s*%");

    private final TrainScoreFinalMapper scoreFinalMapper;
    private final SysUserMapper userMapper;
    private final TrainProjectPublishMapper publishMapper;
    private final DataFormatter dataFormatter = new DataFormatter();

    @Value("${training.score.alpha:0.4}")
    private BigDecimal alpha;
    @Value("${training.score.beta:0.3}")
    private BigDecimal beta;
    @Value("${training.score.gamma:0.3}")
    private BigDecimal gamma;

    public ScoreServiceImpl(TrainScoreFinalMapper scoreFinalMapper,
                            SysUserMapper userMapper,
                            TrainProjectPublishMapper publishMapper) {
        this.scoreFinalMapper = scoreFinalMapper;
        this.userMapper = userMapper;
        this.publishMapper = publishMapper;
    }

    @PostConstruct
    public void init() {
        validateWeights();
    }

    @Override
    public List<TrainScoreFinal> list(Long publishId) {
        validateWeights();
        List<TrainScoreFinal> list = scoreFinalMapper.list(publishId);
        Map<Long, ScoreWeight> weightCache = new HashMap<>();
        list.forEach(item -> {
            ScoreWeight weight = weightCache.computeIfAbsent(item.getPublishId(), this::resolveWeightByPublishId);
            item.setFinalScore(calculateTotalScore(item.getUsualScore(), item.getTaskScore(), item.getReportScore(), weight));
        });
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
        saveOrUpdateByPublishStudent(scoreFinal);
        return scoreFinal;
    }

    @Override
    public void update(TrainScoreFinal scoreFinal) {
        validatePayload(scoreFinal);
        ScoreWeight weight = resolveWeightByPublishId(scoreFinal.getPublishId());
        scoreFinal.setFinalScore(calculateTotalScore(scoreFinal.getUsualScore(), scoreFinal.getTaskScore(), scoreFinal.getReportScore(), weight));
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

    @Override
    public ScoreImportResult importFromFile(MultipartFile file, Long defaultPublishId) {
        validateWeights();
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        String filename = file.getOriginalFilename();
        String lowerName = filename == null ? "" : filename.toLowerCase();
        ScoreImportResult result = new ScoreImportResult();
        try {
            if (lowerName.endsWith(".csv")) {
                importFromCsv(file, defaultPublishId, result);
            } else if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
                importFromExcel(file, defaultPublishId, result);
            } else {
                throw new BizException("仅支持.xlsx/.xls/.csv格式的成绩文件");
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException("成绩导入失败: " + ex.getMessage());
        }
        return result;
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
        validateWeightSet(alpha, beta, gamma, "系统默认成绩权重配置错误");
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

    private BigDecimal calculateTotalScore(BigDecimal processScore, BigDecimal teamScore, BigDecimal finalScore, ScoreWeight weight) {
        BigDecimal p = processScore == null ? SCORE_MIN : processScore;
        BigDecimal t = teamScore == null ? SCORE_MIN : teamScore;
        BigDecimal f = finalScore == null ? SCORE_MIN : finalScore;
        return p.multiply(weight.alpha)
                .add(t.multiply(weight.beta))
                .add(f.multiply(weight.gamma))
                .setScale(SCORE_SCALE, RoundingMode.HALF_UP);
    }

    private void saveOrUpdateByPublishStudent(TrainScoreFinal scoreFinal) {
        ScoreWeight weight = resolveWeightByPublishId(scoreFinal.getPublishId());
        scoreFinal.setFinalScore(calculateTotalScore(scoreFinal.getUsualScore(), scoreFinal.getTaskScore(), scoreFinal.getReportScore(), weight));
        TrainScoreFinal existing = scoreFinalMapper.selectByPublishAndStudent(scoreFinal.getPublishId(), scoreFinal.getStudentId());
        if (existing == null) {
            scoreFinalMapper.insert(scoreFinal);
            return;
        }
        scoreFinal.setId(existing.getId());
        int affected = scoreFinalMapper.update(scoreFinal);
        if (affected <= 0) {
            throw new BizException("成绩记录保存失败，请重试");
        }
    }

    private ScoreWeight resolveWeightByPublishId(Long publishId) {
        ScoreWeight fallback = new ScoreWeight(alpha, beta, gamma, "GLOBAL");
        if (publishId == null) {
            return fallback;
        }
        TrainProjectPublish publish = publishMapper.selectById(publishId);
        if (publish == null) {
            return fallback;
        }
        ScoreWeight fromPublish = resolveWeightFromPublish(publish);
        return fromPublish == null ? fallback : fromPublish;
    }

    private ScoreWeight resolveWeightFromPublish(TrainProjectPublish publish) {
        if (publish.getProcessWeight() != null || publish.getTeamWeight() != null || publish.getFinalWeight() != null) {
            if (publish.getProcessWeight() == null || publish.getTeamWeight() == null || publish.getFinalWeight() == null) {
                throw new BizException("开设计划评分权重配置不完整，请补齐过程/协作/答辩三项");
            }
            validateWeightSet(publish.getProcessWeight(), publish.getTeamWeight(), publish.getFinalWeight(),
                    "开设计划评分权重配置错误（开设ID=" + publish.getId() + "）");
            return new ScoreWeight(publish.getProcessWeight(), publish.getTeamWeight(), publish.getFinalWeight(), "PUBLISH_WEIGHT");
        }
        ScoreWeight parsed = parseWeightFromAssessment(publish.getAssessmentStandard());
        if (parsed != null) {
            validateWeightSet(parsed.alpha, parsed.beta, parsed.gamma,
                    "开设计划考核标准解析出的评分权重非法（开设ID=" + publish.getId() + "）");
            parsed.source = "ASSESSMENT_STANDARD";
            return parsed;
        }
        return null;
    }

    private ScoreWeight parseWeightFromAssessment(String assessmentStandard) {
        if (!StringUtils.hasText(assessmentStandard)) {
            return null;
        }
        Matcher matcher = ASSESS_ITEM_PATTERN.matcher(assessmentStandard);
        BigDecimal process = BigDecimal.ZERO;
        BigDecimal team = BigDecimal.ZERO;
        BigDecimal fin = BigDecimal.ZERO;
        boolean hit = false;
        while (matcher.find()) {
            String label = matcher.group(1);
            String valueText = matcher.group(2);
            BigDecimal percent;
            try {
                percent = new BigDecimal(valueText);
            } catch (Exception ex) {
                continue;
            }
            BigDecimal ratio = percent.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
            if (isProcessLabel(label)) {
                process = process.add(ratio);
                hit = true;
                continue;
            }
            if (isTeamLabel(label)) {
                team = team.add(ratio);
                hit = true;
                continue;
            }
            if (isFinalLabel(label)) {
                fin = fin.add(ratio);
                hit = true;
            }
        }
        if (!hit) {
            return null;
        }
        return new ScoreWeight(process, team, fin, "ASSESSMENT_STANDARD");
    }

    private boolean isProcessLabel(String label) {
        String text = normalizeLabel(label);
        return text.contains("过程") || text.contains("平时") || text.contains("文档") || text.contains("阶段");
    }

    private boolean isTeamLabel(String label) {
        String text = normalizeLabel(label);
        return text.contains("团队") || text.contains("协作") || text.contains("任务") || text.contains("互评");
    }

    private boolean isFinalLabel(String label) {
        String text = normalizeLabel(label);
        return text.contains("答辩") || text.contains("结题") || text.contains("成果") || text.contains("报告") || text.contains("终");
    }

    private String normalizeLabel(String label) {
        return label == null ? "" : label.replace("（", "(").replace("）", ")").trim();
    }

    private void validateWeightSet(BigDecimal a, BigDecimal b, BigDecimal c, String errorPrefix) {
        if (!isWeightInRange(a) || !isWeightInRange(b) || !isWeightInRange(c)) {
            throw new BizException(errorPrefix + "：三项权重必须在0到1之间");
        }
        BigDecimal sum = a.add(b).add(c);
        BigDecimal delta = sum.subtract(WEIGHT_SUM_TARGET).abs();
        if (delta.compareTo(WEIGHT_SUM_TOLERANCE) > 0) {
            throw new BizException(errorPrefix + "：三项权重之和必须等于1");
        }
    }

    private void importFromExcel(MultipartFile file, Long defaultPublishId, ScoreImportResult result) throws Exception {
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                throw new BizException("导入文件内容为空");
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BizException("导入文件缺少表头");
            }
            Map<String, Integer> header = buildHeaderMap(headerRow);
            ensureImportHeaders(header, defaultPublishId);

            Map<Long, TrainProjectPublish> publishCache = new HashMap<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String publishIdText = readCell(row, header, "publishid", "开设id", "开设计划id", "开设计划");
                String termName = readCell(row, header, "学期", "termname", "term");
                String className = readCell(row, header, "班级", "classname", "class");
                String projectName = readCell(row, header, "项目名称", "科目名称", "课程名称", "projectname", "subjectname");
                String studentIdText = readCell(row, header, "学生id", "studentid");
                String studentName = readCell(row, header, "学生姓名", "姓名", "studentname");
                String usualScoreText = readCell(row, header, "过程得分", "平时分", "usualscore");
                String taskScoreText = readCell(row, header, "团队协作得分", "协作得分", "任务得分", "taskscore");
                String reportScoreText = readCell(row, header, "答辩得分", "报告得分", "reportscore");
                if (isBlankRow(publishIdText, termName, className, projectName, studentIdText, studentName, usualScoreText, taskScoreText, reportScoreText)) {
                    continue;
                }
                processImportRow(i + 1, defaultPublishId, publishCache, result,
                        publishIdText, termName, className, projectName,
                        studentIdText, studentName, usualScoreText, taskScoreText, reportScoreText);
            }
        }
    }

    private void importFromCsv(MultipartFile file, Long defaultPublishId, ScoreImportResult result) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (!StringUtils.hasText(headerLine)) {
                throw new BizException("导入文件内容为空");
            }
            List<String> headerCells = parseCsvLine(stripBom(headerLine));
            Map<String, Integer> header = buildHeaderMap(headerCells);
            ensureImportHeaders(header, defaultPublishId);

            Map<Long, TrainProjectPublish> publishCache = new HashMap<>();
            int rowNum = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                List<String> cells = parseCsvLine(line);
                String publishIdText = readCsvCell(cells, header, "publishid", "开设id", "开设计划id", "开设计划");
                String termName = readCsvCell(cells, header, "学期", "termname", "term");
                String className = readCsvCell(cells, header, "班级", "classname", "class");
                String projectName = readCsvCell(cells, header, "项目名称", "科目名称", "课程名称", "projectname", "subjectname");
                String studentIdText = readCsvCell(cells, header, "学生id", "studentid");
                String studentName = readCsvCell(cells, header, "学生姓名", "姓名", "studentname");
                String usualScoreText = readCsvCell(cells, header, "过程得分", "平时分", "usualscore");
                String taskScoreText = readCsvCell(cells, header, "团队协作得分", "协作得分", "任务得分", "taskscore");
                String reportScoreText = readCsvCell(cells, header, "答辩得分", "报告得分", "reportscore");
                if (isBlankRow(publishIdText, termName, className, projectName, studentIdText, studentName, usualScoreText, taskScoreText, reportScoreText)) {
                    continue;
                }
                processImportRow(rowNum, defaultPublishId, publishCache, result,
                        publishIdText, termName, className, projectName,
                        studentIdText, studentName, usualScoreText, taskScoreText, reportScoreText);
            }
        }
    }

    private void processImportRow(int rowNum,
                                  Long defaultPublishId,
                                  Map<Long, TrainProjectPublish> publishCache,
                                  ScoreImportResult result,
                                  String publishIdText,
                                  String termName,
                                  String className,
                                  String projectName,
                                  String studentIdText,
                                  String studentName,
                                  String usualScoreText,
                                  String taskScoreText,
                                  String reportScoreText) {
        result.setTotalRows(result.getTotalRows() + 1);
        try {
            Long publishId = resolvePublishId(publishIdText, termName, className, projectName, defaultPublishId);
            TrainProjectPublish publish = publishCache.computeIfAbsent(publishId, publishMapper::selectById);
            if (publish == null) {
                throw new BizException("未找到开设计划，publishId=" + publishId);
            }
            Long studentId = resolveStudentId(studentIdText, studentName, className, publish);
            SysUser student = userMapper.selectById(studentId);
            if (student == null || !"STUDENT".equals(student.getUserType())) {
                throw new BizException("学生不存在或已删除，studentId=" + studentId);
            }
            if (!matchesClassScope(student.getClassName(), publish.getClassName())) {
                throw new BizException("学生不属于该开设班级范围，student=" + student.getRealName());
            }

            TrainScoreFinal score = new TrainScoreFinal();
            score.setPublishId(publishId);
            score.setStudentId(studentId);
            score.setUsualScore(normalizeComponent(parseScore(usualScoreText, "过程得分"), "过程得分"));
            score.setTaskScore(normalizeComponent(parseScore(taskScoreText, "团队协作得分"), "团队协作得分"));
            score.setReportScore(normalizeComponent(parseScore(reportScoreText, "答辩得分"), "答辩得分"));
            saveOrUpdateByPublishStudent(score);
            result.setSuccessRows(result.getSuccessRows() + 1);
        } catch (Exception ex) {
            result.setFailedRows(result.getFailedRows() + 1);
            if (result.getErrors().size() < MAX_IMPORT_ERRORS) {
                result.getErrors().add("第" + rowNum + "行: " + ex.getMessage());
            }
        }
    }

    private Long resolvePublishId(String publishIdText,
                                  String termName,
                                  String className,
                                  String projectName,
                                  Long defaultPublishId) {
        Long publishId = parseLong(publishIdText);
        if (publishId != null) {
            return publishId;
        }
        if (defaultPublishId != null) {
            return defaultPublishId;
        }
        if (StringUtils.hasText(termName) && StringUtils.hasText(className) && StringUtils.hasText(projectName)) {
            TrainProjectPublish publish = publishMapper.selectByTermClassAndProject(termName.trim(), className.trim(), projectName.trim());
            if (publish != null) {
                return publish.getId();
            }
        }
        throw new BizException("缺少开设计划信息，请提供publishId或完整的学期/班级/科目名称");
    }

    private Long resolveStudentId(String studentIdText, String studentName, String className, TrainProjectPublish publish) {
        Long studentId = parseLong(studentIdText);
        if (studentId != null) {
            return studentId;
        }
        if (!StringUtils.hasText(studentName)) {
            throw new BizException("缺少学生标识，请填写学生ID或学生姓名");
        }
        List<String> classScopes = parseClassScope(StringUtils.hasText(className) ? className : publish.getClassName());
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0)
                .eq("status", 1)
                .eq("user_type", "STUDENT")
                .eq("real_name", studentName.trim());
        if (!classScopes.isEmpty()) {
            wrapper.in("class_name", classScopes);
        }
        List<SysUser> list = userMapper.selectList(wrapper);
        if (list.isEmpty()) {
            throw new BizException("未找到学生：" + studentName);
        }
        if (list.size() > 1) {
            String classes = list.stream().map(SysUser::getClassName).distinct().collect(Collectors.joining("、"));
            throw new BizException("学生姓名不唯一，请改用学生ID。候选班级：" + classes);
        }
        return list.get(0).getId();
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String key = normalizeHeader(readCell(headerRow.getCell(i)));
            if (StringUtils.hasText(key)) {
                map.putIfAbsent(key, i);
            }
        }
        return map;
    }

    private Map<String, Integer> buildHeaderMap(List<String> headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String key = normalizeHeader(headers.get(i));
            if (StringUtils.hasText(key)) {
                map.putIfAbsent(key, i);
            }
        }
        return map;
    }

    private void ensureImportHeaders(Map<String, Integer> header, Long defaultPublishId) {
        Integer usualIndex = findHeaderIndex(header, "过程得分", "平时分", "usualscore");
        Integer taskIndex = findHeaderIndex(header, "团队协作得分", "协作得分", "任务得分", "taskscore");
        Integer reportIndex = findHeaderIndex(header, "答辩得分", "报告得分", "reportscore");
        if (usualIndex == null || taskIndex == null || reportIndex == null) {
            throw new BizException("导入文件缺少成绩列，请包含过程得分、团队协作得分、答辩得分");
        }
        boolean hasStudentId = findHeaderIndex(header, "学生id", "studentid") != null;
        boolean hasStudentName = findHeaderIndex(header, "学生姓名", "姓名", "studentname") != null;
        if (!hasStudentId && !hasStudentName) {
            throw new BizException("导入文件缺少学生标识列，请包含学生ID或学生姓名");
        }
        boolean hasPublishId = findHeaderIndex(header, "publishid", "开设id", "开设计划id", "开设计划") != null;
        boolean hasPublishInfo = findHeaderIndex(header, "学期", "termname", "term") != null
                && findHeaderIndex(header, "班级", "classname", "class") != null
                && findHeaderIndex(header, "项目名称", "科目名称", "课程名称", "projectname", "subjectname") != null;
        if (!hasPublishId && !hasPublishInfo && defaultPublishId == null) {
            throw new BizException("导入文件缺少开设计划信息，请包含publishId或学期+班级+科目名称，或在界面选择开设计划后导入");
        }
    }

    private String readCell(Row row, Map<String, Integer> header, String... aliases) {
        Integer idx = findHeaderIndex(header, aliases);
        if (idx == null) {
            return "";
        }
        if (row.getCell(idx) == null) {
            return "";
        }
        return readCell(row.getCell(idx));
    }

    private String readCsvCell(List<String> cells, Map<String, Integer> header, String... aliases) {
        Integer idx = findHeaderIndex(header, aliases);
        if (idx == null || idx < 0 || idx >= cells.size()) {
            return "";
        }
        return trim(cells.get(idx));
    }

    private Integer findHeaderIndex(Map<String, Integer> header, String... aliases) {
        for (String alias : aliases) {
            String key = normalizeHeader(alias);
            Integer idx = header.get(key);
            if (idx != null) {
                return idx;
            }
        }
        return null;
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase()
                .replace("（", "(")
                .replace("）", ")")
                .replace("：", "")
                .replace(":", "")
                .replaceAll("[\\s_\\-]", "");
    }

    private String readCell(org.apache.poi.ss.usermodel.Cell cell) {
        String value = dataFormatter.formatCellValue(cell);
        return value == null ? "" : value.trim();
    }

    private boolean isBlankRow(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return false;
            }
        }
        return true;
    }

    private Long parseLong(String value) {
        String text = trim(value);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            if (text.contains(".")) {
                BigDecimal decimal = new BigDecimal(text);
                return decimal.longValue();
            }
            return Long.parseLong(text);
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal parseScore(String value, String name) {
        String text = trim(value);
        if (!StringUtils.hasText(text)) {
            throw new BizException(name + "不能为空");
        }
        String normalized = text.replace("%", "").trim();
        try {
            return new BigDecimal(normalized);
        } catch (Exception ex) {
            throw new BizException(name + "格式错误: " + text);
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private List<String> parseClassScope(String className) {
        if (!StringUtils.hasText(className)) {
            return new ArrayList<>();
        }
        String[] tokens = CLASS_SPLIT_PATTERN.split(className.trim());
        List<String> list = new ArrayList<>();
        for (String token : tokens) {
            if (StringUtils.hasText(token)) {
                list.add(token.trim());
            }
        }
        return list;
    }

    private boolean matchesClassScope(String studentClassName, String publishClassName) {
        if (!StringUtils.hasText(studentClassName) || !StringUtils.hasText(publishClassName)) {
            return false;
        }
        String stu = studentClassName.trim();
        if (stu.equals(publishClassName.trim())) {
            return true;
        }
        List<String> scopes = parseClassScope(publishClassName);
        return scopes.stream().anyMatch(stu::equals);
    }

    private String stripBom(String line) {
        if (line == null || line.isEmpty()) {
            return line;
        }
        if (line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }

    private List<String> parseCsvLine(String line) {
        List<String> list = new ArrayList<>();
        if (line == null) {
            return list;
        }
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (ch == ',' && !inQuotes) {
                list.add(current.toString().trim());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        list.add(current.toString().trim());
        return list;
    }

    private static class ScoreWeight {
        private final BigDecimal alpha;
        private final BigDecimal beta;
        private final BigDecimal gamma;
        private String source;

        private ScoreWeight(BigDecimal alpha, BigDecimal beta, BigDecimal gamma, String source) {
            this.alpha = alpha;
            this.beta = beta;
            this.gamma = gamma;
            this.source = source;
        }
    }
}
