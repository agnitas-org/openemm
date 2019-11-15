/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.agnitas.beans.ComTrackpointDef;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;

public class ActionOperationUpdateCustomerImpl implements EmmActionOperation {
	
	/**
	 * The logger.
	 */
	private static final Logger logger = Logger.getLogger(ActionOperationUpdateCustomerImpl.class);

	private ComRecipientDao recipientDao;
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) {
		ActionOperationUpdateCustomerParameters op =(ActionOperationUpdateCustomerParameters) operation;
		int companyID = op.getCompanyId();
		String columnName = op.getColumnName();
		int updateType = op.getUpdateType();
		String updateValue = op.getUpdateValue();
		
		if (params.get("customerID") == null) {
			return false;
		} else {
			int customerID = (Integer) params.get("customerID");
			try {
				if (op.isUseTrack()) {
					switch ((Integer)params.get("trackingPointType")) {
						case ComTrackpointDef.TYPE_SIMPLE:
							updateValue = String.valueOf(params.get("trackingValue"));
							break;
						case ComTrackpointDef.TYPE_NUM:
							updateValue = String.valueOf(params.get("trackingValue"));
							break;
						case ComTrackpointDef.TYPE_ALPHA:
							updateValue = (String)params.get("trackingValue");
							break;
						default:
							throw new RuntimeException("Invalid tracking point type");
					}
				}
				
				recipientDao.updateForActionOperationUpdateCustomer(companyID, columnName, updateType, generateUpdateValue(params, updateValue), customerID);
				
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}

	/**
	 * Replace parameter references in updatestatements with the given values.
	 * If the value holds ##foo## it is replaced by the value of the
	 * requestparameter foo.
	 * 
	 * @param params
	 *            The requestparameters sent to the form.
	 * @return The resulting value after replacing parameters.
	 */
	private String generateUpdateValue(Map<String, Object> params, String updateValue) {
		Matcher aMatcher = null;
		Pattern aRegExp = Pattern.compile("##[^#]+##");
		StringBuffer aBuf = new StringBuffer(updateValue);
		String tmpString = null;
		String tmpString2 = null;

		try {
			aMatcher = aRegExp.matcher(aBuf);
			while (aMatcher.find()) {
				tmpString = aBuf.toString().substring(aMatcher.start() + 2, aMatcher.end() - 2);
				tmpString2 = "";
				if (params.get(tmpString) != null) {
					tmpString2 = params.get(tmpString).toString();
				}
				aBuf.replace(aMatcher.start(), aMatcher.end(), tmpString2);
				aMatcher = aRegExp.matcher(aBuf);
			}
		} catch (Exception e) {
			logger.error("Exception: " + e, e);
		}

		return aBuf.toString();
	}
	
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}
}
