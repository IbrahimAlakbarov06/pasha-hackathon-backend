package com.bravo.brain.controller;

import com.bravo.brain.model.dto.UserDto;
import com.bravo.brain.model.enums.Role;
import com.bravo.brain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserController {

    private final UserService userService;

    // POST /api/users — yeni user yarat
    @PostMapping
    public ResponseEntity<UserDto.CreateResponse> createUser(@Valid @RequestBody UserDto.CreateRequest req) {
        return ResponseEntity.ok(userService.createUser(req));
    }

    // GET /api/users — bütün userlər
    @GetMapping
    public ResponseEntity<List<UserDto.UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /api/users/role/{role} — rol üzrə
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDto.UserResponse>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // PUT /api/users/{id} — redaktə et
    @PutMapping("/{id}")
    public ResponseEntity<UserDto.UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto.UpdateRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    // DELETE /api/users/{id} — deaktiv et
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok("User deaktiv edildi");
    }
}