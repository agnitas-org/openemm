package com.agnitas.emm.core.admin.service;

import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.emm.core.Permission;

public class OpenemmPermissionFilter implements PermissionFilter {

    @Override
    public boolean isVisible(Permission permission) {
        return permission != null && permission.isVisible();
    }

    @Override
    public Set<Permission> getAllVisiblePermissions() {
        return Permission.getAllSystemPermissions().stream()
                .filter(this::isVisible).collect(Collectors.toSet());
    }
}
