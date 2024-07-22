package com.tikchat.entity.vo;

import com.tikchat.utils.StringTools;

import java.io.Serializable;

/**
 * <p>
 * app发布
 * </p>
 *
 * @author Paprika
 * @since 2024-06-30
 */
public class AppUpdateVO implements Serializable {

    private static final long serialVersionUID = -2436757545563L;

    private Integer id;

    private String appVersion;

    private String updateDesc;

    private Long size;


    private String[] updateDescArray;

    public String[] getUpdateDescArray() {
        if (!StringTools.isEmpty(updateDesc)){
            //以|为分隔符号  得到一个desc的数组
            return updateDesc.split("\\|");
        }
        return updateDescArray;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setUpdateDescArray(String[] updateDescArray) {
        this.updateDescArray = updateDescArray;
    }

    private String InstallerAPPName;

    private String outerLink;

    public String getInstallerAPPName() {
        return InstallerAPPName;
    }

    public void setInstallerAPPName(String installerAPPName) {
        InstallerAPPName = installerAPPName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getOuterLink() {
        return outerLink;
    }

    public void setOuterLink(String outerLink) {
        this.outerLink = outerLink;
    }


}
