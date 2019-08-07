/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mobile.dao;

import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.emm.core.mobile.bean.ComDeviceHistoryData;

public interface ComDeviceHistoryDao {
	// use this method, if you just want an entry with the actual timestamp.
	public void writeElement(int device_id, long customer_id, long mailing_id, @VelocityCheck int company_id);
	// use this method, if you want a timestamp other than now.
	public void writeElement(int device_id, long customer_id, long mailing_id, @VelocityCheck int company_id, Date timestamp);
	public void writeElement(ComDeviceHistoryData deviceHistoryData);
	// this method checks, if the the cust_xyz_devicehistory_tbl exists. It returns true, if it exists.
	public boolean isTableExisting(@VelocityCheck int companyID);
	// this method checks, if the the cust_xyz_devicehistory_seq exists. It returns true, if it exists.
	public boolean isSequenceExisting(@VelocityCheck int companyID);
	// this method creates the device-history table for the given company_id
	public void createDeviceHistoryTable(@VelocityCheck int companyID) throws Exception;
	// this method creates the device-history sequence for the given company_id
	public void createDeviceHistorySequence(@VelocityCheck int companyID) throws Exception;

}


