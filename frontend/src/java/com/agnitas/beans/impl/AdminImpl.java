/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.text.SimpleDateFormat;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.agnitas.beans.AdminGroup;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Company;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.supervisor.beans.Supervisor;

public class AdminImpl implements Admin {
	protected Company company = new CompanyImpl();
	protected int adminID;
	protected int layoutID;
	protected int layoutBaseID;
	protected int gender = 2;
	protected String title;
	protected String adminCountry;
	protected String shortname;
	protected String username;
	protected String fullname;
	protected String adminLang;
	protected String adminLangVariant = "";
	protected String adminTimezone;
	protected Date creationDate;
	protected Date lastPasswordChange = new Date();
	private int defaultImportProfileID;
	protected List<AdminGroup> groups = new ArrayList<>();
	protected Set<Permission> adminPermissions;
	protected Set<Permission> companyPermissions;
	protected String statEmail;
    protected String companyName;
    protected String email;
    protected String securePasswordHash;
    protected String firstName;
	protected Date lastNewsDate;
	protected Date lastMessageDate;
	protected String adminPhone;
	private int accessLimitingTargetGroupID;
    private Set<Integer> altgIds = new HashSet<>();
	private Date lastLoginDate;
	private boolean restful = false;
	protected String employeeID;

	/**
     * This member is not stored in db, but encrypted into securePasswordHash by PasswordEncryptor
     */
    protected String passwordForStorage = null;

    /**
     * User with supervisor access.
     * This data will not be stored in db, but is used as a session marker for supervisor in action
     */
    protected Supervisor supervisor;

	@Override
	public void setCompany(Company company) {
		this.company = company;
	}

	@Override
	public void setShortname(String name) {
		shortname = name;
	}

	@Override
	public void setAdminID(int adminID) {
		this.adminID = adminID;
	}

	@Override
	public void setUsername(String username) {
		this.username = StringUtils.trimToNull(username);
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public void setLastPasswordChange(Date lastPasswordChange) {
		this.lastPasswordChange = lastPasswordChange;
	}

	@Override
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	@Override
	public void setAdminLang(String adminLang) {
		this.adminLang = adminLang;
	}

	@Override
	public void setAdminLangVariant(String adminLangVariant) {
		this.adminLangVariant = adminLangVariant;
	}

	@Override
	public void setAdminTimezone(String adminTimezone) {
		this.adminTimezone = adminTimezone;
	}

	@Override
	public void setLayoutID(int layoutID) {
		this.layoutID = layoutID;
	}

	@Override
	public void setLayoutBaseID(int layoutBaseID) {
		this.layoutBaseID = layoutBaseID;
	}

	@Override
	public void setAdminCountry(String adminCountry) {
		this.adminCountry = adminCountry;
	}

	@Override
	public Company getCompany() {
		return company;
	}

	@Override
	public String getShortname() {
		return shortname;
	}

	@Override
	public int getAdminID() {
		return adminID;
	}

	@Override
	public int getCompanyID() {
		return company.getId();
	}

    @Override
    public String getInitialCompanyName() {
        return company.getShortname();
    }

    @Override
	public String getUsername() {
    	return StringUtils.trimToNull(username);
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public Date getLastPasswordChange() {
		return lastPasswordChange;
	}

	@Override
	public String getFullname() {
		return fullname;
	}

	@Override
	public String getAdminLang() {
		return adminLang;
	}

	@Override
	public String getAdminLangVariant() {
		return adminLangVariant;
	}

	@Override
	public String getAdminTimezone() {
		return adminTimezone;
	}

	@Override
	public int getLayoutID() {
		return layoutID;
	}

	@Override
	public int getLayoutBaseID() {
		return layoutBaseID;
	}

	@Override
	public String getAdminCountry() {
		return adminCountry;
	}

    /**
	 * Getter for property groupID.
	 * 
	 * @return Value of property groupID.
	 */
	@Override
	public List<AdminGroup> getGroups() {
		return groups;
	}
	
	@Override
	public List<Integer> getGroupIds() {
		List<Integer> groupIds = new ArrayList<>();
	    if (getGroups() != null && !getGroups().isEmpty()) {
	    	for (AdminGroup group : getGroups()) {
	    		groupIds.add(group.getGroupID());
	    	}
	    }
	    return groupIds;
	}

	/**
	 * Setter for property groupID.
	 * 
	 * @param group
	 */
	@Override
	public void setGroups(List<AdminGroup> groups) {
		this.groups = groups;
	}

	@Override
	public Set<Permission> getAdminPermissions() {
		return adminPermissions;
	}

	@Override
	public void setAdminPermissions(Set<Permission> permissions) {
		this.adminPermissions = permissions;
	}

	@Override
	public Locale getLocale() {
		if (StringUtils.isBlank(adminLang)) {
			return null;
		} else {
			return new Locale(adminLang, adminCountry);
		}
	}

	@Override
	public int getDefaultImportProfileID() {
		return defaultImportProfileID;
	}

	@Override
	public void setDefaultImportProfileID(int defaultImportProfileID) {
		this.defaultImportProfileID = defaultImportProfileID;
	}

    @Override
	public String getStatEmail() {
		return StringUtils.trimToNull(StringUtils.lowerCase(statEmail));
	}

    @Override
	public void setStatEmail(String statEmail) {
		this.statEmail = StringUtils.trimToNull(StringUtils.lowerCase(statEmail));
	}

    @Override
    public String getCompanyName() {
        return companyName;
    }

    @Override
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String getEmail() {
    	return StringUtils.trimToNull(StringUtils.lowerCase(email));
    }

    @Override
    public void setEmail(String email) {
    	this.email = StringUtils.trimToNull(StringUtils.lowerCase(email));
    }

	@Override
	public String getSecurePasswordHash() {
		return securePasswordHash;
	}

	@Override
	public void setSecurePasswordHash(String securePasswordHash) {
		this.securePasswordHash = securePasswordHash;
	}

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    @Override
    public Supervisor getSupervisor() {
    	return supervisor;
    }

    @Override
    public void setSupervisor( Supervisor supervisor) {
    	this.supervisor = supervisor;
    }
    
    @Override
    public boolean isSupervisor() {
    	return supervisor != null;
    }

	@Override
	public String getPasswordForStorage() {
		return passwordForStorage;
	}

	@Override
	public void setPasswordForStorage(String passwordForStorage) {
		this.passwordForStorage = passwordForStorage;
	}

	@Override
	public Date getLastNewsDate() {
		return lastNewsDate;
	}

	@Override
	public void setLastNewsDate(Date lastNewsDate) {
		this.lastNewsDate = lastNewsDate;
	}

	@Override
	public Date getLastMessageDate() {
		return lastMessageDate;
	}

	@Override
	public void setLastMessageDate(Date lastMessageDate) {
		this.lastMessageDate = lastMessageDate;
	}

	@Override
	public int getGender() {
		return gender;
	}

	@Override
	public void setGender(int gender) {
		this.gender = gender;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getAdminPhone() {
		return adminPhone;
	}

	@Override
	public void setAdminPhone(String adminPhone) {
		this.adminPhone = adminPhone;
	}

	/**
	 * Check if admin has any of the demanded permission rights.
	 * Group permissions are checked also.
	 */
	@Override
	public boolean permissionAllowed(Permission... permissions) {
		if (Permission.permissionAllowed(adminPermissions, companyPermissions, permissions)){
			return true;
		} else {
			return permissionAllowedByGroups(permissions);
		}
	}
	
	@Override
	public String toString()  {
		return username + " (ID: " + adminID + ")";
	}
	
	@Override
	public SimpleDateFormat getDateTimeFormatWithSeconds() {
		SimpleDateFormat dateTimeFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, getLocale());
		dateTimeFormat.applyPattern(dateTimeFormat.toPattern().replaceFirst("y+", "yyyy") + " HH:mm:ss");
		dateTimeFormat.setTimeZone(TimeZone.getTimeZone(getAdminTimezone()));
		dateTimeFormat.setLenient(false);
		return dateTimeFormat;
	}
	
	@Override
	public SimpleDateFormat getDateTimeFormat() {
		SimpleDateFormat dateTimeFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, getLocale());
		dateTimeFormat.applyPattern(dateTimeFormat.toPattern().replaceFirst("y+", "yyyy") + " HH:mm");
		dateTimeFormat.setTimeZone(TimeZone.getTimeZone(getAdminTimezone()));
		dateTimeFormat.setLenient(false);
		return dateTimeFormat;
	}

	@Override
	public DateTimeFormatter getDateTimeFormatterWithSeconds() {
		String dateTimeFormatPattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, null, IsoChronology.INSTANCE, getLocale());
		dateTimeFormatPattern = dateTimeFormatPattern.replaceFirst("y+", "yyyy").replaceFirst(", ", " ") + " HH:mm:ss";

		return getDateTimeFormatterByPattern(dateTimeFormatPattern)
				.withZone(TimeZone.getTimeZone(getAdminTimezone()).toZoneId())
				.withResolverStyle(ResolverStyle.STRICT);
	}

	@Override
	public DateTimeFormatter getDateTimeFormatter() {
		String dateTimeFormatPattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, null, IsoChronology.INSTANCE, getLocale());
		dateTimeFormatPattern = dateTimeFormatPattern.replaceFirst("y+", "yyyy").replaceFirst(", ", " ") + " HH:mm";

		return getDateTimeFormatterByPattern(dateTimeFormatPattern)
				.withZone(TimeZone.getTimeZone(getAdminTimezone()).toZoneId())
				.withResolverStyle(ResolverStyle.STRICT);
	}
	
	@Override
	public SimpleDateFormat getDateFormat() {
		SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, getLocale());
		dateFormat.applyPattern(dateFormat.toPattern().replaceFirst("y+", "yyyy"));
		dateFormat.setLenient(false);
		return dateFormat;
	}

	@Override
	public DateTimeFormatter getDateFormatter() {
		String dateFormatPattern = ((SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, getLocale())).toPattern().replaceFirst("y+", "yyyy");
		return getDateTimeFormatterByPattern(dateFormatPattern)
				.withResolverStyle(ResolverStyle.STRICT);
	}

	private DateTimeFormatter getDateTimeFormatterByPattern(String pattern) {
		return AgnUtils.getDateTimeFormatterByPattern(pattern, getLocale(), true);
	}
	
	@Override
	public SimpleDateFormat getTimeFormat() {
		SimpleDateFormat timeFormat = (SimpleDateFormat) SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, getLocale());
		timeFormat.setTimeZone(TimeZone.getTimeZone(getAdminTimezone()));
		timeFormat.setLenient(false);
		return timeFormat;
	}

	@Override
	public void setCompanyPermissions(Set<Permission> companyPermissions) {
		this.companyPermissions = companyPermissions;
	}

	@Override
	public int getAccessLimitingTargetGroupID() {
		return accessLimitingTargetGroupID;
	}

	@Override
	public void setAccessLimitingTargetGroupID(int accessLimitingTargetGroupID) {
		this.accessLimitingTargetGroupID = accessLimitingTargetGroupID;
	}

    @Override
	public Set<Integer> getAltgIds() {
	    return altgIds;
    }

    @Override
	public void setAltgIds(Set<Integer> altgIds) {
        this.altgIds = altgIds;
    }
	
	@Override
	public boolean permissionAllowedByGroups(Permission... permission) {
		if (groups != null && !groups.isEmpty() ) {
			for (AdminGroup adminGroup : getGroups()) {
				if (adminGroup.permissionAllowed(permission)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}

	@Override
	public final Date getLastLoginDate() {
		return lastLoginDate;
	}

	@Override
	public final void setLastLoginDate(final Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	@Override
    public boolean isRestful() {
		return restful;
	}
	
	@Override
	public void setRestful(boolean restful) {
		this.restful = restful;
	}
	
	@Override
	public String getEmployeeID() {
		return employeeID;
	}
	
	@Override
	public void setEmployeeID(String employeeID) {
		this.employeeID = employeeID;
	}

	@Override
	public String getFullUsername() {
		if (isSupervisor()) {
			return username + " (" + supervisor.getSupervisorName() + ")";
		}

		return username;
	}
}
