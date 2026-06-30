package com.joymove.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.joymove.entity.SportProject;
import com.joymove.mapper.SportProjectMapper;
import com.joymove.service.SportProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
public class SportProjectServiceImpl implements SportProjectService {

    @Autowired
    private SportProjectMapper projectMapper;

    @Override
    @Cacheable(value = "sportProject", key = "'enabled'")
    public List<SportProject> getAllEnabled() {
        LambdaQueryWrapper<SportProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportProject::getStatus, 1);
        return projectMapper.selectList(wrapper);
    }

    @Override
    @Cacheable(value = "sportProject", key = "'all'")
    public List<SportProject> getAll() {
        return projectMapper.selectList(null);
    }

    @Override
    @Cacheable(value = "sportProject", key = "#id")
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
    @CacheEvict(value = "sportProject", allEntries = true)
    public void saveOrUpdate(SportProject project) {
        if (project.getId() == null) {
            projectMapper.insert(project);
            log.info("SportProject created: id={}, name={}", project.getId(), project.getName());
        } else {
            projectMapper.updateById(project);
            log.info("SportProject updated: id={}, name={}", project.getId(), project.getName());
        }
    }

    @Override
    @CacheEvict(value = "sportProject", allEntries = true)
    public void delete(Long id) {
        projectMapper.deleteById(id);
        log.info("SportProject deleted: id={}", id);
    }
}
