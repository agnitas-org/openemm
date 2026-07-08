/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.dao;

import java.util.Map;
import java.util.Set;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.emm.core.birtstatistics.model.LinkClickStatisticData;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mobile.bean.DeviceClass;

public interface TrackableLinkStatisticsDao {

    Map<Integer, LinkClickStatisticData> getClickStatisticsPerLink(
            int mailingId,
            int companyId,
            String targetSql,
            Set<UserType> userTypes,
            DateRange timestamp,
            DeviceClass deviceClass,
            Integer customerId
    );

}
