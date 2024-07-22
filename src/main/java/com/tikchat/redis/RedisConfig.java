package com.tikchat.redis;

import com.tikchat.websocket.MessageHandler;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;


@Configuration //或者写@Component
//@Component
//@Primary
public class RedisConfig<V> {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    @Value("${spring.redis.host:}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private Integer redisPort;

    //以下两个其实就是两个服务的配置：
    //Bean是修改运行时类对象bean的设置
    //两个服务分别为RedissonClient即redisson    和  RedisTemplate也即：redis
    /*这个配置只是修改了redisson的端口地址*/
    @Bean(name="redissonClient",destroyMethod = "shutdown")
    public RedissonClient redissonClient(){
        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://"+redisHost+":"+redisPort);
            RedissonClient redissonClient = Redisson.create(config);
            return redissonClient;
//            redissonClient.set
        }catch (Exception e){
//            e.printStackTrace();
            logger.info("redis配置错误，请检查redisson配置.错误信息：{}",e);
        }
        return null;
    }


    @Bean("redisTemplate")
//    @Primary
    public RedisTemplate<String,V> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, V> template = new RedisTemplate<String, V>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet();
        return template;
    }
}
