package com.shanming.mypicturebackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * SpaceLevel 封装类
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    /**
     * 值
     */
    private int value;

    /**
     * 中文
     */
    private String text;

    /**
     * 最大数量
     */
    private long maxCount;

    /**
     * 最大容量
     */
    private long maxSize;
}
