/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.agnitas.beans.DatasourceDescription;
import com.agnitas.beans.Recipient;
import com.agnitas.beans.impl.ViciousFormDataException;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import org.agnitas.emm.core.recipient.service.SubscriberLimitExceededException;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.HttpUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.beans.Company;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.mobilephone.MobilephoneNumber;
import com.agnitas.emm.mobilephone.service.MobilephoneNumberWhitelist;
import com.agnitas.emm.push.pushsubscription.service.PushSubscriptionService;

import jakarta.servlet.http.HttpServletRequest;

public class ActionOperationSubscribeCustomerImpl implements EmmActionOperation {

    private static final Logger logger = LogManager.getLogger(ActionOperationSubscribeCustomerImpl.class);
    
    public static final String DEFAULT_GENDER = "2";
    public static final String DEFAULT_MAILTYPE = "1";

	private ExtensibleUIDService uidService;
	private CompanyDao companyDao;
	private DatasourceDescriptionDao datasourceDescriptionDao;
	private RecipientService recipientService;
	private RecipientFieldService recipientFieldService;
	private PushSubscriptionService pushSubscriptionService;	// Can be set to null
	private MobilephoneNumberWhitelist mobilephoneNumberWhitelist;

	protected BlacklistService blacklistService;

	private BeanLookupFactory beanLookupFactory;
	private ConfigService configService;
	
	private SubscriberLimitCheck subscriberLimitCheck;
	
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

	private final Recipient prepareRecipient(final int companyID, final Integer customerID) {
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
	
	private final void identifyRecipientByKeyColumn(final Recipient recipient, final ActionOperationSubscribeCustomerParameters op, final CaseInsensitiveMap<String, Object> reqParams, final EmmActionOperationErrors actionOperationErrors, final HttpServletRequest request) {
		if (op.isDoubleCheck()) {
			if (op.getKeyColumn() == null) {
				logger.error(String.format("Exception: No keyColumn (%s)", request == null ? "" : request.getQueryString()));
				actionOperationErrors.addErrorCode(ErrorCode.MISSING_KEY_COLUMN);
			} else {
				final String keyVal = (String) reqParams.get(op.getKeyColumn());
				if (keyVal == null) {
					logger.error(String.format("Exception: No keyVal for '%s' (%s)", op.getKeyColumn(), request == null ? "" : request.getQueryString()));
					for (Entry<String, Object> entry : reqParams.entrySet()) {
						logger.error(entry.getKey() + ": " + entry.getValue().toString());
					}
					actionOperationErrors.addErrorCode(ErrorCode.MISSING_KEY_VALUE);
				} else {
					recipientService.findByKeyColumn(recipient, op.getKeyColumn(), keyVal);
				}
			}
		}
	}
	
	private final boolean isBlankOrNotNumber(final String value) {
		return StringUtils.isBlank(value) || !AgnUtils.isNumber(value);
	}
	
	private final void checkAndNormalizeMobilePhoneNumber(final int companyID, final CaseInsensitiveMap<String, Object> reqParams, final EmmActionOperationErrors actionOperationErrors) {
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
					actionOperationErrors.addErrorCode(ErrorCode.MOBILEPHONE_NUMBER_NOT_ALLOWED);
				}
			} catch(final NumberFormatException e) {
				actionOperationErrors.addErrorCode(ErrorCode.MALFORMED_MOBILEPHONE_NUMBER);
			}
		}
		
	}
	
	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) {
		final ActionOperationSubscribeCustomerParameters op = (ActionOperationSubscribeCustomerParameters) operation;
		final int companyID = op.getCompanyId();

		@SuppressWarnings("unchecked")
		final CaseInsensitiveMap<String, Object> reqParams = new CaseInsensitiveMap<>((Map<String, Object>) params.get("requestParameters")); // suppress warning for this cast

		final Recipient aCust = prepareRecipient(companyID, (Integer) params.get("customerID"));
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
			actionOperationErrors.addErrorCode(ErrorCode.EMAIL_ADDRESS_NOT_ALLOWED);
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
			logger.error("Cannot create customer: " + e.getMessage(), e);
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
			logger.error("Cannot create customer binding: " + e.getMessage(), e);
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
				logger.error("problem generating new UID: " + e, e);
			}
		}

		return true;
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.SUBSCRIBE_CUSTOMER;
    }

    private final void associateWithPushEndpoint(final int customerID, final int companyID, final String endpoint) {
		try {
			if(pushSubscriptionService != null) {
				pushSubscriptionService.associateWithCustomerID(endpoint, customerID, companyID);
			}
		} catch(final Exception e) {
			if (logger.isInfoEnabled()) {
				final String msg = String.format("Cannot associate customer ID %d (company %d) with push endpoint '%s'", customerID, companyID, endpoint);
				logger.info(msg, e);
			}
		}
	}

	public final void setUidService(final ExtensibleUIDService service) {
		this.uidService = Objects.requireNonNull(service, "UID service cannot be null");
	}

	public final void setCompanyDao(final CompanyDao dao) {
		this.companyDao = Objects.requireNonNull(dao, "Company DAO cannot be null");
	}

	public final void setDatasourceDescriptionDao(final DatasourceDescriptionDao dao) {
		this.datasourceDescriptionDao = Objects.requireNonNull(dao, "Datasource description DAO cannot be null");
	}

	public void setRecipientService(RecipientService recipientService) {
		this.recipientService = Objects.requireNonNull(recipientService, "Recipient Service cannot be null");
	}
	
	public void setRecipientFieldService(RecipientFieldService recipientFieldService) {
		this.recipientFieldService = Objects.requireNonNull(recipientFieldService, "RecipientField Service cannot be null");
	}

	public void setBlacklistService(BlacklistService blacklistService) {
		this.blacklistService = Objects.requireNonNull(blacklistService, "Blacklist Service cannot be null");
	}

	public final void setBeanLookupFactory(final BeanLookupFactory factory) {
		this.beanLookupFactory = Objects.requireNonNull(factory, "Bean lookup factory cannot be null");
	}

	public final void setPushSubscriptionService(final PushSubscriptionService service) {
		this.pushSubscriptionService = service;
	}
	
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
	
	public final void setMobilephoneNumberWhitelist(final MobilephoneNumberWhitelist whitelist) {
		this.mobilephoneNumberWhitelist = Objects.requireNonNull(whitelist, "Mobilephone whitelist is null");
	}
	
	public final void setSubscriberLimitCheck(final SubscriberLimitCheck check) {
		this.subscriberLimitCheck = Objects.requireNonNull(check, "subscriberLimitCheck");
	}
}
