/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;

/**
 * POJO containing one recipient entry.
 */
public interface RecipientHistory extends Comparable<RecipientHistory> {

    // TODO: replace with RecipientFields enum
    String USER_TYPE = "USER_TYPE";
    String USER_STATUS = "USER_STATUS";
    String USER_REMARK = "USER_REMARK";
    String EXIT_MAILING_ID = "EXIT_MAILING_ID";
    String EMAIL = "EMAIL";
    String FIRSTNAME = "FIRSTNAME";
    String LASTNAME = "LASTNAME";
    String GENDER = "GENDER";
    String MAILTYPE = "MAILTYPE";
    String TITLE = "TITLE";
    String DATASOURCE_ID = "DATASOURCE_ID";
    String MAILINGLIST_DELETED = "MAILINGLIST_DELETED";
    String CUSTOMER_BINDING_DELETED = "CUSTOMER_BINDING_DELETED";

    Date getChangeDate();

    void setChangeDate(Date changeDate);

    String getFieldName();

    void setFieldName(String fieldName);

    Object getNewValue();

    void setNewValue(Object newValue);

    Object getOldValue();

    void setOldValue(Object oldValue);

    Number getMediaType();

    void setMediaType(Number mediaType);

    String getMailingList();

    void setMailingList(String mailingList);
}
