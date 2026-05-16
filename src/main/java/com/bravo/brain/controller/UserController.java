package com.bravo.brain.controller;

import com.bravo.brain.model.dto.UserDto;
import com.bravo.brain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/admin/users?activeOnly=false
    @GetMapping
    public ResponseEntity<List<UserDto.UserResponse>> getUsers(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(userService.getAllUsers(activeOnly));
    }

    // POST /api/admin/users
    @PostMapping
    public ResponseEntity<UserDto.UserResponse> createUser(
            @Valid @RequestBody UserDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(req));
    }

    // PUT /api/admin/users/{userId}
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto.UserResponse> updateUser(
            @PathVariable String userId,
            @RequestBody UserDto.UpdateRequest req) {
        return ResponseEntity.ok(userService.updateUser(userId, req));
    }

    // PATCH /api/admin/users/{userId}/deactivate
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable String userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }
}