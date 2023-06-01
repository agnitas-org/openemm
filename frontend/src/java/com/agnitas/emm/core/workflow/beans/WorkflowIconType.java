/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;


import com.agnitas.beans.IntEnum;

public enum WorkflowIconType implements IntEnum {
    START(Constants.START_VALUE, Constants.START_ID),
    STOP(Constants.STOP_VALUE, Constants.STOP_ID),
    DECISION(Constants.DECISION_VALUE, Constants.DECISION_ID),
    DEADLINE(Constants.DEADLINE_VALUE, Constants.DEADLINE_ID),
    PARAMETER(Constants.PARAMETER_VALUE, Constants.PARAMETER_ID),
    REPORT(Constants.REPORT_VALUE, Constants.REPORT_ID),
    RECIPIENT(Constants.RECIPIENT_VALUE, Constants.RECIPIENT_ID),
    ARCHIVE(Constants.ARCHIVE_VALUE, Constants.ARCHIVE_ID),
    FORM(Constants.FORM_VALUE, Constants.FORM_ID),
    MAILING(Constants.MAILING_VALUE, Constants.MAILING_ID),

	MAILING_MEDIATYPE_SMS(Constants.MAILING_MEDIATYPE_SMS_VALUE, Constants.MAILING_MEDIATYPE_SMS_ID),
	MAILING_MEDIATYPE_POST(Constants.MAILING_MEDIATYPE_POST_VALUE, Constants.MAILING_MEDIATYPE_POST_ID),
	ACTION_BASED_MAILING(Constants.ACTION_BASED_MAILING_VALUE, Constants.ACTION_BASED_MAILING_ID),
	DATE_BASED_MAILING(Constants.DATE_BASED_MAILING_VALUE, Constants.DATE_BASED_MAILING_ID),
	FOLLOWUP_MAILING(Constants.FOLLOWUP_MAILING_VALUE, Constants.FOLLOWUP_MAILING_ID),
	IMPORT(Constants.IMPORT_VALUE, Constants.IMPORT_ID),
	EXPORT(Constants.EXPORT_VALUE, Constants.EXPORT_ID);

    private final String name;
    private final int id;

    public static WorkflowIconType fromId(int id) {
        return IntEnum.fromId(WorkflowIconType.class, id);
    }

    public static WorkflowIconType fromId(int id, boolean safe) {
        return IntEnum.fromId(WorkflowIconType.class, id, safe);
    }

    WorkflowIconType(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    public static class Constants {
        public static final String START_VALUE     = "start";
        public static final String STOP_VALUE      = "stop";
        public static final String DECISION_VALUE  = "decision";
        public static final String DEADLINE_VALUE  = "deadline";
        public static final String PARAMETER_VALUE = "parameter";
		public static final String REPORT_VALUE    = "report";
		public static final String RECIPIENT_VALUE = "recipient";
        public static final String ARCHIVE_VALUE   = "archive";
        public static final String FORM_VALUE = "form";
        public static final String MAILING_VALUE   = "mailing";
/*		public static final String OWN_WORKFLOW_VALUE = "ownWorkflow";
		public static final String SC_BIRTHDAY_VALUE = "scBirthday";
		public static final String SC_DOI_VALUE = "scDOI";
		public static final String SC_ABTEST_VALUE = "scABTest";*/
		public static final String MAILING_MEDIATYPE_SMS_VALUE = "mailing_mediatype_sms";
		public static final String MAILING_MEDIATYPE_POST_VALUE = "mailing_mediatype_post";
		public static final String ACTION_BASED_MAILING_VALUE = "actionbased_mailing";
		public static final String DATE_BASED_MAILING_VALUE = "datebased_mailing";
		public static final String FOLLOWUP_MAILING_VALUE = "followup_mailing";
		public static final String IMPORT_VALUE = "import";
		public static final String EXPORT_VALUE = "export";

        public static final int START_ID        = 0;
        public static final int STOP_ID         = 1;
        public static final int DECISION_ID     = 2;
        public static final int DEADLINE_ID     = 3;
        public static final int PARAMETER_ID    = 4;
        public static final int REPORT_ID       = 5;
        public static final int RECIPIENT_ID    = 6;
        public static final int ARCHIVE_ID      = 7;
        public static final int FORM_ID         = 8;
        public static final int MAILING_ID      = 9;
/*		public static final int OWN_WORKFLOW_ID = 10;
		public static final int SC_BIRTHDAY_ID  = 11;
		public static final int SC_DOI_ID       = 12;
		public static final int SC_ABTEST_ID    = 13;*/
		public static final int ACTION_BASED_MAILING_ID    = 14;
		public static final int DATE_BASED_MAILING_ID    = 15;
		public static final int FOLLOWUP_MAILING_ID    = 16;
		public static final int IMPORT_ID    = 17;
		public static final int EXPORT_ID    = 18;
		public static final int MAILING_MEDIATYPE_SMS_ID = 19;
		public static final int MAILING_MEDIATYPE_POST_ID = 20;
    }

}
