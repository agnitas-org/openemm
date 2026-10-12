/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.beans.Company;
import com.agnitas.beans.DatasourceDescription;
import com.agnitas.beans.Recipient;
import com.agnitas.beans.impl.ViciousFormDataException;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.blacklist.service.BlacklistService;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;
import com.agnitas.emm.core.recipient.exception.SubscriberLimitExceededException;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.mobilephone.MobilephoneNumber;
import com.agnitas.emm.mobilephone.service.MobilephoneNumberWhitelist;
import com.agnitas.emm.push.pushsubscription.service.PushSubscriptionService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionOperationSubscribeCustomerImpl implements EmmActionOperation<ActionOperationSubscribeCustomerParameters> {

    private static final Logger logger = LogManager.getLogger(ActionOperationSubscribeCustomerImpl.class);
    
    public static final String DEFAULT_GENDER = "2";
    public static final String DEFAULT_MAILTYPE = "1";

	private final ExtensibleUIDService uidService;
	private final CompanyDao companyDao;
	private final DatasourceDescriptionDao datasourceDescriptionDao;
	private final RecipientService recipientService;
	private final RecipientFieldService recipientFieldService;
	private final PushSubscriptionService pushSubscriptionService;	// Can be null
	private final MobilephoneNumberWhitelist mobilephoneNumberWhitelist;
	private final BlacklistService blacklistService;
	private final BeanLookupFactory beanLookupFactory;
	private final ConfigService configService;
	private final SubscriberLimitCheck subscriberLimitCheck;

	public ActionOperationSubscribeCustomerImpl(
			ExtensibleUIDService uidService,
			CompanyDao companyDao,
			DatasourceDescriptionDao datasourceDescriptionDao,
			RecipientService recipientService,
			RecipientFieldService recipientFieldService,
			PushSubscriptionService pushSubscriptionService,
			MobilephoneNumberWhitelist mobilephoneNumberWhitelist,
			BlacklistService blacklistService,
			BeanLookupFactory beanLookupFactory,
			ConfigService configService,
			SubscriberLimitCheck subscriberLimitCheck
	) {
		this.uidService = uidService;
		this.companyDao = companyDao;
		this.datasourceDescriptionDao = datasourceDescriptionDao;
		this.recipientService = recipientService;
		this.recipientFieldService = recipientFieldService;
		this.pushSubscriptionService = pushSubscriptionService;
		this.mobilephoneNumberWhitelist = mobilephoneNumberWhitelist;
		this.blacklistService = blacklistService;
		this.beanLookupFactory = beanLookupFactory;
		this.configService = configService;
		this.subscriberLimitCheck = subscriberLimitCheck;
	}

	private int getDatasourceID(int companyID, String form) {
		String description = "Form: " + form;
		DatasourceDescription dsDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.AutoinsertForms, companyID, description);

		if (dsDescription == null) {
			dsDescription = beanLookupFactory.getBeanDatasourceDescription();

			dsDescription.setId(0);
			dsDescription.setCompanyID(companyID);
			dsDescription.setSourceGroupType(SourceGroupType.AutoinsertForms);
			dsDescription.setCreationDate(new java.util.Date());
			dsDescription.setDescription(description);
			dsDescription.setDescription2("ActionOperationSubscribeCustomerImpl");
			datasourceDescriptionDao.save(dsDescription);
		}
		return dsDescription.getId();
	}

	private Recipient prepareRecipient(int companyID, Integer customerID) {
		final Recipient recipient = beanLookupFactory.getBeanRecipient();
		
		recipient.setCompanyID(companyID);
		
		if (configService.getBooleanValue(ConfigValue.UseRecipientFieldService, companyID)) {
			recipient.setCustDBStructure(recipientFieldService.getRecipientDBStructure(companyID));
		} else {
			recipient.setCustDBStructure(recipientService.getRecipientDBStructure(companyID));
		}

		if(customerID != null) {
			recipient.setCustomerID(customerID);
		}
		
		return recipient;
	}
	
	private void identifyRecipientByKeyColumn(Recipient recipient, ActionOperationSubscribeCustomerParameters op, CaseInsensitiveMap<String, Object> reqParams, EmmActionOperationErrors actionOperationErrors, HttpServletRequest request) {
		if (op.isDoubleCheck()) {
			if (op.getKeyColumn() == null) {
				logger.error("Exception: No keyColumn ({})", request == null ? "" : request.getQueryString());
				actionOperationErrors.addErrorCode(ErrorCode.MISSING_KEY_COLUMN);
			} else {
				final String keyVal = (String) reqParams.get(op.getKeyColumn());
				if (keyVal == null) {
					logger.error("Exception: No keyVal for '{}' ({})", op.getKeyColumn(), request == null ? "" : request.getQueryString());
					for (Entry<String, Object> entry : reqParams.entrySet()) {
						logger.error("{}: {}", entry.getKey(), entry.getValue());
					}
					actionOperationErrors.addErrorCode(ErrorCode.MISSING_KEY_VALUE);
				} else {
					recipientService.findByKeyColumn(recipient, op.getKeyColumn(), keyVal);
				}
			}
		}
	}
	
	private boolean isBlankOrNotNumber(String value) {
		return StringUtils.isBlank(value) || !AgnUtils.isNumber(value);
	}
	
	private void checkAndNormalizeMobilePhoneNumber(int companyID, CaseInsensitiveMap<String, Object> reqParams, EmmActionOperationErrors actionOperationErrors) {
		final String PARAM_NAME = "SMSNUMBER";
		
		final String value = (String) reqParams.get(PARAM_NAME);
		
		if(StringUtils.isNotBlank(value)) {
			try {
				final MobilephoneNumber number = new MobilephoneNumber(value.trim());

				// If parsing was successful, check that number is allowed
				if(this.mobilephoneNumberWhitelist.isWhitelisted(number, companyID)) {
					// If number is ok, write back the number. Its in normalized form now.
					reqParams.put(PARAM_NAME, number.toString());
				} else {
					actionOperationErrors.addErrorCode(ErrorCode.MOBILEPHONE_NUMBER_NOT_WHITELISTED);
				}
			} catch(final NumberFormatException e) {
				actionOperationErrors.addErrorCode(ErrorCode.MALFORMED_MOBILEPHONE_NUMBER);
			}
		}
		
	}
	
	@Override
	public boolean execute(
			ActionOperationSubscribeCustomerParameters op,
			Map<String, Object> params,
			EmmActionOperationErrors actionOperationErrors
	) {
		Integer customerId;
		try {
            customerId = parseIntegerParam(params.get("customerID"));
        } catch (Exception e) {
			actionOperationErrors.addErrorCode(ErrorCode.INVALID_CUSTOMER_ID);
			return false;
		}

		final int companyID = op.getCompanyId();

		@SuppressWarnings("unchecked")
		final CaseInsensitiveMap<String, Object> reqParams = new CaseInsensitiveMap<>((Map<String, Object>) params.get("requestParameters")); // suppress warning for this cast

		final Recipient aCust = prepareRecipient(companyID, customerId);
		final HttpServletRequest request = (HttpServletRequest) params.get("_request");
	
		// no agnUID so try to find by key column
		if (aCust.getCustomerID() == 0) {
			identifyRecipientByKeyColumn(aCust, op, reqParams, actionOperationErrors, request);
			
			if(!actionOperationErrors.isEmpty()) {
				return false;
			}
		}
		
		final boolean isNewCust = aCust.getCustomerID() == 0;

		// when a customer id was found, load
		if (aCust.getCustomerID() != 0) {
			aCust.setCustParameters(recipientService.getCustomerDataFromDb(companyID, aCust.getCustomerID(), aCust.getDateFormat()));
		} else {
			// Check if gender parameter has a valid value set
			if (isBlankOrNotNumber((String) reqParams.get("gender"))) {
				reqParams.put("GENDER", DEFAULT_GENDER);
			}

			// Check if mailtype parameter has a valid value set
			if (isBlankOrNotNumber((String) reqParams.get("mailtype"))) {
				reqParams.put("MAILTYPE", DEFAULT_MAILTYPE);
			}
		}

		// Check if datasourceid parameter has a valid value set
		if (isBlankOrNotNumber((String) reqParams.get("DATASOURCE_ID"))) {
			// else the default datasourceid of the form will be used (which is
			// the standard case)
			final int datasourceID = getDatasourceID(companyID, (String) reqParams.get("agnFN"));
			reqParams.put("DATASOURCE_ID", Integer.toString(datasourceID));
		}
		
		checkAndNormalizeMobilePhoneNumber(companyID, reqParams, actionOperationErrors);
		if(!actionOperationErrors.isEmpty()) {
			return false;
		}

		if (configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, companyID)) {
			reqParams.put(RecipientStandardField.DoNotTrack.getColumnName(), "1");
		}

		// copy the request parameters into the customer
		if (!recipientService.importRequestParameters(aCust, reqParams, null)) {
			return false;
		}

		// is the email valid and not blacklisted?

		if(!AgnUtils.isEmailValid(aCust.getEmail())) {
			actionOperationErrors.addErrorCode(ErrorCode.EMAIL_ADDRESS_INVALID);
			return false;
		}

		if (blacklistService.blacklistCheck(aCust.getEmail(), aCust.getCompanyID())) {
			actionOperationErrors.addErrorCode(ErrorCode.EMAIL_ADDRESS_BLACKLISTED);
			return false; // abort, EMAIL is not allowed
		}

		try {
			if (aCust.getCustomerID() == 0) {
				subscriberLimitCheck.checkSubscriberLimit(aCust.getCompanyID());
			}
			
			if (!recipientService.updateRecipientInDB(aCust)) {
				// return error on failure
				return false;
			}
		} catch(final SubscriberLimitExceededException e) {
			actionOperationErrors.addErrorCode(ErrorCode.SUBSCRIBER_LIMIT_EXCEEDED);
			
			throw e;
		} catch (ViciousFormDataException dataException) {
			throw dataException;
		} catch (Exception e) {
			logger.error("Cannot create customer: {}", e.getMessage(), e);
			beanLookupFactory.getBeanJavaMailService().sendExceptionMail(companyID, "Cannot create customer: " + e.getMessage(), e);
			return false;
		}
		
		aCust.setCustParameters(reqParams);
		recipientService.updateDataSource(aCust);

		aCust.setListBindings(recipientService.getMailinglistBindings(companyID, aCust.getCustomerID()));
		try {
			recipientService.updateBindingsFromRequest(aCust, params, op.isDoubleOptIn(), request == null ? null : request.getRemoteAddr(), HttpUtils.getReferrer(request));
		} catch (ViciousFormDataException dataException) {
			// Delete the customer, which has vicious data (hacker?)
			recipientService.deleteCustomerDataFromDb(companyID, aCust.getCustomerID());
			throw dataException;
		} catch (Exception e) {
			logger.error("Cannot create customer binding: {}", e.getMessage(), e);
			beanLookupFactory.getBeanJavaMailService().sendExceptionMail(companyID, "Cannot create customer binding: " + e.getMessage(), e);
			return false;
		}

		if (op.isDoubleOptIn()) {
			// next Event-Mailing goes to a user with status 5
			params.put("__agn_USER_STATUS", "5");
		}

		params.put("customerID", aCust.getCustomerID());

		if (reqParams.containsKey("PUSH_ENDPOINT")) {
			associateWithPushEndpoint(aCust.getCustomerID(), aCust.getCompanyID(), (String) reqParams.get("PUSH_ENDPOINT"));
		}

		if (isNewCust && aCust.getCustomerID() != 0) {
			// generate new agnUID
			try {
				final Company company = companyDao.getCompany(companyID);
				
				final ExtensibleUID uid = UIDFactory.from(configService.getLicenseID(), aCust);

				if (company != null) {
					params.put("agnUID", uidService.buildUIDString(uid));
				}
			} catch (Exception e) {
				logger.error("problem generating new UID:", e);
			}
		}

		return true;
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.SUBSCRIBE_CUSTOMER;
    }

    private void associateWithPushEndpoint(int customerID, int companyID, String endpoint) {
		try {
			if(pushSubscriptionService != null) {
				pushSubscriptionService.associateWithCustomerID(endpoint, customerID, companyID);
			}
		} catch (Exception e) {
			logger.info("Cannot associate customer ID {} (company {}) with push endpoint '{}'", customerID, companyID, endpoint, e);
		}
	}

}
