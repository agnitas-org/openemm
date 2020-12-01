/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.ViciousFormDataException;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.beans.ComCompany;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComDatasourceDescriptionDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.mobilephone.MobilephoneNumber;
import com.agnitas.emm.mobilephone.service.MobilephoneNumberWhitelist;
import com.agnitas.emm.push.pushsubscription.service.PushSubscriptionService;

public class ActionOperationSubscribeCustomerImpl implements EmmActionOperation {
    private static final Logger logger = Logger.getLogger(ActionOperationSubscribeCustomerImpl.class);
    
    public static final String DEFAULT_GENDER = "2";
    public static final String DEFAULT_MAILTYPE = "1";

	private ExtensibleUIDService uidService;
	private ComCompanyDao companyDao;
	private ComDatasourceDescriptionDao datasourceDescriptionDao;
	private ComRecipientDao recipientDao;
	private PushSubscriptionService pushSubscriptionService;	// Can be set to null
	private MobilephoneNumberWhitelist mobilephoneNumberWhitelist;

	private BeanLookupFactory beanLookupFactory;
	private ConfigService configService;
	
	/**
	 * Private constructor to prevent invalid instantiation
	 */
	private ActionOperationSubscribeCustomerImpl() {}

	private int getDatasourceID(int companyID, String form) {
		String description = "Form: " + form;
		DatasourceDescription dsDescription = datasourceDescriptionDao.getByDescription(4, companyID, description);

		if (dsDescription == null) {
			dsDescription = beanLookupFactory.getBeanDatasourceDescription();

			dsDescription.setId(0);
			dsDescription.setCompanyID(companyID);
			dsDescription.setSourcegroupID(4);
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
		recipient.loadCustDBStructure();

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
					recipient.findByKeyColumn(op.getKeyColumn(), keyVal);
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
		final CaseInsensitiveMap<String, Object> reqParams = new CaseInsensitiveMap<>((Map<String, Object>) params.get("requestParameters"));

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
			aCust.getCustomerDataFromDb();
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

		// copy the request parameters into the customer
		if (!aCust.importRequestParameters(reqParams, null)) {
			return false;
		}

		// is the email valid and not blacklisted?
		if(!aCust.emailValid()) {
			actionOperationErrors.addErrorCode(ErrorCode.EMAIL_ADDRESS_INVALID);
			return false;
		}
		if (aCust.blacklistCheck()) {
			actionOperationErrors.addErrorCode(ErrorCode.EMAIL_ADDRESS_NOT_ALLOWED);
			return false; // abort, EMAIL is not allowed
		}

		try {
			if (!aCust.updateInDB()) {
				// return error on failure
				return false;
			}
		} catch (ViciousFormDataException dataException) {
			throw dataException;
		} catch (Exception e) {
			logger.error("Cannot create customer: " + e.getMessage(), e);
			beanLookupFactory.getBeanJavaMailService().sendExceptionMail("Cannot create customer: " + e.getMessage(), e);
			return false;
		}
		
		aCust.setCustParameters(reqParams);
		recipientDao.updateDataSource(aCust);

		aCust.loadAllListBindings();
		try {
			aCust.updateBindingsFromRequest(params, op.isDoubleOptIn(), request == null ? null : request.getRemoteAddr(), HttpUtils.getReferrer(request));
		} catch (ViciousFormDataException dataException) {
			// Delete the customer, which has vicious data (hacker?)
			aCust.deleteCustomerDataFromDb();
			throw dataException;
		} catch (Exception e) {
			logger.error("Cannot create customer binding: " + e.getMessage(), e);
			beanLookupFactory.getBeanJavaMailService().sendExceptionMail("Cannot create customer binding: " + e.getMessage(), e);
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
				final ComCompany company = companyDao.getCompany(companyID);
				
				final ComExtensibleUID uid = UIDFactory.from(configService.getLicenseID(), aCust);

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

	public final void setCompanyDao(final ComCompanyDao dao) {
		this.companyDao = Objects.requireNonNull(dao, "Company DAO cannot be null");
	}

	public final void setDatasourceDescriptionDao(final ComDatasourceDescriptionDao dao) {
		this.datasourceDescriptionDao = Objects.requireNonNull(dao, "Datasource description DAO cannot be null");
	}

	public final void setRecipientDao(final ComRecipientDao dao) {
		this.recipientDao = Objects.requireNonNull(dao, "Recipient DAO cannot be null");
	}
	
	public final void setBeanLookupFactory(final BeanLookupFactory factory) {
		this.beanLookupFactory = Objects.requireNonNull(factory, "Bean lookup factory cannot be null");
	}

	@Required
	public final void setPushSubscriptionService(final PushSubscriptionService service) {
		this.pushSubscriptionService = service;
	}
	
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
	
	/**
	 * 
	 */
	@Required
	public final void setMobilephoneNumberWhitelist(final MobilephoneNumberWhitelist whitelist) {
		this.mobilephoneNumberWhitelist = Objects.requireNonNull(whitelist, "Mobilephone whitelist is null");
	}
}
