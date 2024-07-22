package com.tikchat;

import com.tikchat.redis.RedisUtils;
import com.tikchat.websocket.netty.NettyWebSocketStarter;
import io.lettuce.core.RedisConnectionException;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import org.slf4j.Logger;

@Component("initRun")
public class InitRun implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitRun.class);

    @Resource
    private DataSource dataSource;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private NettyWebSocketStarter nettyWebSocketStarter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            dataSource.getConnection();
            redisUtils.get("test");
//            nettyWebSocketStarter.startNetty();
            new Thread(nettyWebSocketStarter).start();
            logger.info("服务启动成功，可以开始愉快的开发了！");
        }catch (SQLException e){
//            e.printStackTrace();
            logger.error("数据库配置错误,请检查数据库配置！");
            System.out.println("数据库配置错误");
        }catch (RedisConnectionException e){
            logger.error("redis配置错误,请检查redis配置！");
        }catch (Exception e){
            logger.error("服务器启动失败:",e);
        }

    }
}
