/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.log4j;

import org.apache.logging.log4j.Logger;

public class TraceLog4JTag extends AbstractLog4JTag {
	private static final long serialVersionUID = 3655027599744472736L;

	@Override
	protected final void logMessage(final Logger logger, final String msg) {
		logger.trace(msg);
	}

	@Override
	protected final void logMessage(final Logger logger, final String msg, final Throwable t) {
		logger.trace(msg, t);
	}
}
