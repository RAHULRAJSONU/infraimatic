package com.ezyinfra.product.checkpost.identity.web;

import com.ezyinfra.product.checkpost.identity.data.entity.Role;
import com.ezyinfra.product.checkpost.identity.data.record.RoleCreateRecord;
import com.ezyinfra.product.checkpost.identity.service.RoleService;
import com.ezyinfra.product.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/identity/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/role")
    public ResponseEntity<Role> createRole(@Valid @RequestBody RoleCreateRecord request) {
        return ResponseEntity.ok(roleService.createRole(request));
    }

    @GetMapping
    public ResponseEntity<Page<Role>> getAllRoles(Pageable p) {
        return ResponseEntity.ok(roleService.listRoles(p));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable UUID id) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id " + id));
        return ResponseEntity.ok(role);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable UUID id, @RequestBody RoleCreateRecord roleCreateRecord) {
        Role updatedRole = roleService.updateRole(id, roleCreateRecord);
        return ResponseEntity.ok(updatedRole);
    }

    @PatchMapping("/{id}/add/privileges")
    public ResponseEntity<Role> addPrivilegeToRole(@PathVariable UUID id, Set<UUID> privilegeIds) {
        return ResponseEntity.ok(roleService.addPrivilegeToRole(id, privilegeIds));
    }

    @PatchMapping("/{id}/remove/privileges")
    public ResponseEntity<Role> removePrivilegeToRole(@PathVariable UUID id, Set<UUID> privilegeIds) {
        return ResponseEntity.ok(roleService.removePrivilegeToRole(id, privilegeIds));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
