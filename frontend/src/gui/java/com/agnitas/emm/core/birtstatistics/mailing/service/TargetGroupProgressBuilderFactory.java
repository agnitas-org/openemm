/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.service;

import java.util.SequencedMap;

import com.agnitas.emm.core.birtstatistics.mailing.dao.LinkProgressStatsDao;
import com.agnitas.emm.core.birtstatistics.mailing.dao.MailingProgressStatsDao;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import org.springframework.stereotype.Component;

@Component
public class TargetGroupProgressBuilderFactory {

    private final MailingProgressStatsDao mailingProgressStatsDao;
    private final LinkProgressStatsDao linkProgressStatsDao;

    TargetGroupProgressBuilderFactory(
            MailingProgressStatsDao mailingProgressStatsDao,
            LinkProgressStatsDao linkProgressStatsDao
    ) {
        this.mailingProgressStatsDao = mailingProgressStatsDao;
        this.linkProgressStatsDao = linkProgressStatsDao;
    }

    public TargetGroupOpeningsProgressBuilder openings(
            int mailingId,
            int companyId,
            DateRange dateRange,
            SequencedMap<Integer, LightTarget> targets,
            boolean hourScale
    ) {
        return new TargetGroupOpeningsProgressBuilder(
                mailingProgressStatsDao,
                mailingId,
                companyId,
                dateRange,
                targets,
                hourScale
        );
    }

    public TargetGroupClicksProgressBuilder clicks(
            int mailingId,
            int companyId,
            DateRange dateRange,
            SequencedMap<Integer, LightTarget> targets,
            boolean hourScale
    ) {
        return new TargetGroupClicksProgressBuilder(
                mailingProgressStatsDao,
                mailingId,
                companyId,
                dateRange,
                targets,
                hourScale
        );
    }

    public TargetGroupLinkClicksProgressBuilder linkClicks(
            int linkId,
            int companyId,
            DateRange dateRange,
            SequencedMap<Integer, LightTarget> targets,
            boolean hourScale
    ) {
        return new TargetGroupLinkClicksProgressBuilder(
                linkProgressStatsDao,
                linkId,
                companyId,
                dateRange,
                targets,
                hourScale
        );
    }

}
