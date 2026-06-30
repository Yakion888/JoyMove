package com.joymove.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.joymove.entity.FamilyMoment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FamilyMomentMapper extends BaseMapper<FamilyMoment> {

    /** 分页查询公开记录（含用户/孩子/项目关联） */
    IPage<FamilyMoment> selectPublishedPage(Page<FamilyMoment> page, @Param("projectId") Long projectId);

    /** 根据ID查详情 */
    FamilyMoment selectDetailById(@Param("id") Long id);

    /** 增加点赞数 */
    int incrementLikeCount(@Param("id") Long id);

    /** 增加评论数 */
    int incrementCommentCount(@Param("id") Long id);

    /** 分页查用户记录 */
    IPage<FamilyMoment> selectByUserIdPage(Page<FamilyMoment> page, @Param("userId") Long userId);

    /** 查询用户全部已发布记录（无分页上限，status=1） */
    List<FamilyMoment> selectAllByUserId(@Param("userId") Long userId);

    /** 全文搜索 */
    IPage<FamilyMoment> searchPublished(Page<FamilyMoment> page, @Param("keyword") String keyword);

    /** 用户活动总数 */
    int countByUserId(@Param("userId") Long userId);

    /** 用户参与的不同项目数 */
    int countDistinctProjects(@Param("userId") Long userId);

    /** 用户在特定项目的活动数 */
    int countByProjectId(@Param("userId") Long userId, @Param("projectId") Long projectId);

    /** 用户在项目组中的活动数 */
    int countByProjectIds(@Param("userId") Long userId, @Param("projectIds") List<Long> projectIds);

    /** 用户特定心情的活动数 */
    int countByEmotion(@Param("userId") Long userId, @Param("emotion") Integer emotion);

    /** 查询用户某月所有记录（用于日历） */
    List<FamilyMoment> selectByUserIdAndMonth(@Param("userId") Long userId,
                                              @Param("year") Integer year,
                                              @Param("month") Integer month);

    /** 用户有记录的不同日期数（累计运动天数） */
    int countDistinctRecordDates(@Param("userId") Long userId);

    /** 用户所有有记录的不同日期（DESC，用于计算连续天数） */
    List<String> selectDistinctRecordDates(@Param("userId") Long userId);

    /** 待审核记录 */
    List<FamilyMoment> selectPending();

    /** 按状态查询 */
    List<FamilyMoment> selectByStatus(@Param("status") Integer status);
}
