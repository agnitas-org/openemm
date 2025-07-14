/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.user.form;

import com.agnitas.emm.core.admin.enums.UiLayoutType;
import com.agnitas.web.forms.PaginationForm;

import java.util.Locale;

public class UserSelfForm extends PaginationForm {

    private int id;
    private int gender;
    private String username;
    private String password;
    private String passwordConfirm;
    private int companyID;
    private String fullname;
    private Locale adminLocale;
    private String adminTimezone;
    private String[] groupIDs = new String[]{};
    private String statEmail;
    private String companyName;
    private String email;
    private int layoutBaseId;
    private UiLayoutType uiLayoutType;
    private String initialCompanyName;
    private String firstname;
    private String employeeID;
    private int mailingContentView;
    private int dashboardMailingsView;
    private int mailingSettingsView;
    private int livePreviewPosition;
    private int statisticLoadType;
    private String title;
    private String currentPassword;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Locale getAdminLocale() {
        return adminLocale;
    }

    public void setAdminLocale(Locale adminLocale) {
        this.adminLocale = adminLocale;
    }

    public String getAdminTimezone() {
        return adminTimezone;
    }

    public void setAdminTimezone(String adminTimezone) {
        this.adminTimezone = adminTimezone;
    }

    public String[] getGroupIDs() {
        return groupIDs;
    }

    public void setGroupIDs(String[] groupIDs) {
        this.groupIDs = groupIDs;
    }

    public String getStatEmail() {
        return statEmail;
    }

    public void setStatEmail(String statEmail) {
        this.statEmail = statEmail;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public int getMailingContentView() {
        return mailingContentView;
    }

    public void setMailingContentView(int mailingContentView) {
        this.mailingContentView = mailingContentView;
    }

    public int getDashboardMailingsView() {
        return dashboardMailingsView;
    }

    public void setDashboardMailingsView(int dashboardMailingsView) {
        this.dashboardMailingsView = dashboardMailingsView;
    }

    public int getMailingSettingsView() {
        return mailingSettingsView;
    }

    public void setMailingSettingsView(int mailingSettingsView) {
        this.mailingSettingsView = mailingSettingsView;
    }

    public int getLivePreviewPosition() {
        return livePreviewPosition;
    }

    public void setLivePreviewPosition(int livePreviewPosition) {
        this.livePreviewPosition = livePreviewPosition;
    }

    public int getStatisticLoadType() {
        return statisticLoadType;
    }

    public void setStatisticLoadType(int statisticLoadType) {
        this.statisticLoadType = statisticLoadType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UiLayoutType getUiLayoutType() {
        return uiLayoutType;
    }

    public void setUiLayoutType(UiLayoutType uiLayoutType) {
        this.uiLayoutType = uiLayoutType;
    }
}
