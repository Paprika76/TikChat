package com.tikchat.websocket;

import com.tikchat.entity.dto.MessageSendDto;
import com.tikchat.utils.JsonUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @ClassName MessageHandler
 * @Description 消息处理器  发送redisson消息队列到多服务器上
 * @Author Paprika
 * @date 2024-07-19
 **/
@Component("messageHandler")
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private static final String MESSAGE_TOPIC = "message.topic";

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @PostConstruct//服务启动时就会开启这个函数  rTopic.addListener不断监听 如果有MessageSendDto的class的东西传过来 那么就会执行操作
    public void listenMessageFromRedisson() {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDto.class, (MessageSendDto, sendDto) -> {
            logger.info("收到广播消息:{}", JsonUtils.convertObj2Json(sendDto));
            channelContextUtils.sendMessage(sendDto);
        });
    }
        //我们是众多服务器中的一台  所以我们接收到用户消息后 发消息到所有服务器上
    // 然后在所有服务器上都会查找有没有messageSendDto中的receiveUserId  找到了就会发送过去
    /*所以我们这里就需要*/
    public void sendMessage(MessageSendDto messageSendDto){
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.publish(messageSendDto);//将Dto广播发给所有服务器  然后真正的发消息是在听广播的代码逻辑那里
        // 每个服务器都发  只是说发送时没找到接收者的channel  因此没找到的话就直接没发成功呗 找到了的话就会发过去了呗
    }

}
