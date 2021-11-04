/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.ImportUtils;
import org.agnitas.web.StrutsActionBase;
import org.agnitas.web.forms.ImportBaseFileForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.agnitas.emm.core.upload.bean.UploadData;
import com.agnitas.web.ImportProfileColumnsAction;

public class ImportProfileColumnsForm extends ImportBaseFileForm {

    private static final long serialVersionUID = -2073563662277194710L;
    protected int action;
    protected int profileId;
    protected ImportProfile profile;
    protected String[] dbColumns;
    private String newColumnMappingName;
    private String newColumnMappingValue;
    protected String valueType;
    protected Map<String, String> dbColumnsDefaults;
    private List<UploadData> csvFiles;
    private Set<Integer> columnIndexes = new HashSet<>();

    public List<UploadData> getCsvFiles() {
        return csvFiles;
    }

    public void setCsvFiles(List<UploadData> csvFiles) {
        this.csvFiles = csvFiles;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public ImportProfile getProfile() {
        return profile;
    }

    public void setProfile(ImportProfile profile) {
        this.profile = profile;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String[] getDbColumns() {
        return dbColumns;
    }

    public void setDbColumns(String[] dbColumns) {
        this.dbColumns = dbColumns;
    }

    public String getNewColumnMappingValue() {
        return newColumnMappingValue;
    }

    public void setNewColumnMappingValue(String newColumnMappingValue) {
        this.newColumnMappingValue = ImportUtils.fixEncoding(newColumnMappingValue);
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public void resetFormData() {
        profile = null;
        newColumnMappingValue = "";
        dbColumns = new String[0];
        dbColumnsDefaults = new HashMap<>();
        columnIndexes.clear();
    }

    @Override
    public ActionErrors formSpecificValidate(ActionMapping actionMapping, HttpServletRequest request) {
        ActionErrors errors = super.formSpecificValidate(actionMapping, request);
        if (errors == null) {
            errors = new ActionErrors();
        }
        storeMappings(request);
        if ((action == StrutsActionBase.ACTION_SAVE || action == ImportProfileColumnsAction.ACTION_SAVE_AND_START) &&
                !ImportUtils.hasNoEmptyParameterStartsWith(request, "removeMapping")) {
            if (AgnUtils.parameterNotEmpty(request, "add")) {
                if (!StringUtils.isEmpty(newColumnMappingName) && columnExists(newColumnMappingName, profile.getColumnMapping())) {
                    errors.add("newColumn", new ActionMessage("error.import.column.duplicate"));
                }
            } else {
                Set<String> dbColumnSet = new HashSet<>();
                List<ColumnMapping> mappings = profile.getColumnMapping();
                for (ColumnMapping mapping : mappings) {
                    if (!ColumnMapping.DO_NOT_IMPORT.equals(mapping.getDatabaseColumn())) {
                        if (dbColumnSet.contains(mapping.getDatabaseColumn())) {
                            errors.add("mapping_" + mapping.getFileColumn(),
                                    new ActionMessage("error.import.column.dbduplicate"));
                        }
                        dbColumnSet.add(mapping.getDatabaseColumn());
                    }
                }
            }
        }
        return errors;
    }

    private void storeMappings(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            if (name.startsWith("dbColumn_")) {
                int index = Integer.valueOf(name.substring(9));
                String dbColumn = request.getParameter(name);
                boolean mandatory = request.getParameter("mandatory_" + index) != null;
                boolean encrypted = request.getParameter("encrypted_" + index) != null;

                String defaultValue = request.getParameter("default_value_" + index);
                defaultValue = ImportUtils.fixEncoding(defaultValue);

                List<ColumnMapping> columnMappings = profile.getColumnMapping();
                if (index < columnMappings.size()) {
                    ColumnMapping mapping = columnMappings.get(index);
                    mapping.setDatabaseColumn(dbColumn);
                    mapping.setMandatory(mandatory);
                    mapping.setEncrypted(encrypted);
                    mapping.setDefaultValue(defaultValue);
                    if (ImportUtils.getHiddenColumns(AgnUtils.getAdmin(request)).contains(dbColumn)) {
                        mapping.setDatabaseColumn(ColumnMapping.DO_NOT_IMPORT);
                    }
                }
            }
        }
    }

    public ColumnMapping getMappingByFileColumn(String csvColumn, List<ColumnMapping> columnMappings) {
        for (ColumnMapping columnMapping : columnMappings) {
            if (csvColumn.equals(columnMapping.getFileColumn())) {
                return columnMapping;
            }
        }
        return null;
    }

    public boolean columnExists(String csvColumn, List<ColumnMapping> columnMappings) {
        for (ColumnMapping columnMapping : columnMappings) {
            if (csvColumn.equals(columnMapping.getFileColumn()) || (csvColumn.equals(columnMapping.getDatabaseColumn())
                    && !ColumnMapping.DO_NOT_IMPORT.equals(columnMapping.getDatabaseColumn()))) {
                return true;
            }
        }
        return false;
    }

    public int getMappingNumber() {
        return profile.getColumnMapping().size();
    }

    public Map<String, String> getDbColumnsDefaults() {
        return dbColumnsDefaults;
    }

    public void setDbColumnsDefaults(Map<String, String> dbColumnsDefaults) {
        this.dbColumnsDefaults = dbColumnsDefaults;
    }

    public Set<Integer> getColumnIndexes() {
        return columnIndexes;
    }

    public void setColumnIndexes(Set<Integer> columnIndexes) {
        this.columnIndexes = columnIndexes;
    }

    public String getColumnIndex(int elementId) {
        return columnIndexes.contains(elementId) ? "on" : "";
    }

    public void setColumnIndex(int elementId, String value) {
        if (AgnUtils.interpretAsBoolean(value)) {
            columnIndexes.add(elementId);
        }
    }

    public String getNewColumnMappingName() {
        return newColumnMappingName;
    }

    public void setNewColumnMappingName(String newColumnMappingName) {
        this.newColumnMappingName = newColumnMappingName;
    }
}
