/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

/**
 * This class defines several constant values and strings for application
 * wide use.
 */
public class Const {

	private Const() {

	}

	public static class Component {

		public static final String NAME_HEADER = "agnHead";
		public static final String NAME_TEXT = "agnText";
		public static final String NAME_PREHEADER = "agnPreheader";
		public static final String NAME_CLEARANCE = "agnClearance";
		public static final String NAME_HTML = "agnHtml";
		public static final String NAME_PRINT = "agnPRINT";
		public static final String NAME_SMS = "agnSMS";
		public static final String NAME_RCS = "agnRCS";
		public static final String NAME_RCSTITLE = "agnRCSTITLE";
		public static final String NAME_RCSSMS = "agnRCSSMS";

		private Component() {

		}
	}

	public static class DynName {

		public static final String DEFAULT_MAILING_HTML_DYNNAME = "HTML-Version";
		public static final String DEFAULT_MAILING_TEXT_DYNNAME = "Text";

		private DynName() {

		}
	}
	public static final class Mvc {

		public static final String MESSAGES_VIEW = "messages";
		public static final String DELETE_VIEW = "delete_modal";
		public static final String ERROR_MSG = "Error";
		public static final String CHANGES_SAVED_MSG = "default.changes_saved";
		public static final String NOTHING_SELECTED_MSG = "error.default.nothing_selected";
		public static final String SELECTION_DELETED_MSG = "default.selection.deleted";
		public static final String PERMISSION_DENIED_MSG = "error.permissionDenied";

		private Mvc() {
		}
	}
}
