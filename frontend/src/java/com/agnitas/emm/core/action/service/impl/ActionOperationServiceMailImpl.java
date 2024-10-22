/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.agnitas.beans.Recipient;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.velocity.VelocityResult;
import org.agnitas.emm.core.velocity.VelocityWrapper;
import org.agnitas.emm.core.velocity.VelocityWrapperFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationServiceMailParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.service.RecipientFieldService;

public class ActionOperationServiceMailImpl implements EmmActionOperation {

	private static final Logger logger = LogManager.getLogger(ActionOperationServiceMailImpl.class);

	private JavaMailService javaMailService;
	private ConfigService configService;

	private BeanLookupFactory beanLookupFactory;
	
	private BlacklistService blacklistService;
	private RecipientService recipientService;
	private RecipientFieldService recipientFieldService;

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) {
		ActionOperationServiceMailParameters op = (ActionOperationServiceMailParameters) operation;
		int companyID = op.getCompanyId();

		if (getRequestParameter(params, "sendServiceMail") != null && getRequestParameter(params, "sendServiceMail").equals("no")) {
			/*
			 * TODO Introduce info flags
			 * 
			 * Cannot use actionOperationErrors.addErrorCode(ErrorCode.SERVICE_MAIL_MANUALLY_BLOCKED);
			 * Adding an error results in a failed execution.
			 * 
			 * Info flags can be used to indicate something like this (blocked sending service mail)
			 * without signaling a failed execution of an action step
			 */
			return true; // do nothing, manually blocked
		}

		Recipient fromCustomer = null;
		if (getRequestParameter(params, "customerID") != null) {
			fromCustomer = beanLookupFactory.getBeanRecipient();
			fromCustomer.setCompanyID(companyID);
			
			if (configService.getBooleanValue(ConfigValue.UseRecipientFieldService, companyID)) {
				fromCustomer.setCustDBStructure(recipientFieldService.getRecipientDBStructure(companyID));
			} else {
				fromCustomer.setCustDBStructure(recipientService.getRecipientDBStructure(companyID));
			}

			Integer tmpNum = Integer.parseInt(getRequestParameter(params, "customerID"));
			fromCustomer.setCustomerID(tmpNum.intValue());
			if (fromCustomer.getCustomerID() != 0) {
            	fromCustomer.setCustParameters(recipientService.getCustomerDataFromDb(companyID, fromCustomer.getCustomerID(), fromCustomer.getDateFormat()));
			}
		}

		String toAddress;
		if (getRequestParameter(params, "sendServiceMailToAdr") != null) {
			toAddress = getRequestParameter(params, "sendServiceMailToAdr");
		} else {
			toAddress = op.getToAddress();
		}
		
		List<String> toAddressList = new ArrayList<>();
		for (String singleAdr : AgnUtils.splitAndTrimList(toAddress)) {
			if (StringUtils.isNotBlank(singleAdr)) {
				singleAdr = singleAdr.toLowerCase();
				if (singleAdr.toLowerCase().startsWith("$requestparameters.")) {
					String requestParameterName = singleAdr.substring("$requestparameters.".length());
					singleAdr = getRequestParameter(params, requestParameterName);
				} else if (singleAdr.toLowerCase().startsWith("$customerdata.")) {
					if (fromCustomer == null) {
						logger.error("Velocity errors: Missing customer");
						actionOperationErrors.addErrorCode(ErrorCode.UNKNOWN_RECIPIENT);
						return false;
					} else {
						String customerParameterName = singleAdr.substring("$customerdata.".length());
						singleAdr = fromCustomer.getCustParametersNotNull(customerParameterName);
					}
				} else {
					singleAdr = AgnUtils.normalizeEmail(singleAdr);
				}
				if (StringUtils.isNotBlank(singleAdr) && blacklistService.blacklistCheck(singleAdr, companyID)) {
					logger.error("Velocity errors: Recipients address is blacklisted: " + singleAdr);
					actionOperationErrors.addErrorCode(ErrorCode.RECEIVER_ADDRESS_BLACKLISTED);
					return false;
				} else {
					toAddressList.add(singleAdr);
				}
			}
		}
		toAddress = StringUtils.join(toAddressList, ", ");

		String fromAddress = null;
		if (StringUtils.isNotBlank(op.getFromAddress())) {
			fromAddress = op.getFromAddress();
			if (fromAddress.toLowerCase().startsWith("$requestparameters.")) {
				String requestParameterName = fromAddress.substring("$requestparameters.".length());
				fromAddress = getRequestParameter(params, requestParameterName);
			} else if (fromAddress.toLowerCase().startsWith("$customerdata.")) {
				if (fromCustomer == null) {
					logger.error("Velocity errors: Missing customer");
					actionOperationErrors.addErrorCode(ErrorCode.UNKNOWN_RECIPIENT);
					return false;
				} else {
					String customerParameterName = fromAddress.substring("$customerdata.".length());
					fromAddress = fromCustomer.getCustParametersNotNull(customerParameterName);
				}
			}
		} else {
			if (fromCustomer != null) {
				fromAddress = fromCustomer.getCustParametersNotNull("EMAIL");
			} else if (getRequestParameter(params, "sendServiceMailFromAdr") != null) {
				fromAddress = getRequestParameter(params, "sendServiceMailFromAdr");
			} else if (getRequestParameter(params, "fromEmail") != null) {
				fromAddress = getRequestParameter(params, "fromEmail");
			}
		}

		if (fromAddress != null) {
			try {
				fromAddress = AgnUtils.checkAndNormalizeEmail(fromAddress);
				if (StringUtils.isNotBlank(fromAddress) && blacklistService.blacklistCheck(fromAddress, companyID)) {
					logger.error("Velocity errors: From address is blacklisted: " + fromAddress);
					actionOperationErrors.addErrorCode(ErrorCode.SENDER_ADDRESS_BLACKLISTED);
					return false;
				}
			} catch (Exception e) {
				logger.error("Velocity errors: " + e.getMessage(), e);
				actionOperationErrors.addErrorCode(ErrorCode.GENERAL_ERROR);
				return false;
			}
		} else {
			actionOperationErrors.addErrorCode(ErrorCode.NO_SENDER_ADDRESS);
			return false;
		}
		
		String replyAddress = null;
		if (StringUtils.isNotBlank(op.getReplyAddress())) {
			replyAddress = op.getReplyAddress();
			if (replyAddress.toLowerCase().startsWith("$requestparameters.")) {
				String requestParameterName = replyAddress.substring("$requestparameters.".length());
				replyAddress = getRequestParameter(params, requestParameterName);
			} else if (replyAddress.toLowerCase().startsWith("$customerdata.")) {
				if (fromCustomer == null) {
					logger.error("Velocity errors: Missing customer");
					actionOperationErrors.addErrorCode(ErrorCode.UNKNOWN_RECIPIENT);
					return false;
				} else {
					String customerParameterName = replyAddress.substring("$customerdata.".length());
					replyAddress = fromCustomer.getCustParametersNotNull(customerParameterName);
				}
			}
			try {
				replyAddress = AgnUtils.normalizeEmail(replyAddress);
				if (StringUtils.isNotBlank(replyAddress) && blacklistService.blacklistCheck(replyAddress, companyID)) {
					logger.error("Velocity errors: Reply address is blacklisted: " + replyAddress);
					actionOperationErrors.addErrorCode(ErrorCode.REPLY_ADDRESS_BLACKLISTED);
					return false;
				}
			} catch (Exception e) {
				logger.error("Error sending service mail", e);
				actionOperationErrors.addErrorCode(ErrorCode.GENERAL_ERROR);
				return false;
			}
		}

		try {
			VelocityWrapperFactory factory = beanLookupFactory.getBeanVelocityWrapperFactory();
			VelocityWrapper velocity = factory.getWrapper(companyID);

			StringWriter emailTextWriter = new StringWriter();
			VelocityResult velocityResult = velocity.evaluate(params, op.getTextMail(), emailTextWriter, 0, op.getActionId());
			if (velocityResult.hasErrors()) {
				logger.error("Velocity errors: " + velocityResult.getErrorMessages());
				actionOperationErrors.addErrorCode(ErrorCode.GENERAL_ERROR);
				return false;
			}
			String emailtext = emailTextWriter.toString();

			StringWriter subjectWriter = new StringWriter();
			velocityResult = velocity.evaluate(params, op.getSubjectLine(), subjectWriter, 0, op.getActionId());
			if (velocityResult.hasErrors()) {
				logger.error("Velocity errors: " + velocityResult.getErrorMessages());
				actionOperationErrors.addErrorCode(ErrorCode.GENERAL_ERROR);
				return false;
			}
			String subject = subjectWriter.toString();

			String emailhtml = null;
			if (op.getMailtype() != MailType.TEXT.getIntValue()) {
				StringWriter emailHtmlWriter = new StringWriter();
				velocityResult = velocity.evaluate(params, op.getHtmlMail(), emailHtmlWriter, 0, op.getActionId());
				if (velocityResult.hasErrors()) {
					logger.error("Velocity errors: " + velocityResult.getErrorMessages());
					actionOperationErrors.addErrorCode(ErrorCode.GENERAL_ERROR);
					return false;
				}
				emailhtml = emailHtmlWriter.toString();
			}

			return javaMailService.sendEmail(companyID, fromAddress, null, replyAddress, null, null, toAddress, null, configService.getValue(ConfigValue.DefaultBccEmail, companyID), subject, emailtext, emailhtml, "iso-8859-1");
		} catch (Exception e) {
			logger.error("Velocity error", e);
			actionOperationErrors.addErrorCode(ErrorCode.GENERAL_ERROR);

			return false;
		}
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.SERVICE_MAIL;
    }

    @SuppressWarnings("unchecked")
	private String getRequestParameter(Map<String, Object> params, String parameterName) {
		Object returnValue = null;
		if (params.containsKey(parameterName)) {
			returnValue = params.get(parameterName);
		} else if (params.containsKey("requestParameters")) {
			returnValue = ((Map<String, Object>) params.get("requestParameters")).get(parameterName); // suppress warning for this cast
		}
		
		if (returnValue == null) {
			return null;
		} else {
			return returnValue.toString();
		}
	}

	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}

	public void setBeanLookupFactory(BeanLookupFactory beanLookupFactory) {
		this.beanLookupFactory = beanLookupFactory;
	}

	public void setBlacklistService(BlacklistService blacklistService) {
		this.blacklistService = blacklistService;
	}

	@Required
	public void setRecipientService(RecipientService recipientService) {
		this.recipientService = recipientService;
	}
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}
	
	public void setRecipientFieldService(RecipientFieldService recipientFieldService) {
		this.recipientFieldService = Objects.requireNonNull(recipientFieldService, "RecipientField Service cannot be null");
	}
}
