/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.beans.TrackpointDef;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionOperationUpdateCustomerImpl implements EmmActionOperation<ActionOperationUpdateCustomerParameters> {
	
	private static final Logger logger = LogManager.getLogger(ActionOperationUpdateCustomerImpl.class);

	private final RecipientDao recipientDao;

	public ActionOperationUpdateCustomerImpl(RecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean execute(
			ActionOperationUpdateCustomerParameters op,
			Map<String, Object> params,
			EmmActionOperationErrors actionOperationErrors
	) {
		int companyID = op.getCompanyId();
		String columnName = op.getColumnName();
		int updateType = op.getUpdateType();
		String updateValue = op.getUpdateValue();
		
		if (params.get("customerID") == null) {
			actionOperationErrors.addErrorCode(ErrorCode.MISSING_CUSTOMER_ID);
			return false;
		}

		Integer customerID;
		try {
			customerID = parseIntegerParam(params.get("customerID"));
		} catch (Exception e) {
			actionOperationErrors.addErrorCode(ErrorCode.INVALID_CUSTOMER_ID);
			return false;
		}

		try {
			if (op.isUseTrack()) {
				Integer type = (Integer) params.get("trackingPointType");
				switch (type != null ? type : -1) {
					case TrackpointDef.TYPE_SIMPLE:
						updateValue = String.valueOf(params.get("trackingValue"));
						break;
					case TrackpointDef.TYPE_NUM:
						updateValue = String.valueOf(params.get("trackingValue"));
						break;
					case TrackpointDef.TYPE_ALPHA:
						updateValue = (String)params.get("trackingValue");
						break;
					default:
						actionOperationErrors.addErrorCode(ErrorCode.INVALID_TRACKING_POINT_TYPE);
						return false;
				}
			}

			recipientDao.updateForActionOperationUpdateCustomer(
					companyID,
					columnName,
					updateType,
					generateUpdateValue(params, updateValue),
					customerID
			);

			return true;
		} catch (Exception e) {
			actionOperationErrors.addErrorCode(ErrorCode.GENERAL_ERROR);
			return false;
		}
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.UPDATE_CUSTOMER;
    }

    /**
	 * Replace parameter references in updatestatements with the given values.
	 * If the value holds ##foo## it is replaced by the value of the
	 * requestparameter foo.
	 * 
	 * @param params The requestparameters sent to the form.
	 * @return The resulting value after replacing parameters.
	 */
	private String generateUpdateValue(Map<String, Object> params, String updateValue) {
		Pattern pattern = Pattern.compile("##[^#]+##");
		StringBuilder result = new StringBuilder(updateValue);
		String tmpStr;
		String tmpStr2;

		try {
			Matcher aMatcher = pattern.matcher(result);
			while (aMatcher.find()) {
				tmpStr = result.substring(aMatcher.start() + 2, aMatcher.end() - 2);
				tmpStr2 = "";
				if (params.get(tmpStr) != null) {
					tmpStr2 = params.get(tmpStr).toString();
				}
				result.replace(aMatcher.start(), aMatcher.end(), tmpStr2);
				aMatcher = pattern.matcher(result);
			}
		} catch (Exception e) {
			logger.error("Exception: {}", e.getMessage(), e);
		}

		return result.toString();
	}
}
