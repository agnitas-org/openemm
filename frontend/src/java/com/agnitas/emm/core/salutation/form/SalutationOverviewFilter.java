/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.salutation.form;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.agnitas.web.forms.PaginationForm;

public class SalutationOverviewFilter extends PaginationForm {

    private int companyId;
    private Integer salutationId;
    private String name; // description column in db
    private String gender0; // male
    private String gender1; // female
    private String gender2; // unknown/diverse
    private String gender4; // practice
    private String gender5; // company

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public Integer getSalutationId() {
        return salutationId;
    }

    public void setSalutationId(Integer salutationId) {
        this.salutationId = salutationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender0() {
        return gender0;
    }

    public void setGender0(String gender0) {
        this.gender0 = gender0;
    }

    public String getGender1() {
        return gender1;
    }

    public void setGender1(String gender1) {
        this.gender1 = gender1;
    }

    public String getGender2() {
        return gender2;
    }

    public void setGender2(String gender2) {
        this.gender2 = gender2;
    }

    public String getGender4() {
        return gender4;
    }

    public void setGender4(String gender4) {
        this.gender4 = gender4;
    }

    public String getGender5() {
        return gender5;
    }

    public void setGender5(String gender5) {
        this.gender5 = gender5;
    }

    public boolean isUiFiltersSet() {
        return salutationId != null
            || isNotBlank(name)
            || isNotBlank(gender0)
            || isNotBlank(gender1)
            || isNotBlank(gender2)
            || isNotBlank(gender4)
            || isNotBlank(gender5);
    }
}
