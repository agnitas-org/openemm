/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.common.service.impl;

import com.agnitas.emm.common.exceptions.ZipArchiveException;
import com.agnitas.emm.common.service.ZipArchiveService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.Tuple;
import org.agnitas.util.ZipUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.zip.ZipOutputStream;

import static org.agnitas.util.FileUtils.getUniqueFileName;

@Service("zipArchiveService")
public class ZipArchiveServiceImpl<T> implements ZipArchiveService<T> {

    private static final Logger logger = LogManager.getLogger(ZipArchiveServiceImpl.class);

    @Override
    public File createZipArchive(Collection<T> items, String fileNameSuffix, Function<T, Tuple<String, byte[]>> dataFunction) {
        if (CollectionUtils.isEmpty(items)) {
            throw new ZipArchiveException("Zip archive can't be created without files!");
        }

        File zip = createTempEmptyZip(fileNameSuffix);
        tryWriteEntriesToZip(zip, items, dataFunction);
        return zip;
    }

    private File createTempEmptyZip(String fileNameSuffix) {
        try {
            String fileName = String.format("%s-%s", fileNameSuffix, new Date().getTime());
            return File.createTempFile(fileName, ".zip", AgnUtils.createDirectory(AgnUtils.getTempDir()));
        } catch (IOException e) {
            throw new ZipArchiveException("Error while creating zip archive!", e);
        }
    }

    private void tryWriteEntriesToZip(File archive, Collection<T> items, Function<T, Tuple<String, byte[]>> dataFunction) {
        try (OutputStream stream = new FileOutputStream(archive)) {
            writeEntriesToZipArchive(stream, items, dataFunction);
        } catch (IOException e) {
            tryDeleteFileIfExist(archive);
            throw new ZipArchiveException("Error while add data to zip archive!", e);
        }
    }

    private void writeEntriesToZipArchive(OutputStream archiveStream, Collection<T> items, Function<T, Tuple<String, byte[]>> dataFunction) throws IOException {
        ZipOutputStream zipStream = null;
        try {
            zipStream = ZipUtilities.openNewZipOutputStream(archiveStream);
            zipStream.setLevel(ZipOutputStream.STORED);

            try {
                Set<String> namesInUse = new HashSet<>();
                for (T item : items) {
                    Tuple<String, byte[]> entryData = dataFunction.apply(item);

                    if (entryData != null) {
                        byte[] content = entryData.getSecond();

                        if (content != null) {
                            String filename = getUniqueFileName(entryData.getFirst(), namesInUse::contains);
                            ZipUtilities.addFileDataToOpenZipFileStream(content, filename, zipStream);
                            namesInUse.add(filename);
                        }
                    }
                }
            } finally {
                try {
                    ZipUtilities.closeZipOutputStream(zipStream);
                    zipStream = null;
                } catch (IOException e) {
                    logger.error("Cannot close output ZIP stream", e);
                }
            }
        } catch (Exception e) {
            if (zipStream != null) {
                zipStream.close();
            }
            throw e;
        }
    }

    private void tryDeleteFileIfExist(File archive) {
        try {
            Files.deleteIfExists(archive.toPath());
        } catch (IOException e) {
            logger.error("Cannot delete temporary archive file: {}", archive.getAbsolutePath());
        }
    }
}
