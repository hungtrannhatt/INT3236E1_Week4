package com.example.demo.dto;

import com.example.demo.model.Role;

public record RegisterRequest(String username, String password, Role role) {}
