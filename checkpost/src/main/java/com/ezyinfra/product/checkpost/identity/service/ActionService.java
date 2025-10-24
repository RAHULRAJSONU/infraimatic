package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.entity.Action;
import com.ezyinfra.product.checkpost.identity.data.record.ActionCreateRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ActionService {

    Action createAction(ActionCreateRecord action);

    Optional<Action> getActionById(UUID id);

    Action updateAction(UUID id, ActionCreateRecord action);

    boolean deleteAction(UUID id);

    Page<Action> listActions(Pageable p);
}
