package com.tikchat.mapper;

import com.tikchat.entity.ChatSessionUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tikchat.entity.dto.ChatSessionUserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 会话用户 Mapper 接口
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
@Mapper
@Repository
public interface ChatSessionUserMapper extends BaseMapper<ChatSessionUser> {

//    @Select("select u.*,s.last_receive_time from chat_session_user u" +
    @Select("select u.* from chat_session_user u" +
            " inner join chat_session s on s.session_id=u.session_id " +
            "where u.user_id=#{user_id} order by s.last_receive_time desc")
    List<ChatSessionUser> selectChatSessionListByUserId(String user_id);

    @Select("select u.*,s.last_message,s.last_receive_time," +
            "(select count(*) from user_contact uc where uc.contact_id=u.contact_id ) as member_count from " +
            "chat_session_user u inner join chat_session s on s.session_id=u.session_id " +
            "where u.user_id=#{user_id} order by s.last_receive_time desc")
    List<ChatSessionUserDto> selectChatSessionDTOListByUserId(String user_id);

}
