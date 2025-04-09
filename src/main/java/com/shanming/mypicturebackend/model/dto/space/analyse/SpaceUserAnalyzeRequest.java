package com.shanming.mypicturebackend.model.dto.space.analyse;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户上传行为分析请求体
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 时间维度：day / week / month
     */
    private String timeDimension;
}
