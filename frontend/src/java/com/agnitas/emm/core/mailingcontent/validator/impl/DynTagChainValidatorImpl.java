/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.validator.impl;

import java.util.List;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagChainValidator;
import com.agnitas.emm.core.mailingcontent.validator.DynTagValidator;
import com.agnitas.web.mvc.Popups;
import org.springframework.stereotype.Component;

@Component
public class DynTagChainValidatorImpl implements DynTagChainValidator {
    private List<DynTagValidator> validators;

    public DynTagChainValidatorImpl(List<DynTagValidator> validators) {
        this.validators = validators;
    }

    @Override
    public boolean validate(DynTagDto dynTagDto, Popups popups, ComAdmin admin) {
        for (DynTagValidator validator : validators) {
            if (!validator.validate(dynTagDto, popups, admin)) {
                return false;
            }
        }

        return true;
    }
}
