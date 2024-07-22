package com.tikchat.mapper;

import com.tikchat.entity.GroupInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@Mapper
@Repository
public interface GroupInfoMapper extends BaseMapper<GroupInfo> {
    @Select("select group_info.*,user_info.nick_name from group_info inner join user_info on user_info.user_id=group_info.group_owner_id    order by create_time desc")
//    List<UserContactApplyDto> selectGroupList();
    List<Map<String, Object>> selectGroupList();

}
