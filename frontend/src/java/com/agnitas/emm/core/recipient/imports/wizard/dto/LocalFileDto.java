/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.imports.wizard.dto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.agnitas.emm.core.commons.dto.FileDto;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class LocalFileDto implements FileDto {

    private final String filePath;
    private final String originalFileName;

    public LocalFileDto(String filePath) {
        this.filePath = filePath;
        this.originalFileName = getFileName();
    }

    public LocalFileDto(String filePath, String originalFileName) {
        this.filePath = filePath;
        this.originalFileName = originalFileName;
    }

    @Override
    public String getName() {
        return originalFileName;
    }

    @Override
    public byte[] toBytes() throws IOException {
        return FileUtils.readFileToByteArray(toFile());
    }

    @Override
    public File toFile() {
        return new File(filePath);
    }

    @Override
    public InputStream toInputStream() throws IOException {
        return FileUtils.openInputStream(toFile());
    }

    private String getFileName() {
        File file = toFile();
        return FilenameUtils.getName(file.getName());
    }
}
