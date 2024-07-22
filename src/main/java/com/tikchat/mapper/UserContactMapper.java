package com.tikchat.mapper;

import com.tikchat.entity.GroupInfo;
import com.tikchat.entity.UserContact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tikchat.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 联系人 Mapper 接口
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@Mapper
@Repository
public interface UserContactMapper extends BaseMapper<UserContact> {

    @Select("select user_id from user_contact where contact_id = #{0}")
    List<String> selectUserIdListByContactId(String contact_id);
//    Map<String, Object> selectUserIdByContactId(Integer contact_id);
//    @Select("select * from user_info where user_id in #{0}")
//    List<UserInfo> selectUserInfoByUserIds(List<String> userIds);

//    // 内连接查询，通过 contact_id 直接获取关联的用户信息
//    @Select("SELECT u.* " +
//            "FROM user_info u " +
//            "INNER JOIN user_contact c ON u.user_id = c.user_id " +
//            "WHERE c.contact_id = #{contact_id}")
//    List<UserInfo> selectUserInfosByContactId(String contact_id);

    // 内连接查询，通过 contact_id 直接获取关联的用户信息
    @Select("SELECT u.sex,u.nick_name " +
            "FROM user_info u " +
            "INNER JOIN user_contact c ON u.user_id = c.user_id " +
            "WHERE c.contact_id = #{contact_id} order by c.update_time")
    List<UserInfo> selectUserInfoListByContactId(String contact_id);

    // 内连接查询，通过 contact_id 直接获取关联的用户信息
    @Select("SELECT u.nick_name " +
            "FROM user_contact c " +
            "INNER JOIN user_info u ON u.user_id = c.contact_id " +
            "WHERE c.user_id = #{user_id} and (c.status in (1,3) OR (c.status = 5 AND c.version <> 1)) order by c.update_time")
    List<UserInfo> selectUserInfoListByUserIdFromUserContact(String user_id);

    /**
     * 注意 后面第二遍做这个项目的时候主要就是改进sql的编写  用xml写
     * 而不是用原生sql  我们要先在navicat里写原生sql通过后再在xml里写
     * 为了安全性！！为了安全性！！为了安全性！！为了安全性！！为了安全性！！为了安全性！！防止sql注入！！！！！！！！！！！！！！！
     * @param user_id
     * @return
     */
    //TODO 改进版需要做：用xml写sql
    // 还有改下面的一个bug  在我dealWithApply时 处理申请时
    // 如果直接拉黑人的话是也会把人加到UserContact表里的
    // 但是在申请人那里 会显示我们在“我的好友”的列表  因为就算被拉黑了也还是会显示的
    // 但问题就在于你们首先要之前是过好友才行吧？所以我们还要解决这个bug
    // 不过我已经通过原生的sql解决了一点了 下面的sql代码  也就是    (c.status in (1,3)  OR (c.status = 5 AND c.version <> 1))
    //  不过好像已经解决了   因为我使用了一个新的 UserContactStatusEnum 即：FRIEND_BLACK_ME_FIRST
    // 如果是这个enum那么就不让他显示在联系人列表  因为联系人列表显示的是FRIEND_BLACK_ME 还有其他几种enum

    @Select("SELECT g.group_name " +
            "FROM user_contact c " +
            "INNER JOIN group_info g ON g.group_id = c.contact_id " +
            "WHERE c.user_id = #{user_id} AND g.group_owner_id!=c.user_id " +
            " AND " +
            "(c.status in (1,3)  OR (c.status = 5 AND c.version <> 1)) order by c.update_time")


    List<GroupInfo> selectGroupInfoListByUserIdFromUserContact(String user_id);


//    // 内连接查询，通过 contact_id 直接获取关联的用户信息
//    @Select("SELECT u.sex,u.nick_name " +
//            "FROM user_info u " +
//            "INNER JOIN user_contact c ON u.user_id = c.user_id " +
//            "WHERE c.contact_id = #{contact_id} order by c.update_time")
//    List<UserInfo> selectUserInfoListByContactId(String contact_id);




}
