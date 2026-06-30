package com.joymove.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joymove.entity.Notification;
import com.joymove.mapper.NotificationMapper;
import com.joymove.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public IPage<Notification> getByUserId(int pageNum, int pageSize, Long userId) {
        return notificationMapper.selectByUserId(new Page<>(pageNum, pageSize), userId);
    }

    @Override
    public int countUnread(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }

    @Override
    @Async("taskExecutor")
    public void create(Long userId, Integer type, String message, Long relatedId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setMessage(message);
        n.setRelatedId(relatedId);
        n.setIsRead(0);
        notificationMapper.insert(n);
        log.info("[ASYNC] Notification created [{}]: id={}, userId={}, type={}",
                Thread.currentThread().getName(), n.getId(), userId, type);
    }
}
