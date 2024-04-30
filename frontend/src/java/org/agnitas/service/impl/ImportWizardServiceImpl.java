/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.commons.dto.FileDto;
import com.agnitas.emm.core.recipient.imports.wizard.dto.LocalFileDto;
import com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardFileStepForm;
import com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps;
import com.agnitas.emm.core.upload.bean.DownloadData;
import com.agnitas.emm.core.upload.dao.ComUploadDao;
import com.agnitas.emm.core.upload.service.UploadService;
import com.agnitas.emm.core.upload.service.dto.UploadFileDescription;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.agnitas.beans.ImportStatus;
import org.agnitas.beans.impl.ImportStatusImpl;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.ImportWizardHelper;
import org.agnitas.service.ImportWizardService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.Blacklist;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.CsvDataInvalidItemCountException;
import org.agnitas.util.CsvReader;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.ImportUtils.ImportErrorType;
import org.agnitas.util.importvalues.ImportMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.upload.FormFile;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.web.ComImportWizardForm;
import org.springframework.web.multipart.MultipartFile;

public class ImportWizardServiceImpl implements ImportWizardService {

    private static final Logger logger = LogManager.getLogger(ImportWizardServiceImpl.class);

    protected BlacklistService blacklistService;
    private ConfigService configService;
    private UploadService uploadService;

    @Deprecated(forRemoval = true)
    // TODO: remove after migration GWUA-5173 finished. use service instead
    private ComUploadDao uploadDao;

    @Required
    public void setBlacklistService(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Required
    public void setUploadDao(ComUploadDao uploadDao) {
        this.uploadDao = uploadDao;
    }

    @Required
    public void setUploadService(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    // Remove after GWUA-5173 has been successfully tested
    @Override
    public SimpleServiceResult checkAndReadCsvFile(int companyId, ComImportWizardForm form) {
        List<Message> errors = new ArrayList<>();
        FormFile importFile = null;

        if (!form.isUseCsvUpload() && form.getCsvFile() != null) {
            importFile = form.getCsvFile();
            form.setAttachmentCsvFileID(0);
        } else if (form.isUseCsvUpload() && form.getAttachmentCsvFileID() != 0) {
            try {
                importFile = getFormFileByUploadId(form.getAttachmentCsvFileID(), "text/csv");
            } catch (CsvDataInvalidItemCountException e) {
                errors.add(Message.of("error.import.data.itemcount", e.getExpected(), e.getActual(), e.getErrorLineNumber()));
            } catch (Exception e) {
                errors.add(Message.of("error.import.exception", e.getMessage()));
            }
        }

        if (importFile == null) {
            // bug-fix clean up csvData
            form.getImportWizardHelper().setFileData(null);
            form.setCsvFileName(null);
            form.setAttachmentCsvFileID(0);

            return SimpleServiceResult.simpleError(errors);
        }

        SimpleServiceResult checkedResult = checkAllowedImportFileSize(companyId, importFile);

        if (checkedResult.isSuccess()) {
            try {
                form.getImportWizardHelper().setFileData(importFile.getFileData());
                form.setCsvFileName(importFile.getFileName());
            } catch (IOException e) {
                logger.error("Error occured: " + e.getMessage(), e);
            }
        }

        return checkedResult;
    }

    @Override
    public SimpleServiceResult checkAndReadCsvFile(ImportWizardSteps steps, Admin admin) throws IOException {
        ImportWizardFileStepForm form = steps.getFileStep();

        ImportWizardHelper helper = steps.getHelper();
        if (!form.isUseCsvUpload() && form.getCsvFile() != null && !form.getCsvFile().isEmpty()) {
            helper.setFile(getFileDtoByUploadedFile(form.getCsvFile(), admin));
            form.setAttachmentCsvFileId(0);
        } else if (form.isUseCsvUpload() && form.getAttachmentCsvFileId() != 0) {
            try {
                helper.setFile(getFileDtoByUploadId(form.getAttachmentCsvFileId()));
            } catch (Exception e) {
                return SimpleServiceResult.simpleError(Message.of("error.import.exception", e.getMessage()));
            }
        }

        if (helper.getFile() == null || StringUtils.isEmpty(helper.getFile().getName())) {
            // bug-fix clean up csvData
            helper.setFileData(null);
            form.setAttachmentCsvFileId(0);
            return SimpleServiceResult.simpleError(Message.of("error.classicimport.no_csv_file"));
        } else if (StringUtils.endsWithIgnoreCase(helper.getFile().getName(), ".zip")
                || StringUtils.endsWithIgnoreCase(helper.getFile().getName(), ".gz")) {
            return SimpleServiceResult.simpleError(Message.of("import.error.zipFile_not_allowed"));
        }

        SimpleServiceResult checkedResult = checkAllowedImportFileSize(admin.getCompanyID(), helper.getFile());
        if (checkedResult.isSuccess()) {
            try {
                helper.setFileData(helper.getFile().toBytes());
            } catch (IOException e) {
                logger.error("Error occured: {}", e.getMessage(), e);
            }
        }
        return checkedResult;
    }

    @Override
    @Deprecated(forRemoval = true)
    // TODO: remove after migration GWUA-5173 finished
    public FormFile getFormFileByUploadId(int uploadID, String mime) throws Exception {
        DownloadData downloadData = uploadDao.getDownloadData(uploadID);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            uploadDao.sendDataToStream(uploadID, os);

            final String fileName = downloadData.getFilename();
            final byte[] fileData = os.toByteArray();
            final Integer fileSize = downloadData.getFilesize();
            final String mimeType = mime;

            return new FormFile() {
                @Override
                public void destroy() {
                    // nothing to do
                }

                @Override
                public String getContentType() {
                    return mimeType;
                }

                @Override
                public byte[] getFileData() {
                    return fileData;
                }

                @Override
                public String getFileName() {
                    return fileName;
                }

                @Override
                public int getFileSize() {
                    return fileSize;
                }

                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(fileData);
                }

                @Override
                public void setContentType(String contentType) {
                    // nothing to do
                }

                @Override
                public void setFileName(String fileNameUnused) {
                    // nothing to do
                }

                @Override
                public void setFileSize(int fileSizeUnused) {
                    // nothing to do
                }
            };
        } catch (Exception e) {
            throw e;
        }
    }

    private FileDto getFileDtoByUploadId(int uploadID) {
        File file = uploadService.getDataForDownload(uploadID);
        UploadFileDescription uploadDescription = uploadService.getUploadFileDescription(uploadID);
        return new LocalFileDto(file.getAbsolutePath(), uploadDescription.getFileName());
    }

    private FileDto getFileDtoByUploadedFile(MultipartFile file, Admin admin) throws IOException {
        File tempImportFile = ImportUtils.createTempImportFile(file, admin);
        return new LocalFileDto(tempImportFile.getAbsolutePath(), file.getOriginalFilename());
    }

    private SimpleServiceResult checkAllowedImportFileSize(int companyID, FileDto fileDto) {
        int maxSizeAllowedForClassicImport = configService.getIntegerValue(ConfigValue.ClassicImportMaxFileSize, companyID);
        if (maxSizeAllowedForClassicImport >= 0) {
            try {
                int csvFileSize = fileDto.toBytes().length;
                if (csvFileSize <= 0) {
                    return SimpleServiceResult.simpleError(Message.of("autoimport.error.emptyFile", fileDto.getName()));
                }
                if (csvFileSize > maxSizeAllowedForClassicImport) {
                    return SimpleServiceResult.simpleError(Message.of("error.import.maximum_filesize_exceeded", AgnUtils.getHumanReadableNumber(maxSizeAllowedForClassicImport, "Byte", false)));
                }
            } catch (Exception e) {
                return SimpleServiceResult.simpleError(Message.of("error.import.exception", e.getMessage()));
            }
        }

        int maxRowsAllowedForClassicImport = configService.getIntegerValue(ConfigValue.ClassicImportMaxRows, companyID);
        if (maxRowsAllowedForClassicImport >= 0) {
            // Also there might be an error within the csv structure (escaped linebreaks in CSV), the linebreaks should be counted as csv entries.
            // This is not fully correct, but allows the user to ignore some invalid csv lines later on in the GUI.
            try (LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(fileDto.toInputStream()))) {
                while ((lineNumberReader.readLine()) != null) {
                    // Just read through the data to count the number of lines
                }

                if (lineNumberReader.getLineNumber() > maxRowsAllowedForClassicImport) {
                    return SimpleServiceResult.simpleError(Message.of("error.import.maxlinesexceeded", lineNumberReader.getLineNumber(), maxRowsAllowedForClassicImport));
                }
            } catch (Exception e) {
                return SimpleServiceResult.simpleError(Message.of("error.import.exception", e.getMessage()));
            }
        }
        return new SimpleServiceResult(true);
    }
    
    // Remove after GWUA-5173 has been successfully tested
    private SimpleServiceResult checkAllowedImportFileSize(int companyID, FormFile csvFileToCheck) {
        int maxSizeAllowedForClassicImport = configService.getIntegerValue(ConfigValue.ClassicImportMaxFileSize, companyID);
        if (maxSizeAllowedForClassicImport >= 0) {
            try {
                int csvFileSize = csvFileToCheck.getFileSize();
                if (csvFileSize > maxSizeAllowedForClassicImport) {
                    return SimpleServiceResult.simpleError(Message.of("error.import.maximum_filesize_exceeded", AgnUtils.getHumanReadableNumber(maxSizeAllowedForClassicImport, "Byte", false)));
                }
            } catch (Exception e) {
                return SimpleServiceResult.simpleError(Message.of("error.import.exception", e.getMessage()));
            }
        }

        int maxRowsAllowedForClassicImport = configService.getIntegerValue(ConfigValue.ClassicImportMaxRows, companyID);
        if (maxRowsAllowedForClassicImport >= 0) {
            // Also there might be an error within the csv structure (escaped linebreaks in CSV), the linebreaks should be counted as csv entries.
            // This is not fully correct, but allows the user to ignore some invalid csv lines later on in the GUI.
            try (LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(csvFileToCheck.getInputStream()))) {
                while ((lineNumberReader.readLine()) != null) {
                    // Just read through the data to count the number of lines
                }

                if (lineNumberReader.getLineNumber() > maxRowsAllowedForClassicImport) {
                    return SimpleServiceResult.simpleError(Message.of("error.import.maxlinesexceeded", lineNumberReader.getLineNumber(), maxRowsAllowedForClassicImport));
                }
            } catch (Exception e) {
                return SimpleServiceResult.simpleError(Message.of("error.import.exception", e.getMessage()));
            }
        }

        return new SimpleServiceResult(true);
    }

    @Override
    public ImportWizardHelper createHelper() {
        ImportWizardHelperImpl helper = new ImportWizardHelperImpl();

        helper.setStatus(new ImportStatusImpl());

        return helper;
    }

    // Remove after GWUA-5173 has been successfully tested
    @Override
    public void parseFirstLine(ImportWizardHelper helper)  throws ImportWizardContentParseException{
        int colNum = 0;

        if (helper.getFileData().length == 0) {
            throw new ImportWizardContentParseException("error.import.no_file");
        }
        
        if (!(helper.getMode() != ImportMode.ADD.getIntValue() && helper.getMode() != ImportMode.ADD_AND_UPDATE.getIntValue() && helper.getStatus().getKeycolumn().equalsIgnoreCase("customer_id"))) {
            helper.getDbAllColumns().remove("customer_id");
        }

        helper.setCsvAllColumns(new ArrayList<>());

        try {
            char separator = helper.getStatus().getSeparator();
            Character stringQuote = null;
            if (StringUtils.isNotEmpty(helper.getStatus().getDelimiter())) {
                stringQuote = helper.getStatus().getDelimiter().charAt(0);
            }
            try (CsvReader csvReader = new CsvReader(new ByteArrayInputStream(helper.getFileData()), helper.getStatus().getCharset(), separator, stringQuote)) {
				csvReader.setAlwaysTrim(true);
                List<String> firstlineData = null;
                if ((firstlineData = csvReader.readNextCsvLine()) != null) {
                	String duplicateCsvColumn = CsvReader.checkForDuplicateCsvHeader(firstlineData, false);
        			if (duplicateCsvColumn != null) {
        				throw new ImportWizardContentParseException("error.duplicate.csvcolumn", duplicateCsvColumn);
        			}
        			
                    CsvColInfo aCol = null;
                    List<String> tempList = new ArrayList<>();
                    for (String csvColumnHeader : firstlineData) {
                        //csvColumnHeader = csvColumnHeader.trim().toLowerCase();
                        aCol = new CsvColInfo();
                        aCol.setName(csvColumnHeader);
                        aCol.setActive(false);
                        aCol.setType(CsvColInfo.TYPE_UNKNOWN);

                        // add column to csvAllColumns:
                        if (!tempList.contains(aCol.getName())) {
                            tempList.add(aCol.getName());
                        } else {
                            throw new ImportWizardContentParseException("error.import.column", aCol.getName());
                        }
                        helper.getCsvAllColumns().add(aCol);
                        colNum++;
                        helper.setCsvMaxUsedColumn(colNum);
                    }
                }
            }
        } catch (ImportWizardContentParseException e) {
            logger.error("parseFirstline: " + e.getMessage() + " key: " + e.getErrorMessageKey(), e);
            throw e;
        } catch (Exception e) {
            logger.error("parseFirstline: " + e, e);
            throw new ImportWizardContentParseException("error.import.parse.firstline", e.getMessage());
        }
    }

    @Override
    public ServiceResult<List<CsvColInfo>> parseFirstLineNew(ImportWizardHelper helper) throws IOException {
        ImportStatus status = helper.getStatus();
        if (helper.getFile().toBytes().length == 0) {
            return ServiceResult.error(Message.of("error.import.no_file"));
        }
        try (CsvReader csvReader = new CsvReader(
                new ByteArrayInputStream(helper.getFile().toBytes()),
                status.getCharset(),
                status.getSeparator(),
                StringUtils.isNotEmpty(helper.getStatus().getDelimiter()) ? helper.getStatus().getDelimiter().charAt(0) : null)) {
            csvReader.setAlwaysTrim(true);
            List<String> firstLineHeaders = csvReader.readNextCsvLine();
            String duplicateCsvColumn = CsvReader.checkForDuplicateCsvHeader(firstLineHeaders, false);
            if (duplicateCsvColumn != null) {
                logger.error("parseFirstLine: A duplicate CSV column was found: {}", duplicateCsvColumn);
                return ServiceResult.error(Message.of("error.duplicate.csvcolumn", duplicateCsvColumn));
            }
            return ServiceResult.success(firstLineHeaders.stream().map(CsvColInfo::new).collect(Collectors.toList()));
        } catch (ImportWizardContentParseException e) {
            logger.error("parseFirstline: {}  key: {}", e.getMessage() + e.getErrorMessageKey(), e);
            if (e.getAdditionalErrorData() != null && e.getAdditionalErrorData().length > 0) {
                return ServiceResult.error(Message.of(e.getErrorMessageKey(), e.getAdditionalErrorData()));
            } else {
                return ServiceResult.error(Message.of(e.getErrorMessageKey()));
            }
        } catch (Exception e) {
            logger.error("parseFirstLine: {}", e.getMessage(), e);
            return ServiceResult.error(Message.of("error.import.parse.firstline", e.getMessage()));
        }
    }

    @Override
    public void parseContent(ImportWizardHelper helper) throws ImportWizardContentParseException{
        boolean hasGENDER = false;
        boolean hasMAILTYPE = false;
        boolean hasKeyColumn = false;

        helper.setUniqueValues(new HashSet<>());
        helper.setParsedContent(new LinkedList<>());
        helper.setLinesOK(0);

        helper.setDbInsertStatus(0);

        try {
            @SuppressWarnings("unused")
			String charsetCheck = new String(helper.getFileData(), helper.getStatus().getCharset());
        } catch (UnsupportedEncodingException e) {
            throw new ImportWizardContentParseException("error.import.charset",e);
        }

        try {
            Set<String> blacklistEntries = blacklistService.loadBlackList(helper.getCompanyID());
            helper.setBlacklistHelper(new Blacklist());
            for (String blackListEntry : blacklistEntries) {
                helper.getBlacklistHelper().add(blackListEntry, false);
            }
        } catch (Exception e) {
            throw new ImportWizardContentParseException("error.import.blacklist", e);
        }

        // check in the columnMapping for the key column,
        // and eventually for gender and mailtype:
        for (CsvColInfo value : helper.getColumnMapping().values()) {
            if (value.getName().equalsIgnoreCase(ComImportWizardForm.GENDER_KEY) ) {
                hasGENDER = true;
            }

            if (value.getName().equalsIgnoreCase(ComImportWizardForm.MAILTYPE_KEY)) {
                hasMAILTYPE = true;
            }

            if (value.getName().equalsIgnoreCase(helper.getStatus().getKeycolumn())) {
                hasKeyColumn = true;
            }
        }

        if (!hasKeyColumn) {
            throw new ImportWizardContentParseException("error.import.no_keycolumn_mapping");
        }

        if (helper.getMode() == ImportMode.ADD.getIntValue() || helper.getMode() == ImportMode.ADD_AND_UPDATE.getIntValue()) {
            if (!hasGENDER) {
                throw new ImportWizardContentParseException("error.import.column.gender.required");
            }
            if (!hasMAILTYPE) {
                throw new ImportWizardContentParseException("error.import.no_mailtype_mapping");
            }
        }

        try {
            char separator = helper.getStatus().getSeparator();
            Character stringQuote = null;
            if (StringUtils.isNotEmpty(helper.getStatus().getDelimiter())) {
                stringQuote = helper.getStatus().getDelimiter().charAt(0);
            }
            try (CsvReader csvReader = new CsvReader(new ByteArrayInputStream(helper.getFileData()), helper.getStatus().getCharset(), separator, stringQuote)) {
				csvReader.setAlwaysTrim(true);
                // read first csv line again; do not parse (already parsed in parseFirstLine):
                List<String> firstLineData = csvReader.readNextCsvLine();
                String firstLineDataJoined = StringUtils.join(firstLineData, separator);

                // prepare download-files for errors and parsed data
                helper.setErrorData(new HashMap<>());
                helper.getErrorData().put(ImportErrorType.DATE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.EMAIL_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.KEYDOUBLE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.GENDER_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.MAILTYPE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.NUMERIC_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.STRUCTURE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.BLACKLIST_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.setParsedData(new StringBuffer(firstLineDataJoined + '\n'));

                // read the rest of the csv-file:
                helper.setReadlines(0);
                helper.setLinesOK(0);
                List<String> nextLineData;
                // Only read the first block of lines to avoid trouble with heap space
                while ((nextLineData = csvReader.readNextCsvLine()) != null && helper.getLinesOK() < ComImportWizardForm.BLOCK_SIZE) {
                    LinkedList<Object> aLineContent = helper.parseLine(nextLineData, false);
                    if (aLineContent != null) {
                        helper.getParsedContent().add(aLineContent);
                        helper.getParsedData().append(StringUtils.join(nextLineData, separator)).append("\n");
                        helper.setLinesOK(helper.getLinesOK() + 1);
                    }
                    helper.setReadlines(helper.getReadlines() + 1);
                }
            }
        } catch (Exception e) {
            logger.error("parseContent: " + e, e);
            helper.setError(ImportErrorType.STRUCTURE_ERROR, e.getMessage());
        }
    }

    @Override
    public void parseContentNew(ImportWizardHelper helper) throws ImportWizardContentParseException {
        helper.setPreviewOffset(0);

        helper.setUniqueValues(new HashSet<>());
        helper.setParsedContent(new LinkedList<>());
        helper.setDbInsertStatus(0);
        helper.getStatus().getErrors().clear();

        tryToCheckCharset(helper);
        tryLoadBlocklist(helper);
        checkMissingMapping(helper);

        try {
            char separator = helper.getStatus().getSeparator();
            Character stringQuote = null;
            if (StringUtils.isNotEmpty(helper.getStatus().getDelimiter())) {
                stringQuote = helper.getStatus().getDelimiter().charAt(0);
            }
            try (CsvReader csvReader = new CsvReader(new ByteArrayInputStream(helper.getFile().toBytes()), helper.getStatus().getCharset(), separator, stringQuote)) {
                csvReader.setAlwaysTrim(true);
                // read first csv line again; do not parse (already parsed in parseFirstLine):
                List<String> firstLineData = csvReader.readNextCsvLine();
                String firstLineDataJoined = StringUtils.join(firstLineData, separator);

                // prepare download-files for errors and parsed data
                helper.setErrorData(new HashMap<>());
                helper.getErrorData().put(ImportErrorType.DATE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.EMAIL_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.KEYDOUBLE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.GENDER_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.MAILTYPE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.NUMERIC_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.STRUCTURE_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.getErrorData().put(ImportErrorType.BLACKLIST_ERROR, new StringBuffer(firstLineDataJoined + '\n'));
                helper.setParsedData(new StringBuffer(firstLineDataJoined + '\n'));

                // read the rest of the csv-file:
                List<String> nextLineData;
                int counter = 0;
                // Only read the first block of lines to avoid trouble with heap space
                while ((nextLineData = csvReader.readNextCsvLine()) != null && counter < ComImportWizardForm.BLOCK_SIZE) {
                    LinkedList<Object> aLineContent = helper.parseLine(nextLineData, false);
                    if (aLineContent != null) {
                        helper.getParsedContent().add(aLineContent);
                        helper.getParsedData().append(StringUtils.join(nextLineData, separator)).append("\n");
                    }
                    counter++;
                }
            }
        } catch (Exception e) {
            logger.error("parseContent: {}", e.getMessage(), e);
            helper.setError(ImportErrorType.STRUCTURE_ERROR, e.getMessage());
        }
    }

    // check in the columnMapping for the key column,
    // and eventually for gender and mailtype:
    private void checkMissingMapping(ImportWizardHelper helper) throws ImportWizardContentParseException {
        if (helper.getColumnMapping().values().stream()
                .noneMatch(value -> helper.getStatus().getKeycolumn().equalsIgnoreCase(value.getName()))) {
            throw new ImportWizardContentParseException("error.import.no_keycolumn_mapping");
        }
        if (helper.getMode() == ImportMode.ADD.getIntValue()
                || helper.getMode() == ImportMode.ADD_AND_UPDATE.getIntValue()) {
            if (helper.getColumnMapping().values().stream()
                    .noneMatch(value -> "gender".equalsIgnoreCase(value.getName()))) {
                throw new ImportWizardContentParseException("error.import.column.gender.required");
            }
            if (helper.getColumnMapping().values().stream()
                    .noneMatch(value -> "mailtype".equalsIgnoreCase(value.getName()))) {
                throw new ImportWizardContentParseException("error.import.no_mailtype_mapping");
            }
        }
    }

    private void tryLoadBlocklist(ImportWizardHelper helper) throws ImportWizardContentParseException {
        try {
            Set<String> blocklistEntries = blacklistService.loadBlackList(helper.getCompanyID());
            helper.setBlacklistHelper(new Blacklist());
            for (String blackListEntry : blocklistEntries) {
                helper.getBlacklistHelper().add(blackListEntry, false);
            }
        } catch (Exception e) {
            throw new ImportWizardContentParseException("error.import.blacklist", e);
        }
    }

    private void tryToCheckCharset(ImportWizardHelper helper) throws ImportWizardContentParseException {
        try {
            @SuppressWarnings("unused")
            String charsetCheck = new String(helper.getFile().toBytes(), helper.getStatus().getCharset());
        } catch (UnsupportedEncodingException e) {
            throw new ImportWizardContentParseException("error.import.charset", e);
        } catch (IOException e) {
            throw new ImportWizardContentParseException("error.import.no_file");
        }
    }

    @Override
    public void doParse(ImportWizardHelper helper) throws ImportWizardContentParseException {
        // start at the top of the csv file:
        helper.setPreviewOffset(0);
        // change this to process the column name mapping from --
        // previous action:
        parseContent(helper);
        //dbAllColumns = recipientDao.readDBColumns( companyID);
    }

    @Override
    public int getLinesOKFromFile(ImportWizardHelper helper) throws Exception {
        logDebug("--- getLinesOKFromFile start in service ---");
        int linesOkInFile = 0;
        helper.getUniqueValues().clear();

        Character stringQuote = StringUtils.isNotEmpty(helper.getStatus().getDelimiter()) ? helper.getStatus().getDelimiter().charAt(0) : null;
        try (CsvReader csvReader = new CsvReader(new ByteArrayInputStream(helper.getFile().toBytes()), helper.getStatus().getCharset(), helper.getStatus().getSeparator(), stringQuote)) {
            csvReader.setAlwaysTrim(true);
            // Skip csv column headers
            List<String> csvFileHeaders = csvReader.readNextCsvLine();

            String duplicateCsvColumn = CsvReader.checkForDuplicateCsvHeader(csvFileHeaders, false);
            if (duplicateCsvColumn != null) {
                throw new ImportWizardContentParseException("error.duplicate.csvcolumn", duplicateCsvColumn);
            }

            List<String> nextCsvData;
            while ((nextCsvData = csvReader.readNextCsvLine()) != null) {
                if (helper.parseLine(nextCsvData) != null) {
                    linesOkInFile++;
                }
            }
        }
        logDebug("--- getLinesOKFromFile end in service---");
        return linesOkInFile;
    }

    @Override
    public JSONArray getParsedContentJson(ImportWizardHelper helper, Admin admin) {
        SimpleDateFormat dateTimeFormat = getAdminDateTimeFormatWithDefaultTimeZone(admin);
        List<String> headers = helper.getCsvAllColumns().stream()
                .filter(CsvColInfo::isActive)
                .map(CsvColInfo::getName)
                .collect(Collectors.toList());
        JSONArray parsedContentJson = new JSONArray();

        for (LinkedList<Object> row : helper.getParsedContent()) {
            JSONObject entry = new JSONObject();
            for (int i = 0; i < row.size(); i++) {
                entry.element(headers.get(i), getParseContentColValStr(row.get(i), dateTimeFormat));
            }
            parsedContentJson.element(entry);
        }
        return parsedContentJson;
    }

    private SimpleDateFormat getAdminDateTimeFormatWithDefaultTimeZone(Admin admin) {
        SimpleDateFormat dateTimeFormat = admin.getDateTimeFormat();
        // Do not use a special timezone here, because the data comes from the users data file directly
        // and wasn't stored in db before, where it would have been converted in systems default timezone.
        dateTimeFormat.setTimeZone(TimeZone.getDefault());
        return dateTimeFormat;
    }

    private String getParseContentColValStr(Object colVal, SimpleDateFormat dateTimeFormat) {
        if (colVal == null) {
            return "";
        }
        switch (colVal.getClass().getName()) {
            case "java.lang.String":
                return StringEscapeUtils.escapeHtml4((String) colVal);
            case "java.lang.Double":
                return "" + ((Double) colVal).longValue();
            case "java.util.Date":
                return dateTimeFormat.format((java.util.Date) colVal);
            default:
                return "";
        }
    }

    private void logDebug(String log) {
        if (logger.isDebugEnabled()) {
            logger.debug(log);
        }
    }
}
