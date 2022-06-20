/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.Recipient;
import org.agnitas.dao.MailingDao;
import org.agnitas.dao.SourceGroupType;
import org.agnitas.emm.core.velocity.VelocityResult;
import org.agnitas.emm.core.velocity.VelocityWrapper;
import org.agnitas.emm.core.velocity.VelocityWrapperFactory;
import org.agnitas.emm.core.velocity.emmapi.CompanyAccessCheck;
import org.agnitas.emm.core.velocity.emmapi.VelocityBindingEntry;
import org.agnitas.emm.core.velocity.emmapi.VelocityBindingEntryWrapper;
import org.agnitas.emm.core.velocity.emmapi.VelocityMailing;
import org.agnitas.emm.core.velocity.emmapi.VelocityMailingDao;
import org.agnitas.emm.core.velocity.emmapi.VelocityMailingDaoWrapper;
import org.agnitas.emm.core.velocity.emmapi.VelocityMailingWrapper;
import org.agnitas.emm.core.velocity.emmapi.VelocityRecipient;
import org.agnitas.emm.core.velocity.emmapi.VelocityRecipientWrapper;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.TimeoutLRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationExecuteScriptParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.util.ScriptHelper;

public class ActionOperationExecuteScriptImpl implements EmmActionOperation {

	/** The logger. */
	private static final Logger logger = LogManager.getLogger(ActionOperationExecuteScriptImpl.class);
	
	private MailingDao mailingDao;

	private BeanLookupFactory beanLookupFactory;
	
	private JavaMailService javaMailService;
	
	private CompanyAccessCheck companyAccessCheck;
	
	private DatasourceDescriptionDao datasourceDescriptionDao;
	
	private SendActionbasedMailingService sendActionbasedMailingService;
	
    protected TimeoutLRUMap<Integer, Integer> datasourceIdCache = new TimeoutLRUMap<>(100, 300000);

	private final void registerRecipient(final Map<String, Object> params, final int companyID) {
		final Recipient cust = beanLookupFactory.getBeanRecipient();
		cust.setCompanyID(companyID);
		
		// Set velocity specific datasource id
		try {
			Integer datasourceDescriptionId = datasourceIdCache.get(companyID);
			if (datasourceDescriptionId == null) {
				DatasourceDescription datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.Velocity, companyID, "Velocity");
				if (datasourceDescription != null) {
					datasourceDescriptionId = datasourceDescription.getId();
					datasourceIdCache.put(companyID, datasourceDescriptionId);
				}
			}
			
			if (datasourceDescriptionId != null) {
				cust.getCustParameters().put(ComCompanyDaoImpl.STANDARD_FIELD_DATASOURCE_ID, datasourceDescriptionId);
				cust.getCustParameters().put(ComCompanyDaoImpl.STANDARD_FIELD_LATEST_DATASOURCE_ID, datasourceDescriptionId);
			}
		} catch (Exception e) {
			logger.error("Cannot set velocity datasource_id in recipient for company " + companyID, e);
		}

		final VelocityRecipient apiClass = new VelocityRecipientWrapper(companyID, cust, this.companyAccessCheck);
		params.put("Customer", apiClass);
	}
	
	private final void registerBindingEntry(final Map<String, Object> params, final int companyId) {
		// neu von ma
		final BindingEntry binding = beanLookupFactory.getBeanBindingEntry();

		final VelocityBindingEntry apiClass = new VelocityBindingEntryWrapper(companyId, binding, companyAccessCheck);
		params.put("BindingEntry", apiClass);
	}
	
	private final void registerMailing(final Map<String, Object> params, final int companyID) {
		final Mailing mail = beanLookupFactory.getBeanMailing();
		mail.setCompanyID(companyID);
		
		final VelocityMailing apiClass = new VelocityMailingWrapper(mail, this.sendActionbasedMailingService);
		
		params.put("Mailing", apiClass);
	}
	
	private final void registerMailingDao(final Map<String, Object> params, final int companyID) {
		final VelocityMailingDao apiClass = new VelocityMailingDaoWrapper(companyID, mailingDao, companyAccessCheck);
		
		params.put("MailingDao", apiClass);
	}
	
	private final void registerScriptHelper(final Map<String, Object> params, final int companyID) {
		final ScriptHelper newScriptHelper = beanLookupFactory.getBeanScriptHelper();
		newScriptHelper.setCompanyID(companyID);
		newScriptHelper.setMailingID((Integer) params.get("mailingID"));
		newScriptHelper.setFormID((Integer) params.get("formID"));
		params.put("ScriptHelper", newScriptHelper);
	}
	
	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors errors) {
		boolean result=false;
		
		final ActionOperationExecuteScriptParameters op = (ActionOperationExecuteScriptParameters) operation;
		final int companyID = op.getCompanyId();
		final String script = op.getScript();

		registerRecipient(params, companyID);
		registerBindingEntry(params, companyID);
		registerMailing(params, companyID);
		registerMailingDao(params, companyID);
		registerScriptHelper(params, companyID);

		try {
			final VelocityWrapperFactory factory = beanLookupFactory.getBeanVelocityWrapperFactory();
			final VelocityWrapper velocity = factory.getWrapper( companyID);
			
			final StringWriter aWriter = new StringWriter();
			final VelocityResult velocityResult = velocity.evaluate( params, script, aWriter, 0, op.getActionId());

            if (velocityResult.hasErrors()) {
				final Iterator<?> it = velocityResult.getErrors().get();
            	while (it.hasNext()) {
            		logger.warn("Error in velocity script action " + operation.getCompanyId() + "/" + operation.getActionId()+ ": " + it.next());
            	}
            }

            if (params.containsKey("scriptResult")) {
                if (params.get("scriptResult").equals("1")) {
                    result = true;
                }
            }
        } catch(final Exception e) {
        	logger.error("Velocity error", e);

            params.put("velocity_error", AgnUtils.getUserErrorMessage(e));
            javaMailService.sendVelocityExceptionMail(companyID, (String) params.get("formURL"),e);
        }

		return result;
	}

	@Override
	public final ActionOperationType processedType() {
		return ActionOperationType.EXECUTE_SCRIPT;
	}

	public void setMailingDao(final MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setBeanLookupFactory(final BeanLookupFactory beanLookupFactory) {
		this.beanLookupFactory = beanLookupFactory;
	}

	public void setJavaMailService(final JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}
	
	@Required
	public final void setCompanyAccessCheck(final CompanyAccessCheck check) {
		this.companyAccessCheck = Objects.requireNonNull(check, "CompanyAccessCheck is null");
	}
	
	@Required
	public final void setDatasourceDescriptionDao(final DatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = Objects.requireNonNull(datasourceDescriptionDao, "datasourceDescriptionDao is null");
	}
	
	@Required
	public final void setSendActionbasedMailingService(final SendActionbasedMailingService service) {
		this.sendActionbasedMailingService = Objects.requireNonNull(service, "SendActionbasedMailingService is null");
	}
}
