package com.shanming.mypicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shanming.mypicturebackend.model.dto.picture.PictureQueryRequest;
import com.shanming.mypicturebackend.model.dto.picture.PictureReviewRequest;
import com.shanming.mypicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.shanming.mypicturebackend.model.dto.picture.PictureUploadRequest;
import com.shanming.mypicturebackend.model.entity.Picture;
import com.shanming.mypicturebackend.model.entity.User;
import com.shanming.mypicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author LEG
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-26 15:51:37
*/
public interface PictureService extends IService<Picture> {


    /**
     * 批量抓取图片
     * @param pictureUploadByBatchRequest 图片批量抓取请求体
     * @param loginUser 登录用户
     * @return 成功的抓取数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 封装图片查询请求体对应的查询条件
     * @param pictureQueryRequest 图片查询请求体
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 校验图片是否合规
     * @param picture 图片
     */
    void validPicture(Picture picture);

    /**
     * 上传图片
     * @param inputSource 文件源
     * @param pictureUploadRequest 图片上传请求体
     * @param loginUser 登录用户
     * @return 脱敏图片信息
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 拿到保存了用户信息的脱敏图片信息（单条）
     * @param picture 图片
     * @param request 请求
     * @return 脱敏用户信息
     */
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 拿到保存了用户信息的脱敏图片信息(分页)
     * @param picturePage 分页图片
     * @param request 请求
     * @return 脱敏用户信息
     */
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);


    /**
     * 图片审核
     * @param pictureReviewRequest 图片审核请求体
     * @param loginUser 正在使用功能的用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);
}
