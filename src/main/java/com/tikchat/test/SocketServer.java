package com.tikchat.test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SocketServer {
    public static void main(String[] args) {
        ServerSocket server = null;

        Map<String,Socket> CLIENT_MAP = new HashMap<>();

        try {
            server = new ServerSocket(1024);//这是我们创建的一个server服务接口
            System.out.println("服务已启动，等待客户端连接");

            while(true){
                Socket socket = server.accept();//得到一个socket用户端对象 我们接下来会获取这个对象

                String ip = socket.getInetAddress().getHostAddress();
                System.out.println("有客户端连接"+ip+"，端口:"+socket.getPort());
                String clientKey = ip+socket.getPort();
                //把新连接的socket加进列表里
                CLIENT_MAP.put(clientKey,socket);
                //每次连接上一个socket都会启动一个一直接收这个socket客户端发过来的消息 并且季节收到消息就发给特定的其他socket客户端的人
                new Thread(() -> {
                    while(true){
                        try {
                            //等待获取client输入的东西 client输入了东西回车后就会获取到inputStream了
                            InputStream inputStream = socket.getInputStream();
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            String readData = bufferedReader.readLine();
                            System.out.println("客户端返回了数据给我这个服务端："+readData);


//                            CLIENT_MAP.forEach((k,v)->{
//                                System.out.println(k+":"+v);
//                                    }
//                            );
                            //之所以这两个要写在一起是因为要一接收到消息就把消息给特定的人员  所以是要放一起的

                            CLIENT_MAP.forEach((k,v)->{
                                if (v!=socket)
                                    try {
                                        //获取v对应的输出流  要准备给他发消息
                                        OutputStream outputStream = v.getOutputStream();
                                        PrintWriter printWriter = new PrintWriter(outputStream);
                                        printWriter.println(ip+":"+socket.getPort()+"发送了消息给你："+readData);
                                        printWriter.flush();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            );




//                            //获取到client输入数据后我们再传给其他对应要接收的client
//                            //也就是说我们这个server要起到移动电信卫星的作用，用来做消息的中转站
//                            //但是我们这里先不传给其他client我们先至少要实现传回一条语句“成功接收client的消息”的消息给回client
//                            OutputStream outputStream = socket.getOutputStream();
//                            PrintWriter printWriter = new PrintWriter(outputStream);
//                            printWriter.println("我是服务端，我成功收到你的消息！即："+readData);
//                            printWriter.flush();


                        }catch (Exception e){
                            e.printStackTrace();
                            break;
                        }
                    }
                }).start();
            }


//            new Thread(()->{
//
//            }).start();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
