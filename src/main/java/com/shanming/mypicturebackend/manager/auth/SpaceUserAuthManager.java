package com.shanming.mypicturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.shanming.mypicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.shanming.mypicturebackend.manager.auth.model.SpaceUserRole;
import com.shanming.mypicturebackend.model.entity.Space;
import com.shanming.mypicturebackend.model.entity.SpaceUser;
import com.shanming.mypicturebackend.model.entity.User;
import com.shanming.mypicturebackend.model.enums.SpaceRoleEnum;
import com.shanming.mypicturebackend.model.enums.SpaceTypeEnum;
import com.shanming.mypicturebackend.service.SpaceService;
import com.shanming.mypicturebackend.service.SpaceUserService;
import com.shanming.mypicturebackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 空间成员权限控制
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    //配置文件对应的实体类
    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        //使用hutool的读取配置文件方法为json
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        //将json转换成实体类
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     * @param spaceUserRole
     * @return
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        //输入一个空间角色得到它的权限列表
        //如果输入为空，则返回空列表
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }
        //如果不为空则查找
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles()
                .stream()
                //将列表中等于角色的元素过滤出
                .filter(r -> r.getKey().equals(spaceUserRole))
                //找到过滤元素的第一个
                .findFirst()
                //如果找不到则返回空
                .orElse(null);

        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }

    /**
     * 获取权限列表
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }

}
