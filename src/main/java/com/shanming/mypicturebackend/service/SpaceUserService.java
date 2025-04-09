package com.shanming.mypicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanming.mypicturebackend.model.dto.space.SpaceAddRequest;
import com.shanming.mypicturebackend.model.dto.space.SpaceQueryRequest;
import com.shanming.mypicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.shanming.mypicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.shanming.mypicturebackend.model.entity.Space;
import com.shanming.mypicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shanming.mypicturebackend.model.entity.User;
import com.shanming.mypicturebackend.model.vo.SpaceUserVO;
import com.shanming.mypicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author LEG
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-04-04 19:49:50
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 封装空间查询请求体对应的查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 校验空间成员是否合规
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取空间成员包装类（单条）
     */
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员包装类(列表)
     */
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUsers);

    /**
     * 创建空间成员
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

}
