package com.tikchat.mapper;

import com.tikchat.entity.ChatSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 会话信息 Mapper 接口
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
@Mapper
@Repository
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

}
