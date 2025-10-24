package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.common.exception.ResourceNotFoundException;
import com.ezyinfra.product.checkpost.identity.data.entity.Action;
import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import com.ezyinfra.product.checkpost.identity.data.entity.Resource;
import com.ezyinfra.product.checkpost.identity.data.record.PrivilegeCreateRecord;
import com.ezyinfra.product.checkpost.identity.data.repository.PrivilegeRepository;
import com.ezyinfra.product.checkpost.identity.service.ActionService;
import com.ezyinfra.product.checkpost.identity.service.PrivilegeService;
import com.ezyinfra.product.checkpost.identity.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class PrivilegeServiceImpl implements PrivilegeService {

    private final PrivilegeRepository privilegeRepository;
    private final ResourceService resourceService;
    private final ActionService actionService;

    public PrivilegeServiceImpl(
            PrivilegeRepository privilegeRepository,
            ResourceService resourceService,
            ActionService actionService) {
        this.privilegeRepository = privilegeRepository;
        this.resourceService = resourceService;
        this.actionService = actionService;
    }

    @Override
    public Privilege createPrivilege(PrivilegeCreateRecord createRecord) {
        log.info("Creating Privilege, request: {}", createRecord);
        Privilege entity = createRecord.toEntity();
        Resource resource = resourceService.getResourceById(createRecord.resourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found for id: " + createRecord.resourceId()));
        Action action = actionService.getActionById(createRecord.actionId())
                .orElseThrow(() -> new ResourceNotFoundException("Action not found for id: " + createRecord.resourceId()));
        entity.setResource(resource);
        entity.setAction(action);
        return privilegeRepository.save(entity);
    }

    @Override
    public Optional<Privilege> getPrivilegeById(UUID id) {
        return privilegeRepository.findById(id);
    }

    @Override
    public List<Privilege> getAllPrivilegesByIds(Set<UUID> ids) {
        return privilegeRepository.findAllByIdInAndActive(new ArrayList<>(ids), Boolean.TRUE);
    }

    @Override
    public Privilege updatePrivilege(UUID id, PrivilegeCreateRecord privilegeDetails) {
        log.info("Updating Privilege, id: {}, request: {}", id, privilegeDetails);
        return privilegeRepository.findById(id)
                .map(privilege -> {
                    privilege.setName(privilegeDetails.name());
                    privilege.setDescription(privilegeDetails.description());
                    privilege.setActive(privilegeDetails.active());
                    return privilegeRepository.save(privilege);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Privilege not found for id: " + id));
    }

    @Override
    public void deletePrivilege(UUID id) {
        privilegeRepository.findById(id)
                .map(privilege -> {
                    privilege.setActive(false);
                    privilegeRepository.save(privilege);
                    return true;
                })
                .orElseThrow(() -> new ResourceNotFoundException("Privilege not found for id: " + id));
    }

    @Override
    public Page<Privilege> listPrivileges(Pageable p) {
        return privilegeRepository.findAll(p);
    }
}
