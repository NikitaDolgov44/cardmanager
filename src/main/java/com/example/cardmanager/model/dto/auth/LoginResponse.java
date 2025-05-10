package com.example.cardmanager.model.dto.auth;

import com.example.cardmanager.model.entity.enums.RoleType;

public record LoginResponse(
        String token,
        String email,
        RoleType role
) {}
