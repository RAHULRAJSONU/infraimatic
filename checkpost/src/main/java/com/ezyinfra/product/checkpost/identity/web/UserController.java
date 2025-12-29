package com.ezyinfra.product.checkpost.identity.web;

import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.checkpost.identity.data.record.UserUpdateRequest;
import com.ezyinfra.product.checkpost.identity.service.UserService;
import com.ezyinfra.product.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/identity/user")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<User>> getAll(@PageableDefault(size = 10) Pageable pageable,
                                             @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(userService.findAll(pageable, UserStatus.get(status)));
    }

    @GetMapping("/email")
    public ResponseEntity<User> findByEmail(@RequestParam("email") String email,
                                            @RequestParam(value = "status", required = false) String status) {
        return ResponseEntity.ok(userService.findByEmailAndStatus(email, UserStatus.get(status)));
    }

    @PatchMapping
    public ResponseEntity<User> updateUser(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @PatchMapping("/change-status")
    public ResponseEntity<User> changeStatus(@RequestParam("id") UUID id,
                                             @RequestParam("status") UserStatus status) {
        return ResponseEntity.ok(userService.changeUserStatus(id, status));
    }
}
