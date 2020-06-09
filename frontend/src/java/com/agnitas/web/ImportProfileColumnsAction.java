/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.ProfileField;
import org.agnitas.beans.impl.ColumnMappingImpl;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.ProfileFieldDao;
import org.agnitas.service.ImportException;
import org.agnitas.service.ImportProfileService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.CsvReader;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.TempFileInputStream;
import org.agnitas.util.ZipUtilities;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.agnitas.web.ImportBaseFileAction;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.Permission;
import com.agnitas.web.forms.ImportProfileColumnsForm;

/**
 * Action that handles import profile column mapping management
 */
public class ImportProfileColumnsAction extends ImportBaseFileAction {

    public static final int ACTION_UPLOAD = ACTION_LAST + 1;
    public static final int ACTION_ADD_COLUMN = ACTION_LAST + 2;
    public static final int ACTION_SKIP = ACTION_LAST + 3;
    public static final int ACTION_REMOVE_MAPPING = ACTION_LAST + 4;
    public static final int ACTION_BULK_REMOVE = ACTION_LAST + 5;
    public static final int ACTION_SAVE_AND_START = ACTION_LAST + 6;

    private static final transient Logger logger = Logger.getLogger(ImportProfileColumnsAction.class);
    private static final String UAL_ACTION = "edit import profile";
    private static final String NO_VALUE = "<no value>";
    private static final String LOG_HEAD = "%s (%d), csv column %s (%d)";

    protected ImportProfileService importProfileService;
    protected ComRecipientDao recipientDao;
    protected ProfileFieldDao profileFieldDao;
    protected ImportRecipientsDao importRecipientsDao;

    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
            case ACTION_UPLOAD:
                return "upload";
            case ACTION_ADD_COLUMN:
                return "add_column";
            case ACTION_SKIP:
                return "skip";
            case ACTION_REMOVE_MAPPING:
                return "remove_mapping";
            case ACTION_BULK_REMOVE:
                return "bulk_remove";
            case ACTION_SAVE_AND_START:
                return "save_and_start";
            default:
                return super.subActionMethodName(subAction);
        }
    }
    
	protected void initUI(final ImportProfileColumnsForm aForm, final ComAdmin admin, final ActionMessages errors) {
		// Nothing to do here
	}
	
    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     * <br>
     * ACTION_VIEW: resets all form data and loads import profile with columns of recipient table.<br>
     * Also adds column mappings from uploaded csv-file using import profile settings for parsing and
     * forwards to view page.
     * <br><br>
     * ACTION_UPLOAD: adds column mappings from uploaded csv-file using import profile settings for parsing and
     * forwards to view page.
     * <br><br>
     * ACTION_ADD_COLUMN: creates a completely new mapping and adds it to profile mappings list. By default database
     * column for new mapping is not set. Forwards to view page.
     * <br><br>
     * ACTION_REMOVE_MAPPING: removes existing column mapping from import profile and forwards to view page.
     * <br><br>
     * ACTION_SAVE: updates import profile with mappings in database and forwards to mappings view page.<br>
     * Before updating import profile method <code>checkErrorsOnSave</code> is called. In default
     * implementation (OpenEMM) just returns false. It can be overridden by sub classes of
     * ImportProfileColumnsAction for additional validation.
     * <br><br>
     * Any other ACTION_* would cause a forward to mappings view page.
     * <br>
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form    The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing. <br>
     *                If the request parameter "add" is set - changes action to ACTION_ADD_COLUMN.<br>
     *                If the request parameter "removeMapping" is set - changes action to ACTION_REMOVE_MAPPING.
     * @param res     The HTTP response we are creating
     * @return destination specified in struts-config.xml to forward to next jsp
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a servlet exception occurs
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse res) throws IOException, ServletException {
        // Validate the request parameters specified by the user
        ImportProfileColumnsForm aForm;
        ActionMessages errors = new ActionMessages();
        ComAdmin admin = AgnUtils.getAdmin(request);

        if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }
        if (form != null) {
            aForm = (ImportProfileColumnsForm) form;
        } else {
            aForm = new ImportProfileColumnsForm();
        }

        logger.info("Action: " + aForm.getAction());

        if (AgnUtils.parameterNotEmpty(request, "remove_file")) {
            aForm.setCsvFile(null);
        }

        initUI(aForm, admin, errors);

        super.execute(mapping, form, request, res);

        // Validate the request parameters specified by the user
        ActionMessages messages = new ActionMessages();
        ActionForward destination = null;

        if (fileUploadPerformed) {
            aForm.setAction(ACTION_UPLOAD);
        }

        if (fileRemovePerformed) {
            aForm.setAction(ACTION_SKIP);
        }

        if (AgnUtils.parameterNotEmpty(request, "add")) {
            aForm.setAction(ACTION_ADD_COLUMN);
        }

        if (ImportUtils.hasNoEmptyParameterStartsWith(request, "removeMapping")) {
            aForm.setAction(ACTION_REMOVE_MAPPING);
        }

        try {
            switch (aForm.getAction()) {

                case ACTION_VIEW:
                    aForm.setCsvFile(null);
                    aForm.setCurrentFileName(null);
                    removeStoredCsvFile(request);
                    
                    aForm.reset(mapping, request);
                    aForm.resetFormData();
                    loadImportProfile(aForm, request);
                    loadDbColumns(aForm, request);
                    aForm.setAction(ImportProfileColumnsAction.ACTION_SAVE);
                    destination = mapping.findForward("view");
                    break;

                case ACTION_UPLOAD:
					setColumnsFromFile(aForm, request, errors);
                    aForm.setAction(ImportProfileColumnsAction.ACTION_SAVE);
                    destination = mapping.findForward("view");
                    break;

                case ACTION_ADD_COLUMN:
                    addColumn(aForm);
                    aForm.setAction(ImportProfileColumnsAction.ACTION_SAVE);
                    destination = mapping.findForward("view");
                    break;

                case ACTION_REMOVE_MAPPING:
                    String value = ImportUtils.getNotEmptyValueFromParameter(request, "removeMapping_");
                    if(StringUtils.isNotEmpty(value)){
                        Integer columnIndex = Integer.valueOf(value);
                        List<ColumnMapping> mappingList = aForm.getProfile().getColumnMapping();
                        mappingList.remove(columnIndex.intValue());
                    }
                    aForm.setAction(ImportProfileColumnsAction.ACTION_SAVE);
                    destination = mapping.findForward("view");
                    break;

                case ACTION_BULK_REMOVE:
                    List<Integer> sortedIndexes = new ArrayList<>(aForm.getColumnIndexes());
                    sortedIndexes.sort(((o1, o2) -> o2 - o1));
                    for(Integer columnIndex : sortedIndexes){
                        aForm.getProfile().getColumnMapping().remove(columnIndex.intValue());
                    }
                    aForm.setAction(ImportProfileColumnsAction.ACTION_SAVE);
                    destination = mapping.findForward("view");
                    break;

                case ACTION_SAVE:
                case ACTION_SAVE_AND_START:
                    if (!checkErrorsOnSave(AgnUtils.getAdmin(request), aForm, errors)) {
                        ImportProfile oldProfile = importProfileService.getImportProfileById(aForm.getProfileId());
                        ImportProfile newProfile = aForm.getProfile();
                        saveMappings(newProfile);
                        newProfile = importProfileService.getImportProfileById(newProfile.getId());
                        logChangedMappings(newProfile, oldProfile, admin);
                        aForm.setProfile(newProfile);
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                        
                        if (aForm.getAction() == ACTION_SAVE_AND_START) {
	                        request.setAttribute("defaultProfileId", aForm.getProfile().getId());
	                        destination = mapping.findForward("start");
                        } else {
                            aForm.setAction(ImportProfileColumnsAction.ACTION_SAVE);
                            destination = mapping.findForward("view");
                        }
                    } else {
                        aForm.setAction(ImportProfileColumnsAction.ACTION_SAVE);
                        destination = mapping.findForward("view");
                    }
                    break;

                default:
                    aForm.setAction(ImportProfileColumnsAction.ACTION_SAVE);
                    destination = mapping.findForward("view");
            }

        } catch (Exception e) {
            logger.error("execute: " + e, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception"));
        }
        
        if (destination != null && "view".equals(destination.getName())) {
        	request.setAttribute("isCustomerIdImportNotAllowed", !AgnUtils.getAdmin(request).permissionAllowed(Permission.IMPORT_CUSTOMERID));
        	request.setAttribute("isReadonly", aForm.getProfile().isAutoMapping());
        }

        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }

        if (!messages.isEmpty()) {
            saveMessages(request, messages);
        }

        return destination;
    }


    @Required
    public void setImportProfileService(ImportProfileService importProfileService) {
        this.importProfileService = importProfileService;
    }

    @Required
    public void setRecipientDao(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    @Required
    public void setProfileFieldDao(ProfileFieldDao profileFieldDao) {
        this.profileFieldDao = profileFieldDao;
    }

    @Required
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}

    /**
     * In default implementation (OpenEMM) just returns false. It can be overridden by sub classes of
     * ImportProfileColumnsAction for additional validation.
     *
     * @param aForm  form
     * @param errors <code>ActionMessages</code> object to put errors to
     * @return true if errors are found
     * @throws Exception
     */
    protected boolean checkErrorsOnSave(ComAdmin admin, ImportProfileColumnsForm aForm, ActionMessages errors) throws Exception {
        boolean returnValue = false;

        // Check for valid default values
        for (ColumnMapping mapping : aForm.getProfile().getColumnMapping()) {
            String dbColumnName = mapping.getDatabaseColumn();
            String defaultValue = mapping.getDefaultValue();
            boolean checkThisValue;

            if (StringUtils.isNotBlank(mapping.getFileColumn())) {
                // Default value for normal db column which is included in csv data
                if (defaultValue != null && defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
                    defaultValue = defaultValue.substring(1, mapping.getDefaultValue().length() - 1);
                }
                checkThisValue = true;
            } else if (mapping.getDefaultValue().startsWith("'") && mapping.getDefaultValue().endsWith("'")) {
                // Additional db value which is NOT included in csv data
                defaultValue = defaultValue.substring(1, mapping.getDefaultValue().length() - 1);
                checkThisValue = true;
            } else {
                // Additional db function
                checkThisValue = false;
            }

            if (checkThisValue && StringUtils.isNotEmpty(mapping.getDefaultValue()) && !ColumnMapping.DO_NOT_IMPORT.equalsIgnoreCase(dbColumnName)) {
                ProfileField profileField = profileFieldDao.getProfileField(aForm.getProfile().getCompanyId(), dbColumnName);
                if (profileField.getSimpleDataType() == DbColumnType.SimpleDataType.Numeric && !AgnUtils.isDouble(defaultValue)) {
                    errors.add("global", new ActionMessage("error.import.column.invalid_default.numeric", dbColumnName));
                    returnValue = true;
                } else if (profileField.getSimpleDataType() == DbColumnType.SimpleDataType.Date) {
                    String dateFormatPattern = DateFormat.getDateFormatById(aForm.getProfile().getDateFormat()).getValue();
                    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatPattern);
                    try {
                        dateFormat.parse(defaultValue);
                    } catch (Exception e) {
                        errors.add("global", new ActionMessage("error.import.column.invalid_default.date", dbColumnName, dateFormatPattern));
                        returnValue = true;
                    }
                }
            }
        }
        
        // Check not-nullable fields
        CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(aForm.getProfile().getCompanyId());
		if (aForm.getProfile().getImportMode() == ImportMode.ADD.getIntValue()
				|| aForm.getProfile().getImportMode() == ImportMode.ADD_AND_UPDATE.getIntValue()
				|| aForm.getProfile().getImportMode() == ImportMode.ADD_AND_UPDATE_FORCED.getIntValue()) {
			for (Entry<String, DbColumnType> columnEntry : customerDbFields.entrySet()) {
				if (!columnEntry.getValue().isNullable()
						&& DbUtilities.getColumnDefaultValue(importRecipientsDao.getDataSource(), "customer_" + aForm.getProfile().getCompanyId() + "_tbl", columnEntry.getKey()) == null
						&& !"customer_id".equalsIgnoreCase(columnEntry.getKey())
						&& !"gender".equalsIgnoreCase(columnEntry.getKey())
						&& !"mailtype".equalsIgnoreCase(columnEntry.getKey())
						&& !ComCompanyDaoImpl.STANDARD_FIELD_BOUNCELOAD.equalsIgnoreCase(columnEntry.getKey())) {
					boolean notNullColumnIsSet = false;
					for (ColumnMapping mapping : aForm.getProfile().getColumnMapping()) {
						if (columnEntry.getKey().equalsIgnoreCase(mapping.getDatabaseColumn()) && (mapping.getFileColumn() != null || mapping.getDefaultValue() != null)) {
							notNullColumnIsSet = true;
							break;
						}
					}
					if (!notNullColumnIsSet) {
						errors.add("global", new ActionMessage("error.import.missingNotNullableColumnInMapping", columnEntry.getKey()));
	                    returnValue = true;
					}
				}
			}
			
			for (ColumnMapping mapping : aForm.getProfile().getColumnMapping()) {
				if ("gender".equalsIgnoreCase(mapping.getDatabaseColumn())) {
					if (StringUtils.isBlank(mapping.getFileColumn()) && (StringUtils.isBlank(mapping.getDefaultValue()) || mapping.getDefaultValue().trim().equals("''") || mapping.getDefaultValue().trim().equalsIgnoreCase("null"))) {
						errors.add("global", new ActionMessage("error.import.missingNotNullableColumnInMapping", "gender"));
	                    returnValue = true;
					}
				} else if ("mailtype".equalsIgnoreCase(mapping.getDatabaseColumn())) {
					if (StringUtils.isBlank(mapping.getFileColumn()) && (StringUtils.isBlank(mapping.getDefaultValue()) || mapping.getDefaultValue().trim().equals("''") || mapping.getDefaultValue().trim().equalsIgnoreCase("null"))) {
						errors.add("global", new ActionMessage("error.import.missingNotNullableColumnInMapping", "mailtype"));
	                    returnValue = true;
					}
				}
			}
		}

        // Some fields that may not be imported by users via csv data, but must only be set by the system
        for (String dbColumnName : ImportUtils.getHiddenColumns(admin)) {
            for (ColumnMapping mapping : aForm.getProfile().getColumnMapping()) {
            	if (mapping.getDatabaseColumn().equalsIgnoreCase(dbColumnName)) {
                	if ("customer_id".equalsIgnoreCase(dbColumnName) && aForm.getProfile().getId() > 0) {
                		// if customer_id was configured in the mapping by some other user, it is allowed to keep it now
                		ImportProfile previousProfile = importProfileService.getImportProfileById(aForm.getProfile().getId());
                		ColumnMapping previousCustomerIdMapping = null;
                		for (ColumnMapping previousMapping : previousProfile.getColumnMapping()) {
                			if ("customer_id".equalsIgnoreCase(previousMapping.getDatabaseColumn())) {
                				previousCustomerIdMapping = previousMapping;
                				break;
                			}
                		}
                		if (previousCustomerIdMapping == null || !previousCustomerIdMapping.getFileColumn().equals(mapping.getFileColumn())) {
	                		errors.add("global", new ActionMessage("error.import.column.invalid", dbColumnName));
	                        returnValue = true;
	                	}
                	} else {
                		errors.add("global", new ActionMessage("error.import.column.invalid", dbColumnName));
                        returnValue = true;
                	}
                }
            }
        }

        if (aForm.getProfile().getImportMode() == ImportMode.ADD.getIntValue()
                || aForm.getProfile().getImportMode() == ImportMode.ADD_AND_UPDATE.getIntValue()
                || aForm.getProfile().getImportMode() == ImportMode.UPDATE.getIntValue()) {
            // Check if keycolumns are part of the imported data
            for (String keyColumnName : aForm.getProfile().getKeyColumns()) {
                boolean foundKeyColumn = false;
                for (ColumnMapping mapping : aForm.getProfile().getColumnMapping()) {
                    if (mapping.getDatabaseColumn().equalsIgnoreCase(keyColumnName)) {
                        foundKeyColumn = true;
                        break;
                    }
                }
                if (!foundKeyColumn) {
                    errors.add("global", new ActionMessage("error.import.keycolumn_missing", keyColumnName));
                    returnValue = true;
                }
            }
        }

        return returnValue;
    }

    /**
     * Saves column mappings done by user on edit-page
     *
     * @param profile, profile to save
     * @throws Exception
     */
    protected void saveMappings(ImportProfile profile) throws Exception {
        importProfileService.saveImportProfile(profile);
    }

    private void logChangedMappings(ImportProfile newImport, ImportProfile oldProfile, ComAdmin admin) {
        List<ColumnMapping> mappings = newImport.getColumnMapping();
        Map<Integer, ColumnMapping> oldMappings = Collections.emptyMap();
        if (oldProfile != null) {
            oldMappings = oldProfile.getColumnMapping().stream()
                    .collect(Collectors.toMap(ColumnMapping::getId, item -> item));
        }
        for (ColumnMapping mapping : mappings) {
            StringBuilder log = new StringBuilder();
            int mappingId = mapping.getId();
            if (oldMappings.containsKey(mappingId)) {
                ColumnMapping oldMapping = oldMappings.get(mappingId);
                appendExistingColumnLog(mapping, oldMapping, log);
                oldMappings.remove(mappingId);
            } else {
                appendNewColumnLog(mapping, log);
            }
            if (log.length() != 0) {
                String fileColumn = StringUtils.defaultIfEmpty(mapping.getFileColumn(), NO_VALUE);
                log.insert(0, String.format(LOG_HEAD,
                        newImport.getName(), newImport.getId(), fileColumn, mapping.getId()));
                log.append(".");
                writeUserActivityLog(admin, "edit import profile", log.toString());
            }
        }
        for (Map.Entry<Integer, ColumnMapping> entry : oldMappings.entrySet()) {
            ColumnMapping mapping = entry.getValue();
            String fileColumn = StringUtils.defaultIfEmpty(mapping.getFileColumn(), NO_VALUE);
            writeUserActivityLog(admin, "edit import profile",
                    String.format(LOG_HEAD + " removed.", newImport.getName(), newImport.getId(), fileColumn, mapping.getId()));
        }
    }

    private void appendExistingColumnLog(ColumnMapping newMapping, ColumnMapping existingMapping, StringBuilder log) {
        String oldMappingValue = StringUtils.defaultIfEmpty(existingMapping.getDatabaseColumn(), NO_VALUE),
                newMappingValue = StringUtils.defaultIfEmpty(newMapping.getDatabaseColumn(), NO_VALUE),
                oldDefaultValue = StringUtils.defaultIfEmpty(existingMapping.getDefaultValue(), NO_VALUE),
                newDefaultValue = StringUtils.defaultIfEmpty(newMapping.getDefaultValue(), NO_VALUE);
        Boolean oldMandatorySlider = existingMapping.isMandatory();
        if (!StringUtils.equals(oldMappingValue, newMappingValue)) {
            log.append(", ").append(String.format("changed database mapping from \"%s\" to \"%s\"",
                    oldMappingValue, newMappingValue));
        }
        if (oldMandatorySlider != newMapping.isMandatory()) {
            log.append(", ").append(String.format("mandatory set to %b", newMapping.isMandatory()));
        }
        if (!StringUtils.equals(oldDefaultValue, newDefaultValue)) {
            log.append(", ").append(String.format("default value changed from %s to %s", oldDefaultValue, newDefaultValue));
        }
    }

    private void appendNewColumnLog(ColumnMapping newMapping, StringBuilder log) {
        String dbColumn = StringUtils.defaultIfEmpty(newMapping.getDatabaseColumn(), NO_VALUE),
                defaultValue = StringUtils.defaultIfEmpty(newMapping.getDefaultValue(), NO_VALUE);
        log.append(", ").append(String.format("database mapping set to %s", dbColumn))
                .append(String.format(", mandatory set to %b", newMapping.isMandatory()))
                .append(String.format(", default value set to %s", defaultValue));
    }

    /**
     * Handles column adding that user performed on edit-page
     *
     * @param aForm a form
     */
    protected void addColumn(ImportProfileColumnsForm aForm) {
        String columnValue = aForm.getNewColumn();
        if ("value".equalsIgnoreCase(aForm.getValueType())) {
            columnValue = "'" + columnValue + "'";
        }
        ColumnMappingImpl mapping = new ColumnMappingImpl();
        mapping.setFileColumn("");
        mapping.setDatabaseColumn("");
        mapping.setProfileId(aForm.getProfileId());
        mapping.setMandatory(false);
        mapping.setDefaultValue(columnValue);
        aForm.getProfile().getColumnMapping().add(mapping);
        aForm.setNewColumn("");
    }

    /**
     * Loads columns of recipient table
     *
     * @param aForm   a form
     * @param request request
     * @throws Exception
     */
    protected void loadDbColumns(ImportProfileColumnsForm aForm, HttpServletRequest request) throws Exception {
        CaseInsensitiveMap<String, CsvColInfo> dbColumns = recipientDao.readDBColumns(AgnUtils.getCompanyID(request));
        ImportUtils.removeHiddenColumns(dbColumns, AgnUtils.getAdmin(request));
        List<String> lKeySet = new ArrayList<>(dbColumns.keySet());
        Collections.sort(lKeySet);
        aForm.setDbColumns(lKeySet.toArray(new String[0]));
        // load DB columns default values
        List<ProfileField> profileFields = profileFieldDao.getProfileFields(AgnUtils.getCompanyID(request));
        for (ProfileField profileField : profileFields) {
            aForm.getDbColumnsDefaults().put(profileField.getColumn(), profileField.getDefaultValue());
        }
    }

    /**
     * Adds column mappings from uploaded csv-file using import profile settings
     * for parsing csv file
     *
     * @param aForm   a form
     * @param request request
     * @throws Exception
     */
    protected void setColumnsFromFile(ImportProfileColumnsForm aForm, HttpServletRequest request, ActionMessages errors) throws Exception {
        try {
			if (aForm.getProfile() == null) {
			    loadImportProfile(aForm, request);
			}
			ImportProfile profile = aForm.getProfile();
			File file = getCurrentFile(request);
			if (file == null) {
			    return;
			}
			
			if (!ImportUtils.checkIfImportFileHasData(file, profile.isZipped(), profile.getZipPassword())) {
				errors.add("global", new ActionMessage("autoimport.error.emptyFile", getCurrentFileName(request)));
				return;
			}

			Map<String, CsvColInfo> dbColumns = recipientDao.readDBColumns(AgnUtils.getCompanyID(request));

			char separator = Separator.getSeparatorById(profile.getSeparator()).getValueChar();
			Character stringQuote = TextRecognitionChar.getTextRecognitionCharById(profile.getTextRecognitionChar()).getValueCharacter();

			try (CsvReader csvReader = new CsvReader(getImportInputStream(profile, file), Charset.getCharsetById(profile.getCharset()).getCharsetName(), separator, stringQuote)) {
			    csvReader.setAlwaysTrim(true);

			    List<String> fileHeaders = csvReader.readNextCsvLine();

			    if (!fileHeaders.isEmpty()) {
			        ComAdmin admin = AgnUtils.getAdmin(request);
			        writeUserActivityLog(admin, UAL_ACTION, "Found columns based on csv - file import : " + fileHeaders.toString());
			    }

			    if (profile.isNoHeaders()) {
			        List<String> fileColumnIdentifiers = new ArrayList<>();
			        for (int i = 1; i <= fileHeaders.size(); i++) {
			            fileColumnIdentifiers.add("column_" + i);
			        }
			        fileHeaders = fileColumnIdentifiers;
			    }

			    // Check for duplicate csv file columns
			    if (fileHeaders.contains("")) {
			        errors.add("global", new ActionMessage("error.import.column.name.empty"));
			    }
			    String duplicateCsvColumn = CsvReader.checkForDuplicateCsvHeader(fileHeaders, profile.isAutoMapping());
			    if (duplicateCsvColumn != null) {
			        errors.add("global", new ActionMessage("error.import.column.csv.duplicate"));
			    }

			    List<ColumnMapping> newMappings = new ArrayList<>();
			    // Set the file columns for a new profile mapping
			    for (String newCsvColumn : fileHeaders) {
			    	ColumnMapping existingColumnMapping = null;
			    	for (ColumnMapping columnMmapping : profile.getColumnMapping()) {
			    		if (columnMmapping.getFileColumn() != null && columnMmapping.getFileColumn().equals(newCsvColumn)) {
			    			existingColumnMapping = columnMmapping;
			    			break;
			    		}
			    	}
			    	if (existingColumnMapping == null) {
						ColumnMapping newMapping = new ColumnMappingImpl();
						newMapping.setProfileId(profile.getId());
						newMapping.setMandatory(false);
						newMapping.setFileColumn(newCsvColumn);
						// Look for a matching db column (without '-', '_', caseinsensitive)
						String maybeColumnName = newCsvColumn.replace("-", "").replace("_", "").toLowerCase();
						for (String dbColumn : dbColumns.keySet()) {
							if (maybeColumnName.equalsIgnoreCase(dbColumn.replace("-", "").replace("_", ""))) {
								newMapping.setDatabaseColumn(dbColumn);
								break;
							}
						}
						if (StringUtils.isEmpty(newMapping.getDatabaseColumn())) {
							newMapping.setDatabaseColumn(ColumnMapping.DO_NOT_IMPORT);
						}
						newMappings.add(newMapping);
			    	} else {
						newMappings.add(existingColumnMapping);
			    	}
			    }
			
				// Keep the already set column mappings for non-data-file columns
				for (ColumnMapping columnMmapping : profile.getColumnMapping()) {
					if (StringUtils.isEmpty(columnMmapping.getFileColumn())) {
						newMappings.add(columnMmapping);
					}
				}
			    profile.setColumnMapping(newMappings);
			}
		} catch (Exception e) {
			logger.error("Error while mapping import columns: " + e.getMessage(), e);
			errors.add("global", new ActionMessage("error.import.exception", e.getMessage()));
		}
    }
    
    private InputStream getImportInputStream(ImportProfile profile, File file) throws FileNotFoundException {
    	if (profile.isZipped()) {
	    	try {
	            if (profile.getZipPassword() == null) {
	            	InputStream dataInputStream = ZipUtilities.openZipInputStream(new FileInputStream(file));
	                ZipEntry zipEntry = ((ZipInputStream) dataInputStream).getNextEntry();
	                if (zipEntry == null) {
	                	throw new ImportException(false, "error.unzip.noEntry");
	                }
	                return dataInputStream;
	            } else {
	                File unzipPath = new File(file.getAbsolutePath() + ".unzipped");
	                unzipPath.mkdir();
	                ZipUtilities.decompressFromEncryptedZipFile(file, unzipPath, profile.getZipPassword());

	                // Check if there was only one file within the zip file and use it for import
	                String[] filesToImport = unzipPath.list();
	                if (filesToImport.length != 1) {
	                    throw new Exception("Invalid number of files included in zip file");
	                }
	                InputStream dataInputStream = new FileInputStream(unzipPath.getAbsolutePath() + "/" + filesToImport[0]);
	                return new TempFileInputStream(dataInputStream, unzipPath);
	            }
			} catch (ImportException e) {
				throw e;
			} catch (Exception e) {
				throw new ImportException(false, "error.unzip", e.getMessage());
			}
	    } else {
	        return new FileInputStream(file);
	    }
    }

    /**
     * Loads import profile from DB using Dao
     *
     * @param aForm   a form
     * @param request request
     */
    protected void loadImportProfile(ImportProfileColumnsForm aForm, HttpServletRequest request) {
        ImportProfile profile = importProfileService.getImportProfileById(aForm.getProfileId());
        aForm.setProfile(profile);
    }
}
