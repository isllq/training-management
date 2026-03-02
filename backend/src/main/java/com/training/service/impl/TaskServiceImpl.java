package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.TrainTaskMapper;
import com.training.model.entity.TrainTask;
import com.training.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private final TrainTaskMapper taskMapper;

    public TaskServiceImpl(TrainTaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public List<TrainTask> list(Long publishId) {
        List<TrainTask> tasks = taskMapper.list(publishId);
        fillProgress(tasks);
        return tasks;
    }

    @Override
    public List<TrainTask> listByClassName(Long publishId, String className) {
        List<TrainTask> tasks = taskMapper.listByClassName(publishId, className);
        fillProgress(tasks);
        return tasks;
    }

    @Override
    public TrainTask create(TrainTask task) {
        if (task.getStatus() == null) {
            task.setStatus(1);
        }
        if (task.getStageType() == null || task.getStageType().trim().isEmpty()) {
            task.setStageType("DAILY");
        }
        taskMapper.insert(task);
        return task;
    }

    @Override
    public void update(TrainTask task) {
        int affected = taskMapper.update(task);
        if (affected <= 0) {
            throw new BizException("任务不存在或已删除");
        }
    }

    @Override
    public void delete(Long id) {
        int affected = taskMapper.softDelete(id);
        if (affected <= 0) {
            throw new BizException("任务不存在或已删除");
        }
    }

    private void fillProgress(List<TrainTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }
        for (TrainTask task : tasks) {
            int expected = task.getExpectedGroupCount() == null ? 0 : task.getExpectedGroupCount();
            int submitted = task.getSubmittedGroupCount() == null ? 0 : task.getSubmittedGroupCount();
            int rate = 0;
            if (expected > 0) {
                rate = (int) Math.round((submitted * 100.0d) / expected);
            } else if (submitted > 0) {
                rate = 100;
            }
            if (rate < 0) {
                rate = 0;
            } else if (rate > 100) {
                rate = 100;
            }
            task.setCompletionRate(rate);
        }
    }
}
