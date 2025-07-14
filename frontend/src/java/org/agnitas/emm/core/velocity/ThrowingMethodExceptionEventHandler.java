/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.emm.core.velocity;

import org.agnitas.emm.core.velocity.emmapi.AccessToCompanyDeniedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.introspection.Info;

import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;

public final class ThrowingMethodExceptionEventHandler implements MethodExceptionEventHandler {
	
	private static final transient Logger LOGGER = LogManager.getLogger(ThrowingMethodExceptionEventHandler.class);

	@Override
	public Object methodException(final Context context, final Class<?> clazz, final String method, final Exception exception, final Info info) {		
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info(String.format("Method %s#%s threw exception in Velocity script", clazz.getCanonicalName(), method), exception);
		}
		
		if(context.containsKey(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME)) {
			if(exception instanceof AccessToCompanyDeniedException) {
				((EmmActionOperationErrors) context.get(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME)).addErrorCode(ErrorCode.ACCESS_TO_COMPANY_DENIED);
			}
		}
	
		return null;
	}

}
