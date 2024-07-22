package com.tikchat.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class CopyTools {

    //赋值一个List
    public static <T,S> List<T> conpyList(List<S> sList, Class<T> classz){
        List<T> list = new ArrayList<>();
        for(S s:sList){
            T t = null;
            try {
                t = classz.newInstance();
            }catch (Exception e){
                e.printStackTrace();
            }
            BeanUtils.copyProperties(s,t);
            list.add(t);
        }
        return list;
    }

    //复制单个对象
    public static <T,S> T copy(S s, Class<T> classz){
        T t = null;
        try {
            t = classz.newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        BeanUtils.copyProperties(s,t);
        return t;
    }


}
