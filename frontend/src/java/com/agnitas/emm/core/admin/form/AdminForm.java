/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.form;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.impl.AdminPreferencesImpl;

public class AdminForm {

	/** The logger. */
    private static final transient Logger LOGGER = LogManager.getLogger(AdminForm.class);

    private String username;
    private String fullname;
    private String firstname;
    private String employeeID;
    private String companyName;
    private String email;
    private String statEmail;

    private int adminID = 0;
    private int companyID = 1;
    private String password;
    private String passwordConfirm;
    private Locale adminLocale = Locale.GERMANY;
    private String adminTimezone = "Europe/Berlin";
    private List<Integer> groupIDs = new ArrayList<>();
    private int layoutBaseId;
    private String initialCompanyName;
    private String adminPhone;
    private int gender = 2;
    private String title;
    private String language;
    private AdminPreferences adminPreferences = new AdminPreferencesImpl();
    private int altgId;
    private Set<Integer> altgIds = new HashSet<>();

	public String getUsername() {
        return StringUtils.trimToNull(username);
    }

    public void setUsername(String username) {
        this.username = StringUtils.trimToNull(username);
    }

    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public Locale getAdminLocale() {
        return adminLocale;
    }

    public void setAdminLocale(Locale adminLocale) {
        this.adminLocale = adminLocale;
        if (adminLocale != null) {
            language = adminLocale.toString();
        }
    }

    public String getAdminTimezone() {
        return adminTimezone;
    }

    public void setAdminTimezone(String adminTimezone) {
        this.adminTimezone = adminTimezone;
    }

    public List<Integer> getGroupIDs() {
        return groupIDs;
    }

    public void setGroupIDs(List<Integer> groupIDs) {
        this.groupIDs = groupIDs;
    }

    public String getStatEmail() {
        return StringUtils.trimToNull(StringUtils.lowerCase(statEmail));
    }

    public void setStatEmail(String statEmail) {
        this.statEmail = StringUtils.trimToNull(StringUtils.lowerCase(statEmail));
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return StringUtils.trimToNull(StringUtils.lowerCase(email));
    }

    public void setEmail(String email) {
        this.email = StringUtils.trimToNull(StringUtils.lowerCase(email));
    }

    public int getLayoutBaseId() {
        return layoutBaseId;
    }

    public void setLayoutBaseId(int layoutBaseId) {
        this.layoutBaseId = layoutBaseId;
    }

    public String getInitialCompanyName() {
        return initialCompanyName;
    }

    public void setInitialCompanyName(String initialCompanyName) {
        this.initialCompanyName = initialCompanyName;
    }

    public String getAdminPhone() {
        return adminPhone;
    }

    public void setAdminPhone(String adminPhone) {
        this.adminPhone = adminPhone;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        if (language != null) {
            int aPos = language.indexOf('_');
            String lang = language.substring(0, aPos);
            String country = language.substring(aPos + 1);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Got lang: " + lang + " Country: " + country);
            }
            adminLocale = new Locale(lang, country);
        }
    }

    public AdminPreferences getAdminPreferences() {
        return adminPreferences;
    }

    public void setAdminPreferences(AdminPreferences adminPreferences) {
        this.adminPreferences = adminPreferences;
    }

    public int getAltgId() {
        return altgId;
    }

    public void setAltgId(int altgId) {
        this.altgId = altgId;
    }

    public Set<Integer> getAltgIds() {
        return altgIds;
    }

    public void setAltgIds(Set<Integer> altgIds) {
        this.altgIds = altgIds;
    }
    
	public String getEmployeeID() {
		return employeeID;
	}

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }
}
