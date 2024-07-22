package com.tikchat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.tikchat.entity.enums.OnlineTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author Paprika
 * @since 2024-06-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="UserInfo对象", description="用户信息表")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    @TableId(value = "user_id", type = IdType.ID_WORKER)
    private String userId;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "0:直接加入 1:同意后加好友")
    private Integer joinType;

    @ApiModelProperty(value = "性别:0:女 1:男")
    private Integer sex;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "个性签名")
    private String personalSignature;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "最后一次登陆的时间")
    private Long lastLoginTime;

    @ApiModelProperty(value = "地区")
    private String areaName;

    @ApiModelProperty(value = "地区编号  这个不咋清楚")
    private String areaCode;

    @ApiModelProperty(value = "最后退出登录时间  用于判断更新登陆状态 还要有毫秒！")
    private Long lastOffTime;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "乐观锁：更新的版本")
    @Version
    private Integer version;

    @ApiModelProperty(value = "逻辑删除0:默认值未删除 1:已删除")
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private Integer onlineType;

    public Integer getOnlineType(){
        if(lastLoginTime!=null&&lastLoginTime>lastOffTime){
            return OnlineTypeEnum.ONLINE.getType();
        }else{
            return OnlineTypeEnum.OFFLINE.getType();
        }
    }


}
