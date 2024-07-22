
package com.tikchat.controller;

import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.entity.config.AppConfig;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.enums.AppUpdateInstallerFileType;
import com.tikchat.entity.vo.AppUpdateVO;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.mapper.AppUpdateMapper;
import com.tikchat.redis.RedisComponent;
import com.tikchat.utils.CopyTools;
import com.tikchat.utils.StringTools;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.File;


import com.tikchat.service.AppUpdateService;
import com.tikchat.entity.AppUpdate;


@RestController("appUpdateController")
@RequestMapping("/udpate")
public class AppUpdateController extends ABaseControllertest{
    @Resource
    private AppUpdateService appUpdateService;

    @Resource
    private AppUpdateMapper appUpdateMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    // 编写时间 2024-06-30 23:13
    @RequestMapping("/checkVersion")
    @GlobalInterceptor
    public ResponseVO checkVersion(String appVersion,String uid){
        //每次登陆进入软件都会访问这个路由  还有可以点击关于 查看版本信息
        //然后我们需要看你是内测灰度发布的人员  所以需要你的uid
        //还有就是这个不需要request检测  因为不登陆也能看版本的
        if (StringTools.isEmpty(appVersion)){
            return getSuccessResponseVO(null);
        }
        AppUpdate latestAppUpdate = appUpdateService.getLatestVersion(appVersion, uid);
        if (latestAppUpdate==null){
            return getSuccessResponseVO(null);
        }
        /*还要返回下载文件的大小  想象一下嘛 看抖音叫你更新时长什么样就行*/
        AppUpdateVO appUpdateVO = CopyTools.copy(latestAppUpdate, AppUpdateVO.class);
        if (AppUpdateInstallerFileType.LOCAL.getType().equals(latestAppUpdate.getInstallerFileType())){
            File file = new File(Constants.APP_FILES_FULL_PATH + latestAppUpdate.getVersion() + Constants.APP_EXE_SUFFIX);
            appUpdateVO.setSize(file.length());
        }else{
            //说明是外链 没法获取installer安装包的大小
            appUpdateVO.setSize(0L);
        }
        appUpdateVO.setInstallerAPPName(Constants.APP_NAME+latestAppUpdate.getAppVersion()+Constants.APP_EXE_SUFFIX);

        return getSuccessResponseVO(appUpdateVO);
    }




}

