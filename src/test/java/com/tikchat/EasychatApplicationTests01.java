package com.tikchat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tikchat.entity.UserInfo;
import com.tikchat.entity.UserInfoBeauty;
import com.tikchat.mapper.UserInfoBeautyMapper;
import com.tikchat.mapper.UserInfoMapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest
@MapperScan("com.tikchat.mapper")
class tikchatApplicationTests01 {

    // 继承了BaseMapper，所有的方法都来自己父类
    // 我们也可以编写自己的扩展方法！
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserInfoBeautyMapper userInfoBeautyMapper;

    @Test
    void contextLoads() {
        // 参数是一个 Wrapper ，条件构造器，这里我们先不用 null
        // 查询全部用户
        List<UserInfo> users = userInfoMapper.selectList(null);
        users.forEach(System.out::println);
    }

    // 测试插入
    @Test
    public void testInsert(){
        UserInfo user = new UserInfo();
        user.setUserId("U12451251813");
        user.setEmail("3235104591@qq.com");
        user.setPassword("123456");
        user.setJoinType(1);
        user.setNickName("paprika");
        user.setPersonalSignature("真好笑");
        user.setSex(1);
        user.setStatus(1);
//        user.setLastLoginTime(new Date());
        user.setAreaName("江西-赣州");
        user.setAreaCode("342300");

        int result = userInfoMapper.insert(user); // 帮我们自动生成id
        System.out.println(result); // 受影响的行数
        System.out.println(user); // 发现，id会自动回填
    }

    // 测试插入
    @Test
    public void testInsertBeauty(){
        UserInfoBeauty user = new UserInfoBeauty();
        user.setUserId("U12451251813");
//        user.setId("U12451251813");
        user.setEmail("3235104591@qq.com");
        user.setStatus(1);

        int result = userInfoBeautyMapper.insert(user); // 帮我们自动生成id
        System.out.println(result); // 受影响的行数
        System.out.println(user); // 发现，id会自动回填
    }


//
//    // 测试更新
//    @Test
//    public void testUpdate(){
//        UserInfo user = new UserInfo();
//        // 通过条件自动拼接动态sql
//        user.setId(8L);
//        user.setName("aaa关注公众号：狂神说");
//        user.setAge(20);
//        // 注意：updateById 但是参数是一个 对象！
//        int i = userInfoMapper.updateById(user);
//        System.out.println(i);
//    }
//
//    // 测试乐观锁成功！
//    @Test
//    public void testOptimisticLocker(){
//        // 1、查询用户信息
//        UserInfo user = userInfoMapper.selectById(6L);
//        // 2、修改用户信息
//        user.setName("kuangshen");
//        user.setEmail("24736743@qq.com");
//        // 3、执行更新操作
//        userInfoMapper.updateById(user);
//    }
//
//
//    // 测试乐观锁失败！多线程下
//    @Test
//    public void testOptimisticLocker2(){
//
//        // 线程 1
//        UserInfo user = userInfoMapper.selectById(1L);
//        user.setName("kuangshen111");
//        user.setEmail("24736743@qq.com");
//
//        // 模拟另外一个线程执行了插队操作
//        UserInfo user2 = userInfoMapper.selectById(1L);
//        user2.setName("kuangshen222");
//        user2.setEmail("24736743@qq.com");
//        userInfoMapper.updateById(user2);
//
//        // 自旋锁来多次尝试提交！
//        userInfoMapper.updateById(user); // 如果没有乐观锁就会覆盖插队线程的值！
//    }
//

    // 测试查询
    @Test
    public void testSelect01(){
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("nick_name", "paprika");
        List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
        System.out.println(userInfos);
    }

    // 测试查询
    @Test
    public void testSelect02(){
        Map<String, Object> map = new HashMap<>();
        map.put("nick_name","paprika");
//        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
//        userInfoQueryWrapper.eq("nick_name", "paprika");
        List<UserInfo> userInfos = userInfoMapper.selectByMap(map);
        System.out.println(userInfos);
    }

    // 测试查询
    @Test
    public void testSelect03(){
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("nick_name", "paprika");
        List<UserInfo> userInfos = userInfoMapper.selectList(userInfoQueryWrapper);
        System.out.println(userInfos);
    }

    // 测试查询
    @Test
    public void testSelectById(){
        UserInfo user = userInfoMapper.selectById("U12451251813");
        System.out.println(user);
    }




//
//    // 测试批量查询！
//    @Test
//    public void testSelectByBatchId(){
//        List<UserInfo> users = userInfoMapper.selectBatchIds(Arrays.asList(1, 2, 3));
//        users.forEach(System.out::println);
//    }
//
//    // 按条件查询之一使用map操作
//    @Test
//    public void testSelectByBatchIds(){
//        HashMap<String, Object> map = new HashMap<>();
//        // 自定义要查询
//        map.put("name","狂神说Java");
//        map.put("age",3);
//
//        List<UserInfo> users = userInfoMapper.selectByMap(map);
//        users.forEach(System.out::println);
//    }
//
    // 测试分页查询
    @Test
    public void testPage(){
        //  参数一：当前页
        //  参数二：页面大小
        //  使用了分页插件之后，所有的分页操作也变得简单的！
        Page<UserInfo> page = new Page<>(1,5);
//        page.setDesc(Collections.singletonList(OrderItem.desc("created_at")));
        // 使用 QueryWrapper 设置排序规则
//        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
//        queryWrapper.orderBy(true, true, "update_time"); // 第一个 true 表示是否进行排序，第二个 true 表示升序，字段名为 "created_at"

//        userInfoMapper.selectPage(page,null);
        userInfoMapper.selectPage(page,new QueryWrapper<UserInfo>().orderBy(true, false, "update_time"));
        page.getRecords().forEach(System.out::println);
        System.out.println(page.getRecords());
        System.out.println(page.getTotal());

    }
//
//
//    // 测试删除
//    @Test
//    public void testDeleteById(){
//        userInfoMapper.deleteById(1L);
//    }
//
//    // 通过id批量删除
//    @Test
//    public void testDeleteBatchId(){
//        userInfoMapper.deleteBatchIds(Arrays.asList(1240620674645544961L,1240620674645544962L));
//    }
//
//    // 通过map删除
//    @Test
//    public void testDeleteMap(){
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("name","狂神说Java");
//        userInfoMapper.deleteByMap(map);
//    }
//
    @Test
    public void test101(){
        String latest = "0.1.102";
        String toSaveAppUpdate = "0.0.10101";
        System.out.println(latest.replaceFirst("\\.", ""));
        System.out.println(toSaveAppUpdate.replaceFirst("\\.",""));
        Double latestVersion = Double.parseDouble(latest.replaceFirst("\\.", ""));
        Double currentVersion = Double.parseDouble(toSaveAppUpdate.replaceFirst("\\.",""));
        System.out.println("latestVersion:"+latestVersion);
        System.out.println("currentVersion:"+currentVersion);

}

}
