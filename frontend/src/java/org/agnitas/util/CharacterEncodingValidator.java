/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;


import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.mailing.forms.MailingSettingsForm;
import org.agnitas.beans.MailingComponent;
import org.agnitas.exceptions.CharacterEncodingValidationExceptionMod;
import org.agnitas.exceptions.EncodingError;

import com.agnitas.beans.DynamicTag;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;

import java.nio.charset.CharsetEncoder;
import java.util.Set;

/**
 * This class validates the content of a mailing against the character set
 * defined in the mailing.
 */
public interface CharacterEncodingValidator {

	/**
	 * Validates a mailing component.
	 * 
	 * @param component the mailing component to be validated
	 * @param charsetName the name of the character set to be used
	 * @return true if the mailing component passed validated otherwise false
	 */
	boolean validate( MailingComponent component, String charsetName);
	
	/**
	 * Validates a dynamic tag.
	 * 
	 * @param dynTag DynamicTag to be validated
	 * @param charsetName character set to be used for validation
	 * @return true if the DynamicTag passed validated otherwise false
	 */
	boolean validate(DynamicTag dynTag, String charsetName);

    /**
  	 * Validates text and HTML template of the given form and the content blocks of the given Mailing object. 
  	 * 
  	 * @param form form to validate text and HTML template
  	 * @param mailing mailing to validate content blocks
  	 */
    void validateMod(MailingSettingsForm form, Mailing mailing) throws CharacterEncodingValidationExceptionMod;

	Set<EncodingError> validateMod(String string, CharsetEncoder charsetEncoder);

    void validateContentMod(DynTagDto dynTag, String charset) throws CharacterEncodingValidationExceptionMod;
}
