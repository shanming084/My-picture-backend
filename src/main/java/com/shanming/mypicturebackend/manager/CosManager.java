 package com.shanming.mypicturebackend.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.shanming.mypicturebackend.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     */
    // 将本地文件上传到 COS
    public PutObjectResult putObject (String key, File file){
        //指定存储桶的位置，密钥和文件
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     */
    // 将本地文件上传到 COS
    public COSObject getObject (String key){
        //指定存储桶的位置，密钥和文件
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传对象(附带图片信息)
     */
    // 将本地文件上传到 COS
    public PutObjectResult putPictureObject (String key, File file){
        //指定存储桶的位置，密钥和文件
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        //对图片进行处理(获取基本信息也被视为一种图片的处理)
        PicOperations picOperations = new PicOperations();
        // 1表示返回图片信息
        picOperations.setIsPicInfo(1);
        //构造处理函数
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }
}
