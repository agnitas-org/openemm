/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.imports.service.impl;

import static java.util.Objects.requireNonNullElse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ColumnMapping;
import com.agnitas.beans.DatasourceDescription;
import com.agnitas.beans.ImportProfile;
import com.agnitas.beans.ImportStatus;
import com.agnitas.beans.impl.DatasourceDescriptionImpl;
import com.agnitas.beans.impl.ImportProfileImpl;
import com.agnitas.beans.impl.ImportStatusImpl;
import com.agnitas.dao.ImportRecipientsDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.auto_import.bean.RemoteFile;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;
import com.agnitas.emm.core.import_profile.bean.ImportDataType;
import com.agnitas.emm.core.imports.exception.ImportValidationException;
import com.agnitas.emm.core.imports.service.RecipientImportService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.data.CsvDataProvider;
import com.agnitas.emm.data.DataProvider;
import com.agnitas.emm.data.ExcelDataProvider;
import com.agnitas.emm.data.JsonDataProvider;
import com.agnitas.emm.data.OdsDataProvider;
import com.agnitas.messages.Message;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.ProfileImportWorker;
import com.agnitas.service.ProfileImportWorkerFactory;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.impl.CSVColumnState;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.CsvColInfo;
import com.agnitas.util.CsvDataBreakInsideCellException;
import com.agnitas.util.CsvDataException;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.importvalues.Charset;
import com.agnitas.util.importvalues.CheckForDuplicates;
import com.agnitas.util.importvalues.DateFormat;
import com.agnitas.util.importvalues.ImportMode;
import com.agnitas.util.importvalues.MailType;
import com.agnitas.util.importvalues.Separator;
import com.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class RecipientImportServiceImpl implements RecipientImportService {

    private static final Logger logger = LogManager.getLogger(RecipientImportServiceImpl.class);

    private static final String IMPORT_FILE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "RecipientImport";
    private static final JsonMapper MAPPER = new JsonMapper();
    private static final String EMAIL_COL = RecipientStandardField.Email.getColumnName();

    private final ImportRecipientsDao importRecipientsDao;
    private final RecipientDao recipientDao;
    private final ProfileImportWorkerFactory profileImportWorkerFactory;
    private final DataSourceService dataSourceService;

    public RecipientImportServiceImpl(
            ImportRecipientsDao importRecipientsDao,
            RecipientDao recipientDao,
            ProfileImportWorkerFactory profileImportWorkerFactory,
            DataSourceService dataSourceService
    ) {
        this.importRecipientsDao = importRecipientsDao;
        this.recipientDao = recipientDao;
        this.profileImportWorkerFactory = profileImportWorkerFactory;
        this.dataSourceService = dataSourceService;
    }

    @Override
    public ServiceResult<List<List<String>>> parseFileContent(File importFile, ImportProfile profile) throws Exception {
        DataProvider dataProvider = getDataProvider(profile, importFile);
        List<String> dataPropertyNames = dataProvider.getAvailableDataPropertyNames();
        List<List<String>> previewParsedContent = new LinkedList<>();

        CSVColumnState[] columns;
        if (!profile.isNoHeaders()) {
            columns = new CSVColumnState[dataPropertyNames.size()];
            if (profile.isAutoMapping()) {
                CaseInsensitiveMap<String, DbColumnType> customerDbFields = importRecipientsDao.getCustomerDbFields(profile.getCompanyId());
                for (int i = 0; i < dataPropertyNames.size(); i++) {
                    String headerName = dataPropertyNames.get(i);
                    if (StringUtils.isBlank(headerName)) {
                        return ServiceResult.error(Message.of("Invalid empty import data file header for import automapping"));
                    } else if (customerDbFields.containsKey(headerName)) {
                        columns[i] = new CSVColumnState();
                        columns[i].setColName(headerName.toLowerCase());
                        columns[i].setImportedColumn(true);
                    } else {
                        columns[i] = new CSVColumnState();
                        columns[i].setColName(headerName);
                        columns[i].setImportedColumn(false);
                    }
                }
            } else {
                for (int i = 0; i < dataPropertyNames.size(); i++) {
                    String propertyName = dataPropertyNames.get(i);
                    final String columnNameByCvsFileName = getDBColumnNameByImportFilePropertyName(propertyName, profile);
                    if (columnNameByCvsFileName != null) {
                        columns[i] = new CSVColumnState();
                        columns[i].setColName(columnNameByCvsFileName);
                        columns[i].setImportedColumn(true);
                    } else {
                        columns[i] = new CSVColumnState();
                        columns[i].setColName(propertyName);
                        columns[i].setImportedColumn(false);
                    }
                }
            }
        } else {
            int propertyNamesExpected = 0;
            for (ColumnMapping columnMapping : profile.getColumnMapping()) {
                if (StringUtils.isNotBlank(columnMapping.getFileColumn())) {
                    if (!columnMapping.getFileColumn().startsWith("column_")) {
                        return ServiceResult.error(Message.of("error.import.mapping.column.invalid", columnMapping.getFileColumn()));
                    } else {
                        int columnId;
                        try {
                            columnId = Integer.parseInt(columnMapping.getFileColumn().substring(7));
                        } catch (Exception e) {
                            return ServiceResult.error(Message.of("error.import.mapping.column.invalid", columnMapping.getFileColumn()));
                        }
                        propertyNamesExpected = Math.max(propertyNamesExpected, columnId);
                    }
                }
            }

            if (dataPropertyNames.size() != propertyNamesExpected) {
                throw new CsvDataException("Number of import file columns does not fit mapped columns", propertyNamesExpected);
            }
            columns = new CSVColumnState[Math.min(dataPropertyNames.size(), profile.getColumnMapping().size())];

            for (int i = 0; i < columns.length; i++) {
                ColumnMapping columnMapping = profile.getColumnMapping().get(i);
                columns[i] = new CSVColumnState();
                columns[i].setColName(columnMapping.getFileColumn());
                columns[i].setImportedColumn(columnMapping.getDatabaseColumn() != null && !columnMapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT));
            }

            // Add dummy column names to preview data
            final LinkedList<String> columnsList = new LinkedList<>();
            for (int idx = 0; (idx < columns.length) && (idx < dataPropertyNames.size()); idx++) {
                if (Boolean.FALSE.equals(columns[idx].getImportedColumn())) {
                    continue;
                }
                columnsList.add(columns[idx].getColName());
            }
            previewParsedContent.add(columnsList);
        }
        initColumnsNullableCheck(columns, profile);

        for (int dataItemIndex = 0; dataItemIndex < 20; dataItemIndex++) {
            Map<String, Object> dataItem = null;
            try {
                dataItem = dataProvider.getNextItemData();
            } catch (CsvDataBreakInsideCellException e) {
                return ServiceResult.error(Message.of("error.import.file.linebreak", e.getCellIndex(), e.getErrorLineNumber()));
            }
            if (dataItem == null) {
                break;
            } else {
                final List<String> dataItemValuesForImport = new ArrayList<>();
                for (int idx = 0; (idx < columns.length) && (idx < dataPropertyNames.size()); idx++) {
                    if (Boolean.TRUE.equals(columns[idx].getImportedColumn())) {
                        String propertyName = null;

                        if (profile.isAutoMapping()) {
                            propertyName = columns[idx].getColName().toUpperCase();
                        } else {
                            for (ColumnMapping columnMapping : profile.getColumnMapping()) {
                                if (columns[idx].getColName().equals(columnMapping.getDatabaseColumn())) {
                                    propertyName = columnMapping.getFileColumn();
                                    break;
                                }
                            }
                        }

                        Object dataValueObject = dataItem.get(propertyName);
                        String value;
                        if (dataValueObject == null) {
                            value = "";
                        } else if (dataValueObject instanceof Date dateValueObject) {
                            SimpleDateFormat format = new SimpleDateFormat(DateFormat.getDateFormatById(profile.getDateFormat()).getValue());
                            value = format.format(dateValueObject);
                        } else {
                            value = dataValueObject.toString();
                        }

                        dataItemValuesForImport.add(value);
                    }
                }
                previewParsedContent.add(dataItemValuesForImport);
            }
        }

        List<String> headersList = Arrays.stream(columns)
                .filter(CSVColumnState::getImportedColumn)
                .map(CSVColumnState::getColName)
                .toList();

        previewParsedContent.add(0, headersList);

        return ServiceResult.success(previewParsedContent);
    }

    @Override
    public DataProvider getDataProvider(ImportProfile importProfile, File importFile) throws Exception {
        return switch (ImportDataType.getImportDataTypeForName(importProfile.getDatatype())) {
            case CSV -> {
                Character valueCharacter = TextRecognitionChar.getTextRecognitionCharById(importProfile.getTextRecognitionChar()).getValueCharacter();
                yield new CsvDataProvider(
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
            }
            case Excel -> new ExcelDataProvider(
                    importFile,
                    importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
                    true,
                    importProfile.isNoHeaders(),
                    null,
                    true,
                    null);
            case JSON -> new JsonDataProvider(
                    importFile,
                    importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
                    null,
                    null);
            case ODS -> new OdsDataProvider(
                    importFile,
                    importProfile.getZipPassword() == null ? null : importProfile.getZipPassword().toCharArray(),
                    true,
                    importProfile.isNoHeaders(),
                    null,
                    true,
                    null);
        };
    }

    private String getDBColumnNameByImportFilePropertyName(String headerName, ImportProfile profile) {
        if (headerName == null) {
            return null;
        }

        for (ColumnMapping mapping : profile.getColumnMapping()) {
            if (headerName.equals(mapping.getFileColumn()) && !mapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT)) {
                return mapping.getDatabaseColumn();
            }
        }

        return null;
    }

    private void initColumnsNullableCheck(CSVColumnState[] cols, ImportProfile profile) {
        Map<String, CsvColInfo> columnsInfo = recipientDao.readDBColumns(profile.getCompanyId(), profile.getAdminId(), profile.getKeyColumns());
        for (CSVColumnState columnState : cols) {
            CsvColInfo columnInfo = columnsInfo.get(columnState.getColName());
            if (columnInfo != null) {
                columnState.setNullable(columnInfo.isNullable());
            }
        }
    }

    @Override
    public ImportStatus addAndUpdate(
            List<Map<String, Object>> recipients,
            List<Integer> mailinglistIds,
            MediaTypes mediatype,
            Admin admin
    ) {
       return importRecipients(recipients, mailinglistIds, mediatype, admin, ImportMode.ADD_AND_UPDATE, EMAIL_COL);
    }

    @Override
    public ImportStatus add(
            List<Map<String, Object>> recipients,
            List<Integer> mailinglistIds,
            MediaTypes mediatype,
            Admin admin
    ) {
       return importRecipients(recipients, mailinglistIds, mediatype, admin, ImportMode.ADD, null);
    }

    private ImportStatus importRecipients(
            List<Map<String, Object>> recipients,
            List<Integer> mailinglistIds,
            MediaTypes mediatype,
            Admin admin,
            ImportMode importMode,
            String keyColumn
    ) {
        String uuid = AgnUtils.generateNewUUID().toString().replace("-", "").toUpperCase();
        File importTempFile = createImportTempFile(admin, uuid);

        MAPPER.writeValue(importTempFile, recipients);

        ProfileImportWorker profileImportWorker = profileImportWorkerFactory.getProfileImportWorker(
                false, // Not interactive mode, because there is no error edit GUI
                mailinglistIds,
                uuid,
                admin.getCompanyID(),
                admin,
                saveImportDatasource(admin, importTempFile).getId(),
                prepareImportProfile(admin, importMode, keyColumn, mediatype),
                new RemoteFile(importTempFile.getName(), importTempFile, -1),
                new ImportStatusImpl());
        profileImportWorker.setMaxGenderValue(admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)
                ? ConfigService.MAX_GENDER_VALUE_EXTENDED
                : ConfigService.MAX_GENDER_VALUE_BASIC);

        profileImportWorker.call();

        return getWorkerResultStatus(profileImportWorker);
    }

    private static ImportStatus getWorkerResultStatus(ProfileImportWorker profileImportWorker) {
        if (profileImportWorker.getError() != null) {
            logger.error(profileImportWorker.getError().getMessage());
            if (profileImportWorker.getError() instanceof ImportValidationException validationException) {
                throw new ValidationException(
                        validationException.getErrorMessageKey(),
                        validationException.getAdditionalErrorData()
                );
            }
            throw new IllegalStateException("Error occurred during recipients import. Please contact support team.");
        }
        ImportStatus status = profileImportWorker.getStatus();
        if (status.getFatalError() != null) {
            throw new IllegalStateException(status.getFatalError());
        }
        return status;
    }

    private static File createImportTempFile(Admin admin, String requestUUID) {
        Path importDir = Path.of(IMPORT_FILE_DIRECTORY, String.valueOf(admin.getCompanyID()));
        try {
            Files.createDirectories(importDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create directory: " + importDir, e);
        }

        return importDir.resolve("Recipientimportjson_" + requestUUID + ".json").toFile();
    }

    private static ImportProfile prepareImportProfile(
            Admin admin, ImportMode importMode, String keyColumn, MediaTypes mediaType
    ) {
        ImportProfile importProfile = new ImportProfileImpl();
        importProfile.setCompanyId(admin.getCompanyID());
        importProfile.setAdminId(admin.getAdminID());
        importProfile.setUpdateAllDuplicates(true);
        importProfile.setDateFormat(com.agnitas.util.importvalues.DateFormat.ISO8601.getIntValue());
        importProfile.setImportMode(importMode.getIntValue());
        importProfile.setCheckForDuplicates(CheckForDuplicates.COMPLETE.getIntValue());
        importProfile.setNullValuesAction(0);
        importProfile.setKeyColumn(keyColumn);
        importProfile.setAutoMapping(true);
        importProfile.setDefaultMailType(MailType.HTML.getIntValue());
        importProfile.setDatatype("JSON"); // use JSON Import
        importProfile.setCharset(Charset.UTF_8.getIntValue());
        importProfile.setMediatypes(Set.of(requireNonNullElse(mediaType, MediaTypes.EMAIL)));
        importProfile.setReportLocale(admin.getLocale());
        importProfile.setReportTimezone(admin.getAdminTimezone());
        return importProfile;
    }

    private DatasourceDescription saveImportDatasource(Admin admin, File importTempFile) {
        DatasourceDescription datasource = new DatasourceDescriptionImpl();
        datasource.setCompanyID(admin.getCompanyID());
        datasource.setSourceGroupType(SourceGroupType.File);
        datasource.setCreationDate(new Date());
        datasource.setDescription(importTempFile.getName());
        datasource.setDescription2("Restful-Import");
        dataSourceService.save(datasource);
        return datasource;
    }
}
