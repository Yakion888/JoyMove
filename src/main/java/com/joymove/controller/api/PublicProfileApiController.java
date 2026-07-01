package com.joymove.controller.api;

import com.joymove.common.Result;
import com.joymove.dto.PublicProfileDTO;
import com.joymove.service.FamilyMomentService;
import com.joymove.service.impl.PublicProfileServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户公开主页 API
 */
@RestController
@Tag(name = "用户公开主页", description = "查看其他用户的公开信息")
public class PublicProfileApiController {

    @Autowired
    private PublicProfileServiceImpl publicProfileService;

    @Autowired
    private FamilyMomentService momentService;

    @Operation(summary = "获取用户公开统计数据")
    @GetMapping("/api/user/{id}/stats")
    public Result<?> getStats(@PathVariable Long id) {
        // 防穿透：先查空标记缓存，命中则直接返回，不查 DB
        PublicProfileDTO nullMarker = publicProfileService.getNullMarker(id);
        if (nullMarker != null && nullMarker.getUserId() != null && nullMarker.getUserId() == -1L) {
            return Result.error("用户不存在");
        }

        PublicProfileDTO dto = publicProfileService.getStats(id);
        if (dto == null) {
            // 用户确实不存在 → 写入空标记缓存（1min TTL，防穿透）
            publicProfileService.getNullMarker(id);
            return Result.error("用户不存在");
        }
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
