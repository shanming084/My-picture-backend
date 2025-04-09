package com.shanming.mypicturebackend.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shanming.mypicturebackend.model.dto.space.SpaceAddRequest;
import com.shanming.mypicturebackend.model.dto.space.SpaceQueryRequest;
import com.shanming.mypicturebackend.model.entity.Space;
import com.shanming.mypicturebackend.model.entity.User;
import com.shanming.mypicturebackend.model.vo.SpaceUserVO;
import com.shanming.mypicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author LEG
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-03-31 13:00:12
*/
public interface SpaceService extends IService<Space> {
    /**
     * 封装空间查询请求体对应的查询条件
     * @param spaceQueryRequest 空间查询请求体
     * @return 查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 校验空间是否合规
     * @param space 空间
     * @param add 是否在创建时校验，因为更新和添加的校验逻辑不一致
     *            更新时不需要校验空间名称
     */
    void validSpace(Space space, Boolean add);

    /**
     * 拿到保存了用户信息的脱敏空间信息（单条）
     * @param space 空间
     * @param request 请求
     * @return 脱敏用户信息
     */
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 拿到保存了用户信息的脱敏空间信息(分页)
     * @param spacePage 分页空间
     * @param request 请求
     * @return 脱敏用户信息
     */
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    /**
     * 根据用户的空间级别填充空间容量和空间条数
     * @param space 空间
     */
    public void fillSpaceBySpaceLevel(Space space);

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);
}
