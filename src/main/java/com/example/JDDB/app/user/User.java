package com.example.JDDB.app.user;


import com.example.JDDB.lib.annotations.Column;
import com.example.JDDB.lib.annotations.GeneratedValue;
import com.example.JDDB.lib.annotations.Id;
import com.example.JDDB.lib.annotations.Table;


@Table(name = "user")
public class User {
    @Id
    @GeneratedValue
    private String id;
    private String name;

    @Column(nullale = false)
    private String email;
    private Integer age;

    public User(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                '}';
    }
}