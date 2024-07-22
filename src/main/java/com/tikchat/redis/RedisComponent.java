package com.tikchat.redis;


import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.SysSettingDto;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.utils.StringTools;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RedisComponent {
    //其实就是封装一个session包含基于普通的redis操作的一个类似于session的对象

    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取心跳！！！！！！！！！！！！！！！！   用于判断是否还在登陆状态
     * @param userId
     * @return
     */
    public Long getUserHeartBeat(String userId){
        return (Long) redisUtils.get(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    public void saveUserHeartBeat(String userId){
        redisUtils.setex(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId,System.currentTimeMillis(),Constants.REDIS_TIME_EXPIRE_HEART_BEAT);
    }

    public void removeUserHeartBeat(String userId){
        redisUtils.del(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    //专门用来保存token:
    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto){
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN+tokenUserInfoDto.getToken(), tokenUserInfoDto,Constants.REDIS_TIME_1Day*2);
//        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID+tokenUserInfoDto.getToken(), tokenUserInfoDto.getToken(),Constants.REDIS_TIME_1Day);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID+tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(),Constants.REDIS_TIME_1Day*2);
    }

    //专门用来查询token:
    public TokenUserInfoDto getTokenUserInfoDto(String token){
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
    }
    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String userId){
        return getTokenUserInfoDto((String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId));
    }


    public void removeTokenUserInfoDto(String userId){
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
        if (StringTools.isEmpty(token)){
            return ;
        }
        try {
            redisUtils.del(Constants.REDIS_KEY_WS_TOKEN_USERID+userId);
            redisUtils.del(Constants.REDIS_KEY_WS_TOKEN+token);
        }catch (Exception e){
            //空
        }

    }
    // 存放sysSetting
    public SysSettingDto getSysSetting(){
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto = sysSettingDto == null ? new SysSettingDto() : sysSettingDto;
        return sysSettingDto;
    }

    // 存放sysSetting
    public void saveSysSetting(SysSettingDto sysSettingDto){
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    //我们说过这个channel不可序列化  所以不能放进redis中去  因为redis只能存储基础的简单的对象
    // (其实也就是不同平台的不兼容)  因为channel是一个特定的对象
    // 比如说有某些依赖关系 甚至还有系统的依赖关系   而不只是说是一个简单的对象有哪些字段
    // 而redis中存储的东西无非就是 字符串 哈希 列表 集合 有序集合
    // 我们要存储的channel有些java特性或是某些依赖 不只是一个简单的对象 因此不能存进去!!!!!!!!!
    /*因此以下直接写的方式是不行的   set方法我们使用的是redis的opsForValue()
    是字符串数据结构  会自动序列化和反序列化存储对象 但是channel这个java对象不行!!!!!!!!!!!!!!! */
//    public void saveChannel(String userId, Channel channel){
//        redisUtils.set(Constants.REDIS_KEY_WS_TOKEN + userId, channel);
//    }
//    public Channel getChannel(String userId){
//        return (Channel) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + userId);
//    }
    /*因此我们就使用其他办法*/
    // TODO 存channel这个不可序列化的对象


    // 编写时间 2024-07-16 13:53
    // 因为是队列(列表) 所以每次save其实都是添加 所以每次save都要先删除这个key里面的  所以我们还要新建一个函数clearContactIdList
    //TODO 没搞明白：为什么这个lPushAll和lPush还要设置expireTime为两天？？？？？？  Constants.REDIS_TIME_1Day*2
    // 好像已明白 是这样的：我们在login时一登陆成功就会先clearContactIdList 然后addContactIdList
    // 所以确保了每次登录都会是最新的ContactIdList  至于这个两天失效 是因为你每次登录都是2天的有效期 那肯定两个要保持一样啊！！！！！！！
    public void addContactIdList(String userId,List<String> ContactIdList){
        redisUtils.lPushAll(Constants.REDIS_KEY_USER_CONTACT+userId,ContactIdList,Constants.REDIS_TIME_1Day*2);
    }

    public void clearContactIdList(String userId){
        redisUtils.del(Constants.REDIS_KEY_USER_CONTACT+userId);
    }
    public void removeContactIdFromList(String userId,String contactId){
        redisUtils.remove(Constants.REDIS_KEY_USER_CONTACT+userId,contactId);
    }
    //添加单个联系人到缓存里！！！（是apply通过时addContact()里面调用的 添加了联系人后缓存里也要添加！！！）
    public void addContactId(String userId,String contactId){
        List<String> contactIdList = getContactIdList(userId);
        if(contactIdList.contains(contactId)){
            return ;
        }
        redisUtils.lPush(Constants.REDIS_KEY_USER_CONTACT+userId,contactId,Constants.REDIS_TIME_1Day*2);
    }


    public List<String> getContactIdList(String userId){
        return (List<String>) redisUtils.getQueueList(Constants.REDIS_KEY_USER_CONTACT+userId);
    }








}
