package com.sportsblog.dto;

import lombok.Data;

import java.util.List;

/**
 * 成长时间线 DTO
 */
@Data
public class GrowthTimelineDTO {

    /** 时间线节点 */
    private List<TimelineNode> nodes;

    @Data
    public static class TimelineNode {
        private String date;
        private String projectName;
        private String projectIcon;
        private String content;
        private String emotionEmoji;
        private Integer stars;
        private String location;
        private String childName;
    }
}
