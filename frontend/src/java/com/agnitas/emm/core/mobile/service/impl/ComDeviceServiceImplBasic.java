/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mobile.service.impl;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.mobile.service.ComDeviceService;
import org.apache.log4j.Logger;

public class ComDeviceServiceImplBasic implements ComDeviceService {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComDeviceServiceImplBasic.class);

	@Override
	public int getDeviceId(String userAgent) {
		logger.info("Default implementation - ignore userAgent parameter");
		return DEVICE_UNKNOWN;
	}

	@Override
	public DeviceClass getDeviceClassForLogging(int deviceID) {
        logger.info("Default implementation - ignore deviceID parameter");
		return DeviceClass.DESKTOP;
	}

	@Override
	public DeviceClass getDeviceClassForLogging(String userAgent) {
		return getDeviceClassForLogging(getDeviceId(userAgent));
	}

	@Override
	public DeviceClass getDeviceClassForStatistics(int deviceID) {
        logger.info("Default implementation - ignore deviceID parameter");
		return DeviceClass.DESKTOP;
	}

	@Override
	public DeviceClass getDeviceClassForStatistics(String userAgent) {
		return getDeviceClassForStatistics(getDeviceId(userAgent));
	}
}
