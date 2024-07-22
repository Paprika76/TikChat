package com.tikchat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.tikchat.utils.StringTools;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * app发布
 * </p>
 *
 * @author Paprika
 * @since 2024-06-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="AppUpdate对象", description="app发布")
public class AppUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "版本号")
    private String appVersion;

    @ApiModelProperty(value = "更新描述")
    private String updateDesc;

    @TableField(exist = false) /*设置这个字段不映射到数据库中*/
    private String[] updateDescArray;

    public String[] getUpdateDescArray() {
        if (!StringTools.isEmpty(updateDesc)){
            //以|为分隔符号  得到一个desc的数组
            return updateDesc.split("\\|");
        }
        return updateDescArray;
    }

    public void setUpdateDescArray(String[] updateDescArray) {
        this.updateDescArray = updateDescArray;
    }

    @ApiModelProperty(value = "e:未发布1:灰度发布2:全网发布")
    private Integer status;

    @ApiModelProperty(value = "灰度uid")
    private String grayscaleUid;

    @ApiModelProperty(value = "文件类型0:本地文件1:外链")
    private Integer installerFileType;

    @ApiModelProperty(value = "外链地址")
    private String outerLink;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;


}
