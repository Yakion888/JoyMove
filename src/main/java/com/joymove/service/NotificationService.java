package com.joymove.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joymove.entity.Notification;

public interface NotificationService {

    IPage<Notification> getByUserId(int pageNum, int pageSize, Long userId);

    int countUnread(Long userId);

    void markAllRead(Long userId);

    void create(Long userId, Integer type, String message, Long relatedId);
}
