package com.example.demo.dto;

public record StudentRequest(
        String username,
        String password,
        String fullName,
        String email,
        String rollNumber,
        String department
) {}