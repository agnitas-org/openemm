/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.loginmanager.dao.impl;

import java.util.Objects;

import com.agnitas.emm.core.loginmanager.dao.LoginTrackSettingsDao;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.loginmanager.bean.LoginTrackSettings;

public class GuiLoginTrackSettingsDaoImpl implements LoginTrackSettingsDao {

	private ConfigService configService;
	
	@Override
	public LoginTrackSettings readLoginTrackSettings(int companyID) {
		final int blockTime = this.configService.getIntegerValue(ConfigValue.LoginTracking.WebuiIpBlockTimeSeconds, companyID);
		final int maxFails = this.configService.getIntegerValue(ConfigValue.LoginTracking.WebuiMaxFailedAttempts, companyID);

		// TODO Separate configuration for observation time and login time
		return new LoginTrackSettings(blockTime, maxFails, blockTime);
	}
	
	public void setConfigService(ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config Service is null");
	}
}
