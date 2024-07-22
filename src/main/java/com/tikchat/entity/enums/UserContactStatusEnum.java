package com.tikchat.entity.enums;

import com.tikchat.utils.StringTools;

public enum UserContactStatusEnum {
    //五种！
    NOT_FRIEND(0,"非好友"),
    FRIEND(1,"好友"),
    DELETED_FRIEND(2,"已删除好友"),
    FRIEND_DELETED_ME(3,"我被这个好友删除"),
    BLACK_FRIEND(4,"已被我拉黑的好友"),
    FRIEND_BLACK_ME(5,"被好友拉黑"),
    FRIEND_BLACK_ME_FIRST(6,"首次被好友拉黑"),//首次的话那么
                        // 虽然拉黑是看作被拉黑的人还能看对方只是发不了消息  但是第一次拉黑会被添加到联系人列表中 要解决这个bug！！！！
    ;
    private Integer status;
    private String desc;

    UserContactStatusEnum(Integer status,String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static UserContactStatusEnum getByName(String enumName){
        try {
            if (StringTools.isEmpty(enumName)){
                return null;
            }
            return UserContactStatusEnum.valueOf(enumName.toUpperCase());
        }catch (IllegalArgumentException e){
            return null;
        }
    }

    public static UserContactStatusEnum getByStatus(Integer Status){
        for (UserContactStatusEnum userContactStatusEnum: UserContactStatusEnum.values()){
            if (Status.equals(userContactStatusEnum.getStatus())){
                return userContactStatusEnum;
            }
        }
        return null;
    }


    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
