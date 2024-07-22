package com.tikchat.entity.config;

import com.tikchat.utils.StringTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @FileName AppConfig
 * @Description 配置文件
 * @Author Paprika
 * @date 编写时间 2024-06-23 23:11
 **/

@Component("appConfig")
public class AppConfig {
    @Value("${ws.port:}")
    private Integer wsPort;

    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.emails:}")
    private String adminEmails;

    public Integer getWsPort() {
        return wsPort;
    }

    public String getProjectFolder() {
        //怕格式不对报错 我们把路径简单修改成对的
        if (!StringTools.isEmpty(projectFolder) && !projectFolder.endsWith("/")) {
            projectFolder = projectFolder + "/";
        }

        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }
}
