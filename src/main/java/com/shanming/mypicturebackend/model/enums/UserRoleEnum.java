package com.shanming.mypicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String role;

    private final String description;

    UserRoleEnum(String description, String role) {
        this.description = description;
        this.role = role;
    }

    public static UserRoleEnum getUserRoleEnum(String role) {
        if (ObjUtil.isEmpty(role)) {
            return null;
        }
        for (UserRoleEnum e : UserRoleEnum.values()) {
            if (e.role.equals(role)) {
                return e;
            }
        }
        return null;
    }
}
