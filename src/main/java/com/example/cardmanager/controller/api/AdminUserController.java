package com.example.cardmanager.controller.api;

import com.example.cardmanager.model.dto.response.UserResponse;
import com.example.cardmanager.model.entity.User;
import com.example.cardmanager.model.entity.enums.RoleType;
import com.example.cardmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole()
                ));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long userId,
            @RequestParam RoleType newRole
    ) {
        User user = userService.updateUserRole(userId, newRole);
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole()
        ));
    }
}
