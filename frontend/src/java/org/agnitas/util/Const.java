/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
	public static class Mailtype {
		public static final int
			TEXT = 0,
			HTML = 1,
			HTML_OFFLINE = 2;
	}
	
	public static class Mailing {
		public static final int
			TYPE_NORMAL = 0,
			TYPE_ACTIONBASED = 1,
			TYPE_DATEBASED = 2,
			TYPE_FOLLOWUP = 3,
			TYPE_INTERVAL = 4;
		public static final String
			TYPE_FOLLOWUP_NON_OPENER = "non-opener",
			TYPE_FOLLOWUP_OPENER = "opener",
			TYPE_FOLLOWUP_NON_CLICKER = "non-clicker",
			TYPE_FOLLOWUP_CLICKER = "clicker";
	}
	
	public static class Component {
		public static final String
			NAME_HEADER = "agnHead",
			NAME_TEXT = "agnText",
			NAME_HTML = "agnHtml",
			NAME_FAX = "agnFAX",
			NAME_PRINT = "agnPRINT",
			NAME_MMS = "agnMMS",
			NAME_SMS = "agnSMS",
			NAME_WHATSAPP = "agnWHATSAPP";
		
	}
	
	public static class Workstatus {
		public static final String
			MAILING_STATUS_TEST = "mailing.status.test";
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
