package com.example.demo.dto;

public record TeacherRequest(
        String username,
        String password,
        String fullName,
        String email,
        String department,
        String designation
) {}