package com.tikchat.controller;


import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.entity.UserInfo;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.entity.vo.UserInfoVO;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.UserContactMapper;
import com.tikchat.mapper.UserInfoMapper;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.UserContactService;
import com.tikchat.service.UserInfoService;
import com.tikchat.service.component.ServiceComponent;
import com.tikchat.utils.CopyTools;
import com.tikchat.utils.StringTools;
import com.tikchat.websocket.ChannelContextUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.IOException;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author Paprika
 * @since 2024-06-22
 */
@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseControllertest{
    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ServiceComponent serviceComponent;

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    public ResponseVO getUserInfo(HttpServletRequest req){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        String userId = tokenUserInfoDto.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
        return getSuccessResponseVO(userInfoVO);
    }


    // 首次编写时间 2024-06-29 04:14
    @RequestMapping("/saveUserInfo")
    @GlobalInterceptor
    public ResponseVO saveUserInfo(HttpServletRequest req,
                                   @NotNull UserInfo userInfo,
                                   MultipartFile avatarFile,
                                   MultipartFile avatarCover) throws IOException {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setPassword(null);
        userInfo.setCreateTime(null);
        userInfo.setUpdateTime(null);
        userInfo.setVersion(null);
        userInfo.setStatus(null);
        userInfo.setDeleted(null);

        this.userInfoService.updateUserInfo(userInfo,avatarFile,avatarCover);

//        return getSuccessResponseVO(userInfo);
        return getUserInfo(req);/*这里很巧妙！！！！！！
        save完了后要立即刷新看更新后的样子 所以就直接返回更新后的再次搜索的结果*/
    }

    // 首次编写时间 2024-06-29 04:14
    @RequestMapping("/updatePassword")
    @GlobalInterceptor
    public ResponseVO updatePassword(HttpServletRequest req,
                                   @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String password){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        /*问题又来了  到底是先查还是直接new一个空的类 然后只更新password？
        * 像有的时候完全没必要查了再更新  那就直接new就行
        * 但是我觉得这里有点问题  改密码这么容易吗 不要经过多层校验的吗？  所以我觉得应该是要先查一下再写入的*/
        UserInfo userInfo = userInfoMapper.selectById(tokenUserInfoDto.getUserId());
        /*我觉得要先查一下再写入  所以这个注释掉*/
//        UserInfo userInfo = new UserInfo();
        if(userInfo==null){
            throw new BusinessException("校验用户失败，暂时无法更改密码");
        }
        userInfo.setPassword(StringTools.encodeMd5(password));
        this.userInfoMapper.updateById(userInfo);
        //TODO 已完成 强制退出，重新登录
        channelContextUtils.setChannelOffLine(tokenUserInfoDto.getUserId());

        return getSuccessResponseVO(null);
    }

    // 首次编写时间 2024-06-29 04:14
    @RequestMapping("/logout")
    @GlobalInterceptor
    public ResponseVO logout(HttpServletRequest req){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        //TODO 退出登录 关闭ws连接  不用通知用户说你被强制下线！！！！！！
        channelContextUtils.setChannelOffLine(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }



}

