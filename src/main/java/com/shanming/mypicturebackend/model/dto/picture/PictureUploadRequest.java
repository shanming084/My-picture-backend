package com.shanming.mypicturebackend.model.dto.picture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
  
    /**  
     * 图片 id（用于修改）  
     */  
    private Long id;

    /**
     * 图片路径
     */
    private String fileUrl;
  
    private static final long serialVersionUID = 1L;

}
