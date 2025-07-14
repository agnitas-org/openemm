/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.log4j;

import jakarta.servlet.jsp.tagext.TagSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractLog4JTag extends TagSupport {

	/** Serial version UID. */
	private static final long serialVersionUID = 8924977665204586449L;

	private String loggerName;
	private String message;
	private String optionalExceptionVariable;
	
	public void setLogger(final String loggerName) {
		this.loggerName = loggerName;
	}
	
	public void setMessage(final String message) {
		this.message = message;
	}
	
	public void setException(final String variableName) {
		this.optionalExceptionVariable = variableName;
	}
	
	@Override
	public int doStartTag() {
		final Logger logger = LogManager.getLogger(this.loggerName);
		
		if(optionalExceptionVariable != null) {
			final Throwable exception = (Throwable) pageContext.getAttribute(optionalExceptionVariable);
			
			logMessage(logger, message, exception);
		} else {
			logMessage(logger, message);
		}
		
		return SKIP_BODY;
	}
	
	protected abstract void logMessage(final Logger logger, final String msg);
	protected abstract void logMessage(final Logger logger, final String msg, final Throwable exception);
}
