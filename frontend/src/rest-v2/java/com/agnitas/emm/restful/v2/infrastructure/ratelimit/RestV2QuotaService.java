/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.ratelimit;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.util.quota.api.QuotaLimitExceededException;
import com.agnitas.emm.util.quota.api.QuotaServiceException;
import com.agnitas.emm.util.quota.tokenbucket.Bucket4jQuotaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RestV2QuotaService {

    private final ConfigService configService;
    private final Bucket4jQuotaService quotaService;

    public RestV2QuotaService(ConfigService configService, Bucket4jQuotaService bucket4jQuotaService) {
        this.configService = configService;
        this.quotaService = bucket4jQuotaService;
    }

    public void checkAndTrack(Admin admin, String endpointName) throws QuotaServiceException {
        if (configService.getBooleanValue(ConfigValue.EnableRestfulQuotas, admin.getCompanyID())) {
            return;
        }
        try {
            quotaService.checkAndTrack(admin.getUsername(), admin.getCompanyID(), endpointName);
        } catch (QuotaLimitExceededException e) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
        }
    }
}
