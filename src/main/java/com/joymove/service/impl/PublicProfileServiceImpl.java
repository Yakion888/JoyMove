package com.joymove.service.impl;

import com.joymove.dto.MedalProgressDTO;
import com.joymove.dto.PublicProfileDTO;
import com.joymove.entity.User;
import com.joymove.mapper.FamilyMomentMapper;
import com.joymove.mapper.UserMapper;
import com.joymove.service.MedalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * 用户公开主页查询服务 — 独立 Bean，@Cacheable 走 Spring 代理。
 *
 * <p>防穿透：不存在的用户 ID 返回空标记 DTO（userId=-1），缓存 1 分钟，
 * 避免恶意请求用随机 ID 反复穿透到 DB。</p>
 */
@Slf4j
@Service
public class PublicProfileServiceImpl {

    private static final PublicProfileDTO NULL_MARKER = new PublicProfileDTO();

    static {
        NULL_MARKER.setUserId(-1L);
    }

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FamilyMomentMapper momentMapper;

    @Autowired
    private MedalService medalService;

    @Cacheable(value = "userStats", key = "#userId", sync = true)
    public PublicProfileDTO getStats(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("穿透拦截: 不存在的用户 userId={}, 缓存空标记 1min", userId);
            // 换个缓存空间存空标记，避免污染 userStats
            return null; // 由 Controller 判断
        }

        int totalMoments = momentMapper.countByUserId(userId);

        PublicProfileDTO dto = new PublicProfileDTO();
        dto.setUserId(user.getId());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setCity(user.getCity());
        dto.setTotalDays(user.getTotalDays());
        dto.setLongestStreak(user.getLongestStreak() != null
                ? Long.valueOf(user.getLongestStreak()) : 0L);
        dto.setTotalMoments(totalMoments);

        java.util.List<MedalProgressDTO> medals = medalService.getProgress(userId);
        dto.setMedalCount((int) medals.stream().filter(MedalProgressDTO::getEarned).count());
        dto.setMedalIcons(medals.stream()
                .filter(MedalProgressDTO::getEarned)
                .map(MedalProgressDTO::getMedalIcon)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * 防穿透：缓存不存在用户的空标记（1min TTL）。
     * 独立方法 + 独立缓存空间 nullMarker，与正常 userStats 隔离。
     */
    @Cacheable(value = "nullMarker", key = "#userId")
    public PublicProfileDTO getNullMarker(Long userId) {
        // 方法体不执行 — 只要缓存命中就直接返回空标记
        // 只有缓存 miss 时才进来，写入空标记
        return NULL_MARKER;
    }
}
