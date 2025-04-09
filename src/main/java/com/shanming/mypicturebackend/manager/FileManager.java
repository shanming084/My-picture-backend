package com.shanming.mypicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.shanming.mypicturebackend.config.CosClientConfig;
import com.shanming.mypicturebackend.exception.BusinessException;
import com.shanming.mypicturebackend.exception.ErrorCode;
import com.shanming.mypicturebackend.exception.ThrowUtils;
import com.shanming.mypicturebackend.model.dto.picture.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     * 上传文件通用方法，输入需要上传的文件和前缀
     * 具体的文件名称可以通过解析而来
     * @param multipartFile 上传的文件
     * @param uploadPathPrefix 上传文件的文件路径前缀
     * @return 文件上传封装体
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //校验文件,单独写一个方法
        validPicture(multipartFile);
        //文件上传地址

        //为了不让上传的文件重名，生成16位随机数前缀
        String uuid = RandomUtil.randomString(16);
        //拿到文件的名称
        String originalFilename = multipartFile.getOriginalFilename();
        //为了更好的区分文件，所以添加时间戳前缀
        //只需要文件的后缀而不需要文件的名称，因为这可能导致url混乱
        String uploadFilename = String.format("%s.%s.%s", DateUtil.formatDate(new Date())
        , uuid, FileUtil.getSuffix(originalFilename));

        //前面得到的uploadFilename是文件完整名称
        //但我们需要的是用户指定的文件路径 + 文件完整名称,用户可能需要放入文件夹
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFilename);
        //上传文件
        //使用cos对象存储
        //引入cosManager
        //由于cosManager.putObject只接受File类型，而现在的类型为MultipartFile
        //需要转
        //使用File.createTempFile()创建一个空的临时文件
        //第一个参数是文件存储路径，第二个参数是后缀
        File file = null;
        try {
            file = File.createTempFile(uploadPath,null);
            //将multipartFile放到该临时文件中
            multipartFile.transferTo(file);
            //对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //取出对象的图片信息进行校验
            ImageInfo imageInfo = putObjectResult
                    .getCiUploadResult()
                    .getOriginalInfo()
                    .getImageInfo();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            //NumberUtil.round()四舍五入。第一个参数是需要四舍五入的数字，第二个参数是小数点之后的几位数
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight ,2).doubleValue();

            //封装上传图片响应体
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            //url 就是 存储桶域名 + / + 文件完整名称
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            //从原始文件中取名
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            //从临时文件中取大小
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());

            //返回上传图片响应体
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("file upload error, filepath = " + uploadPath, e);
            throw new RuntimeException(e);
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 通过url上传文件，输入需要上传的文件和前缀
     * @param fileUrl 上传的文件url
     * @param uploadPathPrefix 上传文件的文件路径前缀
     * @return 文件上传封装体
     */
    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
        //校验文件,单独写一个方法
        validPicture(fileUrl);
        //文件上传地址

        //为了不让上传的文件重名，生成16位随机数前缀
        String uuid = RandomUtil.randomString(16);
        //拿到文件的名称
        String originalFilename = FileUtil.mainName(fileUrl);
        //为了更好的区分文件，所以添加时间戳前缀
        //只需要文件的后缀而不需要文件的名称，因为这可能导致url混乱
        String uploadFilename = String.format("%s.%s.%s", DateUtil.formatDate(new Date())
                , uuid, FileUtil.getSuffix(originalFilename));

        //前面得到的uploadFilename是文件完整名称
        //但我们需要的是用户指定的文件路径 + 文件完整名称,用户可能需要放入文件夹
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFilename);
        //上传文件
        //使用cos对象存储
        //引入cosManager
        //由于cosManager.putObject只接受File类型，而现在的类型为MultipartFile
        //需要转
        //使用File.createTempFile()创建一个空的临时文件
        //第一个参数是文件存储路径，第二个参数是后缀
        File file = null;
        try {
            file = File.createTempFile(uploadPath,null);
            //根据路径下载文件并传送到另一个文件中
            HttpUtil.downloadFile(fileUrl, file);
            //对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //取出对象的图片信息进行校验
            ImageInfo imageInfo = putObjectResult
                    .getCiUploadResult()
                    .getOriginalInfo()
                    .getImageInfo();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            //NumberUtil.round()四舍五入。第一个参数是需要四舍五入的数字，第二个参数是小数点之后的几位数
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight ,2).doubleValue();

            //封装上传图片响应体
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            //url 就是 存储桶域名 + / + 文件完整名称
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            //从原始文件中取名
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            //从临时文件中取大小
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());

            //返回上传图片响应体
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("file upload error, filepath = " + uploadPath, e);
            throw new RuntimeException(e);
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 校验文件url，如果不符合条件就抛出异常
     * @param fileUrl 文件url
     */
    private void validPicture(String fileUrl) {
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

    /**
     * 删除临时文件
     * @param file 临时文件
     */
    private static void deleteTempFile(File file) {
        if (file != null) {
            //删除临时文件
            boolean deleteResult = file.delete();
            if (!deleteResult) {
                //获取它的绝对路径
                log.error("file delete error, filepath = {}" , file.getAbsoluteFile());
            }
        }
    }

    /**
     * 校验文件，如果不符合条件就抛出异常
     * @param multipartFile 文件
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不存在");
        //校验文件大小,不能大于两兆
        long fileSize = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(fileSize > 2 * ONE_MB, ErrorCode.PARAMS_ERROR, "文件不能超于两兆" );
        //校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //定义允许上线的文件后缀列表(或者集合)
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "jpeg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }
}
