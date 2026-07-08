/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.web.multipart.MultipartFile;

public class ApacheTikaUtils {

    private static final Logger logger = LogManager.getLogger(ApacheTikaUtils.class);

    private ApacheTikaUtils() {

    }

    public static boolean isValidFont(byte[] data) {
        return hasExpectedContentType(
                data,
                "application/octet-stream",
                 "application/x-font-ttf",
                 "application/x-font-truetype",
                 "application/x-font-opentype",
                 "application/font-woff",
                 "application/font-woff2",
                 "application/vnd.ms-fontobject",
                 "application/font-sfnt",
                 "image/svg+xml"
        );
    }

    public static boolean isValidVideo(byte[] data) {
        return isContentTypeStartsWith(data, "video/");
    }

    public static boolean isValidAudio(byte[] data) {
        return isContentTypeStartsWith(data, "audio/");
    }

    public static boolean isValidPdf(byte[] data) {
        return hasExpectedContentType(data, "application/pdf");
    }

    public static boolean isValidJSON(byte[] data) {
        return hasExpectedContentType(data, "application/json", "text/plain");
    }

    public static boolean isValidImage(InputStream inputStream) {
        try {
            return isValidImage(IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isValidImage(byte[] data) {
        return isContentTypeStartsWith(data, "image/");
    }

    public static boolean hasExpectedContentType(byte[] data, String ... contentTypes) {
        if (contentTypes.length == 0) {
            return false;
        }

        return Arrays.asList(contentTypes).contains(getContentType(data));
    }

    public static boolean isContentTypeStartsWith(byte[] data, String contentTypePart) {
        if (StringUtils.isEmpty(contentTypePart)) {
            return false;
        }

        String detectedContentType = getContentType(data);
        return detectedContentType != null && detectedContentType.startsWith(contentTypePart);
    }

    public static String getFileExtension(InputStream stream, boolean includeDot) {
        String contentType = getContentType(stream);
        return getFileExtension(contentType, includeDot);
    }

    private static String getFileExtension(String contentType, boolean includeDot) {
        TikaConfig config = TikaConfig.getDefaultConfig();
        try {
            String extension = config.getMimeRepository()
                    .forName(contentType)
                    .getExtension();

            if (includeDot) {
                return extension;
            }

            return extension.substring(1);
        } catch (MimeTypeException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getContentType(InputStream stream) {
        try {
            return new Tika().detect(stream);
        } catch (IOException e) {
            logger.error("Cant detect file content type: {}", e.getMessage());
            return "";
        }
    }

    public static String getContentType(MultipartFile file) {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, file.getOriginalFilename());

        try (InputStream tiStream = TikaInputStream.get(file.getInputStream())) {
            return new Tika().detect(tiStream, metadata);
        } catch (IOException e) {
            logger.error("Cant detect content type of file '{}': {}", file.getOriginalFilename(), e.getMessage());
            return "";
        }
    }

    public static String getContentType(byte[] data) {
        return new Tika().detect(data);
    }
}
