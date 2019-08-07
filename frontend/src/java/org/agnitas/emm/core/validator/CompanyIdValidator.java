/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.emm.core.validator.annotation.CompanyId;

public class CompanyIdValidator implements ConstraintValidator<CompanyId, Object> {

    private CompanyService companyService;

    public CompanyIdValidator(CompanyService companyService) {
        this.companyService = companyService;
    }

    @Override
    public void initialize(CompanyId companyId) {
    	// Do nothing
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if (object instanceof Integer) {
            int companyId = (Integer) object;
            if (companyId > 0 ) {
                return companyService.isCompanyExisting(companyId);
            }
        }

        return true;
    }
}
