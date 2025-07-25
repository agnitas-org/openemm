/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.user.service;

import com.agnitas.beans.Admin;
import org.agnitas.emm.core.logintracking.bean.LoginData;

import java.text.SimpleDateFormat;
import java.util.List;

public interface UserSelfService {

    /** Default value for minimum period of days for login data. */
    int DEFAULT_LOGIN_MIN_PERIOD_DAYS = 14;

    List<LoginData> getLoginTrackingList(Admin admin, int minPeriodDays, SimpleDateFormat loginTimeFormat);
}
