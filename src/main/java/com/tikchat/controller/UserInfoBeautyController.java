//
//package com.tikchat.controller;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.tikchat.entity.vo.ResponseVO;
//import org.springframework.web.bind.annotation.*;
//import javax.annotation.Resource;
//import java.util.List;
//
//
//import com.tikchat.service.UserInfoBeautyService;
//import com.tikchat.entity.UserInfoBeauty;
//
//
//import org.springframework.stereotype.Controller;
//
///**
// * <p>
// * 靓号表
// 前端控制器
// * </p>
// *
// * @author Paprika
// * @since 2024-06-22
// */
//@Controller
//@RequestMapping("/tikchat/user-info-beauty")
//    public class UserInfoBeautyController {
//@Resource
//private UserInfoBeautyService userInfoBeautyService;
//
////新增或者更新
//@PostMapping
//public ResponseVO<UserInfoBeauty> save(@RequestBody UserInfoBeauty userInfoBeauty) {
//    userInfoBeautyService.saveOrUpdate(userInfoBeauty);
//        return ResponseVO.success();
//        }
//
//
////删除
//@DeleteMapping("/{id}")
//public ResponseVO<UserInfoBeauty> delete(@PathVariable Integer id) {
//    userInfoBeautyService.removeById(id);
//        return ResponseVO.success();
//        }
//
//@PostMapping("/del/batch")
//public ResponseVO<UserInfoBeauty> deleteBatch(@RequestBody List<Integer> ids) {//批量删除
//        userInfoBeautyService.removeByIds(ids);
//        return Result.success();
//        }
//
////查询所有数据
//@GetMapping
//public ResponseVO<UserInfoBeauty> findAll() {
//        return Result.success(userInfoBeautyService.list());
//        }
//
//@GetMapping("/{id}")
//public ResponseVO<UserInfoBeauty> findOne(@PathVariable Integer id) {
//        return Result.success(userInfoBeautyService.getById(id));
//        }
//
//@GetMapping("/page")
//public ResponseVO<UserInfoBeauty> findPage(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
//        QueryWrapper<UserInfoBeauty> queryWrapper = new QueryWrapper<>();
//        queryWrapper.orderByDesc("id");
//        return Result.success(userInfoBeautyService.page(new Page<>(pageNum, pageSize), queryWrapper));
//        }
//        }
//
