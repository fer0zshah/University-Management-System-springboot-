package com.example.demo.dto;

import com.example.demo.entity.Role;

public record TeacherResponse(
        Long id,
        String username,
        String fullName,
        String email,
        Role role,
        String department,
        String designation
) {}