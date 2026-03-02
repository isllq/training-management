package com.training.service.impl;

import com.training.common.BizException;
import com.training.mapper.TrainProjectMapper;
import com.training.mapper.TrainProjectPublishMapper;
import com.training.model.entity.TrainProject;
import com.training.model.entity.TrainProjectPublish;
import com.training.service.ProjectService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final TrainProjectMapper projectMapper;
    private final TrainProjectPublishMapper publishMapper;

    public ProjectServiceImpl(TrainProjectMapper projectMapper, TrainProjectPublishMapper publishMapper) {
        this.projectMapper = projectMapper;
        this.publishMapper = publishMapper;
    }

    @Override
    public List<TrainProject> listProjects(String keyword) {
        return projectMapper.list(keyword);
    }

    @Override
    public TrainProject createProject(TrainProject project) {
        if (project.getStatus() == null) {
            project.setStatus(1);
        }
        projectMapper.insert(project);
        return project;
    }

    @Override
    public void updateProject(TrainProject project) {
        int affected = projectMapper.update(project);
        if (affected <= 0) {
            throw new BizException("项目不存在或已删除");
        }
    }

    @Override
    public void deleteProject(Long id) {
        int affected = projectMapper.softDelete(id);
        if (affected <= 0) {
            throw new BizException("项目不存在或已删除");
        }
    }

    @Override
    public List<TrainProjectPublish> listPublishes(String className) {
        return publishMapper.list(className);
    }

    @Override
    public TrainProjectPublish createPublish(TrainProjectPublish publish) {
        if (publish.getStatus() == null) {
            publish.setStatus(1);
        }
        if (publish.getGroupCount() == null || publish.getGroupCount() < 0) {
            publish.setGroupCount(0);
        }
        if (publish.getGroupSizeLimit() == null || publish.getGroupSizeLimit() < 0) {
            publish.setGroupSizeLimit(0);
        }
        publishMapper.insert(publish);
        return publish;
    }

    @Override
    public void updatePublish(TrainProjectPublish publish) {
        if (publish.getGroupCount() == null || publish.getGroupCount() < 0) {
            publish.setGroupCount(0);
        }
        if (publish.getGroupSizeLimit() == null || publish.getGroupSizeLimit() < 0) {
            publish.setGroupSizeLimit(0);
        }
        int affected = publishMapper.update(publish);
        if (affected <= 0) {
            throw new BizException("开设记录不存在或已删除");
        }
    }

    @Override
    public void deletePublish(Long id) {
        int affected = publishMapper.softDelete(id);
        if (affected <= 0) {
            throw new BizException("开设记录不存在或已删除");
        }
    }
}
