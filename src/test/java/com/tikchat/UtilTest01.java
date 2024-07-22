package com.tikchat;

import com.tikchat.utils.StringTools;

public class UtilTest01 {
    public static void main(String[] args) {
//        List<Map<String, String>> maps = new ArrayList<>();
//        maps.add('user_id''U90003571017');
//        maps = [{'user_id'='U90003571017'},];



        //  sdgffdgf=534432 & etertgr=7645432 & token=32456345 & nbvcg=236y754
//        String[] queryParams = "sdgffdgf=534432 & etertgr=7645432 & token=32456345 & nbvcg=236y754".split("&");
        String[] queryParams = "sdgffdgf=534432".split("&");
        for (int i = 0; i < queryParams.length; i++) {
            System.out.println(queryParams[i]);
        }
        System.out.println(getTokenFromUrl("ws://127.0.0.1:5051/ws?sdgffdgf=534432&etertgr=7645432&token=32456345&nbvcg=236y754"));
        System.out.println(getTokenFromUrl("ws://127.0.0.1:5051/ws?token=32456345&nbvcg=236y754"));

//        System.out.println(queryParams);
    }



    public static String getTokenFromUrl(String url){
        if (StringTools.isEmpty(url) || url.indexOf("?")<=5){
            //小于等于5说明 ws:// 中有问号肯定不行的
            return null;
        }
        //类似于下面这样
        //   ws://127.0.0.1:5051/ws?sdgffdgf=534432&etertgr=7645432&token=32456345&nbvcg=236y754
        String[] queryParams = url.split("\\?");
        if(queryParams.length!=2){
            return null;
        }
        //  sdgffdgf=534432 & etertgr=7645432 & token=32456345 & nbvcg=236y754
        queryParams = queryParams[1].split("&");
        if (queryParams.length<1){
            return null;
        }
        for (int i = 0; i < queryParams.length; i++) {
            //  sdgffdgf=534432 & etertgr=7645432 & token=32456345 & nbvcg=236y754
            String[] paramKeyValue = queryParams[i].split("=");
            if(paramKeyValue.length!=2){
                return null;
            }
            if (paramKeyValue[0].equals("token")){
                return paramKeyValue[1];
            }
        }

        return null;
    }
}
