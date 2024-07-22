package com.tikchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.entity.UserInfoBeauty;
import com.tikchat.entity.enums.BeautyAccountStatusEnum;
import com.tikchat.entity.enums.ResponseCodeEnum;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.UserContactMapper;
import com.tikchat.mapper.UserInfoBeautyMapper;
import com.tikchat.mapper.UserInfoMapper;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.UserContactService;
import com.tikchat.service.UserInfoBeautyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tikchat.service.UserInfoService;
import com.tikchat.service.component.ServiceComponent;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 靓号表
 服务实现类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-22
 */
@Service
public class UserInfoBeautyServiceImpl extends ServiceImpl<UserInfoBeautyMapper, UserInfoBeauty> implements UserInfoBeautyService {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserInfoBeautyService userInfoBeautyService;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserInfoBeautyMapper userInfoBeautyMapper;

    @Resource
    private ServiceComponent serviceComponent;

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserContactMapper userContactMapper;


    /**
     * 法一傻逼
     *  编写时间 2024-06-30 03:40
     * @param beauty
     */
    @Override
    public void saveBeautyAccount(UserInfoBeauty beauty) {
        // 这是真的save了 就是包含编辑和新增！！！
        /*因为脑瘫的原因 导致他妈的不知道为什么老罗要用对象做参数
        这样的话我必须要传userId而不是user_id 这不是脑瘫是什么？ 而且传user_id就直接报错 因为根本没限制是不是null
        本来可以直接在参数获取中写的  直接就是userId和email必传 其他的参数：id不必传*/
        // TODO 把对象参数改成多参数  多方面的原因：校验麻烦 名称问题很大
        //  1.你乱传参数我识别不到就很傻逼 我直接写参数名叫什么你就传什么参数名 这样才好
        //  2.参数没法方便的限制格式  email和userId都是有格式的 你他妈乱写一同照样能insert 这不乱套了啊
        //  3. 所以我写了一个友好的版本在下面的另一个方法！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        // 首先判断这个靓号是否已经使用！！！！  如果已经使用就不能修改了
        // 其实本来写代码是不会一开始就考虑这个的 一开始可以先写修改的情况 然后新增 然后再判断遗漏了什么 再在前面写
        if(beauty.getUserId()==null||beauty.getEmail()==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        /*判断输入的userId对应的账号是否已经是used已经注册使用了*/
        QueryWrapper<UserInfoBeauty> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", beauty.getUserId());
        UserInfoBeauty dbInfo = userInfoBeautyMapper.selectOne(wrapper);
        /*注意这里的写法 以后要多采纳  就是先dbInfo!=null 那么就限定死了只有这个成立才会判断后面的东西
        * 等于是：if(dbInfo!=null){  if(   ){}        }
        * 即if里面套if*/
        if (dbInfo!=null && BeautyAccountStatusEnum.USED.getStatus().equals(dbInfo.getStatus())){
            //说明已经使用  不能修改
            throw new BusinessException("该靓号已经使用");
        }
        beauty.setStatus(BeautyAccountStatusEnum.NO_USE.getStatus());



        //接下来判断编辑还是新增
        //前端是只输入两个值：email和uid uid不一定是12位的！！！！！
        /*如果邮箱不存在 并且uid也不存在 并且靓号的id也和我传过来的不一样（其实没传id那么就是新增  传了id那么就是编辑）
         是老罗这个忽悠怪没有说明白  这段他写的不好我觉得 */
        QueryWrapper<UserInfoBeauty> userInfoBeautyQueryWrapper = new QueryWrapper<>();
        userInfoBeautyQueryWrapper.eq("email", beauty.getEmail());
        UserInfoBeauty userInfoBeautyByEmail = userInfoBeautyMapper.selectOne(userInfoBeautyQueryWrapper);

        userInfoBeautyQueryWrapper = new QueryWrapper<>();
        userInfoBeautyQueryWrapper.eq("user_id", beauty.getUserId());
        UserInfoBeauty userInfoBeautyByUserId = userInfoBeautyMapper.selectOne(userInfoBeautyQueryWrapper);

        //所以接上面的注释 直接判断前端有没有传id
        if (beauty.getId()==null){
            //说明是新增
            if(userInfoBeautyByEmail!=null){
                throw new BusinessException("该邮箱已经被其他靓号使用，无法再新增");
            }
            if (userInfoBeautyByUserId!=null){
                throw new BusinessException("该靓号UID已经添加，无法再新增");
            }
            //新增
            userInfoBeautyMapper.insert(beauty);
        }

        //说明是编辑 要判断这个信息是不是我自己  因为编辑也是可以新增的
        // 因为这两个值都是唯一的 所以编辑如果两个值都不一样那么也算是新增
        /*首先 判断输入的email对应的账号存不存在  还有是不是我*/
        if (userInfoBeautyByEmail!=null&&!userInfoBeautyByEmail.getId().equals(beauty.getId())){//email对应的账号存在
            //email已存在 接下来判断查出来的  //账号存在并且不是我 说明邮箱已使用
                throw new BusinessException("邮箱已被使用");
        }
        /*判断输入的UID靓号对应的账号存不存在  还有是不是我*/
        if (userInfoBeautyByUserId!=null&&!userInfoBeautyByUserId.getId().equals(beauty.getId())){//email对应的账号存在
            //email已存在 接下来判断查出来的  //账号存在并且不是我 说明邮箱已使用
            throw new BusinessException("该靓号UID已存在");
        }
        //直接updateOrInsert  查有没有这个id的号有的话就直接修改 没的话就新增
        UserInfoBeauty userInfoBeauty = userInfoBeautyMapper.selectById(beauty.getId());
        if (userInfoBeauty==null){
            userInfoBeautyMapper.insert(beauty);
        }else{
            QueryWrapper<UserInfoBeauty> userInfoBeautyQueryWrapper2 = new QueryWrapper<>();
            userInfoBeautyQueryWrapper2.eq("id", beauty.getId());
            userInfoBeautyMapper.update(beauty,userInfoBeautyQueryWrapper2);
        }




    }


    /**
     * 法二才是王道！！！！！！！！！！！！！！！！！！！！！！！！！！！！
     *  编写时间 2024-06-30 03:40
     */
    @Override
    //注意我们传参数分两种：1.  null,userId,email
                    //  2.   id, userId,email
    /*注意 传参进来之前已经在controller里获取前端传过来的参数时自动限制参数的格式了*/
    public void saveBeautyAccount2(Integer id,String userId,String email) {
        UserInfoBeauty beauty = new UserInfoBeauty();
        beauty.setId(id);
        beauty.setUserId(userId);
        beauty.setEmail(email);
        // 这是真的save了 就是包含编辑和新增！！！
        /*因为脑瘫的原因 导致他妈的不知道为什么老罗要用对象做参数
        这样的话我必须要传userId而不是user_id 这不是脑瘫是什么？ 而且传user_id就直接报错 因为根本没限制是不是null
        本来可以直接在参数获取中写的  直接就是userId和email必传 其他的参数：id不必传*/
        // TODO 把对象参数改成多参数  多方面的原因：校验麻烦 名称问题很大
        //  1.你乱传参数我识别不到就很傻逼 我直接写参数名叫什么你就传什么参数名 这样才好
        //  2.参数没法方便的限制格式  email和userId都是有格式的 你他妈乱写一同照样能insert 这不乱套了啊
        //  3. 所以我写了一个友好的版本在下面的另一个方法！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        // 首先判断这个靓号是否已经使用！！！！  如果已经使用就不能修改了
        // 其实本来写代码是不会一开始就考虑这个的 一开始可以先写修改的情况 然后新增 然后再判断遗漏了什么 再在前面写
        if(beauty.getUserId()==null||beauty.getEmail()==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        /*判断输入的userId对应的账号是否已经是used已经注册使用了*/
        QueryWrapper<UserInfoBeauty> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", beauty.getUserId());
        UserInfoBeauty dbInfo = userInfoBeautyMapper.selectOne(wrapper);
        /*注意这里的写法 以后要多采纳  就是先dbInfo!=null 那么就限定死了只有这个成立才会判断后面的东西
         * 等于是：if(dbInfo!=null){  if(   ){}        }
         * 即if里面套if*/
        if (dbInfo!=null && BeautyAccountStatusEnum.USED.getStatus().equals(dbInfo.getStatus())){
            //说明已经使用  不能修改
            throw new BusinessException("该靓号已经使用");
        }
        beauty.setStatus(BeautyAccountStatusEnum.NO_USE.getStatus());



        //接下来判断编辑还是新增
        //前端是只输入两个值：email和uid uid不一定是12位的！！！！！
        /*如果邮箱不存在 并且uid也不存在 并且靓号的id也和我传过来的不一样（其实没传id那么就是新增  传了id那么就是编辑）
         是老罗这个忽悠怪没有说明白  这段他写的不好我觉得 */
        QueryWrapper<UserInfoBeauty> userInfoBeautyQueryWrapper = new QueryWrapper<>();
        userInfoBeautyQueryWrapper.eq("email", beauty.getEmail());
        UserInfoBeauty userInfoBeautyByEmail = userInfoBeautyMapper.selectOne(userInfoBeautyQueryWrapper);

        userInfoBeautyQueryWrapper = new QueryWrapper<>();
        userInfoBeautyQueryWrapper.eq("user_id", beauty.getUserId());
        UserInfoBeauty userInfoBeautyByUserId = userInfoBeautyMapper.selectOne(userInfoBeautyQueryWrapper);

        //所以接上面的注释 直接判断前端有没有传id
        if (beauty.getId()==null){
            //说明是新增
            if(userInfoBeautyByEmail!=null){
                throw new BusinessException("该靓号邮箱已经存在，无法再新增");
            }
            if (userInfoBeautyByUserId!=null){
                throw new BusinessException("该靓号UID已经添加，无法再新增");
            }
            //新增
            userInfoBeautyMapper.insert(beauty);
        }



        //说明是编辑 要判断这个信息是不是我自己  因为编辑也是可以新增的
        // 因为这两个值都是唯一的 所以编辑如果两个值都不一样那么也算是新增
        /*首先 判断输入的email对应的账号存不存在  还有是不是我*/
        if (userInfoBeautyByEmail!=null&&!userInfoBeautyByEmail.getId().equals(beauty.getId())){//email对应的账号存在
            //email已存在 接下来判断查出来的  //账号存在并且不是我 说明邮箱已使用
            throw new BusinessException("邮箱已被注册使用");
        }
        /*判断输入的UID靓号对应的账号存不存在  还有是不是我*/
        if (userInfoBeautyByUserId!=null&&!userInfoBeautyByUserId.getId().equals(beauty.getId())){//email对应的账号存在
            //email已存在 接下来判断查出来的  //账号存在并且不是我 说明邮箱已使用
            throw new BusinessException("该靓号UID已被注册使用");
        }


        //直接updateOrInsert  查有没有这个id的号有的话就直接修改 没的话就新增
        UserInfoBeauty userInfoBeauty = userInfoBeautyMapper.selectById(beauty.getId());
        if (userInfoBeauty==null){
            userInfoBeautyMapper.insert(beauty);
        }else{
            QueryWrapper<UserInfoBeauty> userInfoBeautyQueryWrapper2 = new QueryWrapper<>();
            userInfoBeautyQueryWrapper2.eq("id", beauty.getId());
            userInfoBeautyMapper.update(beauty,userInfoBeautyQueryWrapper2);
        }




    }



}


