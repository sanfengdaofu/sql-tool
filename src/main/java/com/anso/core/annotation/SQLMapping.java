package com.anso.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLMapping {
    boolean id() default false;      //是否为主键

    String tableName() default "";   //数据库表名

    String column() default "";       //数据库列名

    boolean like() default false;    //该字段是否可以模糊查询,面向对象查询需要用到，暂无此功能。

    boolean ignorePacking() default false;  //是否忽略封装该字段

}
