/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.common.mapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;

import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.util.FileDownload;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Named;
import org.springframework.web.multipart.MultipartFile;

public interface UrlContentMcpMapper {

    default byte[] fetchFromUrl(String url) {
        return fetchFromUrl(url, "url");
    }

    default byte[] fetchFromUrl(String url, String fieldName) {
        try {
            return FileDownload.downloadAsBytes(url);
        } catch (IOException e) {
            throw ValidationException.exact("Error for field %s: %s".formatted(fieldName, e.getMessage()));
        }
    }

    @Named("filenameFromUrl")
    default String filenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    default MultipartFile toMultipartFile(String url) {
        return toMultipartFile(url, "url");
    }

    default MultipartFile toMultipartFile(String url, String fieldName) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        byte[] data = fetchFromUrl(url, fieldName);

        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return filenameFromUrl(url);
            }

            @Override
            public String getContentType() {
                String guessed = URLConnection.guessContentTypeFromName(getOriginalFilename());
                return guessed != null ? guessed : "application/octet-stream";
            }

            @Override
            public boolean isEmpty() {
                return data.length == 0;
            }

            @Override
            public long getSize() {
                return data.length;
            }

            @Override
            public byte[] getBytes() {
                return data;
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(data);
            }

            @Override
            public void transferTo(File dest) throws IOException {
                Files.write(dest.toPath(), data);
            }
        };
    }

}
