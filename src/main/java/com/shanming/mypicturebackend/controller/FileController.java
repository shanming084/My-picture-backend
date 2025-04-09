package com.shanming.mypicturebackend.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.shanming.mypicturebackend.annotation.AutoCheck;
import com.shanming.mypicturebackend.common.BaseResponse;
import com.shanming.mypicturebackend.common.ResultUtils;
import com.shanming.mypicturebackend.constants.UserConstants;
import com.shanming.mypicturebackend.exception.BusinessException;
import com.shanming.mypicturebackend.exception.ErrorCode;
import com.shanming.mypicturebackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 测试文件上传
     * @return
     */
    @AutoCheck(mustRole = UserConstants.USER_ROLE_ADMIN)
    @PostMapping("/test/upload")
    //指定MultipartFile数据类型参数， 且指定接受表单里含有file的数据
    public BaseResponse<String> testUploadFile(@RequestPart("file")MultipartFile multipartFile) {
        //获取上传文件的原始文件名，方法返回的文件名是客户端提供的
        String filename = multipartFile.getOriginalFilename();
        //使用格式化来创建文件存储路径
        String filepath = String.format("/test/%s", filename);

        stringRedisTemplate.opsForValue().set(filepath, filename);

        //使用cos对象存储
        //引入cosManager
        //由于cosManager.putObject只接受File类型，而现在的类型为MultipartFile
        //需要转
        //使用File.createTempFile()创建一个空的临时文件
        //第一个参数是文件存储路径，第二个参数是后缀
        File file = null;
        try {
            file = File.createTempFile(filepath,null);
            //将multipartFile放到该临时文件中
            multipartFile.transferTo(file);
            //对象存储
            cosManager.putObject(filepath,file);
            //返回可访问的地址
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new RuntimeException(e);
        } finally {
            if (file != null) {
                //删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}" , filepath);
                }
            }
        }

    }

    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AutoCheck(mustRole = UserConstants.USER_ROLE_ADMIN)
    @GetMapping("/test/download/")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            //根据文件路径得到COSObject对象
            COSObject cosObject = cosManager.getObject(filepath);
            //拿到Object对象中的实际内容输入流
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            //缓存区需要刷新
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }

}
