package com.tikchat.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.controller.ABaseControllertest;
import com.tikchat.entity.UserInfoBeauty;
import com.tikchat.entity.enums.PageSize;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.mapper.UserContactMapper;
import com.tikchat.mapper.UserInfoBeautyMapper;
import com.tikchat.mapper.UserInfoMapper;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.UserContactService;
import com.tikchat.service.UserInfoBeautyService;
import com.tikchat.service.UserInfoService;
import com.tikchat.service.component.ServiceComponent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @FileName AdminUserInfoBeautyController
 * @Description 管理员靓号管理
 * @Author Paprika
 * @date 编写时间 2024-06-30 23:11
 **/

@RestController("adminUserInfoBeautyController")
@RequestMapping("/admin")
@Validated
public class AdminUserInfoBeautyController extends ABaseControllertest {
    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserInfoBeautyService userInfoBeautyService;

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

    // 编写时间 2024-06-30 00:25
    @RequestMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    /*注意这里写了一个自动的参数  老罗没有*/
    public ResponseVO loadBeautyAccountList(Integer pageNo){
        Page<UserInfoBeauty> userInfoBeautyPage = new Page<>(pageNo, PageSize.SIZE15.getSize());
        //根据注册时间排序 而不是根据修改时间排序
        userInfoBeautyMapper.selectPage(userInfoBeautyPage,new QueryWrapper<UserInfoBeauty>().orderBy(true, false, "create_time"));
        List<UserInfoBeauty> userInfoBeautyList = userInfoBeautyPage.getRecords();
        // TODO 注意 这里我返回的是一个list  但是老罗写的是传的参数是 UserInfoQuery对象
        //  而返回给前端的是一个ResultVO里面包含了UserList 所以我们后面还要看看要不要改
        return getSuccessResponseVO(userInfoBeautyList);
    }

    /**
     * 编辑用户信息
     *  编写时间 2024-06-30 00:36
     * @return date
     */
    @RequestMapping("/saveBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveBeautyAccount(UserInfoBeauty userInfoBeauty){
        userInfoBeautyService.saveBeautyAccount(userInfoBeauty);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delBeautyAccount(@NotNull Integer id){
        userInfoBeautyService.removeById(id);
        return getSuccessResponseVO(null);
    }

}

