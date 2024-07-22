package com.tikchat.exception;

import com.tikchat.entity.enums.ResponseCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.saas.reptile.common.result.ResError;


/**
 * @author sed
 * @ClassName: BusinessException
 * @Description: 业务异常类, 使用场景:程序并未出现执行异常情况,人为抛出异常信息。
 * 例如：登录功能,账号不存在或密码错误时,可抛出一个业务异常,自定义异常信息
 * @date 2021-08-31
 */
//@Data
//public class BusinessException_backup extends Exception {
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 异常对应的返回码
     */
    private Integer code;

    /**
     * 异常对应的描述信息
     */
    private String message;

//    private Throwable throwable;

//
    public BusinessException(String message) {
        this.message = message;
    }
    public BusinessException(Integer code) {
        this.code = code;
    }
    public BusinessException(ResponseCodeEnum responseCodeEnum) {
        this.code = responseCodeEnum.getCode();
        this.message = responseCodeEnum.getMsg();
    }
    public BusinessException(ResponseCodeEnum responseCodeEnum,String message) {
        this.code = responseCodeEnum.getCode();
        this.message = message;
    }


    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }



//    public BusinessException(String message, Throwable cause) {
//        this.message = String.format("%s %s", message, cause.getMessage());
//    }

//    public BusinessException(int code, String message, Throwable throwable) {
//        super(message);
//        this.code = code;
//        this.message = message;
//        this.throwable = throwable;
//    }

//    public BusinessException(ResError resError) {
//        this(resError.getCode(), resError.getMessage(), null);
//    }
//
//    public BusinessException(ResError resError, Throwable throwable) {
//        this(resError.getCode(), resError.getMessage(), throwable);
//    }
//
//    public BusinessException(ResError resError, Object... args) {
//        super(resError.getMessage());
//        String message = resError.getMessage();
//        try {
//            message =
//                    String.format("%s %s", resError.getMessage(), OBJECT_MAPPER.writeValueAsString(args));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        this.message = message;
//        this.code = resError.getCode();
//    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

//    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}