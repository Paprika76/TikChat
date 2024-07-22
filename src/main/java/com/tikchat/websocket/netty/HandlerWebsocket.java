package com.tikchat.websocket.netty;

import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.redis.RedisComponent;
import com.tikchat.utils.StringTools;
import com.tikchat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @FileName HandlerWebsocket
 * @Description 自定义websocket处理
 * @Author Paprika
 * @date 编写时间 2024-06-30 23:11
 **/

//本类的作用类似于controller
@Component
@ChannelHandler.Sharable
public class HandlerWebsocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    //只处理文本的websocket消息
    //图片 文件 视频等等还是使用的http协议
    private static final Logger logger = LoggerFactory.getLogger(HandlerHeartBeat.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChannelContextUtils channelContextUtils;


    /**
     * 通道就绪后要用 一般用来做初始化
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有新的连接加入......");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有连接断开......");
        channelContextUtils.removeChannel(ctx.channel());
    }

    //读消息
    /*注意这个是服务器收到消息  我们直接在这里相当于信号塔给对应的人发消息就行*/
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        /*注意 ctx就是channelHandlerContext  频道处理*/
        Channel channel = ctx.channel();
//        logger.info("收到消息{}",textWebSocketFrame.text());
        Attribute<String> attribute = channel.attr((AttributeKey.valueOf(ctx.channel().id().toString())));
        //注意这里通过channel.attr()获取到一个Attribute对象  这个对象其实就是一个键值对!!!!!!!!!
        String userId = attribute.get();
        logger.info("收到消息userId:{}的消息:{}",userId,textWebSocketFrame.text());
        redisComponent.saveUserHeartBeat(userId);

//        channelContextUtils.send2Group(textWebSocketFrame.text());

    }


    // 编写时间 2024-07-15 20:55
    @Override  /*这个函数是握手的时候触发的  每次握手会几乎同时触发两次  是首先校验通不通过
    通过的话那就进行初始化操作:  addContext方法!!!!!!!!!!   */
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //连接后的处理 校验等等
        //核验后面参数中的token
        //1.解析ws 的url中的参数
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String url = complete.requestUri();
            logger.info("url:{}", url);
            String token = getTokenFromUrl(url);
            if (token == null) {
                ctx.channel().close();  //channel就相当于一个流
                return;
            }
            logger.info("token:{}", token);
            /*在redis中去查询token*/
            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(token);
            if (tokenUserInfoDto == null) {
                ctx.channel().close();
                return;
            }

            /*到了这一步就说明 token对的上 可以聊天了  所以ctx.channel就是生效的 我们暂时不让他close掉*/
            //我们直接把这个channel放进redis里去 用于发消息
            /*但是这样不行  因为channel不能序列化 所以只能放到内存里面
            但是放到内存里面优惠有问题: 多机器集群部署时重启机器内存就会没了  要共享内存才行 这样行不通
            因为单机器重启或故障时内存中的channel对象就会消失*/
            //错误做法:
//            redisComponent.saveChannel(tokenUserInfoDto.getUserId(),ctx.channel());

            /*解决办法就是使用Redis Pub/Sub：使用 Redis 的发布与订阅功能，
            将 Channel 对象的信息作为消息发布到 Redis 的一个频道上，然后订阅所有需要接收该频道消息的客户端可以获取到这些信息。
            这种方式能够跨多台服务器共享信息，并且 Redis 作为内存数据库可以提供高效的访问速度。*/
            //正确的办法很可能就是这样做:
            // 将 Channel 信息发布到 Redis 的频道上
//            redisComponent.publishChannelInfo(tokenUserInfoDto.getUserId(), ctx.channel().id().asLongText());

            //老罗的做法    这里做的事很多很多!!!!!!!!!!!!!!!!!!!!!!!!!
            channelContextUtils.addContext(tokenUserInfoDto.getUserId(), ctx.channel());


            /*有关序列化的知识：！！！！！！！！！！！！！！！！！！！！！！！！！！！*/
            /**
             * 序列化的例子:
             * 简单对象 集合对象(ArrayList)
             * 不能序列化的例子:
             * Socket 对象：依赖于 操作系统级 的资源，这些资源无法在不同的环境中重建。
             * Thread 对象：线程对象不能被序列化，因为线程是 操作系统的资源，其状态和行为无法简单地被序列化和还原。
             * 非静态内部类：非静态内部类隐含地依赖于其外部类的实例，因此它们的实例通常不能被直接序列化。
             *
             * 主要是因为：
             * 1.系统资源依赖
             * 2.复杂的状态 Channel 对象可能包含复杂的状态信息，
             * 如当前位置、标记以及与之关联的操作状态等。这些状态信息难以以简单的方式序列化为字节流或其他格式。
             * 3.安全性和保护
             */


        }
    }

    public String getTokenFromUrl(String url){
        if (StringTools.isEmpty(url) || url.indexOf("?")<=1){
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
