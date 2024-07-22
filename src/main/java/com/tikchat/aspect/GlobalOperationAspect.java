package com.tikchat.aspect;


import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.enums.ResponseCodeEnum;
import com.tikchat.exception.BusinessException;
import com.tikchat.redis.RedisUtils;
import com.tikchat.utils.StringTools;

//import org.aspectj.apache.bcel.classfile.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @FileName GlobalOperationAspect
 * @Description 全局的切面
 * @Author Paprika
 * @date 编写时间 2024-06-26 03:59
 **/


@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {
    @Resource
    private RedisUtils redisUtils;

    private static final Logger logger = LoggerFactory.getLogger(GlobalOperationAspect.class);

    @Before("@annotation(com.tikchat.annotation.GlobalInterceptor)")
    public void interceptorDo(JoinPoint point){//参数是切面
        try {
            //point.getSignature() 提供了关于方法签名的信息，如方法名、参数类型等。
            Method method = ((MethodSignature)point.getSignature()).getMethod();
            //通过 getMethod() 方法从 MethodSignature 中获取实际的 java.lang.reflect.Method 对象，即被拦截的方法本身


            //使用 getMethod() 返回的 Method 对象调用 getAnnotation(GlobalInterceptor.class) 方法，尝试获取 GlobalInterceptor 注解的实例。
            //如果 method 方法上没有 GlobalInterceptor 注解，interceptor 将为 null。
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            //但是实际上以下3行是不用写的  因为我们本方法前面已经写了annotation 意思是有这个注解的方法才会执行我们这个方法
            //所以执行我们这个方法的肯定是有这个注解的没必要判断了
            if (interceptor == null) {
                return;
            }
            if (interceptor.checkLogin() || interceptor.checkAdmin()){
                checkLogin(interceptor.checkAdmin());
                /**
                 * 牛逼！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                 * //?问这个是怎么赋值的？ interceptor.checkAdmin()的值怎么来的
                 * //答案揭晓：我们的GlobalInterceptor写了两个方法  一个值是true 一个是false
                 * // 然后拦截器会获取interceptor.checkLogin() 和interceptor.checkAdmin()的值
                 * //我们已经设好了默认值  所以在controller里面的方法前写注解时还可以设上面两个和interceptor的值 没设就是默认值
                 * //所以我们需要使用checkLogin(true);的话 那就需要写注解@GlobalInterceptor(checkAdmin = true)
                 */
            }
        }catch (BusinessException e){
            logger.error("全局拦截异常",e);
            throw e;

        //这样的话就不会出现在网页中看到报错信息的问题了 很丢人
        //服务器报错问题只会响应为500
        }catch (Exception e){
            logger.error("全局拦截异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500.getCode());
//            e.printStackTrace();
        }catch (Throwable e){
            logger.error("全局拦截异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500.getCode());
        }

    }

    //登陆拦截的具体实现
    private void checkLogin(Boolean checkAdmin) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req =  attributes.getRequest();
        String token = req.getHeader("token");
        if (StringTools.isEmpty(token)){
            throw new BusinessException(ResponseCodeEnum.CODE_901.getCode());
        }
        //看请求头里有没有token还有是不是对的token 如果token不存在那么就抛出901的异常
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+token);
        if (tokenUserInfoDto == null){
            throw new BusinessException(ResponseCodeEnum.CODE_901.getCode());
        }


        /**
         * //这个很重要  这3行代码就直接写完了管理员权限的判断！！！是嵌套在了checkLogin里面了 因为管理员判断之前本来就要先判断登陆
         * //如果checkAdmin那么再看一下UserInfo是不是admin  如果都对才能通过（通过的话就是什么都不做） 不通过的话就会throw new
         */
        if (checkAdmin && !tokenUserInfoDto.getAdmin()){
            throw new BusinessException(ResponseCodeEnum.CODE_404.getCode());
        }

    }

}
