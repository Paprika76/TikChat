package com.tikchat.controller;

import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.MessageSendDto;
import com.tikchat.entity.dto.SysSettingDto;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.entity.vo.UserInfoVO;
import com.tikchat.exception.BusinessException;
import com.tikchat.redis.RedisComponent;
import com.tikchat.redis.RedisUtils;
import com.tikchat.service.UserInfoService;
import com.tikchat.utils.StringTools;
import com.tikchat.websocket.MessageHandler;
import com.wf.captcha.ArithmeticCaptcha;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
//import java.util.logging.Logger;
import org.slf4j.Logger;


/**
 * @FileName AccountController
 * @Description 账号控制器
 * @Author Paprika
 * @date
 **/


@RestController("accountController")
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseControllertest{
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    //生成验证码及其答案
    @RequestMapping("/checkCode")
    public ResponseVO checkCode(){
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        logger.info("验证码是："+code);

//        redisUtils.setex(checkCodeKey,code , 60000);
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code , Constants.REDIS_TIME_1MIN);

        String checkCodeBase64 = captcha.toBase64();
        Map<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);//用于获取验证码答案的token!!!!!!
        return getSuccessResponseVO(result);

    }

    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                               @NotEmpty String nickName,
                               @NotEmpty String checkCode){
        //注意以上的注解：@NotEmpty和@NotNull的区别：
        //@NotEmpty是既判断是不是  ""  又判断是不是null
        //而@NotNull则只判断是不是null
        //但是我们的需求是：不能为  空：“”  也不能为null
        try {
            if(checkCode.equals((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))){
                //大量数据库操作逻辑在这 service层
                userInfoService.register(email,nickName,password);
            }else {
//                return
                // 这样就不会执行try中后面的代码 而是直接执行 catch{}  和finally{} 然后执行try后面的代码
//                throw new BusinessException("图片验证码不正确");//是不是因为在try里面所以throw Exception也行
                throw new BusinessException("图片验证码不正确");//是不是因为在try里面所以throw Exception也行
            }
        }
        finally {
            redisUtils.del(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }

//        return getSuccessResponseVO(null);//这里为什么返回空？？？？？？？
        return getSuccessResponseVONullData();//这是我自己写的用于返回null数据的但是有code200 msg的
    }

    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String checkCode){
        password = StringTools.encodeMd5(password);

        //注意以上的注解：@NotEmpty和@NotNull的区别：
        //@NotEmpty是既判断是不是  ""  又判断是不是null
        //而@NotNull则只判断是不是null
        //但是我们的需求是：不能为  空：“”  也不能为null
        try {
            if(!checkCode.equals((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey))){
                throw new BusinessException("验证码不正确！");
            }else {
                //大量数据库操作逻辑在这 service层
                UserInfoVO userInfoVO = userInfoService.login(email, password);

                /**
                 * 注意这里普及一下 po dto vo的区别
                 * po：  持久化对象 Persistent Object
                 * dto： Data Transfer Object（数据传输对象）
                 * vo：  Value Object（值对象）
                 * po就是entity pojo 直接与数据库中相应表一样的数据字段属性
                 * dto是用于service服务层基于po生成的其他有点不一样的使用了业务逻辑的数据
                 * ---------注意dto也经常在service中再次扩展为vo再返回给controller
                 * ---------一般dto转换为vo的代码就是写在service层！！！！都是写在service层中！！！！！
                 * -------只有极少数情况写在controller中  而且也还是不推荐的！！！！ 即：特定于控制器的转换逻辑
                 * vo是用于controller返回一个http response返回值给前端的
                 * vo包含了一个重要data对象 这个对象根据前端需要要改变
                 * vo还包含code msg判断这个response的状况如何
                 * 例如本login需要返回一个vo  其中的data我们可以返回为null也可以返回一个携带很多数据的data对象
                 * 这里我们需要返回很多数据 比如token 是否是管理员 联系人有多少 群组有多少等等信息
                 */

                return getSuccessResponseVO(userInfoVO);
            }
        }
        finally {
            redisUtils.del(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
        }
    }

    @RequestMapping("/getSysSetting")
    public ResponseVO getSysSetting(){
        SysSettingDto sysSetting = redisComponent.getSysSetting();

        return getSuccessResponseVO(sysSetting);

    }

    @RequestMapping("/test")
    public ResponseVO test(){
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageContent("haahah"+System.currentTimeMillis());
        messageHandler.sendMessage(messageSendDto);
        return getSuccessResponseVO(null);

    }


}
