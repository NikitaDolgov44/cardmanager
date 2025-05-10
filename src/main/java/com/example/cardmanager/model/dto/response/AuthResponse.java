package com.example.cardmanager.model.dto.response;

import com.example.cardmanager.model.entity.enums.RoleType;

public record AuthResponse(
        String token,
        String email,
        RoleType role
) {}
