/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.oauth.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.oauth.dao.OAuth2AuthorizationRequestDao;
import com.agnitas.emm.core.oauth.dao.OAuth2RegistrationClientDao;
import com.agnitas.emm.core.oauth.model.OAuthRequestRecord;
import com.agnitas.emm.core.oauth.service.OAuth2AuthorizationRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("oAuth2AuthorizationRequestService")
public class OAuth2AuthorizationRequestServiceImpl implements OAuth2AuthorizationRequestService {

    private static final int TTL_MINUTES = 5;
    private static final int CLIENT_REGISTRATION_RETENTION_DAYS = 90;

    private final OAuth2AuthorizationRequestDao dao;
    private final OAuth2RegistrationClientDao registrationClientDao;
    private final ConfigService configService;

    public OAuth2AuthorizationRequestServiceImpl(OAuth2AuthorizationRequestDao dao, OAuth2RegistrationClientDao registrationClientDao, ConfigService configService) {
        this.configService = configService;
        this.dao = dao;
        this.registrationClientDao = registrationClientDao;
    }

    @Override
    public void save(String state, String requestJson, Admin admin) {
        Instant expiresAt = Instant.now().plus(TTL_MINUTES, ChronoUnit.MINUTES);
        dao.save(state, requestJson, admin.getAdminID(), admin.getCompanyID(), expiresAt);
    }

    @Override
    public Optional<OAuthRequestRecord> find(String state) {
        return dao.findByState(state);
    }

    @Override
    @Transactional
    public Optional<OAuthRequestRecord> findAndRemove(String state) {
        Optional<OAuthRequestRecord> reqRecord = dao.findByState(state);
        reqRecord.ifPresent(r -> dao.deleteByState(r.state()));
        return reqRecord;
    }

    @Override
    public void deleteExpired() {
        dao.deleteExpired();
    }

    @Override
    public void deleteOldClientRegistrations() {
        // The client configured for an external OAuth server is re-saved only on startup, so never age it out
        registrationClientDao.deleteIssuedBefore(
                Instant.now().minus(CLIENT_REGISTRATION_RETENTION_DAYS, ChronoUnit.DAYS),
                configService.getValue(ConfigValue.OAuth2ServerClientId)
        );
    }

}
