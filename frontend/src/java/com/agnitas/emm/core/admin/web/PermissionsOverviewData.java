/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.admin.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
        private AdminGroup groupToEdit;
    
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
    
        public void setGroupToEdit(AdminGroup groupToEdit) {
            this.groupToEdit = groupToEdit;
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
    
        public PermissionsOverviewData build() {
            // For information on rules for changing user rights, see:
            // http://wiki.agnitas.local/doku.php?id=abteilung:allgemein:premiumfeatures&s[]=rechtevergabe#rechtevergabe-moeglichkeiten_in_der_emm-gui
            PermissionsOverviewData options = new PermissionsOverviewData();
            
            if (groupPermissions && !allowedForGroup) {
                return options;
            } else {
	            assert admin != null;
	            
	            if (adminToEdit == null) {
	                adminToEdit = admin;
	            }
	            
	            Set<Permission> filtered = visiblePermissions.stream()
	                    .filter(permission -> (!Permission.CATEGORY_KEY_SYSTEM.equals(permission.getCategory()) || admin.permissionAllowed(permission) || admin.permissionAllowed(Permission.MASTER_SHOW) || admin.getAdminID() == ROOT_ADMIN_ID))
	                    .collect(Collectors.toSet());
	    
	            Map<String, PermissionCategoryEntry> permissionsCategories;
	            if (!groupPermissions) {
	            	permissionsCategories = collectPermissionsByCategory(adminToEdit, filtered);
	            } else {
	            	permissionsCategories = collectPermissionsByCategory(groupToEdit, filtered);
	            }
	            
	            options.permissionsCategories.putAll(permissionsCategories);
	            return options;
            }
        }
    
        private Map<String, PermissionCategoryEntry> collectPermissionsByCategory(ComAdmin adminToCollectPermissionsFor, Set<Permission> filteredVisiblePermissions) {
			Map<String, PermissionCategoryEntry> permissionsCategories = new TreeMap<>();
			for (String orderedCategory : Permission.CATEGORY_DISPLAY_ORDER) {
				permissionsCategories.put(orderedCategory, new PermissionCategoryEntry(orderedCategory));
				if (Permission.SUBCATEGORY_DISPLAY_ORDER.containsKey(orderedCategory)) {
					PermissionCategoryEntry permissionCategoryEntry = permissionsCategories.get(orderedCategory);
					permissionCategoryEntry.subCategories.put("", new PermissionSubCategoryEntry(""));
					for (String orderedSubCategory : Permission.SUBCATEGORY_DISPLAY_ORDER.get(orderedCategory)) {
						permissionCategoryEntry.subCategories.put(orderedSubCategory, new PermissionSubCategoryEntry(orderedSubCategory));
					}
				}
			}
			
			List<Permission> filteredAndSortedVisiblePermissions = new ArrayList<>(filteredVisiblePermissions);
			Collections.sort(filteredAndSortedVisiblePermissions);
			for (Permission permission : filteredAndSortedVisiblePermissions) {
			    String categoryName = permission.getCategory();

			    if (categoryName != null) {
				    String subCategoryName = StringUtils.defaultString(permission.getSubCategory());
				    PermissionSubCategoryEntry subCategory =
				            permissionsCategories.computeIfAbsent(categoryName, PermissionCategoryEntry::new)
				            .subCategories.computeIfAbsent(subCategoryName, PermissionSubCategoryEntry::new);
				    
				    PermissionEntry permissionEntry = new PermissionEntry();
			    	
			    	permissionEntry.name = permission.toString();
			    	permissionEntry.granted = adminToCollectPermissionsFor.permissionAllowed(permission);
			    	
				    for (AdminGroup adminGroup : adminToCollectPermissionsFor.getGroups()) {
				    	if (adminGroup != null && adminGroup.permissionAllowed(permission)) {
							permissionEntry.adminGroup = adminGroup;
				    		break;
				    	}
				    }
				    
					if (permissionEntry.adminGroup != null) {
			    		permissionEntry.changeable = false;
					} else if (permission.isPremium()) {
						permissionEntry.changeable = companyPermissions.contains(permission);
					} else if (Permission.CATEGORY_KEY_SYSTEM.equals(permission.getCategory())) {
						permissionEntry.changeable = admin.permissionAllowed(permission) || admin.permissionAllowed(Permission.MASTER_SHOW) || admin.getAdminID() == ROOT_ADMIN_ID;
					} else {
						permissionEntry.changeable = true;
					}
					
					subCategory.permissions.add(permissionEntry);
			    }
			}
			return permissionsCategories;
        }
    
        private Map<String, PermissionCategoryEntry> collectPermissionsByCategory(AdminGroup groupToCollectPermissionsFor, Set<Permission> filteredVisiblePermissions) {
			Map<String, PermissionCategoryEntry> permissionsCategories = new TreeMap<>();
			for (String orderedCategory : Permission.CATEGORY_DISPLAY_ORDER) {
				permissionsCategories.put(orderedCategory, new PermissionCategoryEntry(orderedCategory));
				if (Permission.SUBCATEGORY_DISPLAY_ORDER.containsKey(orderedCategory)) {
					PermissionCategoryEntry permissionCategoryEntry = permissionsCategories.get(orderedCategory);
					permissionCategoryEntry.subCategories.put("", new PermissionSubCategoryEntry(""));
					for (String orderedSubCategory : Permission.SUBCATEGORY_DISPLAY_ORDER.get(orderedCategory)) {
						permissionCategoryEntry.subCategories.put(orderedSubCategory, new PermissionSubCategoryEntry(orderedSubCategory));
					}
				}
			}
			
			List<Permission> filteredAndSortedVisiblePermissions = new ArrayList<>(filteredVisiblePermissions);
			Collections.sort(filteredAndSortedVisiblePermissions);
			for (Permission permission : filteredAndSortedVisiblePermissions) {
			    String categoryName = permission.getCategory();

			    if (categoryName != null) {
				    String subCategoryName = StringUtils.defaultString(permission.getSubCategory());
				    PermissionSubCategoryEntry subCategory =
				            permissionsCategories.computeIfAbsent(categoryName, PermissionCategoryEntry::new)
				            .subCategories.computeIfAbsent(subCategoryName, PermissionSubCategoryEntry::new);
				    
				    PermissionEntry permissionEntry = new PermissionEntry();
			    	
			    	permissionEntry.name = permission.toString();
			    	
			    	if (groupToCollectPermissionsFor != null) {
				    	permissionEntry.granted = groupToCollectPermissionsFor.permissionAllowed(permission);
				    	
					    for (AdminGroup adminGroup : groupToCollectPermissionsFor.getParentGroups()) {
					    	if (adminGroup != null && adminGroup.permissionAllowed(permission)) {
								permissionEntry.adminGroup = adminGroup;
					    		break;
					    	}
					    }
			    	}
				    
					if (permissionEntry.adminGroup != null) {
			    		permissionEntry.changeable = false;
					} else if (permission.isPremium()) {
						permissionEntry.changeable = companyPermissions.contains(permission);
					} else if (Permission.CATEGORY_KEY_SYSTEM.equals(permission.getCategory())) {
						permissionEntry.changeable = admin.permissionAllowed(permission) || admin.permissionAllowed(Permission.MASTER_SHOW) || admin.getAdminID() == ROOT_ADMIN_ID;
					} else {
						permissionEntry.changeable = true;
					}
					
					subCategory.permissions.add(permissionEntry);
			    }
			}
			return permissionsCategories;
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
        private AdminGroup adminGroup = null;
        private String name;
        private boolean granted = false;
    
        public PermissionEntry() {
        }
    
        public PermissionEntry(boolean changeable, AdminGroup adminGroup, String name, boolean granted) {
            this.changeable = changeable;
            this.adminGroup = adminGroup;
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
            return !changeable && adminGroup != null;
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
    
    public static class PermissionCategoryEntry implements Comparable<PermissionCategoryEntry> {
        private String name;
        private Map<String, PermissionSubCategoryEntry> subCategories;
        
        public PermissionCategoryEntry(String name) {
            this.name = name;
            // Using LinkedHashMap to keep the sorted order
            subCategories = new LinkedHashMap<>();
        }
    
        public String getName() {
            return name;
        }
    
        public Map<String, PermissionSubCategoryEntry> getSubCategories() {
        	Map<String, PermissionSubCategoryEntry> returnMap = new LinkedHashMap<>();
        	List<String> subCategoryKeys = new ArrayList<>(subCategories.keySet());
        	for (String subCategory : subCategoryKeys) {
        		returnMap.put(subCategory, subCategories.get(subCategory));
        	}
            return returnMap;
        }
        
        @Override
    	public int compareTo(PermissionCategoryEntry otherPermissionCategoryEntry) {
        	if (otherPermissionCategoryEntry == null) {
    			return 1;
    		} else {
    			int categoryIndex = Integer.MAX_VALUE;
    			for (int index = 0; index < Permission.CATEGORY_DISPLAY_ORDER.length; index++) {
    				if (Permission.CATEGORY_DISPLAY_ORDER[index].equals(getName())) {
    					categoryIndex = index;
    					break;
    				}
    			}
    			int otherCategoryIndex = Integer.MAX_VALUE;
    			for (int index = 0; index < Permission.CATEGORY_DISPLAY_ORDER.length; index++) {
    				if (Permission.CATEGORY_DISPLAY_ORDER[index].equals(otherPermissionCategoryEntry.getName())) {
    					otherCategoryIndex = index;
    					break;
    				}
    			}
    			return Integer.compare(categoryIndex, otherCategoryIndex);
    		}
        }
    }
}
