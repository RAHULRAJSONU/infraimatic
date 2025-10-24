package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.checkpost.identity.data.entity.Action;
import com.ezyinfra.product.checkpost.identity.data.record.ActionCreateRecord;
import com.ezyinfra.product.checkpost.identity.data.repository.ActionRepository;
import com.ezyinfra.product.checkpost.identity.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ActionServiceImpl implements ActionService {

    @Autowired
    private ActionRepository actionRepository;

    @Override
    public Action createAction(ActionCreateRecord action) {
        Action entity = action.toEntity();
        return actionRepository.save(entity);
    }

    @Override
    public Optional<Action> getActionById(UUID id) {
        return actionRepository.findById(id);
    }

    @Override
    public Action updateAction(UUID id, ActionCreateRecord action) {
        return actionRepository.findById(id)
                .map(a -> {
                    a.setName(action.name());
                    a.setActive(action.active());
                    return actionRepository.save(a);
                })
                .orElse(null);
    }

    @Override
    public boolean deleteAction(UUID id) {
        return actionRepository.findById(id)
                .map(action -> {
                    action.setActive(false);
                    actionRepository.save(action);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Page<Action> listActions(Pageable p) {
        return actionRepository.findAll(p);
    }
}
