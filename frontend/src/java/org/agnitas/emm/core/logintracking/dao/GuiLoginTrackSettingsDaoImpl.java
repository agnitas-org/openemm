/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.dao;

import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.logintracking.bean.LoginTrackSettings;
import org.springframework.beans.factory.annotation.Required;

public final class GuiLoginTrackSettingsDaoImpl implements LoginTrackSettingsDao {

	private ConfigService configService;
	
	@Override
	public final LoginTrackSettings readLoginTrackSettings(final int companyID) {
		final int blockTime = this.configService.getIntegerValue(ConfigValue.LoginTracking.WebuiIpBlockTimeSeconds, companyID);
		final int maxFails = this.configService.getIntegerValue(ConfigValue.LoginTracking.WebuiMaxFailedAttempts, companyID);

		// TODO Separate configuration for observation time and login time
		return new LoginTrackSettings(blockTime, maxFails, blockTime);
	}
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config Service is null");
	}
}
