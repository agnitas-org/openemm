/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportProfileDao;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.service.ImportProfileService;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.ImportMode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.service.RecipientFieldService.RecipientStandardField;

public class ImportProfileServiceImpl implements ImportProfileService {

    private static final Logger logger = LogManager.getLogger(ImportProfileServiceImpl.class);

    private RecipientService recipientService;
    private ImportProfileDao importProfileDao;

    @Override
    public ColumnMapping findColumnMappingByDbColumn(String dbColumnName, List<ColumnMapping> mappings) {
        for (ColumnMapping mapping : mappings) {
            if (mapping.getDatabaseColumn().equalsIgnoreCase(dbColumnName)) {
                return mapping;
            }
        }

        return null;
    }

    @Override
    @Transactional
    public void saveColumnsMappings(List<ColumnMapping> columnMappings, int profileId, Admin admin) {
        List<Integer> columnsForRemove = Collections.emptyList();

        if (profileId != 0) {
            ImportProfile profile = importProfileDao.getImportProfileById(profileId);
            columnsForRemove = getColumnIdsForRemove(columnMappings, profile.getColumnMapping());
        }

        List<String> hiddenColumns = RecipientStandardField.getImportChangeNotAllowedColumns(admin.permissionAllowed(Permission.IMPORT_CUSTOMERID));

        for (ColumnMapping mapping : columnMappings) {
            mapping.setProfileId(profileId);

            if (hiddenColumns.contains(mapping.getDatabaseColumn())) {
                mapping.setDatabaseColumn(ColumnMapping.DO_NOT_IMPORT);
            }
        }

        List<ColumnMapping> columnsForInsert = columnMappings.stream()
                .filter(item -> item.getId() == 0)
                .collect(Collectors.toList());
        List<ColumnMapping> columnsForUpdate = columnMappings.stream()
                .filter(item -> item.getId() != 0)
                .collect(Collectors.toList());

        importProfileDao.deleteColumnMappings(columnsForRemove);
        importProfileDao.insertColumnMappings(columnsForInsert);
        importProfileDao.updateColumnMappings(columnsForUpdate);
    }

    @Override
    public void saveImportProfileWithoutColumnMappings(ImportProfile profile, Admin admin) {
        if (!isManageAllowed(profile, admin)) {
            throw new UnsupportedOperationException();
        }

        try {
            if (profile.getId() == 0) {
                importProfileDao.insertImportProfile(profile);
            } else {
                importProfileDao.updateImportProfile(profile);
            }
        } catch (Exception e) {
            logger.error("Error saving profile:", e);
        }
    }

    @Override
    public ImportProfile getImportProfileById(int id) {
        return importProfileDao.getImportProfileById(id);
    }

    @Override
    public void deleteImportProfileById(int id) {
        importProfileDao.deleteImportProfileById(id);
    }

    @Override
    public List<ImportProfile> getAvailableImportProfiles(Admin admin) {
        return importProfileDao.getImportProfilesByCompanyId(admin.getCompanyID())
                .stream()
                .filter(p -> isManageAllowed(p, admin))
                .collect(Collectors.toList());
    }

    @Override
    public List<ImportProfile> getAvailableImportProfiles(int companyId) {
        return importProfileDao.getImportProfilesByCompanyId(companyId);
    }

    @Override
    public List<Integer> getSelectedMailingListIds(int id, int companyId) {
        return importProfileDao.getSelectedMailingListIds(id, companyId);
    }

    private List<Integer> getColumnIdsForRemove(List<ColumnMapping> mappings, List<ColumnMapping> oldMappings) {
        List<Integer> oldIds = oldMappings.stream()
                .map(ColumnMapping::getId)
                .collect(Collectors.toList());

        List<Integer> newIds = mappings.stream()
                .map(ColumnMapping::getId)
                .collect(Collectors.toList());

        oldIds.removeAll(newIds);
        return oldIds;
    }

    @Override
    public boolean isKeyColumnsIndexed(ImportProfile profile) {
        List<String> columnsToCheck = profile.getKeyColumns();
        return CollectionUtils.isEmpty(columnsToCheck)
                || recipientService.isColumnsIndexed(columnsToCheck, profile.getCompanyId());
    }

    @Override
    public boolean isDuplicatedName(String name, int id, int companyId) {
        int foundImportProfile = importProfileDao.findImportProfileIdByName(name, companyId);

        if (foundImportProfile == -1) {
            return false;
        }

        return foundImportProfile != id;
    }

    @Override
    public boolean isColumnWasImported(String columnName, int id) {
        if (id <= 0) {
            return false;
        }

        return importProfileDao.isColumnWasImported(columnName, id);
    }

    @Override
    public boolean isCheckForDuplicatesAllowed(Admin admin) {
        return admin.permissionAllowed(Permission.IMPORT_MODE_DOUBLECHECKING);
    }

    @Override
    public boolean isUpdateDuplicatesChangeAllowed(Admin admin) {
        return admin.permissionAllowed(Permission.IMPORT_MODE_DUPLICATES);
    }

    @Override
    public boolean isPreprocessingAllowed(Admin admin) {
        return admin.permissionAllowed(Permission.IMPORT_PREPROCESSING);
    }

    @Override
    public boolean isAllMailinglistsAllowed(Admin admin) {
        return admin.permissionAllowed(Permission.IMPORT_MAILINGLISTS_ALL);
    }

    @Override
    public boolean isCustomerIdImportAllowed(Admin admin) {
        return admin.permissionAllowed(Permission.IMPORT_CUSTOMERID);
    }

    @Override
    public boolean isAllowedToShowMailinglists(Admin admin) {
        return admin.permissionAllowed(Permission.MAILINGLIST_SHOW);
    }

    @Override
    public boolean isImportModeAllowed(int mode, Admin admin) {
        try {
            String token = ImportMode.getFromInt(mode).getMessageKey();
            return admin.permissionAllowed(Permission.getPermissionByToken(token));
        } catch (Exception e) {
            logger.error("Error when get import mode! Mode = {}", mode);
            return false;
        }
    }

    @Override
    public boolean isEcryptedImportAllowed(Admin admin) {
        return admin.permissionAllowed(Permission.RECIPIENT_IMPORT_ENCRYPTED);
    }

    @Override
    public Set<ImportMode> getAvailableImportModes(Admin admin) {
        return ImportMode.values().stream()
                .filter(mode -> isImportModeAllowed(mode.getIntValue(), admin))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparingInt(ImportMode::getIntValue))));
    }

    @Override
    public boolean isManageAllowed(ImportProfile profile, Admin admin) {
        if (CheckForDuplicates.COMPLETE.getIntValue() == profile.getCheckForDuplicates() && !isCheckForDuplicatesAllowed(admin)) {
            return false;
        }

        if (!profile.getUpdateAllDuplicates() && !isUpdateDuplicatesChangeAllowed(admin)) {
            return false;
        }

        if (profile.getImportProcessActionID() > 0 && !isPreprocessingAllowed(admin)) {
            return false;
        }

        if (profile.isMailinglistsAll() && (!isAllMailinglistsAllowed(admin) || !isAllowedToShowMailinglists(admin))) {
            return false;
        }

        if (RecipientStandardField.CustomerID.getColumnName().equalsIgnoreCase(profile.getFirstKeyColumn()) && !isCustomerIdImportAllowed(admin)) {
            return false;
        }

        if (!isImportModeAllowed(profile.getImportMode(), admin)) {
            return false;
        }

        if (!profile.getMailinglistIds().isEmpty() && !isAllowedToShowMailinglists(admin)) {
            return false;
        }

        boolean encryptedMappingExists = profile.getColumnMapping().stream().anyMatch(m -> m.isEncrypted());
        return !encryptedMappingExists || isEcryptedImportAllowed(admin);
    }

    @Required
    public void setImportProfileDao(ImportProfileDao importProfileDao) {
        this.importProfileDao = importProfileDao;
    }

    @Required
    public void setRecipientService(RecipientService recipientService) {
        this.recipientService = recipientService;
    }
}
