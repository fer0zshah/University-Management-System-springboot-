package com.example.demo.dto;

public record AdminRequest(
        String username,
        String password,
        String fullName,
        String email,
        String office
) {}