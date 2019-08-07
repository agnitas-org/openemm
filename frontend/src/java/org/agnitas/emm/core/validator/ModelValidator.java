/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.validator;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.agnitas.emm.core.validator.annotation.Validate;
import org.apache.commons.validator.Arg;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.Validator;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.ValidatorException;
import org.apache.commons.validator.ValidatorResources;
import org.apache.commons.validator.ValidatorResult;
import org.apache.commons.validator.ValidatorResults;
import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.xml.sax.SAXException;

@Aspect
public class ModelValidator {
	
	private final static Logger log = Logger.getLogger(ModelValidator.class);
	
    private ResourceBundle messagesBundle;
    private ValidatorResources resources;

    public ModelValidator(String validationFile, String propertiesFile) throws IOException, SAXException {
    	if(log.isDebugEnabled()) {
    		log.debug("Created ModelValidator (validation file: " + validationFile + ", properties file: " + propertiesFile + ")");
    	}
    	
    	try(InputStream in = this.getClass().getClassLoader().getResourceAsStream(validationFile)) {
	    	resources = new ValidatorResources(in);
	
	    	messagesBundle = ResourceBundle.getBundle(propertiesFile);
    	}
    }

    @Before(value = "@annotation(annotation) && args(model, ..)")
    public void validate(Validate annotation, Object model) throws ValidatorException {
    	if(log.isDebugEnabled()) {
    		log.debug("formName:"+annotation.value()+", model:"+model.getClass().getName());
    	}

    	validateInternal(annotation, model);
	}

	@Before(value="@annotation(annotation) && args(model, username)", argNames="annotation, model, username")
	public void validate(Validate annotation, Object model, String username) throws ValidatorException {
    	if(log.isDebugEnabled()) {
    		log.debug("formName:"+annotation.value()+", model:"+model.getClass().getName() + ", username: " + username);
    	}

    	validateInternal(annotation, model);
	}

	@Before(value = "@annotation(annotation) && args(model, username, companyId, ..)")
	public void validate(Validate annotation, Object model, String username, int companyId) throws ValidatorException {
    	if(log.isDebugEnabled()) {
    		log.debug("formName:"+annotation.value()+", model:"+model.getClass().getName() + ", username: " + username);
    	}

    	validateInternal(annotation, model);
	}

	@Before(value = "@annotation(annotation) && args(companyId, modelId)")
	public void validate(Validate annotation, int companyId, int modelId) throws ValidatorException {
		if(log.isDebugEnabled()) {
			log.debug("formName: " + annotation.value() + ", companyId: " + companyId);
		}

		Validator validator = new Validator(resources, annotation.value());
		validator.setParameter(Validator.BEAN_PARAM, companyId);
		validator.setParameter(Validator.BEAN_PARAM, modelId);

		validateInternal(validator);
	}

	private void validateInternal(Validate annotation, Object model) throws ValidatorException {
        Validator validator = new Validator(resources, annotation.value());
        validator.setParameter(Validator.BEAN_PARAM, model);
        validateInternal(validator);
	}

	private void validateInternal(Validator validator) throws ValidatorException {
		checkResults(validator.validate());
	}

	@Before(value = "@annotation(annotation) && args(companyId, customerID, custParameters)", argNames = "annotation,companyId,customerID,custParameters")
	public void validate(Validate annotation, int companyId, int customerID, Map<?, ?> custParameters) throws ValidatorException {
    	if(log.isDebugEnabled()) {
    		log.debug("formName:"+annotation.value());
    	}

        Validator validator = new Validator(resources, annotation.value());
        validator.setParameter(Validator.BEAN_PARAM, custParameters);
        validateInternal(validator);

	}
    
	public void validate(String annotation, Object model) throws ValidatorException {
    	if(log.isDebugEnabled()) {
    		log.debug("formName:"+annotation+", model:"+model.getClass().getName());
    	}
		
        Validator validator = new Validator(resources, annotation);
        validator.setParameter(Validator.BEAN_PARAM, model);
        ValidatorResults results = validator.validate();

        checkResults(results);
	}

    private void checkResults(ValidatorResults results) throws IllegalArgumentException {
        for (String fieldNames : results.getPropertyNames()) {
            ValidatorResult result = results.getValidatorResult(fieldNames);
            List<String> actions = result.getField().getDependencyList();
            for (int i = 0; i < actions.size(); ++ i) {
                if (!result.isValid(actions.get(i))) {
                    ValidatorAction action = resources.getValidatorAction(actions.get(i));
                    Field field = result.getField();
                    throw new IllegalArgumentException(getErrorMessage(field, action));
                }
            }
        }
    }


    private String getErrorMessage(Field field, ValidatorAction action) {
        // TODO: add processing of an alternative message
        // that can be associated with a Field and configured with a <msg> xml element.
        // See Resources.getActionMessage(validator, request, va, field)) for references.
		String args[] = getArgs(action.getName(), messagesBundle, field);

		String msg = field.getMsg(action.getName()) != null ? field.getMsg(action.getName()) : action.getMsg();

		return MessageFormat.format(getMessage(messagesBundle, msg), (Object[]) args);
	}    
    
	public static String[] getArgs(String actionName, ResourceBundle messages, Field field) {

		String[] argMessages = new String[4];

		Arg[] args = new Arg[] { 
				field.getArg(actionName, 0), 
				field.getArg(actionName, 1), 
				field.getArg(actionName, 2),
				field.getArg(actionName, 3) };

		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				continue;
			}

			if (args[i].isResource()) {
				argMessages[i] = getMessage(messages, args[i].getKey());
			} else {
				argMessages[i] = args[i].getKey();
			}

		}

		return argMessages;
	}
	
	public static String getMessage(ResourceBundle messages, String key) {
		String message = null;

		if (messages != null) {
			message = messages.getString(key);
		}

		return (message == null) ? "" : message;
	}
}
