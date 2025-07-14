/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.web;

import static com.agnitas.emm.core.import_profile.component.ImportProfileColumnMappingChangesDetector.NO_VALUE;
import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.ImportUtils.RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.impl.ColumnMappingImpl;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.ImportException;
import com.agnitas.service.ImportProfileService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.CaseInsensitiveSet;
import com.agnitas.util.CsvColInfo;
import com.agnitas.util.CsvDataInvalidItemCountException;
import com.agnitas.util.ImportUtils;
import com.agnitas.util.importvalues.Charset;
import com.agnitas.util.importvalues.Separator;
import com.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.common.exceptions.InvalidCharsetException;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.import_profile.bean.ImportDataType;
import com.agnitas.emm.core.import_profile.bean.ImportProfileColumnMapping;
import com.agnitas.emm.core.import_profile.component.ImportProfileColumnMappingChangesDetector;
import com.agnitas.emm.core.import_profile.component.ImportProfileColumnMappingsValidator;
import com.agnitas.emm.core.import_profile.form.ImportProfileColumnsForm;
import com.agnitas.emm.core.recipient.imports.wizard.dto.LocalFileDto;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.data.CsvDataProvider;
import com.agnitas.emm.data.DataProvider;
import com.agnitas.emm.data.ExcelDataProvider;
import com.agnitas.emm.data.JsonDataProvider;
import com.agnitas.emm.data.OdsDataProvider;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/import-profile")
@PermissionMapping("import.profile.columns")
@Deprecated(forRemoval = true) // TODO: remove after EMMGUI-714 will be finished and old design will be removed, also remove related JS/jsp files, tiles
public class ImportProfileColumnsController {

    private static final Logger LOGGER = LogManager.getLogger(ImportProfileColumnsController.class);
    private static final String LOG_HEAD = "%s (%d), csv column %s (%d)";

    private final ImportProfileService importProfileService;
    private final RecipientFieldService recipientFieldService;
    private final ExtendedConversionService conversionService;
    private final ImportProfileColumnMappingsValidator mappingsValidator;
    private final UserActivityLogService userActivityLogService;
    private final ImportProfileColumnMappingChangesDetector changesDetector;
    private final RecipientDao recipientDao;

    public ImportProfileColumnsController(ImportProfileService importProfileService, RecipientFieldService recipientFieldService, ExtendedConversionService conversionService,
                                          ImportProfileColumnMappingsValidator mappingsValidator, UserActivityLogService userActivityLogService,
                                          ImportProfileColumnMappingChangesDetector changesDetector, RecipientDao recipientDao) {

        this.importProfileService = importProfileService;
        this.recipientFieldService = recipientFieldService;
        this.conversionService = conversionService;
        this.mappingsValidator = mappingsValidator;
        this.userActivityLogService = userActivityLogService;
        this.changesDetector = changesDetector;
        this.recipientDao = recipientDao;
    }

    @GetMapping("/{id:\\d+}/columns/view.action")
    public String view(@PathVariable(name = "id") int id, @ModelAttribute("form") ImportProfileColumnsForm form, Admin admin, Model model, HttpSession session) {
        session.removeAttribute(RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME);
        ImportProfile profile = importProfileService.getImportProfileById(id);

        form.setProfileId(profile.getId());
        form.setProfileName(profile.getName());

        model.addAttribute("profileFields", getProfileFieldsMap(profile, admin));
        model.addAttribute("columnMappings", profile.getColumnMapping());
        model.addAttribute("isReadonly", profile.isAutoMapping());
        model.addAttribute("isEncryptedImportAllowed", importProfileService.isEncryptedImportAllowed(admin));

        return "import_wizard_profile_columns_view";
    }
    
    private Map<String, RecipientFieldDescription> getProfileFieldsMap(ImportProfile profile, Admin admin) {
    	List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(admin.getCompanyID());
    	Map<String, RecipientFieldDescription> recipientFieldsMap = new HashMap<>();
    	for (RecipientFieldDescription recipientField : recipientFields) {
    		if (!RecipientStandardField.getImportChangeNotAllowedColumns(admin.permissionAllowed(Permission.IMPORT_CUSTOMERID)).contains(recipientField.getColumnName())) {
    			ProfileFieldMode adminPermission = recipientField.getAdminPermission(admin.getAdminID());
    	        // User may also map readonly columns, but in import action, those are checked to be only used as keycolumns
    			if (adminPermission == ProfileFieldMode.Editable
    					|| (adminPermission == ProfileFieldMode.ReadOnly && profile.getKeyColumns().contains(recipientField.getColumnName()))) {
    				recipientFieldsMap.put(recipientField.getColumnName(), recipientField);
    			}
            }
    	}
        return recipientFieldsMap;
    }

    @PostMapping("/columns/save.action")
    public String save(@ModelAttribute ImportProfileColumnsForm form, @RequestParam(required = false) boolean start, Admin admin, Popups popups) {
        List<ColumnMapping> mappings = conversionService.convert(form.getColumnsMappings(), ImportProfileColumnMapping.class, ColumnMapping.class);
        ImportProfile existingProfile = importProfileService.getImportProfileById(form.getProfileId());

        if (!importProfileService.isManageAllowed(existingProfile, admin)) {
            throw new UnsupportedOperationException();
        }

        if (!mappingsValidator.validate(mappings, existingProfile, admin, popups)) {
            return MESSAGES_VIEW;
        }

        importProfileService.saveColumnsMappings(mappings, form.getProfileId(), admin);

        logMappingsChangesToUAL(mappings, existingProfile, admin);
        popups.success(CHANGES_SAVED_MSG);

        if (start) {
            return String.format("redirect:/recipient/import/view.action?profileId=%d", form.getProfileId());
        }

        return String.format("redirect:/import-profile/%d/columns/view.action", form.getProfileId());
    }

    @PostMapping(value = "/columns/upload.action")
    public @ResponseBody
    DataResponseDto<List<ColumnMapping>> upload(@ModelAttribute ImportProfileColumnsForm form, Admin admin, Popups popups, HttpSession session) throws Exception {
        MultipartFile uploadedFile = form.getUploadFile();
        if (!isUploadedFileValid(uploadedFile, popups)) {
            return new DataResponseDto<>(Collections.emptyList(), popups, false);
        }

        File importFile = ImportUtils.createTempImportFile(uploadedFile, admin);
        List<ColumnMapping> newMappings = readMappingsFromFile(importFile, form.getProfileId(), admin, popups);

        if (!popups.hasAlertPopups()) {
            session.setAttribute(RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME,
                    new LocalFileDto(importFile.getAbsolutePath(), uploadedFile.getOriginalFilename()));
        }

        return new DataResponseDto<>(newMappings, popups, !popups.hasAlertPopups());
    }

    private boolean isUploadedFileValid(MultipartFile uploadedFile, Popups popups) {
        if (uploadedFile == null || uploadedFile.getSize() == 0) {
            popups.alert("error.import.no_file");
            return false;
        }

        if (uploadedFile.getOriginalFilename().contains(File.separator)) {
            popups.alert("error.import.datafilename", File.separator);
            return false;
        }

        return true;
    }

    private List<ColumnMapping> readMappingsFromFile(File file, int profileId, Admin admin, Popups popups) {
        try {
            ImportProfile profile = importProfileService.getImportProfileById(profileId);

            if (!ImportUtils.checkIfImportFileHasData(file, profile.getZipPassword())) {
                popups.alert("autoimport.error.emptyFile", file.getName());
                return Collections.emptyList();
            }

            ServiceResult<List<ColumnMapping>> readingResult = performMappingsReading(file, profile, admin);
            popups.addPopups(readingResult);
            return readingResult.getResult();
        } catch (InvalidCharsetException e) {
            logReadMappingException(e);
            popups.alert("error.import.charset");
        } catch (CsvDataInvalidItemCountException e) {
            logReadMappingException(e);
            popups.alert("error.import.data.itemcount", e.getExpected(), e.getActual(), e.getErrorLineNumber());
        } catch (ImportException e) {
            logReadMappingException(e);
            popups.alert(e.getErrorMessageKey(), e.getAdditionalErrorData());
        } catch (Exception e) {
            logReadMappingException(e);
            popups.alert(Message.of("error.import.exception", e.getMessage()));
        }
        return Collections.emptyList();
    }

    private static void logReadMappingException(Exception e) {
        LOGGER.error("Error while mapping import columns: {}", e.getMessage(), e);
    }

    private ServiceResult<List<ColumnMapping>> performMappingsReading(File importFile, ImportProfile importProfile, Admin admin) throws Exception {
		DataProvider dataProvider = getDataProvider(importProfile, importFile);
		List<String> dataPropertyNames = dataProvider.getAvailableDataPropertyNames();
        List<Message> errorMessages = new ArrayList<>();

        if (!dataPropertyNames.isEmpty()) {
            writeUserActivityLog(admin, "edit import profile", "Found columns based on csv - file import : " + dataPropertyNames.toString());

            if (importProfile.isNoHeaders()) {
            	dataPropertyNames = IntStream.range(1, dataPropertyNames.size() + 1)
                        .mapToObj(index -> "column_" + index)
                        .collect(Collectors.toList());
            }
        }

        if (dataPropertyNames.contains("")) {
            errorMessages.add(Message.of("error.import.column.name.empty"));
        }

        Set<String> processedColumns = new CaseInsensitiveSet();
		Set<String> duplicateColumns = new CaseInsensitiveSet();
        for (String dataPropertyName : dataPropertyNames) {
            if (StringUtils.isBlank(dataPropertyName)) {
            	errorMessages.add(Message.of("error.import.column.name.empty"));
            	break;
            } else if (processedColumns.contains(dataPropertyName)) {
            	duplicateColumns.add(dataPropertyName);
            } else {
            	processedColumns.add(dataPropertyName);
            }
        }
        
        if (duplicateColumns.size() > 0) {
        	errorMessages.add(Message.of("error.import.column.name.duplicate", StringUtils.join(duplicateColumns, ", ")));
        }

        Map<String, CsvColInfo> dbColumns = recipientDao.readDBColumns(admin.getCompanyID(), admin.getAdminID(), importProfile.getKeyColumns());

        List<ColumnMapping> foundMappings = dataPropertyNames.stream()
                .map(fh -> createNewColumnMapping(fh, importProfile.getId(), dbColumns))
                .collect(Collectors.toList());

        return new ServiceResult<>(
                foundMappings,
                errorMessages.isEmpty(),
                Collections.emptyList(),
                Collections.emptyList(),
                errorMessages
        );
    }
    
    protected ColumnMapping createNewColumnMapping(String fileColumn, int profileId, Map<String, CsvColInfo> dbColumns) {
        ColumnMapping mapping = new ColumnMappingImpl();

        mapping.setProfileId(profileId);
        mapping.setFileColumn(fileColumn);

        mapping.setDatabaseColumn(findDependentDbColumn(fileColumn, dbColumns));

        if (StringUtils.isEmpty(mapping.getDatabaseColumn())) {
            mapping.setDatabaseColumn(ColumnMapping.DO_NOT_IMPORT);
        }

        return mapping;
    }

    private String findDependentDbColumn(String fileColumn, Map<String, CsvColInfo> dbColumns) {
        String columnValue = removeNameSeparators(fileColumn);
        return dbColumns.keySet().stream()
                .map(this::removeNameSeparators)
                .filter(columnValue::equalsIgnoreCase)
                .findAny()
                .orElse(null);
    }

    private String removeNameSeparators(String columnName) {
    	if (columnName == null) {
    		return null;
    	} else {
    		return columnName.replace("-", "").replace("_", "");
    	}
    }

	private DataProvider getDataProvider(ImportProfile importProfile, File importFile) throws Exception {
		switch (ImportDataType.getImportDataTypeForName(importProfile.getDatatype())) {
			case CSV:
			Character valueCharacter = TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).getValueCharacter();
			return new CsvDataProvider(
					importFile,
					importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
					Charset.getCharsetById(importProfile.getCharset()).getCharsetName(),
					Separator.getSeparatorById(importProfile.getSeparator()).getValueChar(),
					valueCharacter,
					valueCharacter == null ? '"' : valueCharacter,
					false,
					true,
					importProfile.isNoHeaders(),
					null);
			case Excel:
				return new ExcelDataProvider(
					importFile,
					importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
					true,
					importProfile.isNoHeaders(),
					null,
					true,
					null);
			case JSON:
				return new JsonDataProvider(
					importFile,
					importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
					null,
					null);
			case ODS:
				return new OdsDataProvider(
					importFile,
					importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
					true,
					importProfile.isNoHeaders(),
					null,
					true,
					null);
			default:
				throw new RuntimeException("Invalid import datatype: " + importProfile.getDatatype());
		}
	}

    private void logMappingsChangesToUAL(List<ColumnMapping> newMappings, ImportProfile existingProfile, Admin admin) {
        Map<Integer, ColumnMapping> oldMappings = existingProfile.getColumnMapping().stream()
                .collect(Collectors.toMap(ColumnMapping::getId, item -> item));

        for (ColumnMapping newMapping : newMappings) {
            int mappingId = newMapping.getId();

            ColumnMapping oldMapping = oldMappings.get(mappingId);

            if (oldMapping != null) {
                oldMappings.remove(mappingId);
            }

            StringBuilder builder = changesDetector.detectChanges(newMapping, oldMapping);

            if (builder.length() != 0) {
                String fileColumn = StringUtils.defaultIfEmpty(newMapping.getFileColumn(), NO_VALUE);
                builder.insert(0, String.format(LOG_HEAD,
                        existingProfile.getName(), existingProfile.getId(), fileColumn, newMapping.getId()));
                builder.append(".");
                writeUserActivityLog(admin, "edit import profile", builder.toString());
            }
        }

        for (ColumnMapping oldMapping : oldMappings.values()) {
            String fileColumn = StringUtils.defaultIfEmpty(oldMapping.getFileColumn(), NO_VALUE);
            writeUserActivityLog(admin, "edit import profile",
                    String.format(LOG_HEAD + " removed.", existingProfile.getName(), existingProfile.getId(), fileColumn, oldMapping.getId()));
        }
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, new UserAction(action, description), LOGGER);
    }
}
