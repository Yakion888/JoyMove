package com.joymove.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.joymove.dto.FamilyStatsDTO;
import com.joymove.dto.MomentDetailVO;
import com.joymove.dto.MonthlyCalendarDTO;
import com.joymove.entity.FamilyMoment;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FamilyMomentService {

    /** 发布运动记录 */
    FamilyMoment save(FamilyMoment moment, MultipartFile file);

    /** 更新记录 */
    void update(FamilyMoment moment, MultipartFile file);

    /** 分页查询公开记录 */
    IPage<MomentDetailVO> getPublishedPage(int pageNum, int pageSize, Long projectId);

    /** 查详情 */
    MomentDetailVO getDetailById(Long id);

    /** 点赞 */
    void like(Long id);

    /** 切换点赞状态（点赞/取消） */
    boolean toggleLike(Long momentId, Long userId);

    /** 按标签查公开记录 */
    IPage<MomentDetailVO> getMomentsByTag(String tag, int page, int size);

    /** 热门标签 */
    List<String> getHotTags(int limit);

    /** 分页查用户记录（打卡列表） */
    IPage<MomentDetailVO> getUserMoments(Long userId, int page, int size);

    /** 用户月度运动日历 */
    MonthlyCalendarDTO getUserCalendar(Long userId, int year, int month);

    /** 分页查用户记录 */
    IPage<MomentDetailVO> getByUserIdPage(int pageNum, int pageSize, Long userId);

    /** 搜索 */
    IPage<MomentDetailVO> search(int pageNum, int pageSize, String keyword);

    /** 删除（逻辑） */
    void delete(Long id);

    /** 审核通过 */
    void approve(Long id);

    /** 审核驳回 */
    void reject(Long id, String reason);

    /** 家庭统计 */
    FamilyStatsDTO getFamilyStats(Long userId);

    /** 统计方法 */
    int countByUserId(Long userId);
    int countDistinctProjects(Long userId);
    int countByProjectId(Long userId, Long projectId);
    int countByProjectIds(Long userId, List<Long> projectIds);
    int countByEmotion(Long userId, Integer emotion);
}
