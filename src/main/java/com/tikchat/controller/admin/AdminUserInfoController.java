package com.tikchat.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.controller.ABaseControllertest;
import com.tikchat.entity.UserInfo;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.enums.PageSize;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.mapper.UserContactMapper;
import com.tikchat.mapper.UserInfoMapper;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.UserContactService;
import com.tikchat.service.UserInfoService;
import com.tikchat.service.component.ServiceComponent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @FileName AdminUserInfoController
 * @Description 管理员用户管理
 * @Author Paprika
 * @date 编写时间 2024-06-30 23:11
 **/

@RestController("adminUserInfoController")
@RequestMapping("/admin")
@Validated
public class AdminUserInfoController extends ABaseControllertest {
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

    // 编写时间 2024-06-30 00:25
    @RequestMapping("/loadUserList")
    @GlobalInterceptor(checkAdmin = true)
    /*注意这里写了一个自动的参数  老罗没有*/
    public ResponseVO loadUserList(Integer pageNo){
        Page<UserInfo> userInfosPage = new Page<>(pageNo, PageSize.SIZE15.getSize());


        //根据注册时间排序 而不是根据修改时间排序
        userInfoMapper.selectPage(userInfosPage,new QueryWrapper<UserInfo>().orderBy(true, false, "create_time"));
        List<UserInfo> userInfoList = userInfosPage.getRecords();
        // TODO 注意 这里我返回的是一个list  但是老罗写的是传的参数是 UserInfoQuery对象
        //  而返回给前端的是一个ResultVO里面包含了UserList 所以我们后面还要看看要不要改
        /* 以下是GPT改进后的代码：  userInfoQuery是前端传来的参数！！！！！*/
//        // 根据实际需要，可以从UserInfoQuery对象中获取分页信息
//        Integer pageNo = userInfoQuery.getPageNo(); // 获取页码，默认值可以在UserInfoQuery中设定
//        Integer pageSize = userInfoQuery.getPageSize(); // 获取每页记录数，同样可以在UserInfoQuery中设定
//
//        Page<UserInfo> usersPage = userService.getUserList(userInfoQuery);
//        List<UserInfo> userList = usersPage.getRecords();
//        long total = usersPage.getTotal();
//
//        ResultVO result = new ResultVO();
//        result.setData(userList);
//        result.setTotal(total);

        return getSuccessResponseVO(userInfoList);
    }

    /**
     * 编辑用户信息
     *  编写时间 2024-06-30 00:36
     * @return date
     */
    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(@NotNull Integer status,
                                       @NotNull String userId){
        userInfoService.updateUserStatus(status,userId);
        return getSuccessResponseVO(null);
    }

    // 编写时间 2024-06-30 00:50
    //其实就相当于是用户注册  不会影响其他表 所以直接注册就行
    @RequestMapping("/insertUser")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO insertUser(@NotEmpty @Email String email,
                                 /*password不用传  我们是管理员可以选择直接随机生成 或者默认值*/
                                 @Pattern(regexp = Constants.REGEX_PASSWORD) String password,
                                 @NotEmpty String nickName) {
//        return getSuccessResponseVO(userInfo);
        if (password==null){
            password = Constants.DEFAULT_PASSWORD;//不用加密 后面在register函数里加密了
        }
        userInfoService.register(email, nickName, password);
        return loadUserList(1);/*这里很巧妙！！！！！！
        insert完了后要立即刷新看更新后的样子 所以就直接返回更新后的再次搜索的结果*/
    }

    //  编写时间 2024-06-30 01:01
    //其实就相当于是用户注册  不会影响其他表 所以直接注册就行
    @RequestMapping("/forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceOffLine(@NotEmpty String userId) {
        //TODO 以后再实现 要用到ws强制下线
        userInfoService.forceOffLine(userId);
        return getSuccessResponseVO(null);
    }

}

