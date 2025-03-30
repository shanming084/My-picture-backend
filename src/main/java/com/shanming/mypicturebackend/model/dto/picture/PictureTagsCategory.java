package com.shanming.mypicturebackend.model.dto.picture;

import lombok.Data;

import java.util.List;

/**
 * 标签体
 */
@Data
public class PictureTagsCategory {

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;

}
