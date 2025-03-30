package com.shanming.mypicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求
 */
@Data
public class UserRegisterRequest implements Serializable {


    private static final long serialVersionUID = -1785257758446013196L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;
}
