package com.anso.domain;

import com.anso.core.annotation.SQLMapping;

@SQLMapping(tableName = "a")
public class Student {
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name1='" + name1 + '\'' +
                ", age=" + age +
                ", pid=" + pid +
                '}';
    }

    //    @SQLMapping(id = true, ignorePacking = true)
    private int id;
        @SQLMapping(column = "name")
    private String name1;

    private int age;

    private int pid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
}
