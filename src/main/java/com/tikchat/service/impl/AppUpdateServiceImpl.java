package com.tikchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.entity.AppUpdate;
import com.tikchat.entity.config.AppConfig;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.enums.AppUpdateStatusEnum;
import com.tikchat.entity.enums.ResponseCodeEnum;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.AppUpdateMapper;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.AppUpdateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tikchat.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * app发布 服务实现类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-30
 */
@Service
public class AppUpdateServiceImpl extends ServiceImpl<AppUpdateMapper, AppUpdate> implements AppUpdateService {
    @Resource
    private AppUpdateService appUpdateService;

    @Resource
    private AppUpdateMapper appUpdateMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Override
    public void saveUpdate(AppUpdate toSaveAppUpdate, MultipartFile installerFile) throws IOException {
        /* 插入 或 编辑版本信息*/

        /*首先还要判断编辑时状态是不是未发布*/
        if (toSaveAppUpdate.getId()!=null){
            //通过id查我们这个版本信息是否未发布
            AppUpdate appUpdate1 = appUpdateMapper.selectById(toSaveAppUpdate.getId());
            if (!AppUpdateStatusEnum.INIT.getStatus().equals(appUpdate1.getStatus())){
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(),"该版本已发布,不能修改,请先取消发布后再编辑");
            }
        }

        //首先明确我们要做的业务：
        //1.能插入一条大于最新版本的版本信息
        //2.不能插入小于等于最新版本的版本
        //3.编辑：不能把信息编辑为最新版本  非最新版本随便你怎么更新 但是版本号也记得不能重复！！！
        /* 因此 我们现在获取最新的版本信息*/
        QueryWrapper<AppUpdate> appUpdateQueryWrapper = new QueryWrapper<>();
        appUpdateQueryWrapper.orderByDesc("id");//这样才确保了是谁后面创建的版本
        // 那么谁就是最新的版本  而不是通过create_time来排序 因为有可能两者同时创建 同时插入
        // 这样就有问题了 （不过这不可能 但是其他业务逻辑是要好好琢磨这个的）
        List<AppUpdate> dbAppUpdates = appUpdateMapper.selectList(appUpdateQueryWrapper);
        if (!dbAppUpdates.isEmpty()){
            //获取到最新的版本信息
            AppUpdate latest = dbAppUpdates.get(0);
            Double latestVersion = Double.parseDouble(latest.getAppVersion().replaceFirst("\\.", ""));
            Double currentVersion = Double.parseDouble(toSaveAppUpdate.getAppVersion().replaceFirst("\\.",""));
            System.out.println("latestVersion:"+latestVersion);
            System.out.println("currentVersion:"+currentVersion);
            //判断输入的版本和最新的版本
            /*插入的判断 */
            //插入的判断  id为空说明插入  并且version要大于最新的
            if(toSaveAppUpdate.getId()==null&&currentVersion<=latestVersion){
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(),"版本号必须大于最新版本");
            }

            /*编辑的判断*/
            //获取我们输入的版本对应的id
            QueryWrapper<AppUpdate> appUpdateQueryWrapper1 = new QueryWrapper<>();
            appUpdateQueryWrapper1.eq("app_version", toSaveAppUpdate.getAppVersion());
            AppUpdate dbAppUpdate = appUpdateMapper.selectOne(appUpdateQueryWrapper1);
            //编辑的判断 id不为空说明编辑  并且version要小于等于最新的
            //还有我们输入的version对应的id是不存在的或者是等于我们的才行
            if (toSaveAppUpdate.getId()!=null){
                if (currentVersion > latestVersion){
                    throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(),"不能编辑为比最新还更新的版本!");
                }
                if(dbAppUpdate!=null && dbAppUpdate.getId()!=null && !dbAppUpdate.getId().equals(toSaveAppUpdate.getId())){
                    throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(),"不能编辑为已存在的版本");
                }
                //编辑!!!!! 开编
                toSaveAppUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
                appUpdateMapper.updateById(toSaveAppUpdate);
            }


        }


        //说明还没有数据 那么到了这里就只能插入了 不可能编辑的
        if (toSaveAppUpdate.getId()==null){
            //说明是插入
            toSaveAppUpdate.setStatus(AppUpdateStatusEnum.INIT.getStatus());
            appUpdateMapper.insert(toSaveAppUpdate);
        }

        /*最后看有没有installer文件*/
        if(installerFile!=null){
            File folder = new File(appConfig.getProjectFolder() + Constants.APP_UPDATE_INSTALLER_FOLDER);
            if (!folder.exists()){
                folder.mkdirs();
            }
            installerFile.transferTo(new File(folder.getAbsoluteFile() + "/" + Constants.APP_NAME+toSaveAppUpdate.getVersion()+Constants.APP_EXE_SUFFIX));
        }
    }

    @Override
    public void postUpdate(Integer id, Integer postStatus, String grayscaleUIDs) {
        //首先判断postStatus对不对
        AppUpdateStatusEnum statusEnum = AppUpdateStatusEnum.getByStatus(postStatus);
        if (null == statusEnum){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //本方法可以 "取消发布"(未发布) "灰度发布" "全网发布"  对应三种status
        //直接update就行

        if (AppUpdateStatusEnum.GRAYSCALE == statusEnum ){
            if (StringTools.isEmpty(grayscaleUIDs)){
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            // TODO 灰度发布的逻辑(不包括AppUpdate表的更新 因为表的更新写了 写在这的后面几行了) 这个可以不做
        }else{
            grayscaleUIDs = "";
        }
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setStatus(postStatus);
        appUpdate.setGrayscaleUid(grayscaleUIDs);
        appUpdateMapper.updateById(appUpdate);
    }

    @Override
    public AppUpdate getLatestVersion(String appVersion, String uid) {
        return appUpdateMapper.selectLatestAppUpdate(appVersion, uid);

//        //接着查询你这个版本是不是最新版本
//        QueryWrapper<AppUpdate> appUpdateQueryWrapper = new QueryWrapper<>();
//        appUpdateQueryWrapper.orderByDesc("id");
//        //并且status不为0的
//        appUpdateQueryWrapper.in("status", AppUpdateStatusEnum.GRAYSCALE.getStatus(),AppUpdateStatusEnum.ALL.getStatus());
//        List<AppUpdate> dbAppUpdates = appUpdateMapper.selectList(appUpdateQueryWrapper);
//        if (!dbAppUpdates.isEmpty()){
//            AppUpdate latest = dbAppUpdates.get(0);
//            Double latestVersion = Double.parseDouble(latest.getAppVersion().replaceFirst("\\.", ""));
//            Double currentVersion = Double.parseDouble(appVersion.replaceFirst("\\.",""));
//            if (currentVersion<latestVersion){
//                //发送最新版本的文件 latest 是一个对象 里面包含版本号 desc status
//
//            }
//
//
//        }

    }


}
