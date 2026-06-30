package com.joymove.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户公开主页数据
 */
@Data
public class PublicProfileDTO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String city;
    private Integer totalDays;
    private Integer totalMoments;
    private Integer medalCount;
    private Integer totalDuration;
    private Long longestStreak;
    private List<String> medalIcons;
}
