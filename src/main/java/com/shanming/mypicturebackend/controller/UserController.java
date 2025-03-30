package com.shanming.mypicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shanming.mypicturebackend.annotation.AutoCheck;
import com.shanming.mypicturebackend.common.BaseResponse;
import com.shanming.mypicturebackend.common.DeleteRequest;
import com.shanming.mypicturebackend.common.ResultUtils;
import com.shanming.mypicturebackend.constants.UserConstants;
import com.shanming.mypicturebackend.exception.BusinessException;
import com.shanming.mypicturebackend.exception.ErrorCode;
import com.shanming.mypicturebackend.exception.ThrowUtils;
import com.shanming.mypicturebackend.model.dto.user.*;
import com.shanming.mypicturebackend.model.entity.User;
import com.shanming.mypicturebackend.model.enums.UserRoleEnum;
import com.shanming.mypicturebackend.model.vo.LoginUserVO;
import com.shanming.mypicturebackend.model.vo.UserVO;
import com.shanming.mypicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/user")
@ResponseBody
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/list/page/vo")
    @AutoCheck(mustRole = UserConstants.USER_ROLE_ADMIN)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
        ThrowUtils.throwIf(userQueryRequest == null ,ErrorCode.PARAMS_ERROR);
        //得到请求体里的当前页号和页面大小
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        //使用service的page()创建页面，第一个参数是page对象，第二个参数是查询条件
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        //新建一个脱敏后的Page对象，输入当前页号，页面大小，和页面的总条数
        Page<UserVO> userVOPage = new Page<UserVO>(current, pageSize,userPage.getTotal());
        //使用getRecords拿到用户的所有页面的对象的列表
        //然后将列表拿去脱敏
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        //将脱敏后的列表填充到脱敏用户页面中
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 添加用户
     */
    @PostMapping("/add")
    @AutoCheck(mustRole = UserConstants.USER_ROLE_ADMIN)
    public BaseResponse<Long> userAdd(@RequestBody UserAddRequest userAddRequest) {
        //校验是否为空
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR, "添加的用户为空");
        //将值取出
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        //由于请求体里没有密码，所以只能用默认密码
        String defaultPassword = UserConstants.USER_DEFAULT_PASSWORD;
        String encryptPassword = userService.getEncryptPassword(defaultPassword);
        user.setUserPassword(encryptPassword);
        //添加到数据库中
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AutoCheck(mustRole = UserConstants.USER_ROLE_ADMIN)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AutoCheck(mustRole = UserConstants.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @AutoCheck(mustRole = UserConstants.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 注销用户
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        //将返回的值返回
        return ResultUtils.success(result);
    }



    /**
     * 得到用户登录态
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        //将返回的值返回
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }


    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        //校验是否为空
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "用户登录信息为空");
        //将值取出
        String userPassword = userLoginRequest.getUserPassword();
        String userAccount = userLoginRequest.getUserAccount();
        //调用方法
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        //将返回的值返回
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @AutoCheck(mustRole = "admin")
    @ResponseBody
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        //校验是否为空
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR, "用户注册信息为空");
        //将值取出
        String userPassword = userRegisterRequest.getUserPassword();
        String userAccount = userRegisterRequest.getUserAccount();
        String checkPassword = userRegisterRequest.getCheckPassword();
        //调用方法
        long userId = userService.userRegister(userAccount, userPassword, checkPassword);
        //将返回的值返回
        return ResultUtils.success(userId);
    }


}
