/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.imports.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.imports.form.ImportForm;
import com.agnitas.emm.core.imports.service.MailingImportService;
import com.agnitas.exception.BadRequestException;
import com.agnitas.exception.UiMessageException;
import com.agnitas.messages.Message;
import com.agnitas.service.ImportResult;
import com.agnitas.service.MailingImporter;
import com.agnitas.util.FileUtils;
import com.agnitas.util.ZipUtilities;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

public class MailingImportServiceImpl implements MailingImportService {

    private static final Logger LOGGER = LogManager.getLogger(MailingImportServiceImpl.class);
    private static final String[] SUPPORTED_FILE_EXTENSIONS = {"json", "zip"};

    protected final MailingImporter mailingImporter;

    public MailingImportServiceImpl(MailingImporter mailingImporter) {
        this.mailingImporter = mailingImporter;
    }

    @Override
    public ImportResult importMailing(ImportForm form, Admin admin) {
        MultipartFile uploadedFile = form.getUploadFile();
        String fileExtension = extractFileExtension(uploadedFile.getOriginalFilename());

        if (!isFileUploadingSupports(fileExtension)) {
            throw new BadRequestException(Message.of("error.import.invalidDataType", getSupportedFilesExtensionsAsStr()));
        }

        if (fileExtension.equalsIgnoreCase("json")) {
            return tryImportDataFromJson(form, admin, uploadedFile);
        }

        if (fileExtension.equalsIgnoreCase("zip")) {
            importMailingsFromZipArchive(form, admin);
        }

        return null;
    }

    private ImportResult tryImportDataFromJson(ImportForm form, Admin admin, MultipartFile uploadedFile) {
        try (InputStream input = uploadedFile.getInputStream()) {
            return importMailingDataFromJson(admin, input, form);
        } catch (Exception e) {
            LOGGER.error("Mailing import failed", e);
            if (e instanceof UiMessageException ume) {
                throw ume;
            }
            throw new UiMessageException("error.mailing.import");
        }
    }

    private void importMailingsFromZipArchive(ImportForm form, Admin admin) {
        File tempZipFile = null;
        File tempUnzippedDirectory = null;

        try {
            tempZipFile = File.createTempFile("Mailing_Import", ".json.zip");
            tempUnzippedDirectory = new File(tempZipFile.getAbsolutePath() + ".unzipped");

            try (
                    InputStream inputZipped = form.getUploadFile().getInputStream();
                    FileOutputStream tempZipFileOutputStream = new FileOutputStream(tempZipFile)
            ) {
                IOUtils.copy(inputZipped, tempZipFileOutputStream);
            }

            ZipUtilities.decompress(tempZipFile, tempUnzippedDirectory);

            for (File tmpFile : tempUnzippedDirectory.listFiles()) {
                if (isJsonFile(tmpFile.getName())) {
                    try (InputStream is = new FileInputStream(tmpFile)) {
                        importMailingDataFromJson(admin, is, form);
                    } catch (Exception e) {
                        LOGGER.error("Mailing import failed", e);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (tempZipFile != null && tempZipFile.exists()) {
                tempZipFile.delete();
            }
            if (tempUnzippedDirectory != null && tempUnzippedDirectory.exists()) {
                FileUtils.removeRecursively(tempUnzippedDirectory);
            }
        }
    }

    protected ImportResult importMailingDataFromJson(Admin admin, InputStream input, ImportForm form) {
        return mailingImporter.importMailingFromJson(admin.getCompanyID(), input, form.isTemplate(), form.isOverwriteTemplate(), form.isGrid());
    }

    private boolean isFileUploadingSupports(String fileExtension) {
        return Stream.of(SUPPORTED_FILE_EXTENSIONS)
                .anyMatch(e -> e.equalsIgnoreCase(fileExtension));
    }

    private String getSupportedFilesExtensionsAsStr() {
        return Stream.of(SUPPORTED_FILE_EXTENSIONS)
                .map(e -> "." + e)
                .collect(Collectors.joining(", "));
    }

    private boolean isJsonFile(String fileName) {
        String fileExtension = extractFileExtension(fileName);
        return "json".equalsIgnoreCase(fileExtension);
    }

    private String extractFileExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }
}
