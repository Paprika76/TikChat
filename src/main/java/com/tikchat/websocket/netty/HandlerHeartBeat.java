package com.tikchat.websocket.netty;

import com.tikchat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @FileName HandlerHeartBeat
 * @Description 心跳检测
 * @Author Paprika
 * @date 编写时间 2024-06-30 23:11
 **/

//@Component     /*老罗的没写这个  我写了  因为为了注入channelContextUtils  来方便的获取channel对应的userId*/
public class HandlerHeartBeat extends ChannelDuplexHandler {
                                    /*Duplex是双工的意思  双向通信*/
    private static final Logger logger = LoggerFactory.getLogger(HandlerHeartBeat.class);


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if(evt instanceof IdleStateHandler){
        if(evt instanceof IdleStateEvent){
            IdleStateEvent e = (IdleStateEvent) evt;
            logger.info("e.state():",e.state());
            if(e.state()== IdleState.READER_IDLE){
                Channel channel = ctx.channel();
                String userId = ChannelContextUtils.getUserId(channel);
                logger.info("用户{}的心跳超时",userId);
                /*注意一个问题  就是用户在线的时候就每隔几秒(4秒)就发一条空的websocket消息
                那么这个要怎么实现? 我觉得是通过客户端实现   就是你客户端经常发送空的ws消息给服务端
                  客户端没网络那么几秒后就视为下线!!!   */
                ctx.close();
//            }else if (e.state()== IdleState.READER_IDLE){
            }else if (e.state()== IdleState.WRITER_IDLE){
                ctx.writeAndFlush("heart");
            }
        }

    }
}
