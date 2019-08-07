/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mobile.service;

import com.agnitas.emm.core.mobile.bean.DeviceClass;

public interface ComDeviceService {
	int DEVICE_UNKNOWN = -1;
	int DEVICE_BLACKLISTED_NO_COUNT = -2;
	int DEVICE_BLACKLISTED_NO_SERVICE = -3;
	
	int getDeviceId(String userAgent);
	
	/**
	 * Get the deviceclass of an userAgent-String including eventually unknown-classes
	 * 
	 * @param userAgent
	 * @return
	 */
	DeviceClass getDeviceClassForLogging(String userAgent);
	
	/**
	 * Get the deviceclass of a deviceId including eventually unknown-classes
	 * 
	 * @param deviceId
	 * @return
	 */
	DeviceClass getDeviceClassForLogging(int deviceId);

	/**
	 * Get the deviceclass of an userAgent-String resolving unknown-classes to their base class for statistics
	 * 
	 * @param userAgent
	 * @return
	 */
	DeviceClass getDeviceClassForStatistics(String userAgent);

	/**
	 * Get the deviceclass of a deviceId resolving unknown-classes to their base class for statistics
	 * 
	 * @param deviceId
	 * @return
	 */
	DeviceClass getDeviceClassForStatistics(int deviceId);
}
