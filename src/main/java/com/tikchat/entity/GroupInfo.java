package com.tikchat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="GroupInfo对象", description="")
public class GroupInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "群ID")
    @TableId(value = "group_id", type = IdType.ID_WORKER)
    private String groupId;

    @ApiModelProperty(value = "群组名")
    private String groupName;

    @ApiModelProperty(value = "群主id")
    private String groupOwnerId;

    @ApiModelProperty(value = "群公告")
    private String groupNotice;

    @ApiModelProperty(value = "0:直接加入 1:管理员同意后加入")
    private Integer joinType;

    @ApiModelProperty(value = "状态 1:正常 0:解散")
    private Integer status;

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
