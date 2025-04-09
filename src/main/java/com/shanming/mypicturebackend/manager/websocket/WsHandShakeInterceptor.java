package com.shanming.mypicturebackend.manager.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.shanming.mypicturebackend.manager.auth.SpaceUserAuthManager;
import com.shanming.mypicturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.shanming.mypicturebackend.model.entity.Picture;
import com.shanming.mypicturebackend.model.entity.Space;
import com.shanming.mypicturebackend.model.entity.User;
import com.shanming.mypicturebackend.model.enums.SpaceTypeEnum;
import com.shanming.mypicturebackend.service.PictureService;
import com.shanming.mypicturebackend.service.SpaceService;
import com.shanming.mypicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * WebSocket拦截器
 */
@Component
@Slf4j
//实现HandshakeInterceptor
public class WsHandShakeInterceptor implements HandshakeInterceptor {
    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;
    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;

    public WsHandShakeInterceptor(UserService userService) {
        this.userService = userService;
    }

    /**
     * 握手前
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes Map键值对，往这个键值对传入map，相当于给接下来创建的会话设置了属性
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //获取当前用户
        if (request instanceof ServletServerHttpRequest){
            //将信息转化成httpSR
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            //从请求中获取图片id参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)){
                log.error("缺少图片参数，拒绝握手");
                return false;
            }
            //获取当前登录用户
            User loginUser = userService.getLoginUser(httpServletRequest);
            if (ObjUtil.isEmpty(loginUser)){
                log.error("用户未登录，拒绝握手");
                return false;
            }
            //校验用户是否有编辑当前图片的权限
            Picture picture = pictureService.getById(pictureId);
            if (ObjUtil.isEmpty(picture)){
                log.error("图片不存在，拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            if (spaceId != null){
                 space = spaceService.getById(spaceId);
                if (ObjUtil.isEmpty(space)){
                    log.error("空间不存在，拒绝握手");
                    return false;
                }
                //不是团队空间直接报错
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()){
                    log.error("图片所在空间不是团队空间，拒绝握手");
                    return false;
                }
            }
            //拿到用户对应的权限列表
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            //校验用户是否有编辑当前图片的权限
            //如果是团队空间，并且有编辑者权限，才能建立连接
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)){
                log.error("用户没有编辑图片的权利，拒绝握手");
                return false;
            }
            //设置用户登录信息等属性到WebSocket会话中
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
