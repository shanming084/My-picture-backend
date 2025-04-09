package com.shanming.mypicturebackend.model.dto.picture;

import lombok.Data;

@Data
public class UploadPictureResult {  
  
    /**  
     * 图片地址  
     */  
    private String url;

    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 缩略图Url
     */
    private String thumbnailUrl;
  
    /**  
     * 图片名称  
     */  
    private String picName;  
  
    /**  
     * 文件体积  
     */  
    private Long picSize;  
  
    /**  
     * 图片宽度  
     */  
    private int picWidth;  
  
    /**  
     * 图片高度  
     */  
    private int picHeight;  
  
    /**  
     * 图片宽高比  
     */  
    private Double picScale;  
  
    /**  
     * 图片格式  
     */  
    private String picFormat;  
  
}
