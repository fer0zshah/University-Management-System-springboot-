package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "teachers")
@Getter
@Setter
public class Teacher extends User {

    private String department;
    private String designation;

    public Teacher() {
        setRole(Role.TEACHER);
    }
}