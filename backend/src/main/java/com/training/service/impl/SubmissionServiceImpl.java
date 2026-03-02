package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.TrainSubmissionMapper;
import com.training.model.entity.TrainSubmission;
import com.training.service.SubmissionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubmissionServiceImpl implements SubmissionService {
    private final TrainSubmissionMapper submissionMapper;

    public SubmissionServiceImpl(TrainSubmissionMapper submissionMapper) {
        this.submissionMapper = submissionMapper;
    }

    @Override
    public List<TrainSubmission> list(Long taskId, Long teamId) {
        return submissionMapper.list(taskId, teamId);
    }

    @Override
    public TrainSubmission create(TrainSubmission submission) {
        if (submission.getVersionNo() == null) {
            submission.setVersionNo(1);
        }
        if (submission.getStatus() == null) {
            submission.setStatus(1);
        }
        if (submission.getSubmitTime() == null) {
            submission.setSubmitTime(LocalDateTime.now());
        }
        submissionMapper.insert(submission);
        return submission;
    }

    @Override
    public void update(TrainSubmission submission) {
        if (submission.getSubmitTime() == null) {
            submission.setSubmitTime(LocalDateTime.now());
        }
        int affected = submissionMapper.update(submission);
        if (affected <= 0) {
            throw new BizException("提交记录不存在或已删除");
        }
    }

    @Override
    public void delete(Long id) {
        int affected = submissionMapper.softDelete(id);
        if (affected <= 0) {
            throw new BizException("提交记录不存在或已删除");
        }
    }
}
