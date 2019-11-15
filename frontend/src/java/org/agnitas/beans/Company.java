/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

public interface Company {
	
	public final static String STATUS_DELETED = "deleted";
	public final static String STATUS_ACTIVE = "active";
	public final static String STATUS_INACTIVE = "inactive";
	public final static String STATUS_TODELETE = "todelete";
	
    /**
     * Getter for property id.
     *
     * @return Value of property id.
     */
    int getId();

    /**
     * Getter for property mailtracking.
     *
     * @return Value of property mailtracking.
     */
    int getMailtracking();

    /**
     * Getter for property creatorID.
     *
     * @return Value of property creatorID.
     */
    int getCreatorID();

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
     * Getter for property rdirDomain.
     *
     * @return Value of property rdirDomain.
     */
    String getRdirDomain();

    /**
     * Getter for property mailloopDomain.
     *
     * @return Value of property mailloopDomain.
     */
    public String getMailloopDomain();

    /**
     * Getter for property status.
     *
     * @return Value of property ststus.
     */
    String getStatus();

    /**
     * Getter for property use_utf.
     *
     * @return Value of property use_utf.
     */
    int getUseUTF();
    
    /**
     * Returns maximum number of failed logins before IP address is blocked for a period.
     * 
     * @return maximum allowed number of failed logins
     */
    int getMaxLoginFails();
    
    /**
     * Returns time (in minutes) an IP address is blocked after a series of failed login attempts.
     * 
     * @return block time in seconds
     */
    int getLoginBlockTime();

    /**
     * Setter for property id.
     *
     * @param id New value of property id.
     */
    void setId(int id);

    /**
     * Setter for property mailtracking.
     *
     * @param mailtracking New value of property mailtracking.
     */
    void setMailtracking(int mailtracking);

    /**
     * Setter for property creatorID.
     *
     * @param creatorID New value of property creatorID.
     */
    void setCreatorID(int creatorID);

    /**
     * Setter for property shortname.
     *
     * @param name New value of property shortname.
     */
    void setShortname(String name);

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    void setDescription(String description);

    /**
     * Setter for property rdirDomain.
     *
     * @param rdirDomain New value of property rdirDomain.
     */
    void setRdirDomain(String rdirDomain);

    /**
     * Setter for property mailloopDomain.
     *
     * @param mailloopDomain New value of property mailloopDomain.
     */
    public void setMailloopDomain(String mailloopDomain);

    /**
     * Setter for property status.
     *
     * @param status New value of property status.
     */
    void setStatus(String status);

    /**
     * Setter for property use_utf.
     *
     * @param use_utf New value of property use_utf.
     */
    void setUseUTF(int useUTF);
    
    /**
     * Set maximum allowed number of failed login attempts before blocking IP address temporarily.
     * 
     * @param maximum maximum number of failed logins
     */
    void setMaxLoginFails(int maximum);

    /**
     * Set time to block an IP address after a series of failed login attempts.
     * 
     * @param time time to block in seconds
     */
    void setLoginBlockTime(int time);
    
    void setMinimumSupportedUIDVersion( Number minimumSupportedUIDVersion);
    Number getMinimumSupportedUIDVersion();

    int getMaxRecipients();

    void setMaxRecipients(int maxRecipients);
}
