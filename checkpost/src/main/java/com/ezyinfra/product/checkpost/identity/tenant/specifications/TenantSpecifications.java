package com.ezyinfra.product.checkpost.identity.tenant.specifications;

import com.ezyinfra.product.checkpost.identity.tenant.entity.Tenant;
import com.ezyinfra.product.checkpost.identity.tenant.model.TenantStatus;
import org.springframework.data.jpa.domain.Specification;

public final class TenantSpecifications {

    private TenantSpecifications() {}

    public static Specification<Tenant> hasStatus(TenantStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Tenant> nameContains(String keyword) {
        return (root, query, cb) ->
                keyword == null ? null :
                        cb.like(cb.lower(root.get("name")),
                                "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<Tenant> codeEquals(String code) {
        return (root, query, cb) ->
                code == null ? null : cb.equal(root.get("code"), code);
    }
}