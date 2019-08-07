/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.form;

import javax.validation.Valid;

import com.agnitas.emm.core.company.dto.CompanyInfoDto;
import com.agnitas.emm.core.company.dto.CompanySettingsDto;

public class CompanyViewForm {

    @Valid
    private CompanyInfoDto companyInfoDto;

    @Valid
    private CompanySettingsDto companySettingsDto;

    public CompanyInfoDto getCompanyInfoDto() {
        return companyInfoDto;
    }

    public void setCompanyInfoDto(CompanyInfoDto companyInfoDto) {
        this.companyInfoDto = companyInfoDto;
    }

    public CompanySettingsDto getCompanySettingsDto() {
        return companySettingsDto;
    }

    public void setCompanySettingsDto(CompanySettingsDto companySettingsDto) {
        this.companySettingsDto = companySettingsDto;
    }
}
