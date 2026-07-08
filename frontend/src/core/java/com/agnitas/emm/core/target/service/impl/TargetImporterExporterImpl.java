/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Target;
import com.agnitas.emm.core.target.dto.TargetExportDto;
import com.agnitas.emm.core.target.mapper.TargetMapper;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.exception.BadRequestException;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.spring.validation.ValidationService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TargetImporterExporterImpl implements TargetImporterExporter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final TargetService targetService;
    private final TargetMapper targetMapper;
    private final ValidationService validationService;

    public TargetImporterExporterImpl(
        TargetService targetService,
        TargetMapper targetMapper,
        ValidationService validationService
    ) {
        this.targetService = targetService;
        this.targetMapper = targetMapper;
        this.validationService = validationService;
    }

    @Override
    public ByteArrayResource exportAsJson(int id, int companyId) throws JsonProcessingException {
        Target target = targetService.getTargetGroup(id, companyId);

        byte[] json = objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsBytes(targetMapper.toExportDto(target));

        return new ByteArrayResource(json);
    }

    @Override
    public Target importFromJson(MultipartFile jsonFile, Admin admin) throws IOException {
        if (isJsonFileInvalid(jsonFile)) {
            throw new BadRequestException(Message.of("error.import.invalidDataType", "json"));
        }
        TargetExportDto exportDto = fileToExportDto(jsonFile);

        validationService.validate(exportDto);

        Target target = targetMapper.fromExportDto(exportDto);

        if (targetService.isTargetNameInUse(target.getTargetName(), admin.getCompanyID())) {
            target.setTargetName(getCloneName(target.getTargetName(), admin.getCompanyID(), admin.getLocale()));
        }
        if (StringUtils.isBlank(target.getTargetDescription())) {
            target.setTargetDescription("Imported at " + DateTimeFormatter
                .ofPattern(DateUtilities.DD_MM_YYYY_HH_MM)
                .format(LocalDateTime.now()));
        }
        return target;
    }

    private String getCloneName(String name, int companyId, Locale locale) {
        String prefix = I18nString.t("mailing.CopyOf", locale) + " ";
        return AgnUtils.getUniqueCloneName(name, StringUtils.replaceChars(prefix, " ", "_"),
            50, newName -> targetService.isTargetNameInUse(newName, companyId));
    }

    private static TargetExportDto fileToExportDto(MultipartFile jsonFile) throws IOException {
        try (InputStream is = jsonFile.getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<>() {});
        }
    }

    private static boolean isJsonFileInvalid(MultipartFile jsonFile) {
        return jsonFile == null
               || jsonFile.isEmpty()
               || !"json".equalsIgnoreCase(FilenameUtils.getExtension(jsonFile.getOriginalFilename()));
    }
}
