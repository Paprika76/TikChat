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
 * 联系人申请
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="UserContactApply对象", description="联系人申请")
public class UserContactApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增ID")
    @TableId(value = "apply_id", type = IdType.AUTO)
    private Integer applyId;

    @ApiModelProperty(value = "申请人ID")
    private String applyUserId;

    @ApiModelProperty(value = "接收人ID")
    private String receiveUserId;

    @ApiModelProperty(value = "联系人类型g:好友1：群组")
    private Integer contactType;

    @ApiModelProperty(value = "联系人列表中群组ID 也就是群号 注意发送申请时的接收人ID是群主的ID 同时也要有群组ID  如果是申请加好友的话那么群组ID就是null！！！！！！")
    private String contactId;

    @ApiModelProperty(value = "最后申请时间")
    private Long lastApplyTime;

    @ApiModelProperty(value = "状态0:待处理 1：已同意 2：已拒绝 3：已拉黑")
    private Integer status;

    @ApiModelProperty(value = "申请信息")
    private String applyInfo;

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
