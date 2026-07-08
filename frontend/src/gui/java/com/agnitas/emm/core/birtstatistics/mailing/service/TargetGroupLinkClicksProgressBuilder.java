/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.service;

import java.util.SequencedMap;

import com.agnitas.emm.core.birtstatistics.model.CustomerEventStats;
import com.agnitas.emm.core.birtstatistics.mailing.dao.LinkProgressStatsDao;
import com.agnitas.emm.core.birtstatistics.model.MailingProgressStatisticFilter;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public class TargetGroupLinkClicksProgressBuilder extends TargetGroupEventProgressBuilder {

    private final LinkProgressStatsDao progressStatsDao;

    protected TargetGroupLinkClicksProgressBuilder(
            LinkProgressStatsDao progressStatsDao,
            int linkId,
            int companyId,
            DateRange dateRange,
            SequencedMap<Integer, LightTarget> targets,
            boolean hourScale
    ) {
        super(linkId, companyId, dateRange, targets, hourScale);
        this.progressStatsDao = progressStatsDao;
    }

    @Override
    protected CustomerEventStats getMultiDeviceCounts(MailingProgressStatisticFilter filter) {
        return progressStatsDao.getMultiDeviceClicksStats(filter);
    }

    @Override
    protected CustomerEventStats getSingleDeviceCounts(MailingProgressStatisticFilter filter) {
        return progressStatsDao.getSingleDeviceClicksStats(filter);
    }

}
