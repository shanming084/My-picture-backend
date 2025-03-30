package com.shanming.mypicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.shanming.mypicturebackend.exception.BusinessException;
import com.shanming.mypicturebackend.exception.ErrorCode;
import com.shanming.mypicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * 文件url上传
 */
@Slf4j
@Service
public class PictureUrlUploadImpl extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        //校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR,"url为空");
        //校验URL格式
        try {
            //使用Spring自带的URL对象来校验URL格式
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式有误");
        }
        //校验URL的协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "仅支持HTTP或HTTPS协议的文件地址");
        HttpResponse httpResponse = null;
        try {
            //发送HEAD请求验证文件是否存在
            //创建一个请求头
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            //如果返回结果不是HTTP的ok，说明图片有问题
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK){
                return;
            }
            //文件存在，文件类型校验
            String contentType = httpResponse.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)){
                //允许的图片类型
                final List<String> ALLOW_CONTENT_TYPE = Arrays.asList("image/jpeg","image/jpg","image/webp", "image/png", "image/gif");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPE.contains(contentType.toLowerCase()),ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            //文件存在，文件大小校验
            String contentLengthStr = httpResponse.header("content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)){
                try {
                    //校验文件大小,不能大于两兆
                    long contentLengthLong = Long.parseLong(contentLengthStr);
                    final long ONE_MB = 1024 * 1024;
                    ThrowUtils.throwIf(contentLengthLong > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件不能超于两兆" );
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式异常");
                }
            }
        }
        finally {
            //如果这不为空则释放
            if (httpResponse != null){
                httpResponse.close();
            }
        }

    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }
}
