package com.example.demo.dto;

import com.example.demo.entity.Role;

public record StudentResponse(
        Long id,
        String username,
        String fullName,
        String email,
        Role role,
        String rollNumber,
        String department
) {}