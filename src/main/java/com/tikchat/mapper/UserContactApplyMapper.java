package com.tikchat.mapper;

import com.tikchat.entity.UserContactApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tikchat.entity.dto.UserContactApplyDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 联系人申请 Mapper 接口
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@Mapper
@Repository
public interface UserContactApplyMapper extends BaseMapper<UserContactApply> {
//    // 内连接查询，通过 contact_id 直接获取关联的用户信息
//    @Select("SELECT u.sex,u.nick_name " +
//            "FROM user_info u " +
//            "INNER JOIN user_contact c ON u.user_id = c.user_id " +
//            "WHERE c.contact_id = #{contact_id} order by c.update_time")
//    List<UserInfo> selectUserInfoListByContactId(String contact_id);

    // 内连接查询，通过 contact_id 直接获取关联的用户信息


//    @Select("SELECT u.sex,u.nick_name " +
//            "FROM user_info u " +
//            "INNER JOIN user_contact c ON u.user_id = c.user_id " +
//            "WHERE c.contact_id = #{contact_id} order by c.update_time")

    @Select("SELECT user_contact_apply.*,u.nick_name, CASE WHEN user_contact_apply.contact_type = 1 THEN g.group_name END AS groupName  FROM `user_contact_apply`  LEFT JOIN user_info u on u.user_id=user_contact_apply.apply_user_id LEFT JOIN group_info g on g.group_id= user_contact_apply.contact_id where receive_user_id=#{receive_user_id} Order by last_apply_time Desc")
    List<UserContactApplyDto> selectContactApplyListByReceiveUserId(String receive_user_id);


}
