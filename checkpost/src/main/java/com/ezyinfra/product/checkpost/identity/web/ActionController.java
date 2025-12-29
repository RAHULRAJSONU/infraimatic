package com.ezyinfra.product.checkpost.identity.web;

import com.ezyinfra.product.checkpost.identity.data.entity.Action;
import com.ezyinfra.product.checkpost.identity.data.record.ActionCreateRecord;
import com.ezyinfra.product.checkpost.identity.service.ActionService;
import com.ezyinfra.product.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/identity/action")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @PostMapping
    public ResponseEntity<Action> createAction(@RequestBody ActionCreateRecord createRecord) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actionService.createAction(createRecord));
    }

    @GetMapping
    public ResponseEntity<Page<Action>> getAllActions(Pageable p) {
        return ResponseEntity.ok(actionService.listActions(p));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Action> getActionById(@PathVariable UUID id) {
        Action action = actionService.getActionById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with id " + id));
        return ResponseEntity.ok(action);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Action> updateResource(@PathVariable UUID id, @RequestBody ActionCreateRecord actionCreateRecord) {
        Action updatedAction = actionService.updateAction(id, actionCreateRecord);
        return ResponseEntity.ok(updatedAction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAction(@PathVariable UUID id) {
        actionService.deleteAction(id);
        return ResponseEntity.noContent().build();
    }
}
