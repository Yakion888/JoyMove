package com.joymove.service;

import com.joymove.entity.SportProject;

import java.util.List;

public interface SportProjectService {

    /** 获取所有启用的项目 */
    List<SportProject> getAllEnabled();

    /** 获取全部（含禁用，管理员用） */
    List<SportProject> getAll();

    /** 根据ID获取 */
    SportProject getById(Long id);

    /** 按年龄筛选 */
    List<SportProject> getByAge(int childAge);

    /** 新增/更新 */
    void saveOrUpdate(SportProject project);

    /** 删除 */
    void delete(Long id);
}
