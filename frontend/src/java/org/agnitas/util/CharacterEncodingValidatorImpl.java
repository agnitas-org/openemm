/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.exceptions.CharacterEncodingValidationException;
import org.agnitas.exceptions.CharacterEncodingValidationExceptionMod;
import org.agnitas.exceptions.EncodingError;
import org.agnitas.web.forms.MailingBaseForm;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.web.ComMailingContentForm;

/**
 * This class validates the content of a mailing against the character set
 * defined in the mailing.
 */
public class CharacterEncodingValidatorImpl implements CharacterEncodingValidator {

	/**
	 * Validates text and HTML template of the given form and the content blocks of the given Mailing object. This method is called directly <b>before modifying</b>
	 * the MailingBaseForm object. 
	 * 
	 * @param form form to validate text and HTML template
	 * @param mailing mailing to validate content blocks
	 *
	 */
	@Override
	public void validate(MailingBaseForm form, Mailing mailing) throws CharacterEncodingValidationException {
		Set<String> unencodeableMailingComponents = new HashSet<>();
		Set<String> unencodeableDynamicTags = new HashSet<>();
		
		boolean subjectValid = validate(form, mailing, unencodeableMailingComponents, unencodeableDynamicTags);
		
		if (unencodeableMailingComponents.size() > 0 || unencodeableDynamicTags.size() > 0 || !subjectValid) {
			throw new CharacterEncodingValidationException(subjectValid, unencodeableMailingComponents, unencodeableDynamicTags);
		}
	}
	
	private boolean validate(MailingBaseForm form, Mailing mailing, Set<String> unencodeableMailingComponents, Set<String> unencodeableDynamicTags) {
		CharsetEncoder charsetEncoder = getCharsetEncoder(form);
		
		validate(form, unencodeableMailingComponents, charsetEncoder);
		validateDynamicTags(mailing, unencodeableDynamicTags, charsetEncoder);
		
		return validate( form.getEmailSubject(), charsetEncoder);
	}
	
	@Override
	public void validate(ComMailingContentForm form, Mailing mailing) throws CharacterEncodingValidationException {
		Set<String> unencodeableDynamicTags = new HashSet<>();
		
		validate(form, mailing, unencodeableDynamicTags);
		
		if (unencodeableDynamicTags.size() > 0) {
			throw new CharacterEncodingValidationException(new HashSet<>(), unencodeableDynamicTags);
		}
	}

	@Override
    public void validate(ComMailingContentForm form, String charset) throws CharacterEncodingValidationException {
		Set<String> unencodeableDynamicTags = new HashSet<>();

		validate(form, getCharsetEncoder(charset), unencodeableDynamicTags);

		if (unencodeableDynamicTags.size() > 0) {
			throw new CharacterEncodingValidationException(new HashSet<>(), unencodeableDynamicTags);
		}
	}

    @Override
    public void validateContentMod(ComMailingContentForm form, String charset) throws CharacterEncodingValidationExceptionMod {
		Set<EncodingError> unencodeableDynamicTags = new HashSet<>();

        CharsetEncoder charsetEncoder = getCharsetEncoder(charset);

        for (String content : form.getContentForValidation()) {
            Set<EncodingError> dynTagErrors = validateMod(content, charsetEncoder);
            if (dynTagErrors.size() > 0) {
                for (EncodingError error : dynTagErrors) {
                    unencodeableDynamicTags.add(new EncodingError(form.getDynName(), error.getLine()));
                }
            }
        }
        if (unencodeableDynamicTags.size() > 0) {
			throw new CharacterEncodingValidationExceptionMod(new HashSet<>(), new HashSet<>(), unencodeableDynamicTags);
        }
	}

    @Override
	public void validateContentMod(DynTagDto dynTag, String charset) throws CharacterEncodingValidationExceptionMod {
		CharsetEncoder charsetEncoder = getCharsetEncoder(charset);

		Set<EncodingError> unncodeableDynamicTags = new HashSet<>();
		List<DynContentDto> contentBlocks = dynTag.getContentBlocks();
		for (DynContentDto content : contentBlocks) {
			Set<EncodingError> dynTagErrors = validateMod(content.getContent(), charsetEncoder);
			if (dynTagErrors.size() > 0) {
				for (EncodingError error : dynTagErrors) {
					unncodeableDynamicTags.add(new EncodingError(dynTag.getName(), error.getLine()));
				}
			}
		}

		if (unncodeableDynamicTags.size() > 0) {
			throw new CharacterEncodingValidationExceptionMod(new HashSet<>(), new HashSet<>(), unncodeableDynamicTags);
		}
	}
	
	private void validate(ComMailingContentForm form, Mailing mailing, Set<String> unencodeableDynamicTags) {
		CharsetEncoder charsetEncoder = getCharsetEncoder(mailing);
		
		validate(form, charsetEncoder, unencodeableDynamicTags);
	}
	
	private void validate(ComMailingContentForm form, CharsetEncoder charsetEncoder, Set<String> unencodeableDynamicTags) {
		Map<Integer, DynamicTagContent> dynTags = form.getContent();
		
		for (DynamicTagContent dynamicTagContent : dynTags.values()) {
			if (!validate(dynamicTagContent, charsetEncoder)) {
				unencodeableDynamicTags.add( form.getDynName());
			}
		}
	}
	
	/**
	 * Validates a mailing component.
	 * 
	 * @param component the mailing component to be validated
	 * @param charsetName the name of the character set to be used
	 * @return true if the mailing component passed validated otherwise false
	 */
	@Override
	public boolean validate(MailingComponent component, String charsetName) {
		CharsetEncoder charsetEncoder = getCharsetEncoder( charsetName);
		return validate(component, charsetEncoder);
	}
	
	/**
	 * Validates a dynamic tag.
	 * 
	 * @param dynTag DynamicTag to be validated
	 * @param charsetName character set to be used for validation
	 * @return true if the DynamicTag passed validated otherwise false
	 */
	@Override
	public boolean validate(DynamicTag dynTag, String charsetName) {
		CharsetEncoder charsetEncoder = getCharsetEncoder( charsetName);
		return validate(dynTag, charsetEncoder);
	}
	
	private void validate(MailingBaseForm form, Set<String> unencodeableMailingComponents, CharsetEncoder encoder) {
		String textTemplate = form.getTextTemplate();
		if (textTemplate!= null && !validate(textTemplate, encoder)) {
			unencodeableMailingComponents.add("agnText");
		}
		
		String htmlTemplate = form.getHtmlTemplate();
		if (htmlTemplate != null && !validate(htmlTemplate, encoder)) {
			unencodeableMailingComponents.add("agnHtml");
		}
	}
	
	private void validateDynamicTags(Mailing mailing, Set<String> unencodeableDynamicTags, CharsetEncoder charsetEncoder) {
		// No mailing? Nothing to validate!
		if (mailing == null) {
			return;
		}
		
		Collection<DynamicTag> dynTags = mailing.getDynTags().values();

		for (DynamicTag dynTag : dynTags) {
			if (!validate( dynTag, charsetEncoder)) {
				unencodeableDynamicTags.add( dynTag.getDynName());
			}
		}
	}

	/**
	 * Validates a mailing component using the given CharsetEncoder.
	 * 
	 * @param component the mailing component to be validated
	 * @param charsetEncoder CharsetEncoder to be used for validation
	 * @return true if the mailing component passed validated otherwise false
	 */
	private boolean validate(MailingComponent component, CharsetEncoder charsetEncoder) {
		return validate(component.getEmmBlock(), charsetEncoder);
	}
	
	/**
	 * Validates a dynamic tag using the given CharsetEncoder.
	 * 
	 * @param dynTag DynamicTag to be validated
	 * @param charsetEncoder CharsetEncoder to be used for validation
	 * @return true if the DynamicTag passed validated otherwise false
	 */
	private boolean validate(DynamicTag dynTag, CharsetEncoder charsetEncoder) {
		Collection<DynamicTagContent> contents = dynTag.getDynContent().values();
		
		for (DynamicTagContent content : contents) {
			if (!validate( content, charsetEncoder)) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Validates the content of a DynamicTagContent objects
	 * @param content object to be validates
	 * @param charsetEncoder CharacterEncoder to be used for validation
	 * @return
	 */
	private boolean validate(DynamicTagContent content, CharsetEncoder charsetEncoder) {
		return validate(content.getDynContent(), charsetEncoder);
	}
	
	/**
	 * Validates a String using a CharsetEncoder.
	 * 
	 * @param string String to be validated
	 * @param charsetEncoder CharsetEncoder to be used for validation
	 * @return true if the String passed the validation otherwise false
	 */
	private boolean validate(String string, CharsetEncoder charsetEncoder) {
		return charsetEncoder.canEncode(string);
	}

	/**
	 * Returns a CharsetEncoder matching the character set defined in the given mailing.
	 * @param mailing Mailing to create CharsetEncoder for
	 * @return CharsetEncoder for mailing
	 */
	private CharsetEncoder getCharsetEncoder(Mailing mailing) {
		String charsetName = ((MediatypeEmail) mailing.getMediatypes().get(0)).getCharset();

		return getCharsetEncoder(charsetName);
	}
	
	private CharsetEncoder getCharsetEncoder(MailingBaseForm form) {
		return getCharsetEncoder(form.getEmailCharset());
	}
	
	/**
	 * Creates a CharsetEncoder for the given charset.
	 * 
	 * @param charsetName name of character set
	 * @return CharsetEncoder for given charset
	 */
	private CharsetEncoder getCharsetEncoder(String charsetName) {
		Charset charset = Charset.forName(charsetName);
		
		return charset.newEncoder();
	}

	@Override
    public void validateMod(MailingBaseForm form, Mailing mailing) throws CharacterEncodingValidationExceptionMod {
        Set<EncodingError> subjectErrors = new HashSet<>();
		Set<EncodingError> unencodeableMailingComponents = new HashSet<>();
		Set<EncodingError> unencodeableDynamicTags = new HashSet<>();

		validateMod(form, mailing, subjectErrors, unencodeableMailingComponents, unencodeableDynamicTags);

		if (unencodeableMailingComponents.size() > 0 || unencodeableDynamicTags.size() > 0 || subjectErrors.size() > 0) {
			throw new CharacterEncodingValidationExceptionMod( subjectErrors, unencodeableMailingComponents, unencodeableDynamicTags);
		}
	}

    private void validateMod(MailingBaseForm form, Mailing mailing, Set<EncodingError> subjectErrors, Set<EncodingError> unencodeableMailingComponents, Set<EncodingError> unencodeableDynamicTags) {
		CharsetEncoder charsetEncoder = getCharsetEncoder(form);
        validateSubject(form,subjectErrors, charsetEncoder);
		validateMod(form, unencodeableMailingComponents, charsetEncoder);
		validateDynamicTagsMod(mailing, unencodeableDynamicTags, charsetEncoder);
	}

    private void validateSubject(MailingBaseForm form, Set<EncodingError> subjectErrors, CharsetEncoder encoder) {
        subjectErrors.addAll(validateMod(form.getEmailSubject(), encoder));
    }

    private void validateMod(MailingBaseForm form, Set<EncodingError> unencodeableMailingComponents, CharsetEncoder encoder) {
		String textTemplate = form.getTextTemplate();
    	if (textTemplate != null) {
	        Set<EncodingError> textTemplateErrors =  validateMod(textTemplate, encoder);
			if (textTemplateErrors.size() > 0) {
	            for (EncodingError error : textTemplateErrors) {
	            	unencodeableMailingComponents.add(new EncodingError("agnText", error.getLine()));
	            }
	        }
    	}
    	
    	String htmlTemplate = form.getHtmlTemplate();
		if (htmlTemplate != null) {
			Set<EncodingError> htmlTemplateErrors = validateMod(htmlTemplate, encoder);
			if (htmlTemplateErrors.size() > 0) {
	            for (EncodingError error : htmlTemplateErrors) {
	            	unencodeableMailingComponents.add(new EncodingError("agnHtml", error.getLine()));
	            }
	        }
		}
	}

    private void validateDynamicTagsMod(Mailing mailing, Set<EncodingError> unencodeableDynamicTags, CharsetEncoder charsetEncoder) {
		// No mailing? Nothing to validate!
		if (mailing == null) {
			return;
		}

		Collection<DynamicTag> dynTags = mailing.getDynTags().values();

		for (DynamicTag dynTag : dynTags) {
			unencodeableDynamicTags.addAll(validateContent(dynTag, charsetEncoder));
        }
	}

	private Set<EncodingError> validateContent(DynamicTag dynTag, CharsetEncoder charsetEncoder) {
		Set<EncodingError> unencodeableDynamicTags = new HashSet<>();
		Collection<DynamicTagContent> dynamicTagContents = dynTag.getDynContent().values();
		for (DynamicTagContent content : dynamicTagContents) {
			Set<EncodingError> dynTagErrors = validateMod(content.getDynContent(), charsetEncoder);
			if (dynTagErrors.size() > 0) {
				for (EncodingError error : dynTagErrors) {
					unencodeableDynamicTags.add(new EncodingError(dynTag.getDynName(), error.getLine()));
				}
			}
		}

		return unencodeableDynamicTags;
	}

    public Set<EncodingError> validateMod(String string, CharsetEncoder charsetEncoder) {
    	/*
    	 * We lost information about column of un-encodable character.
    	 * The old implementation did not respect codepoints (and therefore unicode characters with more then 2 bytes length).
    	 * For example the character U+1f382 leads to reports about *two* characters, that are not
    	 * encodable. (0x1f382 is splitted to characters 0x0001 and 0xf382). 
    	 */
    	
        String[] stringLines = string.split("\n");
        Set<EncodingError> errors = new HashSet<>();
        for (int lineIndex = 0; lineIndex < stringLines.length; lineIndex++) {
        	
        	if(!charsetEncoder.canEncode(stringLines[lineIndex])) {
                errors.add(new EncodingError(stringLines[lineIndex], lineIndex + 1));
        	}
        	
        }
		return errors;
	}
}
