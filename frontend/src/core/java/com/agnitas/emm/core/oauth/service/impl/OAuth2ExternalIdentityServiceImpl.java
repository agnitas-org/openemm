/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.oauth.service.impl;

import java.util.Optional;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.oauth.dao.OAuth2ExternalIdentityDao;
import com.agnitas.emm.core.oauth.exception.OAuthIdentityAlreadyLinkedException;
import com.agnitas.emm.core.oauth.exception.OAuthNotConfiguredException;
import com.agnitas.emm.core.oauth.model.OAuthExternalIdentityRecord;
import com.agnitas.emm.core.oauth.service.OAuth2ExternalIdentityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuth2ExternalIdentityServiceImpl implements OAuth2ExternalIdentityService {

    private final ConfigService configService;
    private final OAuth2ExternalIdentityDao dao;

    public OAuth2ExternalIdentityServiceImpl(ConfigService configService, OAuth2ExternalIdentityDao dao) {
        this.configService = configService;
        this.dao = dao;
    }

    @Override
    @Transactional
    public void link(String issuer, String subject, int adminId, int companyId) {
        Optional<OAuthExternalIdentityRecord> existing = dao.find(issuer, subject);
        if (existing.isEmpty()) {
            dao.save(issuer, subject, adminId, companyId);
            return;
        }

        int existingAdminId = existing.get().adminId();
        if (existingAdminId != adminId) {
            throw new OAuthIdentityAlreadyLinkedException(existingAdminId);
        }
    }

    @Override
    public Optional<OAuthExternalIdentityRecord> find(String issuer, String subject) {
        return dao.find(issuer, subject);
    }

    @Override
    public void deleteByAdminForConfiguredIssuer(int adminId) {
        if (!configService.isOAuthConfigured()) {
            throw new OAuthNotConfiguredException();
        }

        dao.delete(configService.getValue(ConfigValue.OAuth2Issuer), adminId);
    }

    @Override
    public boolean isLinkedForConfiguredIssuer(int adminId) {
        if (!configService.isOAuthConfigured()) {
            return false;
        }

        return dao.exists(configService.getValue(ConfigValue.OAuth2Issuer), adminId);
    }

}
