package com.tikchat.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @FileName GlobalInterceptor
 * @Description 全局拦截器
 * @Author Paprika
 * @date 编写时间 2024-06-26 03:59
 **/

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInterceptor {
    //这是一个注解
    // 登陆拦截  但是注意不要用在注册和登陆上
    boolean checkLogin() default true;//默认都是需要的  只有少数几个不需要 即登陆 注册等等 这些我们专门设为false就行

    boolean checkAdmin() default false;//校验是不是管理员  有些操作只能管理员做

}
