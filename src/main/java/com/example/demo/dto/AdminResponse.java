package com.example.demo.dto;

import com.example.demo.entity.Role;

public record AdminResponse(
        Long id,
        String username,
        String fullName,
        String email,
        Role role,
        String office
) {}