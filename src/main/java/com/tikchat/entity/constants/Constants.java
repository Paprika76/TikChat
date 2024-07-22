package com.tikchat.entity.constants;

import com.tikchat.entity.config.AppConfig;
import com.tikchat.entity.enums.UserContactTypeEnum;

public class Constants {

    public static final String APP_NAME = "tikchat";

    public static final Integer REDIS_TIME_1MIN = 60;
    public static final Integer REDIS_TIME_1Day = REDIS_TIME_1MIN * 60 * 24;
    public static final Integer REDIS_TIME_EXPIRE_HEART_BEAT = 6;

    //没接收消息的消息过期时间:
    public static final Long MILLISECOND_HALF_YEAR = (365/2) * 24*60*60*1000L;
    //已接收消息的消息过期时间:
    public static final Long MILLISECOND_7days = 7 * 24*60*60*1000L;


    public static final Integer LENGTH_11 = 11;
    public static final Integer LENGTH_20 = 20;



    public static final String REDIS_KEY_CHECK_CODE = "tikchat:checkcode:";
    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "tikchat:ws:user:heartbeat:";
    public static final String REDIS_KEY_WS_TOKEN = "tikchat:ws:token:";
    public static final String REDIS_KEY_WS_TOKEN_USERID = "tikchat:ws:token:userid:";
    public static final String ROBOT_UID = UserContactTypeEnum.USER.getPrefix() + "robot";

    public static final String REDIS_KEY_USER_CONTACT = "tikchat:userContact:";




    public static final String REDIS_KEY_SYS_SETTING = "tikchat:syssetting";

    public static final String FILE_FOLDER_FILE = "/file/";
    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";
    public static final String IMAGE_SUFFIX = ".png";
    public static final String COVER_IMAGE_SUFFIX = "_cover.png";

    public static final String APP_UPDATE_INSTALLER_FOLDER = "installer_app_files/";
    public static final String APP_EXE_SUFFIX = ".exe";
    public static final String APP_FILES_FULL_PATH = new AppConfig().getProjectFolder()+
            APP_UPDATE_INSTALLER_FOLDER + APP_NAME;

    public static final String APPLY_INFO_TEMPLATE = "我是%s";

    public static final String REGEX_PASSWORD = "(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";
    public static final String DEFAULT_PASSWORD = "123456";
    public static final String REGEX_APP_VERSION = "^\\d+\\.\\d+\\.\\d+$";

    public static final String[] IMAGE_SUFFIX_LIST = new String[]{".jpeg",".jgp",".png",".gif",".bmp",".webp"};

    public static final String[] VIDEO_SUFFIX_LIST = new String[]{".mp4",".avi",".rmvb",".mkv",".mov"};

    public static final Long FILE_SIZE_MB = 1024*1024L;



}
