/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.agnitas.beans.ImportProfile;
import org.agnitas.service.ImportException;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.TempFileInputStream;
import org.agnitas.util.ZipUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

@Component
public class RecipientImportFileInputStreamProvider {

    public InputStream provide(File importFile, ImportProfile profile) throws Exception {
        if (!AgnUtils.isZipArchiveFile(importFile)) {
            return new FileInputStream(importFile);
        }

        try {
            if (profile.getZipPassword() == null) {
                InputStream dataInputStream = ZipUtilities.openSingleFileZipInputStream(importFile);
                if (dataInputStream == null) {
                    throw new ImportException(false, "error.unzip.noEntry");
                }

                return dataInputStream;
            }

            File fileForImport = extractImportFileFromZip(importFile, profile);
            return new TempFileInputStream(fileForImport);
        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportException(false, "error.unzip", e.getMessage());
        }
    }

    private File extractImportFileFromZip(File importFile, ImportProfile profile) throws Exception {
        try (ZipFile zipFile = new ZipFile(importFile)) {
            zipFile.setPassword(profile.getZipPassword().toCharArray());
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();

            boolean existsOnlyOneFile = CollectionUtils.size(fileHeaders) == 1;
            if (!existsOnlyOneFile) {
                throw new Exception("Invalid number of files included in zip file");
            }

            File tempImportFile = new File(importFile.getAbsolutePath() + ".tmp");
            try (FileOutputStream fileOutputStream = new FileOutputStream(tempImportFile)) {
                try (InputStream zipInput = zipFile.getInputStream(fileHeaders.get(0))) {
                    IOUtils.copy(zipInput, fileOutputStream);
                }
            }

            return tempImportFile;
        }
    }
}
