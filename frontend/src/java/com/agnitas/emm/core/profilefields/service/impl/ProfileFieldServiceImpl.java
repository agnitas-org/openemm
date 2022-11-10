/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.profilefields.ProfileFieldException;
import com.agnitas.emm.core.profilefields.bean.ProfileFieldDependentType;
import com.agnitas.emm.core.profilefields.form.ProfileFieldForm;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.service.ComColumnInfoService;

public final class ProfileFieldServiceImpl implements ProfileFieldService {
    /**
     * The logger.
     */
    private static final Logger logger = LogManager.getLogger(ProfileFieldServiceImpl.class);

    private ComProfileFieldDao profileFieldDao;
    private ComColumnInfoService columnInfoService;
    private ComWorkflowService workflowService;
    private ComTargetService targetService;

    @Required
    public void setTargetService(ComTargetService targetService) {
        this.targetService = targetService;
    }

    @Required
    public void setColumnInfoService(ComColumnInfoService columnInfoService) {
        this.columnInfoService = columnInfoService;
    }

    @Required
    public final void setProfileFieldDao(final ComProfileFieldDao dao) {
        this.profileFieldDao = Objects.requireNonNull(dao, "Profile field DAO cannot be null");
    }

    public void setWorkflowService(ComWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public final boolean checkDatabaseNameExists(@VelocityCheck final int companyID, final String fieldNameOnDatabase) throws ProfileFieldException {
        try {
            return profileFieldDao.checkProfileFieldExists(companyID, fieldNameOnDatabase);
        } catch (final Exception e) {
            final String msg = String.format("Cannot check, if profile field '%s' (company ID %d) exists", fieldNameOnDatabase, companyID);

            logger.error(msg, e);

            throw new ProfileFieldException(msg, e);
        }
    }

    @Override
    public final String translateDatabaseNameToVisibleName(@VelocityCheck final int companyID, final String databaseName) throws ProfileFieldException {
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
    public List<ProfileField> getProfileFieldsWithInterest(ComAdmin admin) {
        try {
            return profileFieldDao.getProfileFieldsWithInterest(admin.getCompanyID(), admin.getAdminID());
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
        }

        return new ArrayList<>();
    }

    @Override
    public boolean isAddingNearLimit(@VelocityCheck int companyId) {
        return profileFieldDao.isNearLimit(companyId);
    }

    @Override
    public List<ProfileField> getSortedColumnInfo(@VelocityCheck int companyId) {
        try {
            List<ProfileField> columnInfoList = columnInfoService.getComColumnInfos(companyId);

            columnInfoList.sort(this::compareColumn);

            return columnInfoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProfileField> getFieldWithIndividualSortOrder(@VelocityCheck int companyId, int adminId) {
        try {
            return profileFieldDao.getProfileFieldsWithIndividualSortOrder(companyId, adminId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getCurrentSpecificFieldCount(@VelocityCheck int companyId) {
        try {
            return profileFieldDao.getCurrentCompanySpecificFieldCount(companyId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getMaximumCompanySpecificFieldCount(@VelocityCheck int companyId) {
        try {
            return profileFieldDao.getMaximumCompanySpecificFieldCount(companyId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(@VelocityCheck int companyId, String fieldName) {
        return profileFieldDao.exists(fieldName, companyId);
    }

    @Override
    public ProfileField getProfileField(@VelocityCheck int companyId, String fieldName) {
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
    public List<String> getDependentWorkflows(@VelocityCheck int companyId, String fieldName) {
        List<String> dependentWorkflows = null;

        List<Workflow> workflows = workflowService.getActiveWorkflowsDependentOnProfileField(fieldName, companyId);

        if (!workflows.isEmpty()) {
            dependentWorkflows = workflows.stream()
                    .map(Workflow::getShortname)
                    .distinct()
                    .collect(Collectors.toList());
        }

        return dependentWorkflows;
    }

    @Override
    public String getTrackingDependentWorkflows(@VelocityCheck int companyId, String fieldName) {
        String workflowName = null;

        List<Workflow> workflows = workflowService.getActiveWorkflowsTrackingProfileField(fieldName, companyId);

        if (!workflows.isEmpty()) {
            workflowName = workflows.get(0).getShortname();
        }

        return workflowName;
    }

    @Override
    public Set<String> getSelectedFieldsWithHistoryFlag(@VelocityCheck int companyId) {
        return profileFieldDao.listUserSelectedProfileFieldColumnsWithHistoryFlag(companyId);
    }

    @Override
    public boolean createNewField(ProfileField field, ComAdmin admin) {
        try {
            return profileFieldDao.saveProfileField(field, admin);
        } catch (Exception e) {
            logger.error("Something went wrong when tried to save new field", e);

            return false;
        }
    }

    @Override
    public void removeProfileField(@VelocityCheck int companyId, String fieldName) {
        try {
            profileFieldDao.removeProfileField(companyId, fieldName);
        } catch (ProfileFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updateField(ProfileField field, ComAdmin admin) {
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

        if (changes.size() > 0) {
            userAction = new UserAction("edit profile field", String.format("Short name: %s. Profile field:%n%s",
                    existingShortName, StringUtils.join(changes, System.lineSeparator())));
        }

        return userAction;
    }

    @Override
    public UserAction getEmmChangeLog(ProfileField field, ProfileFieldForm form) {
        List<String> changes = new ArrayList<>();
        UserAction userAction = null;

        String existingShortName = field.getShortname();

        //log if "Field visible" checkbox changed
        if ((field.getModeEdit() == 2) && (form.isFieldVisible())) {
            changes.add("Field visible checked");
        }
        if ((field.getModeEdit() == 0) && (!form.isFieldVisible())) {
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

    @Override
    public List<Dependent<ProfileFieldDependentType>> getDependents(@VelocityCheck int companyId, String fieldName) {
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
	public boolean isReservedKeyWord(String fieldname) {
		return profileFieldDao.isReservedKeyWord(fieldname);
	}

	@Override
	public int getMaximumNumberOfCompanySpecificProfileFields() throws Exception {
		return profileFieldDao.getMaximumNumberOfCompanySpecificProfileFields();
	}

	@Override
	public ProfileField getProfileFieldByShortname(int companyID, String shortname) throws Exception {
		return profileFieldDao.getProfileFieldByShortname(companyID, shortname);
	}
}
