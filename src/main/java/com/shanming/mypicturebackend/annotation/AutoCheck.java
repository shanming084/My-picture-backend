package com.shanming.mypicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//注解生效目标：方法
@Target(ElementType.METHOD)
//注解生效时期：运行期
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoCheck {

    /**
     * 必须具有某个角色
     */
    String mustRole() default "";
}
