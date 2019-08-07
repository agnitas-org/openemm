/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationServiceMailParameters;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import org.agnitas.beans.Recipient;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.velocity.VelocityResult;
import org.agnitas.emm.core.velocity.VelocityWrapper;
import org.agnitas.emm.core.velocity.VelocityWrapperFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ActionOperationServiceMailImpl implements EmmActionOperation {
	/** The logger */
	private static final Logger logger = Logger.getLogger(ActionOperationServiceMailImpl.class);

	private JavaMailService javaMailService;

	private BeanLookupFactory beanLookupFactory;
	
	private BlacklistService blacklistService;

	private ActionOperationServiceMailImpl() {
	}

	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) {
		ActionOperationServiceMailParameters op = (ActionOperationServiceMailParameters) operation;
		int companyID = op.getCompanyId();

		if (getRequestParameter(params, "sendServiceMail") != null && getRequestParameter(params, "sendServiceMail").equals("no")) {
			return true; // do nothing, manually blocked
		}

		Recipient fromCustomer = null;
		if (getRequestParameter(params, "customerID") != null) {
			fromCustomer = beanLookupFactory.getBeanRecipient();
			fromCustomer.setCompanyID(companyID);
			fromCustomer.loadCustDBStructure();
			
			Integer tmpNum = Integer.parseInt(getRequestParameter(params, "customerID"));
			fromCustomer.setCustomerID(tmpNum.intValue());
			if (fromCustomer.getCustomerID() != 0) {
				fromCustomer.getCustomerDataFromDb();
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
					return false;
				}
			} catch (Exception e) {
				logger.error("Velocity errors: " + e.getMessage(), e);
				return false;
			}
		} else {
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
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}

		try {
			VelocityWrapperFactory factory = beanLookupFactory.getBeanVelocityWrapperFactory();
			VelocityWrapper velocity = factory.getWrapper(companyID);

			StringWriter emailTextWriter = new StringWriter();
			VelocityResult velocityResult = velocity.evaluate(params, op.getTextMail(), emailTextWriter, 0, op.getActionId());
			if (velocityResult.hasErrors()) {
				logger.error("Velocity errors: " + velocityResult.getErrors());
				return false;
			}
			String emailtext = emailTextWriter.toString();

			StringWriter subjectWriter = new StringWriter();
			velocityResult = velocity.evaluate(params, op.getSubjectLine(), subjectWriter, 0, op.getActionId());
			if (velocityResult.hasErrors()) {
				logger.error("Velocity errors: " + velocityResult.getErrors());
				return false;
			}
			String subject = subjectWriter.toString();

			String emailhtml = null;
			if (op.getMailtype() != MailType.TEXT.getIntValue()) {
				StringWriter emailHtmlWriter = new StringWriter();
				velocityResult = velocity.evaluate(params, op.getHtmlMail(), emailHtmlWriter, 0, op.getActionId());
				if (velocityResult.hasErrors()) {
					logger.error("Velocity errors: " + velocityResult.getErrors());
					return false;
				}
				emailhtml = emailHtmlWriter.toString();
			}

			return javaMailService.sendEmail(fromAddress, null, replyAddress, null, null, toAddress, null, subject, emailtext, emailhtml, "iso-8859-1");
		} catch (Exception e) {
			logger.error("Velocity error", e);

			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private String getRequestParameter(Map<String, Object> params, String parameterName) {
		Object returnValue = null;
		if (params.containsKey(parameterName)) {
			returnValue = params.get(parameterName);
		} else if (params.containsKey("requestParameters")) {
			returnValue = ((Map<String, Object>) params.get("requestParameters")).get(parameterName);
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
}
