/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import org.apache.struts.action.ActionMessages;

public class GuiConstants {
	public static final String ACTIONMESSAGE_CONTAINER_MESSAGE = ActionMessages.GLOBAL_MESSAGE;
	public static final String ACTIONMESSAGE_CONTAINER_WARNING = "de.agnitas.GLOBAL_WARNING";
	public static final String ACTIONMESSAGE_CONTAINER_WARNING_PERMANENT = "de.agnitas.GLOBAL_WARNING_PERMANENT";

	// Constants to be set to affectedMailingsMessageType and affectedReportsMessageType (see messages-transitional.jsp)
	public static final String MESSAGE_TYPE_ALERT = "ALERT";
	public static final String MESSAGE_TYPE_WARNING = "WARNING";
	public static final String MESSAGE_TYPE_WARNING_PERMANENT = "WARNING_PERMANENT";
}
