/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.target.service.impl;

import java.util.Objects;

import org.apache.log4j.Logger;

import com.agnitas.beans.ComTarget;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.service.RecipientTargetGroupMatcher;

import bsh.Interpreter;

@Deprecated // Replace by some implementation not using BeanShell
public final class BeanShellRecipientTargetGroupMatcher implements RecipientTargetGroupMatcher {
	
	private static final transient Logger logger = Logger.getLogger(BeanShellRecipientTargetGroupMatcher.class);

	private final int recipientCompanyID;
	private final EqlFacade eqlFacade;
	private final Interpreter interpreter;
	
	public BeanShellRecipientTargetGroupMatcher(final int companyID, final Interpreter interpreter, final EqlFacade facade) {
		this.recipientCompanyID = companyID;
		this.interpreter = Objects.requireNonNull(interpreter, "BeanShell interpreter is null");
		this.eqlFacade = Objects.requireNonNull(facade, "EqlFacade is null");
	}
	
	@Override
	public final boolean isInTargetGroup(final ComTarget target) {
		if(this.recipientCompanyID == target.getCompanyID()) {
			try {
				final String beanShellExpression = this.eqlFacade.convertEqlToBeanShellExpression(target);
		
				return (boolean) interpreter.eval(String.format("return (%s)", beanShellExpression));
			} catch(final Exception e) {
				logger.error(String.format("Error checking if recipient is in target group via BeanShell (company ID: %d, target ID: %d)", target.getCompanyID(), target.getId()), e);
				
				return false;
			}
		} else {
			logger.warn(String.format("Mismatching company ID of customer and target group (customer company ID: %d, target group company ID: %d)", this.recipientCompanyID, target.getCompanyID()));
			return false;
		}
	}
	
	@Override
	public final void setRecipientProperty(final String name, final Object value) throws Exception {
		interpreter.set(name, value);
	}

}
