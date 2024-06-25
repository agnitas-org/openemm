/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.form;

import java.util.List;

import com.agnitas.beans.Company;
import com.agnitas.emm.core.commons.dto.DateRange;
import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.Mailinglist;
import org.agnitas.web.forms.PaginationForm;

public class AdminListForm extends PaginationForm {

    private List<Company> companies;
    private List<AdminGroup> adminGroups;
    private List<Mailinglist> mailinglists;
    private String searchFirstName;
    private String searchLastName;
    private String searchEmail;
    private String searchCompany;
    private Integer filterCompanyId;
    private Integer filterMailinglistId;
    private Integer filterAdminGroupId;
    private String filterLanguage;
    private String filterUsername;
    private DateRange filterCreationDate = new DateRange();
    private DateRange filterLastLoginDate = new DateRange();

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }

    public List<AdminGroup> getAdminGroups() {
        return adminGroups;
    }

    public void setAdminGroups(List<AdminGroup> adminGroups) {
        this.adminGroups = adminGroups;
    }

    public List<Mailinglist> getMailinglists() {
        return mailinglists;
    }

    public void setMailinglists(List<Mailinglist> mailinglists) {
        this.mailinglists = mailinglists;
    }

    public String getSearchFirstName() {
        return searchFirstName;
    }

    public void setSearchFirstName(String searchFirstName) {
        this.searchFirstName = searchFirstName;
    }

    public String getSearchLastName() {
        return searchLastName;
    }

    public void setSearchLastName(String searchLastName) {
        this.searchLastName = searchLastName;
    }

    public String getSearchEmail() {
        return searchEmail;
    }

    public void setSearchEmail(String searchEmail) {
        this.searchEmail = searchEmail;
    }

    public String getSearchCompany() {
        return searchCompany;
    }

    public void setSearchCompany(String searchCompany) {
        this.searchCompany = searchCompany;
    }

    public Integer getFilterCompanyId() {
        return filterCompanyId;
    }

    public void setFilterCompanyId(Integer filterCompanyId) {
        this.filterCompanyId = filterCompanyId;
    }

    public Integer getFilterMailinglistId() {
        return filterMailinglistId;
    }

    public void setFilterMailinglistId(Integer filterMailinglistId) {
        this.filterMailinglistId = filterMailinglistId;
    }

    public Integer getFilterAdminGroupId() {
        return filterAdminGroupId;
    }

    public void setFilterAdminGroupId(Integer filterAdminGroupId) {
        this.filterAdminGroupId = filterAdminGroupId;
    }

    public String getFilterLanguage() {
        return filterLanguage;
    }

    public void setFilterLanguage(String filterLanguage) {
        this.filterLanguage = filterLanguage;
    }

    public String getFilterUsername() {
        return filterUsername;
    }

    public void setFilterUsername(String filterUsername) {
        this.filterUsername = filterUsername;
    }

    public DateRange getFilterCreationDate() {
        return filterCreationDate;
    }

    public void setFilterCreationDate(DateRange filterCreationDate) {
        this.filterCreationDate = filterCreationDate;
    }

    public DateRange getFilterLastLoginDate() {
        return filterLastLoginDate;
    }

    public void setFilterLastLoginDate(DateRange filterLastLoginDate) {
        this.filterLastLoginDate = filterLastLoginDate;
    }
}
