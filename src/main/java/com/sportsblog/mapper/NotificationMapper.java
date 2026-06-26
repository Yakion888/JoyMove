package com.sportsblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sportsblog.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    IPage<Notification> selectByUserId(Page<Notification> page, @Param("userId") Long userId);

    int countUnread(@Param("userId") Long userId);

    int markAllRead(@Param("userId") Long userId);
}
