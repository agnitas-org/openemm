/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public class FileResponseBody implements StreamingResponseBody {
    private static final Logger logger = Logger.getLogger(FileResponseBody.class);

    private File file;
    private boolean autoRemove;

    public FileResponseBody(File file, boolean autoRemove) {
        this.file = Objects.requireNonNull(file);
        this.autoRemove = autoRemove;
    }

    @Override
    public void writeTo(OutputStream outputStream) {
        try (FileInputStream archiveStream = new FileInputStream(file)) {
            IOUtils.copy(archiveStream, outputStream);
        } catch (Exception e) {
            logger.error("error while downloading file", e);
        } finally {
            if (autoRemove && !file.delete()) {
                logger.warn("Cannot remove temporary file");
            }
        }
    }
}
