/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.beans;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.Company;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.supervisor.beans.Supervisor;

public interface ComAdmin {

    String getStatEmail();
    void setStatEmail(String statEmail);

    String getCompanyName();
    void setCompanyName(String companyName);

    String getEmail();
    void setEmail(String email);

    String getSecurePasswordHash();
    void setSecurePasswordHash(String securePasswordHash);

    String getFirstName();
    void setFirstName(String firstName);

    /**
     * Returns the supervisor used for login.
     * If no supervisor was used, {@code null} is returned.
     * 
     * @return supervisor from login or {@code null}
     */
    Supervisor getSupervisor();

    /**
     * Set supervisor from login. If no supervisor was used for login, null is returned.
     * 
     * @param supervisor {@link Supervisor}
     */
    void setSupervisor(Supervisor supervisor);

    /**
     * Returns, if the current user is supervisor.
     * 
     * @return {@code true} if current user is supervisor.
     */
    boolean isSupervisor();

    /**
     * Setter for the password, which will not be stored in the db directly, but will be encrypted by the PasswordEncryptor before storage
     * 
     * @param password
     */
    void setPasswordForStorage(String password);

    /**
     * Getter for the password. This method is meant to be called by the ComAdminDao only for storage.
     * The ComAdminDao empties this value after storage.
     */
    String getPasswordForStorage();

    Date getLastNewsDate();
    void setLastNewsDate(Date lastNewsDate);

    Date getLastMessageDate();
    void setLastMessageDate(Date lastMessageDate);

    int getGender();
	void setGender(int gender);

	String getTitle();
    void setTitle(String title);

    String getAdminCountry();
    int getAdminID();

    int getCompanyID();
    String getInitialCompanyName();

    String getAdminLang();
    String getAdminLangVariant();

    /**
     * Getter for property adminPermissions.
     * Admin permissions are the union of permission set for the admin and those
     * set for the group.
     *
     * @return Value of property adminPermissions.
     */
    Set<Permission> getAdminPermissions();
    void setAdminPermissions(Set<Permission> permissions);

    String getAdminTimezone();

    Company getCompany();

    String getFullname();

    List<AdminGroup> getGroups();
	List<Integer> getGroupIds();

    int getLayoutID();
    int getLayoutBaseID();

    String getShortname();
    void setShortname(String name);

    String getUsername();
    void setUsername(String username);

    Date getCreationDate();
    void setCreationDate(Date creationDate);

    Date getLastPasswordChange();
    void setLastPasswordChange(Date lastPasswordChange);

    void setAdminCountry(String adminCountry);

    void setAdminID(int adminID);

    void setAdminLang(String adminLang);

    void setAdminLangVariant(String adminLangVariant);

    void setAdminTimezone(String adminTimezone);

    void setCompany(Company company);

    void setFullname(String fullname);

    void setGroups(List<AdminGroup> groups);

    void setLayoutID(int layoutID);

    void setLayoutBaseID(int layoutBaseID);

    /**
     * Check if admin has any of the demanded permission rights.
	 * Group permissions are checked also.
     *
     * @param permissions the tokens of the permission to check.
     * @return true if Admin has the given permission, false otherwise.
     */
    boolean permissionAllowed(Permission... permissions);

    Locale getLocale();

    int getDefaultImportProfileID();
    void setDefaultImportProfileID(int defaultImportProfileID);
    
    public String getAdminPhone();
	public void setAdminPhone(String adminPhone);
    
	SimpleDateFormat getDateFormat();
	DateTimeFormatter getDateFormatter();
	SimpleDateFormat getTimeFormat();
	SimpleDateFormat getDateTimeFormat();
	DateTimeFormatter getDateTimeFormatter();
	SimpleDateFormat getDateTimeFormatWithSeconds();
	DateTimeFormatter getDateTimeFormatterWithSeconds();
	
	void setCompanyPermissions(Set<Permission> companyPermissions);
		
	public int getAccessLimitingTargetGroupID();
	
	public void setAccessLimitingTargetGroupID(final int id);
	
	public default boolean isAccessLimitedByTargetGroup() {
		return getAccessLimitingTargetGroupID() > 0;
	}
	
	boolean permissionAllowedByGroups(Permission... permission);
	
	public Date getLastLoginDate();
	public void setLastLoginDate(final Date date);
}
