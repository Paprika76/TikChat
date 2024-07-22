package com.tikchat.websocket.netty;

import com.tikchat.entity.config.AppConfig;
import com.tikchat.utils.StringTools;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @FileName NettyWebSocketStarter
 * @Description websocket启动类
 * @Author Paprika
 * @date 编写时间 2024-06-30 23:11
 **/


@Component  //交给SpringBoot来管理  要不然
public class NettyWebSocketStarter implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketStarter.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private static EventLoopGroup workGroup = new NioEventLoopGroup();

    @Resource
    private HandlerWebsocket handlerWebsocket;

    @Resource
    private AppConfig appConfig;



    @PreDestroy
    public void closeNetty(){
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    @Override
    public void run() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup);

            //准备工作
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            /*下面设置几个重要的处理器*/
                            //1.对http协议的支持,使用http的 编码器 和 解码器
                            pipeline.addLast(new HttpServerCodec());
                            //2.聚合解码 httpRequest/httpContent/lastHttpContent到fullHttpRequest
                            //保证接收的http请求的完整性  设置的允许的最大聚合内容长度是 64KB。
                        /*总结: 向 Netty 的处理 pipeline 中添加一个能够将 HTTP 消息部分聚合成完整消息的处理器，
                        限定最大聚合长度为 64KB。*/
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            //3. 四个参数 long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit
                            //第一个 是设置读超时的时间 即测试端(服务端我们的服务器) "一定时间内"<未接收>到被测试端(客户端)的消息
                            //第二个 是设置写超时的时间 即测试端"一定时间内"向测试端(客户端)<发送>的消息
                            //第三个是 所有类型的超时时间  第四个是时间的单位 unit单位
                            pipeline.addLast(new IdleStateHandler(6,0,0, TimeUnit.SECONDS));
                            /*上面的处理器是设置心跳规则  下面这个是具体处理心跳规则*/
                            pipeline.addLast(new HandlerHeartBeat());
                            //将http协议升级为ws协议,对websocket支持 并设置根访问为ws 就像我们的http写的是api
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true,64 * 1024,true,true,10000L));
//                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                            pipeline.addLast(handlerWebsocket);//ws连接处理  获取ws连接和断开连接的处理相当于http的controller层的代码

                        }
                    });
            //
            Integer wsPort = appConfig.getWsPort();//直接读取配置文件  写死的
            String wsPortInProperty = System.getProperty("ws.port");//运行时!!!!!!!!  定义启动参数修改了ws.port后的值
            if(!StringTools.isEmpty(wsPortInProperty)){
                wsPort = Integer.parseInt(wsPortInProperty);
            }

            ChannelFuture channelFuture = serverBootstrap.bind(wsPort).sync();//不要硬编码
            logger.info("netty服务启动成功,端口:{}",appConfig.getWsPort());
            channelFuture.channel().closeFuture().sync();

        }catch (Exception e){
            e.printStackTrace();
            logger.error("启动netty失败",e);
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

//    @Async
//    public void startNetty(){
//
//    }
//
//
//    public static void main(String[] args) {
//
//    }


}
