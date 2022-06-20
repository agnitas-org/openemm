/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.user.service.impl;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.user.service.UserSelfService;
import org.agnitas.emm.core.logintracking.bean.LoginData;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import org.springframework.beans.factory.annotation.Required;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

public class UserSelfServiceImpl implements UserSelfService {

    private LoginTrackService loginTrackService;

    @Override
    public List<LoginData> getLoginTrackingList(ComAdmin admin, int minPeriodDays, SimpleDateFormat loginTimeFormat) {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, -minPeriodDays);
        Date xDaysBefore = calendar.getTime();

        final Optional<LoginData> lastLoginOptional = loginTrackService.findLastSuccessfulLogin(admin.getUsername(), true);

        List<LoginData> list;

        if (lastLoginOptional.isPresent()) {
            final LoginData lastLogin = lastLoginOptional.get();

            if (xDaysBefore.before(lastLogin.getLoginTime())) {
                list = loginTrackService.listLoginAttemptsSince(admin.getUsername(), xDaysBefore);
            } else {
                list = loginTrackService.listLoginAttemptsSince(admin.getUsername(), lastLogin.getLoginTime());
            }
        } else {
            list = loginTrackService.listLoginAttemptsSince(admin.getUsername(), xDaysBefore);
        }

        list.forEach(loginData -> loginData.setLoginDate(loginTimeFormat.format(loginData.getLoginTime())));

        return list;
    }

    @Required
    public void setLoginTrackService(LoginTrackService loginTrackService) {
        this.loginTrackService = loginTrackService;
    }
}
