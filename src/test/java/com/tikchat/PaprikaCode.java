package com.tikchat;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.ArrayList;

public class PaprikaCode {
    public static void main(String[] args) {
        AutoGenerator mpg = new AutoGenerator();

        //1.全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath+"/src/main/java/");
        gc.setAuthor("Paprika");
        gc.setOpen(false);
        gc.setFileOverride(false);

        gc.setServiceName("%sService");
        gc.setIdType(IdType.ID_WORKER);
        gc.setDateType(DateType.ONLY_DATE);
        //对gc.setDateType(DateType.ONLY_DATE);的解释：！！！！！！！！！！！
//        ONLY_DATE：表示只使用 java.util.Date 类型来表示日期时间。
//        SQL_PACK：表示使用 java.sql.Date、java.sql.Time 和 java.sql.Timestamp 来分别表示日期、时间和日期时间。
//        TIME_PACK：表示使用 java.time 包中的日期时间类型，如 java.time.LocalDateTime、java.time.LocalDate、java.time.LocalTime 等。
        gc.setSwagger2(true);
        mpg.setGlobalConfig(gc);



        //2.数据库配置
        DataSourceConfig dsc = new DataSourceConfig();
//        dsc.setUrl("jdbc:mysql://localhost:3306/a_learning_for_mybatis_plus?useSSL=false&useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8");
        dsc.setUrl("jdbc:mysql://localhost:3306/tikchat?useSSL=false&useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("123456");
        dsc.setDbType(DbType.MYSQL);

        //为了将傻逼的tinyint识别为Boolean改为Integer！！！！！！！！！傻逼的Mybatis-plus!!!!!!
        dsc.setTypeConvert(new MySqlTypeConvertCustom());
        mpg.setDataSource(dsc);

        //3.包配置 目录配置 生成文件夹配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName("tikchat");

        pc.setParent("com");
        pc.setEntity("entity");
        pc.setController("controller");
        pc.setMapper("mapper");
        pc.setService("service");
        mpg.setPackageInfo(pc);

        //4.策略配置
        StrategyConfig strategy = new StrategyConfig();
//        strategy.setInclude("user_contact,user_contact_apply");//设置要映射的表名
        strategy.setInclude("chat_session_user");//设置要映射的表名
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(true);
        strategy.setLogicDeleteFieldName("deleted");
        //自动填充配置   一条数据生成的时间和更新时间的配置
//        TableFill gmtCreate = new TableFill("gmt_create", FieldFill.INSERT);
//        TableFill gmtModified = new TableFill("gmt_modified", FieldFill.INSERT_UPDATE);
        TableFill gmtCreate = new TableFill("create_time", FieldFill.INSERT);
        TableFill gmtModified = new TableFill("update_time", FieldFill.INSERT_UPDATE);

        ArrayList<TableFill> tableFills = new ArrayList<>();
        tableFills.add(gmtCreate);
        tableFills.add(gmtModified);
        strategy.setTableFillList(tableFills);

        //乐观锁 配置
        strategy.setVersionFieldName("version");
        //设置url请求路径命名方式
        strategy.setControllerMappingHyphenStyle(true);//contorller的url设置不使用驼峰而是使用下划线
        //如@GetMapping("/getUserById")变成了@GetMapping("/get_user_by_id")
        //其实按照我的习惯应该是设为false的  但是我现在还不是很清楚两者之间的差别
        mpg.setStrategy(strategy);

        //指定自定义模板路径, 位置：/resources/templates/entity2.java.ftl(或者是.vm)
        //注意不要带上.ftl(或者是.vm), 会根据使用的模板引擎自动识别
        TemplateConfig templateConfig = new TemplateConfig()
                .setController("templates/controller.java");
        //配置自定义模板
        mpg.setTemplate(templateConfig);


        mpg.execute();

    }
}



/**
 * 自定义类型转换
 */
class MySqlTypeConvertCustom extends MySqlTypeConvert implements ITypeConvert {
    @Override
    public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
        String t = fieldType.toLowerCase();
        if (t.contains("tinyint(1)")) {
            return DbColumnType.INTEGER;
        }
        return super.processTypeConvert(globalConfig, fieldType);
    }
}
