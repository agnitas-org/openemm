/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.io.StringWriter;
import java.util.Map;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.DatasourceDescription;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Recipient;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.action.operations.ActionOperationExecuteScriptParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.core.velocity.VelocityResult;
import com.agnitas.emm.core.velocity.emmapi.CompanyAccessCheck;
import com.agnitas.emm.core.velocity.emmapi.VelocityBindingEntryWrapper;
import com.agnitas.emm.core.velocity.emmapi.VelocityMailingDaoWrapper;
import com.agnitas.emm.core.velocity.emmapi.VelocityMailingWrapper;
import com.agnitas.emm.core.velocity.emmapi.VelocityRecipientWrapper;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.ScriptHelper;
import com.agnitas.util.TimeoutLRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionOperationExecuteScriptImpl implements EmmActionOperation<ActionOperationExecuteScriptParameters> {

	private static final Logger logger = LogManager.getLogger(ActionOperationExecuteScriptImpl.class);
	
	private final MailingDao mailingDao;
	private final BeanLookupFactory beanLookupFactory;
	private final JavaMailService javaMailService;
	private final CompanyAccessCheck companyAccessCheck;
	private final DatasourceDescriptionDao datasourceDescriptionDao;
	private final SendActionbasedMailingService sendActionbasedMailingService;
	private final SubscriberLimitCheck subscriberLimitCheck;
    protected TimeoutLRUMap<Integer, Integer> datasourceIdCache = new TimeoutLRUMap<>(100, 300000);

	public ActionOperationExecuteScriptImpl(
			MailingDao mailingDao,
			BeanLookupFactory beanLookupFactory,
			JavaMailService javaMailService,
			CompanyAccessCheck companyAccessCheck,
			DatasourceDescriptionDao datasourceDescriptionDao,
			SendActionbasedMailingService sendActionbasedMailingService,
			SubscriberLimitCheck subscriberLimitCheck
	) {
		this.mailingDao = mailingDao;
		this.beanLookupFactory = beanLookupFactory;
		this.javaMailService = javaMailService;
		this.companyAccessCheck = companyAccessCheck;
		this.datasourceDescriptionDao = datasourceDescriptionDao;
		this.sendActionbasedMailingService = sendActionbasedMailingService;
		this.subscriberLimitCheck = subscriberLimitCheck;
	}

	private void registerRecipient(Map<String, Object> params, int companyID) {
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
				cust.getCustParameters().put(RecipientStandardField.DatasourceID.getColumnName(), datasourceDescriptionId);
				cust.getCustParameters().put(RecipientStandardField.LatestDatasourceID.getColumnName(), datasourceDescriptionId);
			}
		} catch (Exception e) {
			logger.error("Cannot set velocity datasource_id in recipient for company {}", companyID, e);
		}

        params.put("Customer", new VelocityRecipientWrapper(companyID, cust, companyAccessCheck, subscriberLimitCheck));
	}
	
	private void registerBindingEntry(Map<String, Object> params, int companyId) {
		BindingEntry binding = beanLookupFactory.getBeanBindingEntry();
        params.put("BindingEntry", new VelocityBindingEntryWrapper(companyId, binding, companyAccessCheck));
	}
	
	private void registerMailing(Map<String, Object> params, int companyID) {
		Mailing mailing = beanLookupFactory.getBeanMailing();
		mailing.setCompanyID(companyID);

        params.put("Mailing", new VelocityMailingWrapper(mailing, this.sendActionbasedMailingService));
	}
	
	private void registerMailingDao(Map<String, Object> params, int companyID) {
        params.put("MailingDao", new VelocityMailingDaoWrapper(companyID, mailingDao, companyAccessCheck));
	}
	
	private void registerScriptHelper(Map<String, Object> params, int companyID) {
		final ScriptHelper newScriptHelper = beanLookupFactory.getBeanScriptHelper();
		newScriptHelper.setCompanyID(companyID);
		newScriptHelper.setMailingID(parseIntegerParam(params.get("mailingID")));
		newScriptHelper.setFormID(parseIntegerParam(params.get("formID")));
		params.put("ScriptHelper", newScriptHelper);
	}

	@Override
	public boolean execute(
			ActionOperationExecuteScriptParameters op,
			Map<String, Object> params,
			EmmActionOperationErrors errors
	) {
        final int companyID = op.getCompanyId();
		final String script = op.getScript();

		registerRecipient(params, companyID);
		registerBindingEntry(params, companyID);
		registerMailing(params, companyID);
		registerMailingDao(params, companyID);
		registerScriptHelper(params, companyID);

		try {
			VelocityResult velocityResult = beanLookupFactory.getBeanVelocityWrapperFactory()
					.getWrapper(companyID)
					.evaluate(params, script, new StringWriter(), 0, op.getActionId());

            if (velocityResult.hasErrors()) {
				if (logger.isWarnEnabled()) {
					for (String message : velocityResult.getErrorMessages()) {
						logger.warn("Error in velocity script action {}/{}: {}", op.getCompanyId(), op.getActionId(), message);
					}
				}

            	errors.addErrorCode(ErrorCode.GENERAL_ERROR);
            	return false;
            }

            return "1".equals(params.get("scriptResult"));
		} catch (Exception e) {
        	logger.error("Velocity error", e);

            params.put("velocity_error", AgnUtils.getUserErrorMessage(e));
            javaMailService.sendVelocityExceptionMail(companyID, (String) params.get("formURL"), e);
			errors.addErrorCode(ErrorCode.GENERAL_ERROR);
			return false;
        }
	}

	@Override
	public final ActionOperationType processedType() {
		return ActionOperationType.EXECUTE_SCRIPT;
	}

}
