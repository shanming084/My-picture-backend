package com.shanming.mypicturebackend.aop;

import com.shanming.mypicturebackend.annotation.AutoCheck;
import com.shanming.mypicturebackend.exception.BusinessException;
import com.shanming.mypicturebackend.exception.ErrorCode;
import com.shanming.mypicturebackend.model.entity.User;
import com.shanming.mypicturebackend.model.enums.UserRoleEnum;
import com.shanming.mypicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

//标识这个类是切面
@Aspect
//加入IOC
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    //表达式一般是方法的所有参数，但这里是注解
    @Around("@annotation(autoCheck)")
    //joinPoint是使用了反射机制的切点,将注解类也输入
    public Object doIntercept(ProceedingJoinPoint joinPoint, AutoCheck autoCheck) throws Throwable {

        //将注解类的方法调用，得到注解方法得到的用户角色
        String mustRole = autoCheck.mustRole();
        //拿到请求对象
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        //将这个请求转换成httpServlet
        ServletRequestAttributes requestAttributes1 = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = requestAttributes1.getRequest();
        //使用mapper拿到登录用户
        User loginUser = userService.getLoginUser(request);
        //对该登录用户的角色遍历用户角色枚举类
        UserRoleEnum mustRoleEnum = UserRoleEnum.getUserRoleEnum(mustRole);
        //如果不需要权限，放行
        if (mustRoleEnum == null){
            return joinPoint.proceed();
        }
        //以下的权限，必须有权限，才会通过
        UserRoleEnum userRoleEnum = UserRoleEnum.getUserRoleEnum(loginUser.getUserRole());
        if (userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //必须是管理员权限但用户不等于管理员
        if (mustRoleEnum.equals(UserRoleEnum.ADMIN) && !userRoleEnum.equals(UserRoleEnum.ADMIN)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "不是管理员");
        }
        return joinPoint.proceed();
    }
}
