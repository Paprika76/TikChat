package com.tikchat.entity.enums;

public enum MessageTypeEnum {
    INIT(0,"","连接WS获取信息"),
    ADD_FRIEND(1,"","添加好友打招呼信息"),
    CHAT(2,"","普通聊天信息"),
    GROUP_CREAT(3,"群组已经创建好,可以一起畅聊了","群创建成功"),
    CONTACT_APPLY(4,"","好友申请"),
    MEDIA_CHAT(5,"","媒体文件"),
    SUCCESS_UPLOAD(6,"","文件上传完成"),
    FAILED_UPLOAD(7,"","文件上传失败"),

    FORCE_OFFLINE(8,"","强制下线"),
    DISSOLUTE_GROUP(9,"群聊已解散","解散群聊"),
    ADD_GROUP(10,"%s加入了群聊","加入群聊"),
    CONTACT_NAME_UPDATE(11,"","更新昵称"),
    LEAVE_GROUP(12,"%s退出了群聊","退出群聊"),
    REMOVE_USER_FROM_GROUP(13,"%s被管理员移出了了群聊","被管理员移出了了群聊"),
    ADD_FRIEND_SELF(14,"%s被管理员移出了了群聊","被管理员移出了了群聊"),
    ;
    private Integer type;
    private String initMessage;
    private String desc;

    MessageTypeEnum(Integer type, String initMessage, String desc) {
        this.type = type;
        this.initMessage = initMessage;
        this.desc = desc;
    }

    public static MessageTypeEnum getByType(Integer type){
        for(MessageTypeEnum item:MessageTypeEnum.values()){
            if(item.getType().equals(type)){
                return item;
            }
        }
        return null;
    }


    public Integer getType() {
        return type;
    }

    public String getInitMessage() {
        return initMessage;
    }

    public String getDesc() {
        return desc;
    }
}
