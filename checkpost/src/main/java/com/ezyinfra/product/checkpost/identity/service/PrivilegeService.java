package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import com.ezyinfra.product.checkpost.identity.data.record.PrivilegeCreateRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PrivilegeService {

    Privilege createPrivilege(PrivilegeCreateRecord createRecord);

    Optional<Privilege> getPrivilegeById(UUID id);

    List<Privilege> getAllPrivilegesByIds(Set<UUID> ids);

    Privilege updatePrivilege(UUID id, PrivilegeCreateRecord privilegeDetails);

    void deletePrivilege(UUID id);

    Page<Privilege> listPrivileges(Pageable p);
}
