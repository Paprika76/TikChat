package com.tikchat.controller.admin;


import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.controller.ABaseControllertest;
import com.tikchat.entity.config.AppConfig;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.SysSettingDto;
import com.tikchat.entity.vo.ResponseVO;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author Paprika
 * @since 2024-06-22
 */
@RestController("adminSettingController")
@RequestMapping("/admin")
@Validated
public class AdminSettingController extends ABaseControllertest {
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

    @Resource
    private AppConfig appConfig;




    //  编写时间 2024-06-30 15:18
    @RequestMapping("/getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSetting(){
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        return getSuccessResponseVO(sysSettingDto);
    }


    //  编写时间 2024-06-30 15:18
    @RequestMapping("/saveSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveSysSetting(@Valid SysSettingDto sysSettingDto,/*写了valid注解就会自动判断每个对应的字段格式对不对*/
                                     //这样的话其实也相当于是一个一个写 然后设置了
                                     MultipartFile robotAvatar,
                                     MultipartFile robotCover) throws IOException {
        //直接判断file就行  然后直接update就行 不用校验sysSettingDto  本来是所有都需要校验的
        // 这个可以搞一个 TODO 以后优化 这种都是做完了后以后优化的  数据校验 什么不能为空之类的
        if (robotAvatar != null && robotCover != null) {
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + Constants.ROBOT_UID + Constants.IMAGE_SUFFIX;
            robotAvatar.transferTo(new File(filePath));
            robotCover.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        }
        redisComponent.saveSysSetting(sysSettingDto);
        return getSuccessResponseVO(null);
    }
}

