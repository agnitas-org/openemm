/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.admin.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.agnitas.beans.AdminGroup;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;

public class PermissionsOverviewData {
    
    private static List<String> standardCategories = Arrays.asList(Permission.ORDERED_STANDARD_RIGHT_CATEGORIES);
    private static List<String> premiumCategories = Arrays.asList(Permission.ORDERED_PREMIUM_RIGHT_CATEGORIES);
    
    public static final int ROOT_ADMIN_ID = 1;
    public static final int ROOT_GROUP_ID = 1;
    
    private Map<String, PermissionCategoryEntry> permissionsCategories;
    
    public Map<String, PermissionCategoryEntry> getPermissionsCategories() {
        return permissionsCategories;
    }
    
    public PermissionsOverviewData() {
        this.permissionsCategories = new TreeMap<>();
    }
    
    public static PermissionsOverviewData.Builder builder() {
        return new PermissionsOverviewData.Builder();
    }
    
    public static class Builder {
        private ComAdmin admin;
        private ComAdmin adminToEdit;
        private boolean masterShowAllowed;
        private boolean newPermissionManagement;
    
        private Set<Permission> visiblePermissions;
        private Set<Permission> companyPermissions;
        private boolean groupPermissions;
        private boolean allowedForGroup;
    
        public void setAdmin(ComAdmin admin) {
            this.admin = admin;
        }
    
        public void setAdminToEdit(ComAdmin adminToEdit) {
            this.adminToEdit = adminToEdit;
        }
    
        public void setVisiblePermissions(Set<Permission> visiblePermissions) {
            this.visiblePermissions = visiblePermissions;
        }
    
        public void setCompanyPermissions(Set<Permission> companyPermissions) {
            this.companyPermissions = companyPermissions;
        }
        
        public void setGroupPermissions(boolean groupPermissions) {
            this.groupPermissions = groupPermissions;
        }
        
        public void setNewPermissionManagement(boolean newPermissionManagement) {
            this.newPermissionManagement = newPermissionManagement;
        }
    
        public PermissionsOverviewData build() {
            // For information on rules for changing user rights, see:
            // http://wiki.agnitas.local/doku.php?id=abteilung:allgemein:premiumfeatures&s[]=rechtevergabe#rechtevergabe-moeglichkeiten_in_der_emm-gui
            PermissionsOverviewData options = new PermissionsOverviewData();
            
            if (groupPermissions && !allowedForGroup) {
                return options;
            }
            
            assert admin != null;
            
            if (adminToEdit == null) {
                adminToEdit = admin;
            }
            
            masterShowAllowed = admin.permissionAllowed(Permission.MASTER_SHOW) || admin.getAdminID() == ROOT_ADMIN_ID;
            
            List<String> categories = new ArrayList<>();
            categories.addAll(standardCategories);
            categories.addAll(premiumCategories);
            
            if (masterShowAllowed) {
                categories.add(Permission.CATEGORY_KEY_SYSTEM);
                categories.add(Permission.CATEGORY_KEY_OTHERS);
            }
    
            Set<Permission> filtered = visiblePermissions.stream()
                    .filter(permission -> categories.contains(permission.getCategory()))
                    .collect(Collectors.toSet());
    
            Map<String, PermissionCategoryEntry> permissionsCategories = collectPermissionsByCategory(adminToEdit.getGroup(), filtered);
            sortPermissionsMap(permissionsCategories);
            
            options.permissionsCategories.putAll(permissionsCategories);
            return options;
        }
    
        private void sortPermissionsMap(Map<String, PermissionCategoryEntry> permissionsCategories) {
            for (PermissionCategoryEntry categoryEntry : permissionsCategories.values()) {
                for (PermissionSubCategoryEntry subCategoryEntry : categoryEntry.subCategories.values()) {
                    subCategoryEntry.permissions.sort(Comparator.comparing(o -> o.name));
                }
            }
        }
    
        private Map<String, PermissionCategoryEntry> collectPermissionsByCategory(AdminGroup adminGroup, Set<Permission> filteredVisiblePermissions) {
            Map<String, PermissionCategoryEntry> permissionsCategories = new TreeMap<>();
            for (Permission permission : filteredVisiblePermissions) {
                String subCategoryName = StringUtils.defaultString(permission.getSubCategory());
                String categoryName = permission.getCategory();
    
                PermissionSubCategoryEntry subCategory =
                        permissionsCategories.computeIfAbsent(categoryName, PermissionCategoryEntry::new)
                        .subCategories.computeIfAbsent(subCategoryName, PermissionSubCategoryEntry::new);
                
                subCategory.permissions.add(getPermissionEntry(adminGroup, permission));
            }
            return permissionsCategories;
        }
    
        private PermissionEntry getPermissionEntry(AdminGroup adminGroup, Permission permission) {
            String categoryName = permission.getCategory();

            PermissionEntry permissionEntry = new PermissionEntry();
            permissionEntry.name = permission.toString();
            permissionEntry.granted = adminToEdit.permissionAllowed(permission);
            
            if (!groupPermissions && adminGroup != null && adminGroup.permissionAllowed(adminGroup.getCompanyID(), permission)) {
                permissionEntry.drivenByAdminCategory = true;
                permissionEntry.changeable = false;
                permissionEntry.adminGroup = adminGroup;
            } else if (newPermissionManagement) {
            	if (permission.isPremium()) {
                    permissionEntry.changeable = companyPermissions.contains(permission);
                } else if (Permission.CATEGORY_KEY_SYSTEM.equals(permission.getCategory()) || Permission.CATEGORY_KEY_OTHERS.equals(permission.getCategory())) {
                    permissionEntry.changeable = admin.permissionAllowed(permission) || masterShowAllowed;
                } else {
                	permissionEntry.changeable = true;
                }
            } else {
            	if (standardCategories.contains(categoryName)) {
                    permissionEntry.changeable = true;
                } else if (premiumCategories.contains(categoryName)) {
                    permissionEntry.changeable = companyPermissions.contains(permission);
                } else {
                    permissionEntry.changeable = admin.permissionAllowed(permission) || masterShowAllowed;
                }
            }
            
            return permissionEntry;
        }
    
        public void setAllowedForGroup(boolean allowedForGroup) {
            this.allowedForGroup = allowedForGroup;
        }
    
        public boolean getAllowedForGroup() {
            return allowedForGroup;
        }
    }
    
    public static class PermissionEntry {
        
        private boolean changeable;
        private AdminGroup adminGroup;
        private boolean drivenByAdminCategory;
        private String name;
        private boolean granted;
    
        public PermissionEntry() {
        }
    
        public PermissionEntry(boolean changeable, AdminGroup adminGroup, boolean drivenByAdminCategory, String name, boolean granted) {
            this.changeable = changeable;
            this.adminGroup = adminGroup;
            this.drivenByAdminCategory = drivenByAdminCategory;
            this.name = name;
            this.granted = granted;
        }
    
        public boolean isChangeable() {
            return changeable;
        }
    
        public AdminGroup getAdminGroup() {
            return adminGroup;
        }
    
        public String getName() {
            return name;
        }
    
        public boolean getGranted() {
            return granted;
        }
        
        public boolean isShowInfoTooltip() {
            return !changeable && drivenByAdminCategory && adminGroup != null;
        }
    }
    
    public static class PermissionSubCategoryEntry {
        private String name;
        private List<PermissionEntry> permissions;
        
        public PermissionSubCategoryEntry(String name) {
            this.name = name;
            this.permissions = new ArrayList<>();
        }
    
        public String getName() {
            return name;
        }
    
        public List<PermissionEntry> getPermissions() {
            return permissions;
        }
    }
    
    public static class PermissionCategoryEntry {
        private String name;
        private Map<String, PermissionSubCategoryEntry> subCategories;
        
        public PermissionCategoryEntry(String name) {
            this.name = name;
            this.subCategories = new TreeMap<>();
        }
    
        public String getName() {
            return name;
        }
    
        public Map<String, PermissionSubCategoryEntry> getSubCategories() {
            return subCategories;
        }
    }
}
