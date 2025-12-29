package com.ezyinfra.product.checkpost.identity.web;

import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import com.ezyinfra.product.checkpost.identity.data.entity.Role;
import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.checkpost.identity.service.UserAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/identity/authz")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthorizationController {

    private final UserAuthorizationService service;

    @PostMapping("/add/roles")
    public ResponseEntity<User> mapRole(@RequestParam("user") UUID userId,
                                        @RequestParam("roles") String roleIds) {
        List<UUID> roleUIds = Stream.of(roleIds.split(",")).map(UUID::fromString).distinct().toList();
        return ResponseEntity.ok(service.mapRolesToUser(userId, roleUIds));
    }

    @PostMapping("/remove/roles")
    public ResponseEntity<User> removeRole(@RequestParam("user") UUID userId,
                                           @RequestParam("roles") String roleIds) {
        List<UUID> roleUIds = Stream.of(roleIds.split(",")).map(UUID::fromString).distinct().toList();
        return ResponseEntity.ok(service.removeRolesFromUser(userId, roleUIds));
    }

    @GetMapping("/{id}/role")
    public ResponseEntity<List<Role>> getRoles(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getAllRoles(id));
    }

    @GetMapping("/{id}/privilege")
    public ResponseEntity<List<Privilege>> getPrivilege(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getAllPrivilege(id));
    }

}