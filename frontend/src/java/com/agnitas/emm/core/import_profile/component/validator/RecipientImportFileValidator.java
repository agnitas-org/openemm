/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component.validator;

import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.service.ImportException;
import org.agnitas.util.ZipDataException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class RecipientImportFileValidator {

    private static final Logger LOGGER = LogManager.getLogger(RecipientImportFileValidator.class);

    public SimpleServiceResult validate(ImportProfile profile, File importFile) {
        try {
            return executeValidation(profile, importFile);
        } catch (ImportException e) {
            return SimpleServiceResult.simpleError(Message.of(e.getErrorMessageKey(), e.getAdditionalErrorData()));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Error with encoding occurred during validation of recipient import file!", e);
            return SimpleServiceResult.simpleError(Message.of("error.import.charset"));
        } catch (ZipDataException e) {
            LOGGER.error("Error with zip data occurred during validation of recipient import file!", e);
            return SimpleServiceResult.simpleError(Message.of("error.import.zip"));
        } catch (Exception e) {
            LOGGER.error("Error occurred during validation of recipient import file!", e);
            return SimpleServiceResult.simpleError(Message.of("error.import.file", e.getMessage()));
        }
    }

    protected abstract SimpleServiceResult executeValidation(ImportProfile importProfile, File importFile) throws Exception;

    protected Optional<String> findMissingRequiredFileColumn(Collection<String> foundColumns, List<ColumnMapping> columnMappings) {
        for (ColumnMapping mapping : columnMappings) {
            if (!mapping.getDatabaseColumn().equals(ColumnMapping.DO_NOT_IMPORT) && StringUtils.isNotEmpty(mapping.getFileColumn())
                    && !foundColumns.contains(mapping.getFileColumn())) {

                return Optional.of(mapping.getFileColumn());
            }
        }

        return Optional.empty();
    }

}
