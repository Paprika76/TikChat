package com.tikchat.mapper;

import com.tikchat.entity.AppUpdate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * app发布 Mapper 接口
 * </p>
 *
 * @author Paprika
 * @since 2024-06-30
 */
@Mapper
@Repository
public interface AppUpdateMapper extends BaseMapper<AppUpdate> {

//    @Select("SELECT * FROM `app_update` WHERE app_version>\"${appVersion}\" and " +
//            "status=2 or(status=1 and FIND_IN_SET(\"#{uid}\",grayscale_uid)) ORDER BY id desc limit 0,1")
//    AppUpdate selectLatestAppUpdate(@Param("appVersion") String appVersion, @Param("uid") String uid);

    @Select("SELECT * FROM `app_update` WHERE app_version>#{appVersion} and " +
            " (status=2 or(status=1 and FIND_IN_SET(#{uid},grayscale_uid))) ORDER BY id desc limit 0,1")
    AppUpdate selectLatestAppUpdate(@Param("appVersion") String appVersion, @Param("uid") String uid);


}
