/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.admin.web;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.agnitas.emm.core.permission.bean.PermissionCategory;
import org.agnitas.beans.AdminGroup;
import org.agnitas.dao.LicenseType;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.PermissionInfo;
import com.agnitas.emm.core.PermissionType;

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
        private Admin admin;
        private Admin adminToEdit;
        private AdminGroup groupToEdit;
    
        private Set<Permission> visiblePermissions;
        private String licenseType;
        private Set<Permission> companyPermissions;
        private Map<String, PermissionInfo> permissionInfos;
        private boolean groupPermissions;
        private boolean allowedForGroup;
        
        public void setAdmin(Admin admin) {
            this.admin = admin;
        }
    
        public void setAdminToEdit(Admin adminToEdit) {
            this.adminToEdit = adminToEdit;
        }
    
        public void setGroupToEdit(AdminGroup groupToEdit) {
            this.groupToEdit = groupToEdit;
        }
    
        public void setVisiblePermissions(Set<Permission> visiblePermissions) {
            this.visiblePermissions = visiblePermissions;
        }

		public void setLicenseType(String licenseType) {
            this.licenseType = licenseType;
		}
    
        public void setCompanyPermissions(Set<Permission> companyPermissions) {
            this.companyPermissions = companyPermissions;
        }
        
        public void setGroupPermissions(boolean groupPermissions) {
            this.groupPermissions = groupPermissions;
        }
        
        public void setPermissionInfos(Map<String, PermissionInfo> permissionInfos) {
        	this.permissionInfos = permissionInfos;
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

				LicenseType licenseTypeFromID = LicenseType.getLicenseTypeByID(licenseType);
	            Set<Permission> filtered = visiblePermissions.stream()
	                    .filter(permission -> {
							return (permission.getPermissionType() != PermissionType.System || admin.permissionAllowed(permission)
								|| ((admin.permissionAllowed(Permission.MASTER_SHOW) || admin.getAdminID() == ROOT_ADMIN_ID)
									&& !(licenseTypeFromID == LicenseType.Inhouse
							    		|| licenseTypeFromID == LicenseType.OpenEMM
							    		|| licenseTypeFromID == LicenseType.OpenEMM_Plus)));
						})
	                    .filter(permission -> (permission.getPermissionType() != PermissionType.Migration || admin.permissionAllowed(Permission.SHOW_MIGRATION_PERMISSIONS)))
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
    
        private Map<String, PermissionCategoryEntry> collectPermissionsByCategory(Admin adminToCollectPermissionsFor, Set<Permission> filteredVisiblePermissions) {
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
			
			for (Entry<String, PermissionInfo> permissionInfosEntry : permissionInfos.entrySet()) {
				Permission permission = Permission.getPermissionByToken(permissionInfosEntry.getKey());
				if (permission != null && filteredVisiblePermissions.contains(permission)) {
				    String categoryName = permissionInfosEntry.getValue().getCategory();
	
				    if (categoryName != null) {
					    String subCategoryName = StringUtils.defaultString(permissionInfosEntry.getValue().getSubCategory());
					    PermissionSubCategoryEntry subCategory =
					            permissionsCategories.computeIfAbsent(categoryName, PermissionCategoryEntry::new)
					            .subCategories.computeIfAbsent(subCategoryName, PermissionSubCategoryEntry::new);
					    
					    PermissionEntry permissionEntry = new PermissionEntry();
				    	
				    	permissionEntry.name = permission.toString();
				    	permissionEntry.granted = adminToCollectPermissionsFor.permissionAllowed(permission);
                        permissionEntry.recent = isNewPermission(permissionInfosEntry.getValue().getCreationDate());

                        for (AdminGroup adminGroup : adminToCollectPermissionsFor.getGroups()) {
					    	if (adminGroup != null && adminGroup.permissionAllowed(permission)) {
								permissionEntry.adminGroup = adminGroup;
					    		break;
					    	}
					    }
					    
						if (permissionEntry.adminGroup != null) {
				    		permissionEntry.changeable = false;
						} else if (permission.getPermissionType() == PermissionType.Premium) {
							permissionEntry.changeable = companyPermissions.contains(permission);
						} else if (permission.getPermissionType() == PermissionType.System) {
							permissionEntry.changeable = companyPermissions.contains(permission) && (admin.permissionAllowed(permission) || admin.permissionAllowed(Permission.MASTER_SHOW) || admin.getAdminID() == ROOT_ADMIN_ID);
						} else {
							permissionEntry.changeable = true;
						}
						
						subCategory.permissions.add(permissionEntry);
				    }
				}
			}
			
			// Remove empty categories and subcategories
			for (String categoryName : new HashSet<>(permissionsCategories.keySet())) {
				Map<String, PermissionSubCategoryEntry> subcategories = permissionsCategories.get(categoryName).getSubCategories();
				for (String subCategoryName : new HashSet<>(subcategories.keySet())) {
					if (subcategories.get(subCategoryName) == null || subcategories.get(subCategoryName).getPermissions() == null || subcategories.get(subCategoryName).getPermissions().size() == 0) {
						subcategories.remove(subCategoryName);
					}
				}
				if (permissionsCategories.get(categoryName) == null || permissionsCategories.get(categoryName).getSubCategories() == null || permissionsCategories.get(categoryName).getSubCategories().size() == 0) {
					permissionsCategories.remove(categoryName);
				}
			}
			
			return permissionsCategories;
        }

        private boolean isNewPermission(Date creationDate) {
            LocalDate creationLocalDate = LocalDate.ofInstant(creationDate.toInstant(), AgnUtils.getZoneId(admin));
            LocalDate currentDateMinus30Days = LocalDate.now(AgnUtils.getZoneId(admin)).minusDays(30);
            return creationLocalDate.isAfter(currentDateMinus30Days);
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

			for (Entry<String, PermissionInfo> permissionInfosEntry : permissionInfos.entrySet()) {
				Permission permission = Permission.getPermissionByToken(permissionInfosEntry.getKey());
				if (permission != null && filteredVisiblePermissions.contains(permission)) {
				    String categoryName = permissionInfosEntry.getValue().getCategory();
	
				    if (categoryName != null) {
					    String subCategoryName = StringUtils.defaultString(permissionInfosEntry.getValue().getSubCategory());
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
						} else if (permission.getPermissionType() == PermissionType.Premium) {
							permissionEntry.changeable = companyPermissions.contains(permission);
						} else if (permission.getPermissionType() == PermissionType.System) {
							permissionEntry.changeable = companyPermissions.contains(permission) && (admin.permissionAllowed(permission) || admin.permissionAllowed(Permission.MASTER_SHOW) || admin.getAdminID() == ROOT_ADMIN_ID);
						} else {
							permissionEntry.changeable = true;
						}
						
						subCategory.permissions.add(permissionEntry);
				    }
				}
			}
			
			// Remove empty categories and subcategories
			for (String categoryName : new HashSet<>(permissionsCategories.keySet())) {
				Map<String, PermissionSubCategoryEntry> subcategories = permissionsCategories.get(categoryName).getSubCategories();
				for (String subCategoryName : new HashSet<>(subcategories.keySet())) {
					if (subcategories.get(subCategoryName) == null || subcategories.get(subCategoryName).getPermissions() == null || subcategories.get(subCategoryName).getPermissions().size() == 0) {
						subcategories.remove(subCategoryName);
					}
				}
				if (permissionsCategories.get(categoryName) == null || permissionsCategories.get(categoryName).getSubCategories() == null || permissionsCategories.get(categoryName).getSubCategories().size() == 0) {
					permissionsCategories.remove(categoryName);
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
        private boolean recent;
    
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

        public boolean isRecent() {
            return recent;
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

        public String getIconCode() {
            return PermissionCategory.from(getName()).getIconCode();
        }
    
        public String getName() {
            return name;
        }
    
        public Map<String, PermissionSubCategoryEntry> getSubCategories() {
            return subCategories;
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
