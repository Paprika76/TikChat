package com.tikchat.controller.admin;


import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.controller.ABaseControllertest;
import com.tikchat.entity.GroupInfo;
import com.tikchat.entity.enums.ResponseCodeEnum;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.GroupInfoMapper;
import com.tikchat.mapper.UserContactMapper;
import com.tikchat.mapper.UserInfoBeautyMapper;
import com.tikchat.mapper.UserInfoMapper;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.GroupInfoService;
import com.tikchat.service.UserContactService;
import com.tikchat.service.UserInfoBeautyService;
import com.tikchat.service.UserInfoService;
import com.tikchat.service.component.ServiceComponent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @FileName AdminGroupController
 * @Description 管理员群组管理
 * @Author Paprika
 * @date 编写时间 2024-06-30 23:11
 **/

@RestController("adminGroupController")
@RequestMapping("/admin")
@Validated
public class AdminGroupController extends ABaseControllertest {
    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserInfoBeautyService userInfoBeautyService;

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserInfoBeautyMapper userInfoBeautyMapper;

    @Resource
    private ServiceComponent serviceComponent;

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserContactMapper userContactMapper;

    //  编写时间 2024-06-30 04:26
    @RequestMapping("/loadGroupList")
    @GlobalInterceptor(checkAdmin = true)
    /*注意这里写了一个自动的参数  老罗没有*/
    public ResponseVO loadGroupList(Integer pageNo){
        List<Map<String, Object>> maps = groupInfoMapper.selectGroupList();

        // TODO 分页的后面再写 我已经基本摸清了老罗的写法了 关键就是这个map的用法要好好学学

        return getSuccessResponseVO(maps);
    }

    // 编写时间 2024-06-30 04:26
    // 解散群聊
    @RequestMapping("/dissoluteGroup")
    @GlobalInterceptor(checkAdmin = true)
    /*注意这里写了一个自动的参数  老罗没有*/
    public ResponseVO dissoluteGroup(@NotEmpty String groupId){
        //判断groupId存不存在
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //注意这个方法在群主解散群聊的地方也要用！！ 所以为了封装  就这样写了
        groupInfoService.dissoluteGroup(groupInfo.getGroupOwnerId(),groupId);
        return getSuccessResponseVO(null);
    }



}

