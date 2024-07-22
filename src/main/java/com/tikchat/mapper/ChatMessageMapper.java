package com.tikchat.mapper;

import com.tikchat.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 聊天消息表 Mapper 接口
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
@Mapper
@Repository
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

}
