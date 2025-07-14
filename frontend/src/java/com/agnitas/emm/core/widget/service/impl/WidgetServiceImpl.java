/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.widget.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.widget.beans.WidgetSettingsBase;
import com.agnitas.emm.core.widget.enums.WidgetType;
import com.agnitas.emm.core.widget.service.WidgetService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class WidgetServiceImpl implements WidgetService {

    private final CompanyTokenService companyTokenService;
    private final ConfigService configService;
    private final ObjectMapper objectMapper;

    public WidgetServiceImpl(CompanyTokenService companyTokenService, ConfigService configService) {
        this.companyTokenService = companyTokenService;
        this.configService = configService;
        objectMapper = new ObjectMapper();
    }

    @Override
    public WidgetType getWidgetType(String token) {
        String type = decodeBase64(splitToken(token)[0]);
        return WidgetType.valueOf(type);
    }

    @Override
    public <T extends WidgetSettingsBase> T parseSettings(String token, Class<T> type) throws JsonProcessingException {
        String settings = decodeBase64(splitToken(token)[1]);
        return objectMapper.readValue(settings, type);
    }

    @Override
    public boolean isTokenValid(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        String[] parts = splitToken(token);
        if (parts.length != 3) {
            return false;
        }

        try {
            return signWithSecretKey(parts[0], parts[1]).equals(parts[2]);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    @Override
    public boolean isTokenValid(String token, WidgetType widgetType) {
        if (!isTokenValid(token)) {
            return false;
        }

        return widgetType.equals(getWidgetType(token));
    }

    private String[] splitToken(String token) {
        return token.split("\\.");
    }

    @Override
    public String generateToken(WidgetType widgetType, WidgetSettingsBase settings, int companyId) throws Exception {
        String companyToken = companyTokenService.getCompanyToken(companyId)
                .orElseThrow(() -> new IllegalStateException("Company token not found! Company ID: " + companyId));

        settings.setCompanyToken(companyToken);
        String payload = objectMapper.writeValueAsString(settings);

        String typeBase64 = encodeToBase64(widgetType.name());
        String payloadBase64 = encodeToBase64(payload);

        return typeBase64 + "." + payloadBase64 + "." + signWithSecretKey(typeBase64, payloadBase64);
    }

    private String signWithSecretKey(String typeBase64, String payloadBase64) throws GeneralSecurityException {
        String message = typeBase64 + "." + payloadBase64;

        Mac hmacSha256 = Mac.getInstance("HmacSHA256");

        String secretKey = configService.getValue(ConfigValue.WidgetTokenSecret);
        if (StringUtils.isBlank(secretKey)) {
            throw new IllegalStateException("Secret key not defined to generate widget token!");
        }

        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(keySpec);

        byte[] hmacBytes = hmacSha256.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return encodeToBase64(hmacBytes);
    }

    private String encodeToBase64(String data) {
        return encodeToBase64(data.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeToBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private String decodeBase64(String data) {
        return new String(Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8)));
    }
}
