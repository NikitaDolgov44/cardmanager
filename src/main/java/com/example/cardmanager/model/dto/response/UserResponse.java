package com.example.cardmanager.model.dto.response;

import com.example.cardmanager.model.entity.enums.RoleType;

public record UserResponse(
        Long id,
        String email,
        RoleType role
) {}