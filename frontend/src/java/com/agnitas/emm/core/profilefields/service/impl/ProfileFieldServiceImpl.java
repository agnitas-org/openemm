/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.ProfileFieldImpl;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.profilefields.ProfileFieldException;
import com.agnitas.emm.core.profilefields.bean.ProfileFieldDependentType;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.post.MandatoryField;
import com.agnitas.service.ColumnInfoService;
import org.agnitas.beans.LightProfileField;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
/**
 * @deprecated Use RecipientFieldService instead
 */
@Deprecated
public final class ProfileFieldServiceImpl implements ProfileFieldService {

    private static final Logger logger = LogManager.getLogger(ProfileFieldServiceImpl.class);

    private ProfileFieldDao profileFieldDao;
    private ColumnInfoService columnInfoService;
    private ComWorkflowService workflowService;
    private ComTargetService targetService;

    @Required
    public void setTargetService(ComTargetService targetService) {
        this.targetService = targetService;
    }

    @Required
    public void setColumnInfoService(ColumnInfoService columnInfoService) {
        this.columnInfoService = columnInfoService;
    }

    @Required
    public final void setProfileFieldDao(final ProfileFieldDao dao) {
        this.profileFieldDao = Objects.requireNonNull(dao, "Profile field DAO cannot be null");
    }

    public void setWorkflowService(ComWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public final String translateDatabaseNameToVisibleName(final int companyID, final String databaseName) throws ProfileFieldException {
        try {
            return this.profileFieldDao.getProfileField(companyID, databaseName).getShortname();
        } catch (final Exception e) {
            final String msg = String.format("Unable to translate profile field database name '%s' to visible name (company ID %d)", databaseName, companyID);

            logger.error(msg, e);

            throw new ProfileFieldException(msg, e);
        }
    }

    @Override
	public final String translateVisibleNameToDatabaseName(final int companyID, final String visibleName) throws ProfileFieldException {
    	try {
    		return this.profileFieldDao.getProfileFieldByShortname(companyID, visibleName).getColumn();
    	} catch(final Exception e) {
            final String msg = String.format("Unable to translate profile field visible name '%s' to database name (company ID %d)", visibleName, companyID);

            logger.error(msg, e);

            throw new ProfileFieldException(msg, e);
        }
	}

	@Override
    public List<ProfileField> getProfileFieldsWithInterest(Admin admin) {
        try {
            return profileFieldDao.getProfileFieldsWithInterest(admin.getCompanyID(), admin.getAdminID());
        } catch (Exception e) {
            logger.error(String.format("Error occurred: %s", e.getMessage()), e);
        }

        return new ArrayList<>();
    }

    @Override
    public boolean isAddingNearLimit(int companyId) {
        return profileFieldDao.isNearLimit(companyId);
    }

    @Override
    public boolean isWithinGracefulLimit(int companyId) {
        return profileFieldDao.isWithinGracefulLimit(companyId);
    }

    @Override
    public PaginatedListImpl<ProfileField> getPaginatedFieldsList(int companyId, String sortColumn, String order, int page, int rowsCount) throws Exception {
        List<ProfileField> fields = getSortedColumnInfo(companyId);
        sortProfileFields(fields, sortColumn, order);

        List<ProfileField> partialFields = buildPartialProfileFieldList(fields, page, rowsCount);

        return new PaginatedListImpl<>(partialFields, fields.size(), rowsCount, page, sortColumn, order);
    }

    private void sortProfileFields(List<ProfileField> profileFields, String column, String order) {
        if (CollectionUtils.isEmpty(profileFields) || StringUtils.isBlank(column)) {
            return;
        }

        Map<String, Comparator<ProfileField>> sortableColumns = new HashMap<>();

        sortableColumns.put("shortname", Comparator.comparing(pf -> pf.getShortname().toLowerCase()));
        sortableColumns.put("column", Comparator.comparing(pf -> pf.getColumn().toLowerCase()));
        sortableColumns.put("dataType", Comparator.comparing(pf -> pf.getDataType().toLowerCase()));
        sortableColumns.put("dataTypeLength", Comparator.comparing(ProfileField::getDataTypeLength));
        sortableColumns.put("defaultValue", Comparator.comparing(pf -> pf.getDefaultValue().toLowerCase()));
        sortableColumns.put("modeEdit", Comparator.comparing(pf -> pf.getModeEdit().getStorageCode()));
        sortableColumns.put("sort", Comparator.comparing(ProfileField::getSort));

        if (sortableColumns.containsKey(column)) {
            Comparator<ProfileField> comparator = sortableColumns.get(column);

            boolean isAscendingOrder = "asc".equalsIgnoreCase(order) || "ascending".equalsIgnoreCase(order);
            if (!isAscendingOrder) {
                comparator = comparator.reversed();
            }

            profileFields.sort(comparator);
        } else {
            logger.error("Profile field comparator was not found for property '{}'!", column);
        }
    }

    private List<ProfileField> buildPartialProfileFieldList(List<ProfileField> fields, int pageNumber, int rowsCount) {
        int entriesOffset = 0;

        if (pageNumber > 1) {
            entriesOffset = rowsCount * (pageNumber - 1);
        }

       return fields.stream()
                .skip(entriesOffset)
                .limit(rowsCount)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfileField> getSortedColumnInfo(int companyId) {
        try {
            List<ProfileField> columnInfoList = columnInfoService.getComColumnInfos(companyId);

            columnInfoList.sort(this::compareColumn);

            return columnInfoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAllExceptSpecifiedNames(List<String> excludedFields, int companyId) {
        // Get all available customer fields
        List<String> availableCustomerFields = getSortedColumnInfo(companyId)
                .stream()
                .map(LightProfileField::getColumn)
                .collect(Collectors.toList());

        availableCustomerFields.removeAll(excludedFields);

        return availableCustomerFields;
    }

    @Override
    public List<ProfileField> getFieldWithIndividualSortOrder(int companyId, int adminId) {
        try {
            return profileFieldDao.getProfileFieldsWithIndividualSortOrder(companyId, adminId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LightProfileField> getLightProfileFields(int companyId) {
        try {
            return profileFieldDao.getLightProfileFields(companyId);
        } catch (Exception e) {
            logger.error(format("Error occurred: {0}", e.getMessage()), e);
            return Collections.emptyList();
        }
    }

    @Override
    public int getCurrentSpecificFieldCount(int companyId) {
        try {
            return profileFieldDao.getCurrentCompanySpecificFieldCount(companyId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getMaximumCompanySpecificFieldCount(int companyId) {
        try {
            return profileFieldDao.getMaximumCompanySpecificFieldCount(companyId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(int companyId, String fieldName) {
        return profileFieldDao.exists(fieldName, companyId);
    }

    @Override
    public ProfileField getProfileField(int companyId, String fieldName) {
        try {
            return columnInfoService.getColumnInfo(companyId, fieldName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProfileField getProfileField(int companyId, String fieldName, int adminId) {
        try {
            return columnInfoService.getColumnInfo(companyId, fieldName, adminId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTrackingDependentWorkflows(int companyId, String fieldName) {
        String workflowName = null;

        List<Workflow> workflows = workflowService.getActiveWorkflowsTrackingProfileField(fieldName, companyId);

        if (!workflows.isEmpty()) {
            workflowName = workflows.get(0).getShortname();
        }

        return workflowName;
    }

    @Override
    public Set<String> getSelectedFieldsWithHistoryFlag(int companyId) {
        return profileFieldDao.listUserSelectedProfileFieldColumnsWithHistoryFlag(companyId);
    }

    @Override
    public void createMandatoryFieldsIfNotExist(Admin admin) {
        int companyID = admin.getCompanyID();

        if (!exists(companyID, MandatoryField.STRASSE.getName())) {
            createNewVarcharField(MandatoryField.STRASSE.getName(), 200, admin);
        }
        if (!exists(companyID, MandatoryField.PLZ.getName())) {
            createNewVarcharField(MandatoryField.PLZ.getName(), 5, admin);
        }
        if (!exists(companyID, MandatoryField.ORT.getName())) {
            createNewVarcharField(MandatoryField.ORT.getName(), 100, admin);
        }
    }

    private boolean createNewVarcharField(String column, int length, Admin admin) {
        ProfileField profileField = new ProfileFieldImpl();

        profileField.setCompanyID(admin.getCompanyID());
        profileField.setColumn(column);
        profileField.setDataType("VARCHAR");
        profileField.setDataTypeLength(length);

       return createNewField(profileField, admin);
    }

    @Override
    public boolean createNewField(ProfileField field, Admin admin) {
        try {
            return profileFieldDao.saveProfileField(field, admin);
        } catch (Exception e) {
            logger.error("Something went wrong when tried to save new field", e);

            return false;
        }
    }

    @Override
    public void removeProfileField(int companyId, String fieldName) {
        try {
            profileFieldDao.removeProfileField(companyId, fieldName);
        } catch (ProfileFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updateField(ProfileField field, Admin admin) {
        try {
            return profileFieldDao.saveProfileField(field, admin);
        } catch (Exception e) {
            logger.error("Something went wrong when tried to update new field", e);

            return false;
        }
    }


    @Override
    public UserAction getCreateFieldLog(String shortName) {
        return new UserAction("create profile field", "Profile field " + shortName + " created");
    }

    @Override
    public UserAction getDeleteFieldLog(String fieldName) {
        return new UserAction("delete profile field", String.format("Profile field %s removed", fieldName));
    }

    @Override
    public UserAction getOpenEmmChangeLog(ProfileField field, ProfileFieldForm form) {
        List<String> changes = new ArrayList<>();
        UserAction userAction = null;

        String existingDescription = field.getDescription().trim();
        String newDescription = form.getDescription().trim();
        String existingDefaultValue = field.getDefaultValue().trim();
        String newDefaultValue = form.getFieldDefault().trim();
        String existingShortName = field.getShortname();
        String newShortName = form.getShortname();

        //log field name changes
        if (!existingShortName.equals(newShortName)) {
            changes.add(String.format("name changed from %s to %s", existingShortName, newShortName));
        }

        //log description name changes
        if (!existingDescription.equals(newDescription)) {
            if (existingDescription.length() <= 0 && newDescription.length() > 0) {
                changes.add("description added");
            }
            if (existingDescription.length() > 0 && newDescription.length() <= 0) {
                changes.add("description removed");
            }
            if (existingDescription.length() > 0 && newDescription.length() > 0) {
                changes.add("description changed");
            }
        }

        //log default value changes
        if (!existingDefaultValue.equals(newDefaultValue)) {
            if (existingDefaultValue.length() <= 0 && newDefaultValue.length() > 0) {
                changes.add(String.format("default value %s added", newDefaultValue));
            }
            if (existingDefaultValue.length() > 0 && newDefaultValue.length() <= 0) {
                changes.add(String.format("default value %s removed", existingDefaultValue));
            }
            if (existingDefaultValue.length() > 0 && newDefaultValue.length() > 0) {
                changes.add(String.format("default value changed from %s to %s", existingDefaultValue, newDefaultValue));
            }
        }

        List<String> deletedFixedValues = detectDeletedFixedValues(field, form);
        if (!deletedFixedValues.isEmpty()) {
            changes.add("fixed values deleted: [" + String.join(", ", deletedFixedValues) + "]");
        }

        List<String> addedFixedValues = detectNewAddedFixedValues(field, form);
        if (!addedFixedValues.isEmpty()) {
            changes.add("fixed values added: [" + String.join(", ", addedFixedValues) + "]");
        }

        if (!changes.isEmpty()) {
            userAction = new UserAction("edit profile field", String.format("Short name: %s. Profile field:%n%s",
                    existingShortName, StringUtils.join(changes, System.lineSeparator())));
        }

        return userAction;
    }

    private List<String> detectNewAddedFixedValues(ProfileField field, ProfileFieldForm form) {
        if (form.getAllowedValues() == null) {
            return Collections.emptyList();
        }

        if (field.getAllowedValues() == null) {
            return List.of(form.getAllowedValues());
        }

        List<String> addedValues = new ArrayList<>();
        List<String> previousValues = List.of(field.getAllowedValues());

        for (String newValue : form.getAllowedValues()) {
            if (!previousValues.contains(newValue)) {
                addedValues.add(newValue);
            }
        }

        return addedValues;
    }

    private List<String> detectDeletedFixedValues(ProfileField field, ProfileFieldForm form) {
        if (field.getAllowedValues() == null) {
            return Collections.emptyList();
        }

        if (form.getAllowedValues() == null) {
            return List.of(field.getAllowedValues());
        }

        List<String> deletedValues = new ArrayList<>();
        List<String> formValues = List.of(form.getAllowedValues());

        for (String fieldValue : field.getAllowedValues()) {
            if (!formValues.contains(fieldValue)) {
                deletedValues.add(fieldValue);
            }
        }

        return deletedValues;
    }

    @Override
    public UserAction getEmmChangeLog(ProfileField field, ProfileFieldForm form) {
        List<String> changes = new ArrayList<>();
        UserAction userAction = null;

        String existingShortName = field.getShortname();

        //log if "Field visible" checkbox changed
        if ((field.getModeEdit() == ProfileFieldMode.NotVisible) && (form.isFieldVisible())) {
            changes.add("Field visible checked");
        }
        if ((field.getModeEdit() == ProfileFieldMode.Editable) && (!form.isFieldVisible())) {
            changes.add("Field visible unchecked");
        }
        //log if "Line after this field" checkbox changed
        if ((field.getLine() == 0) && (form.getLine())) {
            changes.add("Line after this field checked");
        }
        if ((field.getLine() == 2) && (!form.getLine())) {
            changes.add("Line after this field unchecked");
        }
        //log if "Value defines grade of interest" checkbox changed
        if ((field.getInterest() == 0) && (form.isInterest())) {
            changes.add("Value defines grade of interest checked");
        }
        if ((field.getInterest() == 2) && (!form.isInterest())) {
            changes.add("Value defines grade of interest unchecked");
        }
        //log if "Sort" changed
        if (field.getSort() != form.getFieldSort()) {
            changes.add("Sort changed");
        }
        if (!field.getHistorize() && form.isIncludeInHistory()) {
            changes.add("'Include field in history' is activated");
        }

        if (changes.size() > 0) {
            userAction = new UserAction("edit profile field", String.format("Short name: %s.%n%s",
                    existingShortName, StringUtils.join(changes, System.lineSeparator())));
        }

        return userAction;
    }

    // TODO check after ProfileFieldsControllerOld.java has been removed
    @Override
    public List<Dependent<ProfileFieldDependentType>> getDependents(int companyId, String fieldName) {
        List<Workflow> dependentWorkflows = workflowService.getActiveWorkflowsDependentOnProfileField(fieldName, companyId);
        List<TargetLight> dependentTargets = targetService.listTargetGroupsUsingProfileFieldByDatabaseName(fieldName, companyId);

        List<Dependent<ProfileFieldDependentType>> dependents = new ArrayList<>(dependentWorkflows.size() + dependentTargets.size());

        for (Workflow workflow : dependentWorkflows) {
            dependents.add(ProfileFieldDependentType.WORKFLOW.forId(workflow.getWorkflowId(), workflow.getShortname()));
        }

        for (TargetLight target : dependentTargets) {
            dependents.add(ProfileFieldDependentType.TARGET_GROUP.forId(target.getId(), target.getTargetName()));
        }

        return dependents;
    }

    private int compareColumn(ProfileField field1, ProfileField field2) {
        if (field1.isHiddenField() == field2.isHiddenField()) {
            return 0;
        }
        return field1.isHiddenField() ? -1 : 1;
    }

    @Override
    public List<ProfileField> getProfileFields(int companyId) throws Exception {
        return profileFieldDao.getComProfileFields(companyId);
    }

    @Override
    public List<ProfileField> getProfileFields(int companyId, int adminId) throws Exception {
        return profileFieldDao.getComProfileFields(companyId, adminId);
    }

    @Override
    public List<ProfileField> getVisibleProfileFields(int companyId) {
        try {
            List<ProfileField> profileFields = getProfileFields(companyId);
            return filterVisibleFields(profileFields);
        } catch (Exception e) {
            logger.error("Can't retrieve visible profile fields!", e);
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProfileField> getVisibleProfileFields(int adminId, int companyId) {
        try {
            List<ProfileField> profileFields = getProfileFields(companyId, adminId);
            return filterVisibleFields(profileFields);
        } catch (Exception e) {
            logger.error("Can't retrieve visible profile fields!", e);
        }

        return Collections.emptyList();
    }

    private List<ProfileField> filterVisibleFields(List<ProfileField> fields) {
        return fields
                .stream()
                .filter(pf -> !ProfileFieldMode.NotVisible.equals(pf.getModeEdit()))
                .collect(Collectors.toList());
    }

	@Override
	public boolean isReservedKeyWord(String fieldname) {
		return profileFieldDao.isReservedKeyWord(fieldname);
	}

	@Override
	public int getMaximumNumberOfCompanySpecificProfileFields() throws Exception {
		return profileFieldDao.getMaximumNumberOfCompanySpecificProfileFields();
	}

    @Override
    public ProfileField getProfileFieldByShortname(int companyID, String shortname) {
        try {
            return profileFieldDao.getProfileFieldByShortname(companyID, shortname);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProfileField getProfileFieldByShortname(int companyID, String shortname, int adminId) {
        try {
            return profileFieldDao.getProfileFieldByShortname(companyID, shortname, adminId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
