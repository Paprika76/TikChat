package com.tikchat.service;

import com.tikchat.entity.AppUpdate;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * app发布 服务类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-30
 */
public interface AppUpdateService extends IService<AppUpdate> {
    void saveUpdate(AppUpdate appUpdate, MultipartFile installerFile) throws IOException;
    void postUpdate(Integer id,Integer postStatus,String grayscaleUIDs);
    AppUpdate getLatestVersion(String appVersion,String uid);

}
