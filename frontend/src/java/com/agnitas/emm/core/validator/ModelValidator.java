/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.validator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import com.agnitas.emm.core.validator.annotation.Validate;
import org.apache.bval.jsr.ApacheValidationProvider;
import org.apache.bval.jsr.ApacheValidatorConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class ModelValidator {

	private static final Logger logger = LogManager.getLogger(ModelValidator.class);
	
    private ResourceBundle messagesBundle;

    private final Validator validator;

    public ModelValidator(String validationFile, String propertiesFile) {
    	if (logger.isDebugEnabled()) {
    		logger.debug("Created ModelValidator (validation file: " + validationFile + ", properties file: " + propertiesFile + ")");
    	}

	    messagesBundle = ResourceBundle.getBundle(propertiesFile);

        try (ValidatorFactory validatorFactory = Validation.byProvider(ApacheValidationProvider.class)
				.configure()
				.addProperty(ApacheValidatorConfiguration.Properties.VALIDATION_XML_PATH, validationFile)
				.buildValidatorFactory()) {
        	validator = validatorFactory.getValidator();
        }
    }
    
    @Before(value = "@annotation(annotation) && args(model, ..)")
    public void validate(Validate annotation, Object model) {
    	if(logger.isDebugEnabled()) {
    		logger.debug("formGroups:" + Arrays.toString(annotation.groups()) + ", model:"+model.getClass().getName());
    	}

    	validate(model, annotation.groups());
	}

	@Before(value="@annotation(annotation) && args(model, username)", argNames="annotation, model, username")
	public void validate(Validate annotation, Object model, String username) {
    	if(logger.isDebugEnabled()) {
    		logger.debug("formGroups:" + Arrays.toString(annotation.groups()) + ", model:"+model.getClass().getName() + ", username: " + username);
    	}

		validate(model, annotation.groups());
	}

	@Before(value = "@annotation(annotation) && args(model, username, companyId, ..)", argNames = "annotation,model,username,companyId")
	public void validate(Validate annotation, Object model, String username, int companyId) {
    	if(logger.isDebugEnabled()) {
    		logger.debug("formGroups:" + Arrays.toString(annotation.groups()) + ", model:"+model.getClass().getName() + ", username: " + username);
    	}

		validate(model, annotation.groups());
	}

	public void validate(final Object model, final Class<?>... groups) {
    	final Set<ConstraintViolation<Object>> violations = validator.validate(model, groups);
    	checkResults(violations);
	}

	private void checkResults(Set<ConstraintViolation<Object>> violations) {
    	final Pattern pattern = Pattern.compile("([.|\\w]+)(\\{.+})*");
    	for (ConstraintViolation<Object> violation : violations) {
    		final String messageTemplate = violation.getMessageTemplate();
			final Matcher matcher = pattern.matcher(messageTemplate);
			if(matcher.find()) {
				final String mainMessageKey = matcher.group(1);
				final String argsKeys = matcher.group(2);
				throw new IllegalArgumentException(getMessage(mainMessageKey, argsKeys));
			}
		}
	}

	private String getMessage(final String mainKey, final String argsKeys) {
		return MessageFormat.format(getMessage(messagesBundle, mainKey), getArgs(argsKeys));
	}

	private Object[] getArgs(final String argsKeys) {
    	final List<String> args = new ArrayList<>();
    	for(String argKey : argsKeys.split("[{}]")){
    		if(StringUtils.isNotBlank(argKey)) {
    			args.add(getMessage(messagesBundle, argKey));
			}
		}
		return args.toArray();
	}

	private static String getMessage(ResourceBundle messages, String key) {
		String message = null;

		if (messages != null) {
			try {
				message = messages.getString(key);
			} catch (MissingResourceException e) {
				message = key;
			}
		}

		return (message == null) ? "" : message;
	}
}
