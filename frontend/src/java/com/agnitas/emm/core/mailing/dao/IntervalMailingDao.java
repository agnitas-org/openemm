/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dao;

import java.util.Date;
import java.util.List;

public interface IntervalMailingDao {

    String getInterval(int mailingId);

    void saveIntervalSettings(String interval, Date nextStart, int mailingId);

    void updateNextStart(Date nextStart, int mailingId);

    void insertError(String message, int mailingId);

    List<Integer> getMailingIdsForIntervalSend(int companyId);
}
