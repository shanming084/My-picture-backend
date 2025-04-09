 package com.shanming.mypicturebackend.manager;

import cn.hutool.core.io.FileUtil;
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
import java.util.ArrayList;
import java.util.List;

 @Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

     /**
      * 删除对象
      */
     // 将本地文件上传到 COS
     public void deleteObject (String key){
         cosClient.deleteObject(cosClientConfig.getBucket(), key);
     }

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
        //图片压缩(转成webp格式)
        //将原来的Key指定后缀格式
        String webpKey = FileUtil.mainName(key) + ".webp";
        //接下来会有多个规则，所以设置数组
        List<PicOperations.Rule> rules = new ArrayList<>();
        //设置规则参数
        /**
         * PUT /<objectKey> HTTP/1.1
         * Host: <BucketName-APPID>.cos.<Region>.myqcloud.com
         * Date: GMT Date
         * Authorization: Auth String
         * Pic-Operations:
         * {
         *  "is_pic_info":1,
         *  "rules":[{
         *      "fileid":"exampleobject",
         *      "rule":"imageMogr2/format/<Format>"
         *          }]
         * }
         */
        PicOperations.Rule compressRule = new PicOperations.Rule();
        //指定要操作的文件的id
        compressRule.setFileId(webpKey);
        //指定如何转换
        compressRule.setRule("imageMogr2/format/webp");
        //指定要操作的存储桶
        compressRule.setBucket(cosClientConfig.getBucket());
        //将该规则设置到数组中
        rules.add(compressRule);

        //缩略图处理规则,仅对20KB的图片生成缩略图
        if(file.length() > 2 * 1024){
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            //指定要操作的文件的id
            //约定缩略图的id是源文件名称 + thumbnail + 源文件后缀
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            //指定如何转换
            // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",128,128));
            //指定要操作的存储桶
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            //将该规则设置到数组中
            rules.add(thumbnailRule);
        }
        //将规则设置到图片操作中
        picOperations.setRules(rules);
        //构造处理函数
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }
}
