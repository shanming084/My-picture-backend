package com.shanming.mypicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanming.mypicturebackend.exception.BusinessException;
import com.shanming.mypicturebackend.exception.ErrorCode;
import com.shanming.mypicturebackend.exception.ThrowUtils;
import com.shanming.mypicturebackend.manager.FileManager;
import com.shanming.mypicturebackend.manager.upload.PictureUploadImpl;
import com.shanming.mypicturebackend.manager.upload.PictureUploadTemplate;
import com.shanming.mypicturebackend.manager.upload.PictureUrlUploadImpl;
import com.shanming.mypicturebackend.mapper.UserMapper;
import com.shanming.mypicturebackend.model.dto.file.UploadPictureResult;
import com.shanming.mypicturebackend.model.dto.picture.*;
import com.shanming.mypicturebackend.model.entity.Picture;
import com.shanming.mypicturebackend.model.entity.User;
import com.shanming.mypicturebackend.model.enums.PictureReviewStatusEnum;
import com.shanming.mypicturebackend.model.vo.PictureVO;
import com.shanming.mypicturebackend.model.vo.UserVO;
import com.shanming.mypicturebackend.service.PictureService;
import com.shanming.mypicturebackend.mapper.PictureMapper;
import com.shanming.mypicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author LEG
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-03-26 15:51:37
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PictureUploadImpl pictureUpload;

    @Resource
    private PictureUrlUploadImpl pictureUrlUpload;

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        //校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer searchNum = pictureUploadByBatchRequest.getSearchNum();
        ThrowUtils.throwIf(searchNum > 30, ErrorCode.PARAMS_ERROR, "抓取数量不能大于30条");
        //抓取内容
        //抓取地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        //使用jsoup， 连接这个地址，获取文档
        Document document = null;
        try {
             document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        //解析内容
        //jsoup等于获取非常全面的HTML文档
        //获取其外层元素
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isEmpty(div)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        //从外层元素中获取所有的图片
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
          //遍历元素,依次处理上传图片
            //将每一个图片元素中的地址取出
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过:{}", fileUrl);
                continue;
            }
            //处理图片的地址，防止转义或者和对象存储冲突的问题
            //当在地址中遇到问号时将左边的地址单独拿出，这就是需要的地址
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            //将地址上传
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);

            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功 id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= searchNum) {
                break;
            }

            return 0;
        }
        //上传图片
        return 0;
    }

    /**
     * 封装图片查询请求体对应的查询条件
     * @param pictureQueryRequest 图片查询请求体
     * @return 查询条件
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewMessage), "reviewMessage", reviewMessage);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }



    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "未登录");
        //判断新增还是删除
        //通过id来判断图片是新增还是删除
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        //如果是更新
        if (pictureId != null) {
            //根据id将图片查询出
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NO_AUTH_ERROR, "未登录");
            //仅本人或管理员可编辑图片
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
//            boolean exists = this.lambdaQuery()
//                    .eq(Picture::getId, pictureId)
//                    .exists();
//            ThrowUtils.throwIf(!exists, ErrorCode.PARAMS_ERROR, "图片不存在");
        }
        //上传图片，查询图片信息
        //查询用户id划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        //根据inputSource的类型区分上传方式
        //默认它是url上传
        PictureUploadTemplate pictureUploadTemplate = pictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = pictureUrlUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        //构造要入库的图片信息
        Picture picture = new Picture();
        picture.setId(pictureId);
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        //补充审核参数
        this.fillReviewParams(picture,loginUser);
        //操作数据库
        //如果pictureId不为空，表示更新，否则是新增
        if (picture != null){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "操作数据库失败");
        //将图片转换成脱敏图片
        return PictureVO.objToVo(picture);
    }

    /**
     * 为图片填充审核参数
     * @param picture 图片
     * @param loginUser 用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            //管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            picture.setReviewMessage("管理员自动过审");
        }else{
            //非管理员，无论是编辑还是创建默认都是待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 拿到保存了用户信息的脱敏图片信息
     * @param picture 图片
     * @param request 请求
     * @return 脱敏图片信息
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        //如果用户id合法，将脱敏用户信息放到脱敏图片信息中
        Long userId = picture.getUserId();
        if(userId != null && userId > 0){
            User user = userMapper.selectById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        //将传入的图片分页列表全部取出
        List<Picture> pictureList = picturePage.getRecords();
        //创建一个Page类，其用传入的Page的当前页面，Page的大小，全部页面大小
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        //遍历分页列表将它们逐一调用转换成脱敏图片对象
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        //关联查询用户

        //将传入的图片列表中，所有相关的用户提取成不重复的Set
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        //根据用户Set，查询对应的用户列表，并且分组形成Map
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));

        //遍历所有脱敏图片
        pictureVOList.forEach(pictureVO -> {
            //拿到所有脱敏图片信息中关联的用户
            Long userId = pictureVO.getUserId();
            //设置User为空
            User user = null;
            //如果Map中存在关联的用户id，则拿到Map中该用户id该用户列表的第一个用户
            //并赋值到User
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            //将其脱敏并放到脱敏图片信息中
            pictureVO.setUser(userService.getUserVO(user));
        });
        //将脱敏图片列表放到脱敏图片分页中
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        //校验审核请求体里的参数
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getPictureReviewStatus(reviewStatus);
        if (id == null || reviewStatus == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断图片是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //校验审核状态是否重复
        //如果状态一致则异常
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请不要重复审核");
        }
        //数据库操作
        Picture picture = new Picture();
        //将审核信息传入新建的Picture对象
        BeanUtils.copyProperties(pictureReviewRequest, picture);
        //将使用该功能的用户id塞入Picture
        picture.setId(loginUser.getId());
        //当前时间
        picture.setReviewTime(new Date());
        //使用service的修改
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }
}




