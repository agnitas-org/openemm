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
    public void setId(int id);

    /**
     * Setter for property companyID.
     * 
     * @param company New value of property companyID.
     */
    public void setCompanyID( @VelocityCheck int company);

    /**
     * Setter for property charset.
     * 
     * @param charset New value of property charset.
     */
    public void setCharset(String charset);

    /**
     * Setter for property columns.
     * 
     * @param columns New value of property columns.
     */
    public void setColumns(String columns);

    /**
     * Setter for property shortname.
     * 
     * @param shortname New value of property shortname.
     */
    public void setShortname(String shortname);

    /**
     * Setter for property description.
     * 
     * @param description New value of property description.
     */
    public void setDescription(String description);

    /**
     * Setter for property mailinglists.
     * 
     * @param mailinglists New value of property mailinglists.
     */
    public void setMailinglists(String mailinglists);

    /**
     * Setter for property mailinglistID.
     * 
     * @param mailinglistID New value of property mailinglistID.
     */
    public void setMailinglistID(int mailinglistID);

    /**
     * Setter for property delimiter.
     * 
     * @param delimiter New value of property delimiter.
     */
    public void setDelimiter(String delimiter);

    /**
     * Setter for property separator.
     * 
     * @param separator New value of property separator.
     */
    public void setSeparator(String separator);

    /**
     * Setter for property targetID.
     * 
     * @param targetID New value of property targetID.
     */
    public void setTargetID(int targetID);

    /**
     * Setter for property userType.
     * 
     * @param userType New value of property userType.
     */
    public void setUserType(String userType);

    /**
     * Setter for property userStatus.
     * 
     * @param userStatus New value of property userStatus.
     */
    public void setUserStatus(int userStatus);

    /**
     * Setter for property deleted.
     * 
     * @param deleted New value of property deleted.
     */
    public void setDeleted(int deleted);

    /**
     * Getter for property id.
     *
     * @return Value of property id.
     */
    public int getId();

    /**
     * Getter for property companyID.
     *
     * @return Value of property companyID.
     */
    public int getCompanyID();
    
    /**
     * Getter for property charset.
     *
     * @return Value of property charset.
     */
    public String getCharset();
    
    /**
     * Getter for property columns.
     *
     * @return Value of property columns.
     */
    public String getColumns();
    
    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    public String getShortname();
    
    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription();
    
    /**
     * Getter for property mailinglists.
     *
     * @return Value of property mailinglists.
     */
    public String getMailinglists();
    
    /**
     * Getter for property mailinglistID.
     *
     * @return Value of property mailinglistID.
     */
    public int getMailinglistID();

    /**
     * Getter for property delimiter.
     *
     * @return Value of property delimiter.
     */
    public String getDelimiter();

    /**
     * Getter for property separator.
     *
     * @return Value of property separator.
     */
    public String getSeparator();

    /**
     * Getter for property targetID.
     *
     * @return Value of property targetID.
     */
    public int getTargetID();

    /**
     * Getter for property userType.
     *
     * @return Value of property userType.
     */
    public String getUserType();

    /**
     * Getter for property userStatus.
     *
     * @return Value of property userStatus.
     */
    public int getUserStatus();

    /**
     * Getter for property deleted.
     *
     * @return Value of property deleted.
     */
    public int getDeleted();

    public Date getTimestampStart();

    public void setTimestampStart(Date timestampStart);

    public Date getTimestampEnd();

    public void setTimestampEnd(Date timestampEnd);

    public int getTimestampLastDays();

    public void setTimestampLastDays(int timestampLastDays);

    public Date getCreationDateStart();

    public void setCreationDateStart(Date creationDateStart);

    public Date getCreationDateEnd();

    public void setCreationDateEnd(Date creationDateEnd);

    public int getCreationDateLastDays();

    public void setCreationDateLastDays(int creationDateLastDays);

    public Date getMailinglistBindStart();

    public void setMailinglistBindStart(Date mailinglistBindStart);

    public Date getMailinglistBindEnd();

    public void setMailinglistBindEnd(Date mailinglistBindEnd);

    public int getMailinglistBindLastDays();
    
    public void setMailinglistBindLastDays(int mailinglistBindLastDays);

	boolean isAlwaysQuote();

	void setAlwaysQuote(boolean alwaysQuote);
}
