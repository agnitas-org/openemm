/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ExportPredef {
    /**
     * Setter for property id.
     * 
     * @param id New value of property id.
     */
    void setId(int id);

    /**
     * Setter for property companyID.
     * 
     * @param company New value of property companyID.
     */
    void setCompanyID(@VelocityCheck int company);

    /**
     * Setter for property charset.
     * 
     * @param charset New value of property charset.
     */
    void setCharset(String charset);

    /**
     * Setter for property columns.
     * 
     * @param columns New value of property columns.
     */
    void setColumns(String columns);

    /**
     * Setter for property shortname.
     * 
     * @param shortname New value of property shortname.
     */
    void setShortname(String shortname);

    /**
     * Setter for property description.
     * 
     * @param description New value of property description.
     */
    void setDescription(String description);

    /**
     * Setter for property mailinglists.
     * 
     * @param mailinglists New value of property mailinglists.
     */
    void setMailinglists(String mailinglists);

    /**
     * Setter for property mailinglistID.
     * 
     * @param mailinglistID New value of property mailinglistID.
     */
    void setMailinglistID(int mailinglistID);

    /**
     * Setter for property delimiter.
     * 
     * @param delimiter New value of property delimiter.
     */
    void setDelimiter(String delimiter);

    /**
     * Setter for property separator.
     * 
     * @param separator New value of property separator.
     */
    void setSeparator(String separator);

    /**
     * Setter for property targetID.
     * 
     * @param targetID New value of property targetID.
     */
    void setTargetID(int targetID);

    /**
     * Setter for property userType.
     * 
     * @param userType New value of property userType.
     */
    void setUserType(String userType);

    /**
     * Setter for property userStatus.
     * 
     * @param userStatus New value of property userStatus.
     */
    void setUserStatus(int userStatus);

    /**
     * Setter for property deleted.
     * 
     * @param deleted New value of property deleted.
     */
    void setDeleted(int deleted);

    /**
     * Getter for property id.
     *
     * @return Value of property id.
     */
    int getId();

    /**
     * Getter for property companyID.
     *
     * @return Value of property companyID.
     */
    int getCompanyID();
    
    /**
     * Getter for property charset.
     *
     * @return Value of property charset.
     */
    String getCharset();
    
    /**
     * Getter for property columns.
     *
     * @return Value of property columns.
     */
    String getColumns();
    
    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    String getShortname();
    
    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    String getDescription();
    
    /**
     * Getter for property mailinglists.
     *
     * @return Value of property mailinglists.
     */
    String getMailinglists();
    
    /**
     * Getter for property mailinglistID.
     *
     * @return Value of property mailinglistID.
     */
    int getMailinglistID();

    /**
     * Getter for property delimiter.
     *
     * @return Value of property delimiter.
     */
    String getDelimiter();

    /**
     * Getter for property separator.
     *
     * @return Value of property separator.
     */
    String getSeparator();

    /**
     * Getter for property targetID.
     *
     * @return Value of property targetID.
     */
    int getTargetID();

    /**
     * Getter for property userType.
     *
     * @return Value of property userType.
     */
    String getUserType();

    /**
     * Getter for property userStatus.
     *
     * @return Value of property userStatus.
     */
    int getUserStatus();

    /**
     * Getter for property deleted.
     *
     * @return Value of property deleted.
     */
    int getDeleted();

    Date getTimestampStart();

    void setTimestampStart(Date timestampStart);

    Date getTimestampEnd();

    void setTimestampEnd(Date timestampEnd);

    int getTimestampLastDays();

    void setTimestampLastDays(int timestampLastDays);

    Date getCreationDateStart();

    void setCreationDateStart(Date creationDateStart);

    Date getCreationDateEnd();

    void setCreationDateEnd(Date creationDateEnd);

    int getCreationDateLastDays();

    void setCreationDateLastDays(int creationDateLastDays);

    Date getMailinglistBindStart();

    void setMailinglistBindStart(Date mailinglistBindStart);

    Date getMailinglistBindEnd();

    void setMailinglistBindEnd(Date mailinglistBindEnd);

    int getMailinglistBindLastDays();
    
    void setMailinglistBindLastDays(int mailinglistBindLastDays);

	boolean isAlwaysQuote();

	void setAlwaysQuote(boolean alwaysQuote);

	int getDateFormat();

	void setDateFormat(int dateFormat);

	int getDateTimeFormat();

	void setDateTimeFormat(int dateTimeFormat);

	String getTimezone();

	void setTimezone(String timezone);

	String getDecimalSeparator();

	void setDecimalSeparator(String decimalSeparator);

	boolean isTimestampIncludeCurrentDay();

	void setTimestampIncludeCurrentDay(boolean timestampIncludeCurrentDay);

	boolean isCreationDateIncludeCurrentDay();

	void setCreationDateIncludeCurrentDay(boolean creationDateIncludeCurrentDay);

	boolean isMailinglistBindIncludeCurrentDay();

	void setMailinglistBindIncludeCurrentDay(boolean mailinglistBindIncludeCurrentDay);

	boolean isTimeLimitsLinkedByAnd();

	void setTimeLimitsLinkedByAnd(boolean timeLimitsLinkedByAnd);
}
