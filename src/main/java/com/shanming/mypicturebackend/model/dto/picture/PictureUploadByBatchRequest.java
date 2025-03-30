package com.shanming.mypicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片批量抓取请求体
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {


    private static final long serialVersionUID = 8995500640380023160L;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer searchNum;

}
