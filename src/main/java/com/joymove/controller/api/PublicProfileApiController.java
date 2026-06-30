package com.joymove.controller.api;

import com.joymove.common.Result;
import com.joymove.dto.MedalProgressDTO;
import com.joymove.dto.PublicProfileDTO;
import com.joymove.entity.User;
import com.joymove.mapper.UserMapper;
import com.joymove.service.FamilyMomentService;
import com.joymove.service.MedalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * 用户公开主页 API
 */
@RestController
@Tag(name = "用户公开主页", description = "查看其他用户的公开信息")
public class PublicProfileApiController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FamilyMomentService momentService;

    @Autowired
    private MedalService medalService;

    @Operation(summary = "获取用户公开统计数据")
    @GetMapping("/api/user/{id}/stats")
    @Cacheable(value = "userStats", key = "#id")
    public Result<?> getStats(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.error("用户不存在");

        int totalMoments = momentService.countByUserId(id);
        int totalDuration = 0; // simplified: count from DB below

        PublicProfileDTO dto = new PublicProfileDTO();
        dto.setUserId(user.getId());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setCity(user.getCity());
        dto.setTotalDays(user.getTotalDays());
        dto.setLongestStreak(user.getLongestStreak() != null
                ? Long.valueOf(user.getLongestStreak()) : 0L);
        dto.setTotalMoments(totalMoments);

        // 勋章
        java.util.List<MedalProgressDTO> medals = medalService.getProgress(id);
        dto.setMedalCount((int) medals.stream().filter(MedalProgressDTO::getEarned).count());
        dto.setMedalIcons(medals.stream()
                .filter(MedalProgressDTO::getEarned)
                .map(MedalProgressDTO::getMedalIcon)
                .collect(Collectors.toList()));

        return Result.success(dto);
    }

    @Operation(summary = "获取用户公开运动记录列表")
    @GetMapping("/api/user/{id}/moments")
    public Result<?> getMoments(@PathVariable Long id,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return Result.success(momentService.getByUserIdPage(page, size, id));
    }
}
