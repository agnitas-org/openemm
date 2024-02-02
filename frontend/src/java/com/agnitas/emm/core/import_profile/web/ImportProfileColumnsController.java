/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.import_profile.bean.ImportDataType;
import com.agnitas.emm.core.import_profile.bean.ImportProfileColumnMapping;
import com.agnitas.emm.core.import_profile.component.ImportProfileColumnMappingChangesDetector;
import com.agnitas.emm.core.import_profile.component.ImportProfileColumnMappingsValidator;
import com.agnitas.emm.core.import_profile.component.RecipientImportFileInputStreamProvider;
import com.agnitas.emm.core.import_profile.component.reader.ColumnMappingsReader;
import com.agnitas.emm.core.import_profile.component.reader.ImportColumnMappingsReaderFactory;
import com.agnitas.emm.core.import_profile.form.ImportProfileColumnsForm;
import com.agnitas.emm.core.recipient.imports.wizard.dto.LocalFileDto;
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
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.impl.ColumnMappingImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.ImportProfileService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.CsvDataInvalidItemCountException;
import org.agnitas.util.CsvReader;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.agnitas.emm.core.import_profile.component.ImportProfileColumnMappingChangesDetector.NO_VALUE;
import static java.text.MessageFormat.format;
import static org.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static org.agnitas.util.ImportUtils.RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME;

@Controller
@RequestMapping("/import-profile")
@PermissionMapping("import.profile.columns")
public class ImportProfileColumnsController {

    private static final Logger LOGGER = LogManager.getLogger(ImportProfileColumnsController.class);
    private static final String LOG_HEAD = "%s (%d), csv column %s (%d)";

    private final ImportProfileService importProfileService;
    private final ProfileFieldDao profileFieldDao;
    private final ExtendedConversionService conversionService;
    private final ImportProfileColumnMappingsValidator mappingsValidator;
    private final UserActivityLogService userActivityLogService;
    private final ImportProfileColumnMappingChangesDetector changesDetector;
    private final RecipientImportFileInputStreamProvider inputStreamProvider;
    private final ImportColumnMappingsReaderFactory mappingsReaderFactory;
    private final ConfigService configService;
    private final ComRecipientDao recipientDao;

    public ImportProfileColumnsController(ImportProfileService importProfileService, ProfileFieldDao profileFieldDao, ExtendedConversionService conversionService,
                                          ImportProfileColumnMappingsValidator mappingsValidator, UserActivityLogService userActivityLogService,
                                          ImportProfileColumnMappingChangesDetector changesDetector, RecipientImportFileInputStreamProvider inputStreamProvider,
                                          ImportColumnMappingsReaderFactory mappingsReaderFactory, ConfigService configService, ComRecipientDao recipientDao) {

        this.importProfileService = importProfileService;
        this.profileFieldDao = profileFieldDao;
        this.conversionService = conversionService;
        this.mappingsValidator = mappingsValidator;
        this.userActivityLogService = userActivityLogService;
        this.changesDetector = changesDetector;
        this.inputStreamProvider = inputStreamProvider;
        this.mappingsReaderFactory = mappingsReaderFactory;
        this.configService = configService;
        this.recipientDao = recipientDao;
    }

    @GetMapping("/{id:\\d+}/columns/view.action")
    public String view(@PathVariable(name = "id") int id, @ModelAttribute("form") ImportProfileColumnsForm form, Admin admin, Model model, HttpSession session) throws Exception {
        session.removeAttribute(RECIPIENT_IMPORT_FILE_ATTRIBUTE_NAME);
        ImportProfile profile = importProfileService.getImportProfileById(id);

        form.setProfileId(profile.getId());
        form.setProfileName(profile.getName());

        model.addAttribute("profileFields", getProfileFieldsMap(profile, admin));
        model.addAttribute("columnMappings", profile.getColumnMapping());
        model.addAttribute("isReadonly", profile.isAutoMapping());
        model.addAttribute("isEncryptedImportAllowed", importProfileService.isEcryptedImportAllowed(admin));

        return "import_wizard_profile_columns_view";
    }

    private Map<String, ProfileField> getProfileFieldsMap(ImportProfile profile, Admin admin) throws Exception {
        TreeMap<String, ProfileField> profileFieldsMap = new TreeMap<>(profileFieldDao.getProfileFieldsMap(admin.getCompanyID(), admin.getAdminID()));
        return filterHiddenColumns(profileFieldsMap, profile.getKeyColumns(), admin);
    }

    private Map<String, ProfileField> filterHiddenColumns(Map<String, ProfileField> profileFields, List<String> keyColumns, Admin admin) {
        for (String hiddenColumn : ImportUtils.getHiddenColumns(admin)) {
            profileFields.remove(hiddenColumn);
        }

        // User may also map readonly columns, but in import action, those are are checked to be only used as keycolumns
        return profileFields.entrySet().stream()
                .filter(e -> e.getValue().getModeEdit() == ProfileFieldMode.Editable
                        || (e.getValue().getModeEdit() == ProfileFieldMode.ReadOnly && keyColumns.contains(e.getValue().getColumn())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @PostMapping("/columns/save.action")
    public String save(@ModelAttribute ImportProfileColumnsForm form, @RequestParam(required = false) boolean start, Admin admin, Popups popups) throws Exception {
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
        } catch (CsvDataInvalidItemCountException e) {
            LOGGER.error(format("Error while mapping import columns: {0}", e.getMessage()), e);
            popups.alert("error.import.data.itemcount", e.getExpected(), e.getActual(), e.getErrorLineNumber());
        } catch (Exception e) {
            LOGGER.error(format("Error while mapping import columns: {0}", e.getMessage()), e);
            popups.alert("error.import.exception", e.getMessage());
        }

        return Collections.emptyList();
    }

    private ServiceResult<List<ColumnMapping>> performMappingsReading(File importFile, ImportProfile importProfile, Admin admin) throws Exception {
    	if (configService.getBooleanValue(ConfigValue.UseImprovedProfileImport, importProfile.getCompanyId())) {
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

            if (CsvReader.checkForDuplicateCsvHeader(dataPropertyNames, importProfile.isAutoMapping()) != null) {
                errorMessages.add(Message.of("error.import.column.csv.duplicate"));
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
    	} else {
	        ColumnMappingsReader reader = mappingsReaderFactory.detectReader(importProfile);
	        InputStream inputStream = inputStreamProvider.provide(importFile, importProfile);
	
	        return reader.read(inputStream, importProfile, admin);
    	}
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
        return columnName.replace("-", "").replace("_", "");
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
