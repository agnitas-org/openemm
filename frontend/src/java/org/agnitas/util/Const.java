/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

/**
 * This class defines several constant values and strings for application
 * wide use.
 */
public class Const {
	public static class Component {
		public static final String
			NAME_HEADER = "agnHead",
			NAME_TEXT = "agnText",
			NAME_HTML = "agnHtml",
			NAME_FAX = "agnFAX",
			NAME_PRINT = "agnPRINT",
			NAME_MMS = "agnMMS",
			NAME_SMS = "agnSMS";
	}
	
	public static class WorkflowDependencyType {
		public static final int
			ARCHIVE = 1,
			AUTO_EXPORT = 2,
			AUTO_IMPORT = 3,
			MAILING_DELIVERY = 4,
			MAILING_LINK = 5,
			MAILING_REFERENCE = 6,
			MAILINGLIST = 7,
			PROFILE_FIELD = 8,
			PROFILE_FIELD_HISTORY = 9,
			REPORT = 10,
			TARGET_GROUP = 11,
			USER_FORM = 12;
	}

	public static class OptimizedMailGeneration {
		public static final String
			ID_DAY = "day",
			ID_24H = "24h";
	}
}
