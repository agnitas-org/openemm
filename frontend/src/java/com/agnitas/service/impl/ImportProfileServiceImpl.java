/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import static com.agnitas.util.Const.Mvc.ERROR_MSG;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.import_profile.dao.ImportProfileDao;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.messages.Message;
import com.agnitas.service.ImportProfileService;
import com.agnitas.service.ServiceResult;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Const;
import com.agnitas.util.importvalues.CheckForDuplicates;
import com.agnitas.util.importvalues.ImportMode;
import com.agnitas.web.forms.PaginationForm;
import org.agnitas.emm.core.autoimport.bean.AutoImportLight;
import org.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class ImportProfileServiceImpl implements ImportProfileService {

    private static final Logger logger = LogManager.getLogger(ImportProfileServiceImpl.class);

    private RecipientService recipientService;
    private ImportProfileDao importProfileDao;
    private BulkActionValidationService<Integer, ImportProfile> bulkActionValidationService;
    private RecipientFieldService recipientFieldService;

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
        boolean encryptedImportAllowed = isEncryptedImportAllowed(admin);

        List<Integer> columnsForRemove = Collections.emptyList();

        if (profileId != 0) {
            ImportProfile profile = importProfileDao.getImportProfileById(profileId);
            columnsForRemove = getColumnIdsForRemove(columnMappings, profile.getColumnMapping());
        }

        List<String> hiddenColumns = RecipientStandardField.getImportChangeNotAllowedColumns(admin.permissionAllowed(Permission.IMPORT_CUSTOMERID));

        for (ColumnMapping mapping : columnMappings) {
            mapping.setProfileId(profileId);

            if (!encryptedImportAllowed) {
                mapping.setEncrypted(false);
            }

            if (hiddenColumns.contains(mapping.getDatabaseColumn())) {
                mapping.setDatabaseColumn(ColumnMapping.DO_NOT_IMPORT);
            }
        }

        List<ColumnMapping> columnsForInsert = columnMappings.stream()
                .filter(item -> item.getId() == 0)
                .toList();
        List<ColumnMapping> columnsForUpdate = columnMappings.stream()
                .filter(item -> item.getId() != 0)
                .toList();

        importProfileDao.deleteColumnMappings(columnsForRemove);
        importProfileDao.insertColumnMappings(columnsForInsert);
        importProfileDao.updateColumnMappings(columnsForUpdate);
    }

    @Override
    public void saveImportProfileWithoutColumnMappings(ImportProfile profile, Admin admin) {
        if (!isManageAllowed(profile, admin)) {
            throw new UnsupportedOperationException("Import profile (ID: %d) can't be managed by admin (ID: %d)!".formatted(profile.getId(), admin.getAdminID()));
        }

        if (profile.getId() == 0) {
            importProfileDao.insertImportProfile(profile);
        } else {
            importProfileDao.updateImportProfile(profile);
        }
    }

    @Override
    public void updateEmails(String emailForError, String emailForReport, int id) {
        importProfileDao.updateEmails(emailForError, emailForReport, id);
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
    public List<RecipientFieldDescription> getAvailableFieldsForMappings(ImportProfile profile, Admin admin) {
        List<String> notAllowedColumns = RecipientStandardField.getImportChangeNotAllowedColumns(admin.permissionAllowed(Permission.IMPORT_CUSTOMERID));

        return recipientFieldService.getRecipientFields(admin.getCompanyID())
                .stream()
                .filter(f -> !notAllowedColumns.contains(f.getColumnName()))
                .filter(f -> {
                    ProfileFieldMode fieldMode = f.getAdminPermission(admin.getAdminID());
                    // User may also map readonly columns, but in import action, those are checked to be only used as keycolumns
                    return fieldMode == ProfileFieldMode.Editable
                            || (fieldMode == ProfileFieldMode.ReadOnly && profile.getKeyColumns().contains(f.getColumnName()));
                })
                .toList();
    }

    @Override
    public PaginatedListImpl<ImportProfile> getOverview(PaginationForm form, Admin admin) {
        List<ImportProfile> availableProfiles = getAvailableImportProfiles(admin);

        int page = AgnUtils.getValidPageNumber(availableProfiles.size(), form.getPage(), form.getNumberOfRows());
        List<ImportProfile> sortedProfiles = availableProfiles.stream()
                .sorted(getComparator(form))
                .skip((long) (page - 1) * form.getNumberOfRows())
                .limit(form.getNumberOfRows())
                .toList();

        return new PaginatedListImpl<>(sortedProfiles, availableProfiles.size(), form.getNumberOfRows(), page, form.getSortOrDefault("name"), form.getOrder());
    }

    private Comparator<ImportProfile> getComparator(PaginationForm form) {
        Comparator<ImportProfile> comparator = Comparator.comparing(c -> StringUtils.trimToEmpty(c.getName()));

        if (!AgnUtils.sortingDirectionToBoolean(form.getOrder(), true)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    @Override
    public List<ImportProfile> getAvailableImportProfiles(Admin admin) {
        return importProfileDao.getImportProfilesByCompanyId(admin.getCompanyID())
                .stream()
                .filter(p -> isManageAllowed(p, admin))
                .toList();
    }

    @Override
    public List<ImportProfile> findAllByEmailPart(String email, int companyID) {
        return importProfileDao.findAllByEmailPart(email, companyID);
    }

    @Override
    public List<ImportProfile> findAllByEmailPart(String email) {
        return importProfileDao.findAllByEmailPart(email);
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
                .toList();

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
        String token = ImportMode.getFromInt(mode).getMessageKey();
        return admin.permissionAllowed(Permission.getPermissionByToken(token));
    }

    @Override
    public boolean isEncryptedImportAllowed(Admin admin) {
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
            logger.warn("User (adminID: {}) can't manage import profile (ID: {}) because 'Check for duplicates' option enabled", admin.getAdminID(), profile.getId());
            return false;
        }

        if (!profile.getUpdateAllDuplicates() && !isUpdateDuplicatesChangeAllowed(admin)) {
            logger.warn("User (adminID: {}) can't manage import profile (ID: {}) because 'Update all duplicates' option enabled", admin.getAdminID(), profile.getId());
            return false;
        }

        if (profile.getImportProcessActionID() > 0 && !isPreprocessingAllowed(admin)) {
            logger.warn("User (adminID: {}) can't manage import profile (ID: {}) because 'Pre-Import action' option selected", admin.getAdminID(), profile.getId());
            return false;
        }

        if (profile.isMailinglistsAll() && (!isAllMailinglistsAllowed(admin) || !isAllowedToShowMailinglists(admin))) {
            logger.warn("User (adminID: {}) can't manage import profile (ID: {}) because 'All mailing lists' option enabled", admin.getAdminID(), profile.getId());
            return false;
        }

        if (RecipientStandardField.CustomerID.getColumnName().equalsIgnoreCase(profile.getFirstKeyColumn()) && !isCustomerIdImportAllowed(admin)) {
            logger.warn("User (adminID: {}) can't manage import profile (ID: {}) because 'Key column' option is set to 'customer_id'", admin.getAdminID(), profile.getId());
            return false;
        }

        if (!isImportModeAllowed(profile.getImportMode(), admin)) {
            logger.warn("User (adminID: {}) can't manage import profile (ID: {}) because import mode ({}) not allowed for him", admin.getAdminID(), profile.getId(), profile.getImportMode());
            return false;
        }

        if (!profile.getMailinglistIds().isEmpty() && !isAllowedToShowMailinglists(admin)) {
            logger.warn("User (adminID: {}) can't manage import profile (ID: {}) because 'Mailing lists' selected", admin.getAdminID(), profile.getId());
            return false;
        }

        boolean encryptedMappingExists = profile.getColumnMapping().stream().anyMatch(ColumnMapping::isEncrypted);
        if (encryptedMappingExists && !isEncryptedImportAllowed(admin)) {
            logger.warn("User (adminID: {}) can't manage import profile (ID: {}) because exists encrypted mappings", admin.getAdminID(), profile.getId());
            return false;
        }

        return true;
    }

    @Override
    public ServiceResult<List<ImportProfile>> getAllowedForDeletion(Set<Integer> ids, Admin admin) {
        return bulkActionValidationService.checkAllowedForDeletion(ids, id -> getImportProfileForDeletion(id, admin));
    }

    @Override
    public ServiceResult<UserAction> delete(Set<Integer> ids, Admin admin) {
        List<Integer> allowedIds = ids.stream()
                .map(id -> getImportProfileForDeletion(id, admin))
                .filter(ServiceResult::isSuccess)
                .map(r -> r.getResult().getId())
                .toList();

        allowedIds.forEach(this::deleteImportProfileById);

        return ServiceResult.success(
                new UserAction(
                        "delete import profiles",
                        "deleted import profiles with following ids: " + StringUtils.join(allowedIds, ", ")
                ),
                Message.of(Const.Mvc.SELECTION_DELETED_MSG)
        );
    }

    private ServiceResult<ImportProfile> getImportProfileForDeletion(int id, Admin admin) {
        ImportProfile profile = getImportProfileById(id);
        if (profile == null) {
            return ServiceResult.errorKeys("error.general.missing");
        }

        if (!isManageAllowed(profile, admin)) {
            return ServiceResult.errorKeys(ERROR_MSG);
        }

        Optional<AutoImportLight> dependentAutoImport = findDependentAutoImport(id);
        if (dependentAutoImport.isPresent()) {
            AutoImportLight autoImport = dependentAutoImport.get();
            String autoImportDescription = String.format("%s (ID: %d)", autoImport.getShortname(), autoImport.getAutoImportId());

            return ServiceResult.error(Message.of("error.profileStillUsed", autoImportDescription));
        }

        return ServiceResult.success(profile);
    }

    protected Optional<AutoImportLight> findDependentAutoImport(@SuppressWarnings("unused") int profileId) {
        return Optional.empty();
    }

    public void setImportProfileDao(ImportProfileDao importProfileDao) {
        this.importProfileDao = importProfileDao;
    }

    public void setRecipientService(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    public void setRecipientFieldService(RecipientFieldService recipientFieldService) {
        this.recipientFieldService = recipientFieldService;
    }

    public void setBulkActionValidationService(BulkActionValidationService<Integer, ImportProfile> bulkActionValidationService) {
        this.bulkActionValidationService = bulkActionValidationService;
    }
}
