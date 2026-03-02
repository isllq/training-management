package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.TrainQaReplyMapper;
import com.training.mapper.TrainQaThreadMapper;
import com.training.model.entity.TrainQaReply;
import com.training.model.entity.TrainQaThread;
import com.training.service.QaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QaServiceImpl implements QaService {
    private final TrainQaThreadMapper threadMapper;
    private final TrainQaReplyMapper replyMapper;

    public QaServiceImpl(TrainQaThreadMapper threadMapper, TrainQaReplyMapper replyMapper) {
        this.threadMapper = threadMapper;
        this.replyMapper = replyMapper;
    }

    @Override
    public List<TrainQaThread> listThreads(Long publishId) {
        return threadMapper.list(publishId);
    }

    @Override
    public List<TrainQaThread> listThreadsByClass(Long publishId, String className) {
        return threadMapper.listByClassName(publishId, className);
    }

    @Override
    public TrainQaThread createThread(TrainQaThread thread) {
        if (thread.getStatus() == null) {
            thread.setStatus(1);
        }
        threadMapper.insert(thread);
        return thread;
    }

    @Override
    public void updateThread(TrainQaThread thread) {
        if (threadMapper.update(thread) <= 0) {
            throw new BizException("问答主题不存在或已删除");
        }
    }

    @Override
    public void deleteThread(Long id) {
        if (threadMapper.softDelete(id) <= 0) {
            throw new BizException("问答主题不存在或已删除");
        }
    }

    @Override
    public List<TrainQaReply> listReplies(Long threadId) {
        return replyMapper.listByThreadId(threadId);
    }

    @Override
    public TrainQaReply createReply(TrainQaReply reply) {
        replyMapper.insert(reply);
        return reply;
    }

    @Override
    public void deleteReply(Long id) {
        if (replyMapper.softDelete(id) <= 0) {
            throw new BizException("问答回复不存在或已删除");
        }
    }
}
