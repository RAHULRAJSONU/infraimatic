package com.ezyinfra.product.checkpost.identity.web;


import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import com.ezyinfra.product.checkpost.identity.data.record.PrivilegeCreateRecord;
import com.ezyinfra.product.checkpost.identity.service.PrivilegeService;
import com.ezyinfra.product.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/identity/privilege")
public class PrivilegeController {

    private final PrivilegeService privilegeService;

    public PrivilegeController(PrivilegeService privilegeService) {
        this.privilegeService = privilegeService;
    }

    @PostMapping("/privilege")
    public ResponseEntity<Privilege> createPrivilege(@Valid @RequestBody PrivilegeCreateRecord request) {
        return ResponseEntity.ok(privilegeService.createPrivilege(request));
    }

    @GetMapping
    public ResponseEntity<Page<Privilege>> getAllPrivilege(Pageable p) {
        return ResponseEntity.ok(privilegeService.listPrivileges(p));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Privilege> getPrivilegeById(@PathVariable UUID id) {
        Privilege privilege = privilegeService.getPrivilegeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Privilege not found with id " + id));
        return ResponseEntity.ok(privilege);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Privilege> updatePrivilege(@PathVariable UUID id, @RequestBody PrivilegeCreateRecord privilegeCreateRecord) {
        Privilege updatedPrivilege = privilegeService.updatePrivilege(id, privilegeCreateRecord);
        return ResponseEntity.ok(updatedPrivilege);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrivilege(@PathVariable UUID id) {
        privilegeService.deletePrivilege(id);
        return ResponseEntity.noContent().build();
    }
}
