/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.stats;

import com.agnitas.beans.Admin;
import com.agnitas.emm.rest.v2.infrastructure.security.auth.PermissionAllowed;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mailings/{mailingId:\\d+}/statistics")
public class MailingStatsV2Controller {

    private final MailingStatsRestService statsRestService;

    public MailingStatsV2Controller(MailingStatsRestService statsRestService) {
        this.statsRestService = statsRestService;
    }

    @GetMapping
    @PermissionAllowed("stats.show")
    public MailingStatsResponse getStats(@PathVariable @Positive int mailingId, Admin admin) {
        return statsRestService.getStats(mailingId, admin);
    }
}
