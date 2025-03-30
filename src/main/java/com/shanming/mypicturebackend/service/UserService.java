package com.shanming.mypicturebackend.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanming.mypicturebackend.exception.BusinessException;
import com.shanming.mypicturebackend.exception.ErrorCode;
import com.shanming.mypicturebackend.model.dto.user.UserQueryRequest;
import com.shanming.mypicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shanming.mypicturebackend.model.vo.LoginUserVO;
import com.shanming.mypicturebackend.model.vo.UserVO;
import org.springframework.beans.BeanUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author LEG
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-03-24 17:54:47
*/
public interface UserService extends IService<User> {

    /**
     * 用户注销
     * @param request 请求
     * @return 用户信息
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword,String checkPassword);

    /**
     * 获得脱敏后的登录用户信息
     * @param user 用户信息
     * @return 脱敏用户信息
     */
    public LoginUserVO getLoginUserVO(User user);

    /**
     * 密码加密
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);


    /**
     * 得到当前登录用户
     * @param request 请求
     * @return 当前用户
     */
    public User getLoginUser(HttpServletRequest request);

    /**
     * 获得脱敏后的用户信息
     * @param user 用户信息
     * @return 脱敏用户信息
     */
    public UserVO getUserVO(User user);

    /**
     * 获得脱敏后的用户信息列表
     * @param userList 用户信息列表
     * @return 脱敏用户信息列表
     */
    public List<UserVO> getUserVOList(List<User> userList);


    /**
     * 封装用户查询请求体对应的查询条件
     * @param userQueryRequest 用户查询请求体
     * @return 查询条件
     */
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);


    /**
     * 判断用户是否是管理员
     * @param user 用户
     * @return 是否是
     */
    boolean isAdmin(User user);
}
