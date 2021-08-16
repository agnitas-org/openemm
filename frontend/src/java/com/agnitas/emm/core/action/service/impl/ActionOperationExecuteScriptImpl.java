/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import org.agnitas.beans.BindingEntry;
import com.agnitas.beans.Mailing;
import org.agnitas.beans.Recipient;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.velocity.VelocityResult;
import org.agnitas.emm.core.velocity.VelocityWrapper;
import org.agnitas.emm.core.velocity.VelocityWrapperFactory;
import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationExecuteScriptParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.util.ScriptHelper;

public class ActionOperationExecuteScriptImpl implements EmmActionOperation {

	private static final Logger logger = Logger.getLogger(ActionOperationExecuteScriptImpl.class);
	
	private MailingDao mailingDao;

	private BeanLookupFactory beanLookupFactory;
	
	private JavaMailService javaMailService;

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors errors) {
		
		boolean result=false;
		
		ActionOperationExecuteScriptParameters op = (ActionOperationExecuteScriptParameters) operation;
		int companyID = op.getCompanyId();
		String script = op.getScript();
		
		Recipient cust = beanLookupFactory.getBeanRecipient();
		cust.setCompanyID(companyID);
		params.put("Customer", cust);

		// neu von ma
		BindingEntry binding = beanLookupFactory.getBeanBindingEntry();
		params.put("BindingEntry", binding);

		Mailing mail = beanLookupFactory.getBeanMailing();
		mail.setCompanyID(companyID);
		params.put("Mailing", mail);

		params.put("MailingDao", mailingDao);

		ScriptHelper newScriptHelper = beanLookupFactory.getBeanScriptHelper();
		newScriptHelper.setCompanyID(companyID);
		newScriptHelper.setMailingID((Integer) params.get("mailingID"));
		newScriptHelper.setFormID((Integer) params.get("formID"));
		params.put("ScriptHelper", newScriptHelper);

		try {
			VelocityWrapperFactory factory = beanLookupFactory.getBeanVelocityWrapperFactory();
			VelocityWrapper velocity = factory.getWrapper( companyID);
			
            StringWriter aWriter = new StringWriter();
			VelocityResult velocityResult = velocity.evaluate( params, script, aWriter, 0, op.getActionId());

            if (velocityResult.hasErrors()) {
				@SuppressWarnings("rawtypes")
				Iterator it = velocityResult.getErrors().get();
            	while (it.hasNext()) {
            		logger.warn("Error in velocity script action " + operation.getCompanyId() + "/" + operation.getActionId()+ ": " + it.next());
            	}
            }

            if (params.containsKey("scriptResult")) {
                if (params.get("scriptResult").equals("1")) {
                    result = true;
                }
            }
        } catch(Exception e) {
        	logger.error("Velocity error", e);

            params.put("velocity_error", AgnUtils.getUserErrorMessage(e));
            javaMailService.sendVelocityExceptionMail((String) params.get("formURL"),e);
        }

		return result;
	}

	@Override
	public ActionOperationType processedType() {
		return ActionOperationType.EXECUTE_SCRIPT;
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setBeanLookupFactory(BeanLookupFactory beanLookupFactory) {
		this.beanLookupFactory = beanLookupFactory;
	}

	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}
}
