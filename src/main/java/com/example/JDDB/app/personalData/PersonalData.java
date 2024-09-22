package com.example.JDDB.app.personalData;

import org.springframework.data.annotation.Id;

import java.util.Date;


public class PersonalData {
    public PersonalData(String name, String surname, Boolean married, Integer salary, Date birthday) {
        this.name = name;
        this.surname = surname;
        this.married = married;
        this.salary = salary;
        this.birthday = birthday;
    }

    @Id
    private String id;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getSurname() {
        return surname;
    }

    public Boolean getMarried() {
        return married;
    }

    public Integer getSalary() {
        return salary;
    }

    public Date getBirthday() {
        return birthday;
    }

    private String name;
    private String surname;
    private Boolean married;
    private Integer salary;
    private Date birthday;
}
