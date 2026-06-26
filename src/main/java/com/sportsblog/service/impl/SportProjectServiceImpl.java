package com.sportsblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sportsblog.entity.SportProject;
import com.sportsblog.mapper.SportProjectMapper;
import com.sportsblog.service.SportProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SportProjectServiceImpl implements SportProjectService {

    @Autowired
    private SportProjectMapper projectMapper;

    @Override
    public List<SportProject> getAllEnabled() {
        LambdaQueryWrapper<SportProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportProject::getStatus, 1);
        return projectMapper.selectList(wrapper);
    }

    @Override
    public List<SportProject> getAll() {
        return projectMapper.selectList(null);
    }

    @Override
    public SportProject getById(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public List<SportProject> getByAge(int childAge) {
        LambdaQueryWrapper<SportProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(SportProject::getAgeRangeMin, childAge)
               .ge(SportProject::getAgeRangeMax, childAge)
               .eq(SportProject::getStatus, 1);
        return projectMapper.selectList(wrapper);
    }

    @Override
    public void saveOrUpdate(SportProject project) {
        if (project.getId() == null) {
            projectMapper.insert(project);
        } else {
            projectMapper.updateById(project);
        }
    }

    @Override
    public void delete(Long id) {
        projectMapper.deleteById(id);
    }
}
