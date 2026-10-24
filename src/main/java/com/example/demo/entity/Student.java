package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter
@Setter
public class Student extends User {

    @Column(unique = true)
    private String rollNumber;

    private String department;

    public Student() {
        setRole(Role.STUDENT);
    }
}