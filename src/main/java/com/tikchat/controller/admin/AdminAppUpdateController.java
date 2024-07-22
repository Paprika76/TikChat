
package com.tikchat.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.controller.ABaseControllertest;
import com.tikchat.entity.config.AppConfig;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.enums.AppUpdateStatusEnum;
import com.tikchat.entity.enums.ResponseCodeEnum;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.AppUpdateMapper;
import com.tikchat.redis.RedisComponent;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.util.List;


import com.tikchat.service.AppUpdateService;
import com.tikchat.entity.AppUpdate;


import org.springframework.web.multipart.MultipartFile;

/**
 * @FileName AppUpdateController
 * @Description 管理员更新应用
 * @Author Paprika
 * @date 编写时间 2024-06-30 23:11
 **/


@RestController("adminAppUpdateController")
@RequestMapping("/admin")
public class AdminAppUpdateController extends ABaseControllertest {
    @Resource
    private AppUpdateService appUpdateService;

    @Resource
    private AppUpdateMapper appUpdateMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;


    // 编写时间 2024-06-30 16:23
    @RequestMapping("/loadUpdateList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUpdateList(Integer PageNo){
        QueryWrapper<AppUpdate> appUpdateQueryWrapper = new QueryWrapper<>();
        appUpdateQueryWrapper.orderByDesc("id");

        List<AppUpdate> appUpdates = appUpdateMapper.selectList(appUpdateQueryWrapper);
        return getSuccessResponseVO(appUpdates);
    }

    // 编写时间 2024-06-30 16:23
    @RequestMapping("/saveUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveUpdate(Integer id, /* 注意 id可以为空  为空说明是插入 非空说明是编辑*/
                                 //符合1.3.5 或 12.346.2547565的格式
                                 @NotEmpty @Pattern(regexp = Constants.REGEX_APP_VERSION) String appVersion,
                                 @NotEmpty String updateDesc,
                                 @NotNull Integer installerFileType,
                                 String outLink,
                                 MultipartFile installerFile) throws IOException {
        /* 插入 或 编辑版本信息*/
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setAppVersion(appVersion);
        appUpdate.setUpdateDesc(updateDesc);
        appUpdate.setId(id);
        appUpdate.setOuterLink(outLink);
        appUpdate.setInstallerFileType(installerFileType);
        appUpdateService.saveUpdate(appUpdate,installerFile);
        return getSuccessResponseVO(null);
    }

    // 编写时间 2024-06-30 19:02
    @RequestMapping("/delUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO delUpdate(@NotNull Integer id) {
        //首先判断是不是在使用中  只有未使用中才可以删除
        AppUpdate appUpdate = appUpdateMapper.selectById(id);
        if (appUpdate!=null && !AppUpdateStatusEnum.INIT.getStatus().equals(appUpdate.getStatus())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        appUpdateMapper.deleteById(id);
        return getSuccessResponseVO(null);
    }

    // 编写时间 2024-06-30 19:02
    @RequestMapping("/postUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO postUpdate(@NotNull Integer id,
                                 @NotNull Integer postStatus,String grayscaleUIDs) {
        appUpdateService.postUpdate(id,postStatus,grayscaleUIDs);
        return getSuccessResponseVO(null);
    }

}

