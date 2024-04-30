/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component.reader;

import com.agnitas.beans.Admin;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.CsvReader;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ColumnMappingsCsvReader extends ColumnMappingsReader {

    private static final Logger LOGGER = LogManager.getLogger(ColumnMappingsCsvReader.class);

    private final ComRecipientDao recipientDao;
    private final UserActivityLogService userActivityLogService;

    public ColumnMappingsCsvReader(ComRecipientDao recipientDao, UserActivityLogService userActivityLogService) {
        this.recipientDao = recipientDao;
        this.userActivityLogService = userActivityLogService;
    }

    @Override
    public ServiceResult<List<ColumnMapping>> read(InputStream fileStream, ImportProfile profile, Admin admin) throws Exception {
        List<Message> errorMessages = new ArrayList<>();

        try (CsvReader csvReader = newReader(fileStream, profile)) {
            List<String> fileHeaders = csvReader.readNextCsvLine();

            if (!fileHeaders.isEmpty()) {
                writeUserActivityLog(admin, "edit import profile", "Found columns based on csv - file import : " + fileHeaders.toString());

                if (profile.isNoHeaders()) {
                    fileHeaders = IntStream.range(1, fileHeaders.size() + 1)
                            .mapToObj(index -> "column_" + index)
                            .collect(Collectors.toList());
                }
            }

            checkFileHeadersForValidity(fileHeaders, profile, errorMessages);

            Map<String, CsvColInfo> dbColumns = recipientDao.readDBColumns(admin.getCompanyID(), admin.getAdminID(), profile.getKeyColumns());

            List<ColumnMapping> foundMappings = fileHeaders.stream()
                    .map(fh -> createNewColumnMapping(fh, profile.getId(), dbColumns))
                    .collect(Collectors.toList());

            return new ServiceResult<>(
                    foundMappings,
                    errorMessages.isEmpty(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    errorMessages
            );
        }
    }

    private void checkFileHeadersForValidity(List<String> fileHeaders, ImportProfile profile, List<Message> errorMessages) {
        if (fileHeaders.contains("")) {
            errorMessages.add(Message.of("error.import.column.name.empty"));
        }

        if (CsvReader.checkForDuplicateCsvHeader(fileHeaders, profile.isAutoMapping()) != null) {
            errorMessages.add(Message.of("error.import.column.csv.duplicate"));
        }
    }

    private CsvReader newReader(InputStream fileStream, ImportProfile profile) throws Exception {
        Separator separator = Separator.getSeparatorById(profile.getSeparator());
        Character stringQuote = TextRecognitionChar.getTextRecognitionCharById(profile.getTextRecognitionChar()).getValueCharacter();
        Charset charset = Charset.getCharsetById(profile.getCharset());

        CsvReader reader = new CsvReader(fileStream, charset.getCharsetName(), separator.getValueChar(), stringQuote);
        reader.setAlwaysTrim(true);

        return reader;
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, new UserAction(action, description), LOGGER);
    }
}
