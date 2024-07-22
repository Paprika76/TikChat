package com.tikchat.test;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SocketClient {
     public static void main(String[] args) {
          Socket socket = null;
          try {
               socket = new Socket("127.0.0.1",1024);

               //总结：这个OutputStream用于输出消息给别人
               //InputStream用于等待别人发来消息 即接收别人的消息
               OutputStream outputStream = socket.getOutputStream();
               PrintWriter printWriter = new PrintWriter(outputStream);
               System.out.println("请输入内容");
               new Thread(()->{
                    while(true){
                         try {
                              Scanner scanner = new Scanner(System.in);
                              String input = scanner.nextLine();
                              printWriter.println(input);
                              printWriter.flush();
                         }catch (Exception e){
                              e.printStackTrace();
                              break;
                         }
                    }
               }).start();

               //获取别人发给我的消息
               InputStream inputStream = socket.getInputStream();
               InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
               BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

               //不能和上面的写在一起 因为接收消息和发送消息要分开来 有人发消息来就一定要接到来
               //
               new Thread(()->{
                    while(true){
                         try {

                              String readData = bufferedReader.readLine();
                              System.out.println("收到服务端消息："+readData);

                         }catch (Exception e){
                              e.printStackTrace();
                              break;
                         }
                    }
               }).start();



          }catch(Exception e){
               e.printStackTrace();
          }
     }

}
