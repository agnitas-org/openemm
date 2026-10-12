/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailing.validation;

public final class MailingConstraints {

    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 99;
    public static final int SUBJECT_MIN_LENGTH = 2;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
    public static final int REPLY_FULL_NAME_MAX_LENGTH = 255;
    public static final int SENDER_FULL_NAME_MAX_LENGTH = 255;
    public static final int SMS_ADDRESS_MIN_LENGTH = 3;

    public static final String FOLLOWUP_TYPE_REQUIRED_FOR_FOLLOWUP_MSG = "followupType must be specified for follow-up mailings";
    public static final String FOLLOWUP_TYPE_NOT_FOR_NON_FOLLOWUP_MSG = "followupType must not be set for non-follow-up mailings";
    public static final String FOLLOWUP_FOR_ID_REQUIRED_FOR_FOLLOWUP_MSG = "followupForId must be specified for follow-up mailings";
    public static final String FOLLOWUP_FOR_ID_NOT_FOR_NON_FOLLOWUP_MSG = "followupForId must not be set for non-follow-up mailings";

    private MailingConstraints() {}
}
