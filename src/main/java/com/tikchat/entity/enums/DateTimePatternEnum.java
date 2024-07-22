package com.tikchat.entity.enums;


import java.text.SimpleDateFormat;
import java.util.Date;

public enum DateTimePatternEnum {
    YYYY_MM_DD_HH_MM_SS("yyyy-mm-dd HH:mm:ss"),YYYY_MM_DD("yyyy-mm-dd");

    private String pattern;

    DateTimePatternEnum(String pattern){
        this.pattern = pattern;
    }

    public String getPattern(){
        return pattern;
    }

    public static String getCurTimeFormatted(){
        // 定义目标格式
        SimpleDateFormat sdf = new SimpleDateFormat(DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.pattern);

        // 格式化当前时间
        return sdf.format(new Date());
    }


    public static void main(String[] args) {


        System.out.println(DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS);
        System.out.println(DateTimePatternEnum.YYYY_MM_DD);
        System.out.println(DateTimePatternEnum.valueOf("YYYY_MM_DD_HH_MM_SS").getPattern());
//        DateTimePatternEnum.values()
//        System.out.println(DateTimePatternEnum.values());
        System.out.println(DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern());
    }



}
