package com.tikchat.controller;

import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.enums.ResponseCodeEnum;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.exception.BusinessException;
import com.tikchat.redis.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @FileName ABaseControllertest
 * @Description 前端返回信息体
 * @Author Paprika
 * @date 编写时间 2024-06-23 03:59
 **/

public class ABaseControllertest {

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    @Resource
    private RedisUtils redisUtils;

    // 在后面的具体controller里面 如果成功获取则调用这个baseVO
    // T t就是数据体  存放map就行  new HashMap<>();  里面存放ket value值就行

    protected <T> ResponseVO getSuccessResponseVONullData() {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(null);
        return responseVO;
    }

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t) {
        ResponseVO vo = new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        if (e.getCode() == null) {
            vo.setCode(ResponseCodeEnum.CODE_600.getCode());
        } else {
            vo.setCode(e.getCode());
        }
        vo.setInfo(e.getMessage());
        vo.setData(t);
        return vo;
    }

    protected <T> ResponseVO getServerErrorResponseVO(T t){
        ResponseVO vo=new ResponseVO();
        vo.setStatus(STATUC_ERROR);
        vo.setCode(ResponseCodeEnum.CODE_500.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        vo.setData(t);
        return vo;
    }

    protected TokenUserInfoDto getTokenUserInfoDto(HttpServletRequest req){
        String token = req.getHeader("token");
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+token);
        return tokenUserInfoDto;
    }

}
