package com.anso;


import com.anso.core.handle.SQLHandle;
import com.anso.core.util.ClassIdentification;
import com.anso.domain.Student;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlTest {


    @Test
    public void test() throws Exception {

        SQLHandle sqlHandle = new SQLHandle();
        List<Student> list = new ArrayList<>();
        long l = 0;
        try {
            for (int i = 0; i < 1; i++) {
                Student student = new Student();
                student.setAge(i);
                student.setPid(i + 1);
                student.setId(i + 10);
                student.setName1("aaa");
                //设置数据源,一定要在执行sql前设置.如果设置了手动事务,开启多线程就要注意了,连接都是根据线程环境获取的.
//                sqlHandle.setDataSource("a");
                //插入单条,同时插入多个表,JavaBean本身的表也要填写进去
//                sqlHandle.insertPro("insert into user (id,username) values(?,?)", student.getId(), student.getName1());
//                sqlHandle.insertPro(student);
                list.add(student);
            }
            System.out.println("-----------------------------");
            //手动开启事务
            sqlHandle.startTransaction();
            for (int i = 0; i < 2; i++) {
                sqlHandle.insertAll(list, "a2", "a1", "a");
                sqlHandle.rollback();
                sqlHandle.commit();
            }
            l = System.currentTimeMillis();
//            int q = 10 / 0;
            //手动提交事务
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis() - l);
        System.out.println();

//        System.out.println(ClassIdentification.isJDKClass(Student.class));
    }


    @Test
    public void test2() throws SQLException {

        SQLHandle sqlHandle = new SQLHandle();
//        long student = sqlHandle.selectOne("select count(1) from a", Long.class);
        List<Map> student = sqlHandle.selectMany("select * from a", Map.class);
//        List<Student> student = sqlHandle.selectMany("select * from a", Student.class);
//        List<Integer> student = sqlHandle.selectMany("select id from a", int.class);
//        String student = sqlHandle.selectOne("select name from a where id = 1", String.class);
        System.out.println(student);
    }

    @Test
    public void test3() {
//        SQLHandle sqlHandle = new SQLHandle();
//        System.out.println(sqlHandle.update("update a set pid = -3 where id = ?", 2));
//        System.out.println(sqlHandle.delete("delete from a2 "));
        System.out.println(ClassIdentification.isJDKClass(List.class));
        System.out.println(ClassIdentification.isJDKClass(byte.class));
        System.out.println(ClassIdentification.isJDKClass(Short.class));
        System.out.println(ClassIdentification.isJDKClass(BigDecimal.class));
        System.out.println(ClassIdentification.isJDKClass(Date.class));
        System.out.println(ClassIdentification.isJDKClass(int.class));
    }

}
