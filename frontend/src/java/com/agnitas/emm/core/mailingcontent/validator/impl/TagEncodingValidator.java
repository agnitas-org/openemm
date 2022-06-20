/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.validator.impl;

import java.util.Objects;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagValidator;
import com.agnitas.web.mvc.Popups;
import org.agnitas.dao.MailingDao;
import org.agnitas.exceptions.CharacterEncodingValidationExceptionMod;
import org.agnitas.exceptions.EncodingError;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CharacterEncodingValidator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class TagEncodingValidator implements DynTagValidator {
    private CharacterEncodingValidator characterEncodingValidator;
    private MailingDao mailingDao;

    public TagEncodingValidator(CharacterEncodingValidator characterEncodingValidator, MailingDao mailingDao) {
        this.characterEncodingValidator = characterEncodingValidator;
        this.mailingDao = mailingDao;
    }

    @Override
    public boolean validate(DynTagDto dynTagDto, Popups popups, ComAdmin admin) {
        try {
            String parameters = mailingDao.getEmailParameter(dynTagDto.getMailingId());
            
            if (Objects.nonNull(parameters)) {
            	// If mailing contains email parameters, do further checks 
                String charset = AgnUtils.getAttributeFromParameterString(parameters, "charset");
                characterEncodingValidator.validateContentMod(dynTagDto, charset);
            }
            
            /* 
             * Validation is successful when
             * 
             *  - mailing contains email parameters and checks have been passed or
             *  - mailing does not contain email parameters
             */
            
            return true;
        } catch (CharacterEncodingValidationExceptionMod e) {
            if (!e.getSubjectErrors().isEmpty()) {
                popups.alert("error.charset.subject");
            }
            for (EncodingError mailingComponent : e.getFailedMailingComponents()) {
                popups.alert("error.charset.component", mailingComponent.getStrWithError(), mailingComponent.getLine());
            }
            for (EncodingError dynTag : e.getFailedDynamicTags()) {
                popups.alert("error.charset.content", dynTag.getStrWithError(), dynTag.getLine());
            }
        }

        return false;
    }
}
