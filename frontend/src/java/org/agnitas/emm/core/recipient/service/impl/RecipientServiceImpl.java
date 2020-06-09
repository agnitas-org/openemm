/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComProfileField;
import com.agnitas.beans.ComRecipientMailing;
import com.agnitas.beans.impl.ComAdminImpl;
import com.agnitas.beans.impl.ComRecipientLiteImpl;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingException;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.profilefields.ProfileFieldBulkUpdateException;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.recipient.dto.RecipientFieldDto;
import com.agnitas.emm.core.recipient.service.DuplicatedRecipientsExportWorker;
import com.agnitas.emm.core.recipient.service.FieldsSaveResults;
import com.agnitas.emm.core.recipient.service.RecipientWorkerFactory;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.ProfileField;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.RecipientUtils;
import org.agnitas.emm.core.recipient.dto.RecipientLightDto;
import org.agnitas.emm.core.recipient.service.InvalidDataException;
import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientNotExistException;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.recipient.service.RecipientsModel;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.validator.annotation.Validate;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.ImportException;
import org.agnitas.service.RecipientQueryBuilder;
import org.agnitas.service.RecipientSqlOptions;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CaseInsensitiveSet;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_CUSTOMER_ID;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_EMAIL;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FIRSTNAME;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_LASTNAME;

public class RecipientServiceImpl implements RecipientService {

	/**
	 * The logger.
	 */
	private static final transient Logger logger = Logger.getLogger(RecipientServiceImpl.class);
	
	
	public static final List<String> DEFAULT_COLUMNS = Arrays.asList(
			COLUMN_CUSTOMER_ID,
			COLUMN_EMAIL,
			COLUMN_FIRSTNAME,
			COLUMN_LASTNAME
	);

	private ComRecipientDao recipientDao;
	private ComCompanyDao companyDao;
	private ExtensibleUIDService uidService;
	private ComTargetService targetService;
	private RecipientFactory recipientFactory;
	private ConfigService configService;
	private SendActionbasedMailingService sendActionbasedMailingService;
	private ComProfileFieldDao profileFieldDao;
	private ProfileFieldValidationService profileFieldValidationService;
	protected ColumnInfoService columnInfoService;
	protected RecipientQueryBuilder recipientQueryBuilder;
	private RecipientWorkerFactory recipientWorkerFactory;


	@Required
	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}
	
	@Required
	public void setRecipientQueryBuilder(RecipientQueryBuilder recipientQueryBuilder) {
		this.recipientQueryBuilder = recipientQueryBuilder;
	}
	
	@Required
	public void setRecipientWorkerFactory(RecipientWorkerFactory recipientWorkerFactory) {
		this.recipientWorkerFactory = recipientWorkerFactory;
	}
	
	@Override
	@Transactional
	public int findSubscriber(@VelocityCheck int companyId, String keyColumn, String value) {
		try {
			return recipientDao.findByColumn(companyId, keyColumn, value);
		} catch (RuntimeException e) {
			logger.error("Exception", e);
			throw e;
		}
	}

	@Override
	@Transactional
	public void checkColumnsAvailable(RecipientModel model) throws ProfileFieldNotExistException {
		try {
			CaseInsensitiveMap<String, ProfileField> availableProfileFields = recipientDao.getAvailableProfileFields(model.getCompanyId());
			for (String columnName : model.getColumns()) {
				if (!availableProfileFields.containsKey(columnName)) {
					throw new ProfileFieldNotExistException(columnName);
				}
			}
		} catch (Exception e) {
			logger.error("Exception", e);
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	@Transactional
	@Validate("deleteSubscriber") // TODO Check, why use "deleteSubscriber" validation rule here???
	public Map<String, Object> getSubscriber(RecipientModel model) {
		Set<String> columns = model.getColumns();
		if (CollectionUtils.isEmpty(columns)) {
			return recipientDao.getCustomerDataFromDb(model.getCompanyId(), model.getCustomerId());
		} else {
			return recipientDao.getCustomerDataFromDb(model.getCompanyId(), model.getCustomerId(), columns);
		}
	}
	
	@Override
	@Transactional
	@Validate("deleteSubscriber") // TODO Validation rule taken from getSubscriber() above. Check, if this is correct
	public Recipient getRecipient(final RecipientModel model) throws RecipientNotExistException {
		return getRecipient(model.getCompanyId(), model.getCustomerId());
	}
	
	@Override
	@Transactional
	@Validate("deleteSubscriber") // TODO Validation rule taken from getSubscriber() above. Check, if this is correct
	public Recipient getRecipient(final int companyID, final int customerID) throws RecipientNotExistException {
		final Recipient recipient = recipientFactory.newRecipient(companyID);
		recipient.setCustomerID(customerID);
		
		recipient.getCustomerDataFromDb();
		
		return recipient;
	}

	@Override
	public boolean isMailTrackingEnabled(int companyId) {
		return companyId > 0 && recipientDao.isMailtrackingEnabled(companyId);
	}

	@Override
	@Transactional
	public List<Integer> getSubscribers(RecipientsModel model) {
		return recipientDao.getCustomerDataFromDb(model.getCompanyId(), model.isMatchAll(), model.getCriteriaEquals());
	}

	@Override
	@Transactional
	public int getSubscribersSize(RecipientsModel model) {
		return recipientDao.getSizeOfCustomerDataFromDbList(model.getCompanyId(), model.isMatchAll(), model.getCriteriaEquals());
	}

	@Override
	public int getNumberOfRecipients(int companyId) {
		return recipientDao.getNumberOfRecipients(companyId, true);
	}

	@Override
	@Transactional
	public List<Map<String, Object>> getSubscriberMailings(RecipientModel model) {
		if (!recipientDao.exist(model.getCustomerId(), model.getCompanyId())) {
			throw new RecipientNotExistException();
		}

		List<ComRecipientMailing> recipientMailings = recipientDao.getMailingsSentToRecipient(model.getCustomerId(), model.getCompanyId());
		if (CollectionUtils.isNotEmpty(recipientMailings)) {
			final int licenseID = this.configService.getLicenseID();
			final ComExtensibleUID uid = UIDFactory.from(licenseID, model.getCompanyId(), model.getCustomerId());

			final String redirectUrl = prepareEmmFormRedirect(model.getCompanyId());

			return recipientMailings
					.stream()
					.map(mailing -> getBasicDataMap(mailing, uid, redirectUrl))
					.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	private Map<String, Object> getBasicDataMap(final ComRecipientMailing mailing, final ComExtensibleUID uid, final String redirectUrl) {
		final Map<String, Object> basicDataMap = new HashMap<>();

		basicDataMap.put("mailing_id", mailing.getMailingId());
		basicDataMap.put("senddate", mailing.getSendDate());
		basicDataMap.put("mailing_type", mailing.getMailingType());
		basicDataMap.put("mailing_name", mailing.getShortName());
		basicDataMap.put("mailing_subject", mailing.getSubject());
		basicDataMap.put("openings", mailing.getNumberOfOpenings());
		basicDataMap.put("clicks", mailing.getNumberOfClicks());

		if (StringUtils.isNotEmpty(redirectUrl)) {
			final ComExtensibleUID newUID = UIDFactory.copyWithNewMailingID(uid, mailing.getMailingId());
			
			basicDataMap.put("fullview_url", redirectUrl + "&agnUID=" + buildUid(newUID));
		}

		return basicDataMap;
	}

	private String prepareEmmFormRedirect(int companyId) {
		String redirectUrl = companyDao.getRedirectDomain(companyId);
		if (StringUtils.isNotEmpty(redirectUrl)) {
			if (!redirectUrl.endsWith("/")) {
				redirectUrl += "/";
			}
			return redirectUrl + "form.do?agnCI=" + companyId + "&agnFN=fullview";
		}
		return null;
	}

	private String buildUid(final ComExtensibleUID uid) {
		try {
			return uidService.buildUIDString(uid);
		} catch (UIDStringBuilderException | RequiredInformationMissingException e) {
			logger.error("Error occurred on UID generation: " + e.getMessage(), e);
			return "";
		}
	}

	@Override
	@Transactional
	@Validate("addSubscriber")
	public int addSubscriber(RecipientModel model, final String username, final int companyId, List<UserAction> userActions) throws Exception {
		recipientDao.checkParameters(model.getParameters(), model.getCompanyId());

		int returnValue = 0;
		int tmpCustID = 0;

		model.setEmail(model.getEmail().toLowerCase());

		final Recipient newCustomer = recipientFactory.newRecipient(model.getCompanyId());
		newCustomer.loadCustDBStructure();

		Map<String, ProfileField> columnInfo = columnInfoService.getColumnInfoMap(model.getCompanyId());
		for (Entry<String, Object> entry : model.getParameters().entrySet()) {
			String name = entry.getKey();
			String value = (String) entry.getValue();
			
			ProfileField profileField = columnInfo.get(name);
			String typeName = profileField.getDataType();

			if (AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR)
					|| AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH)
					|| AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY)
					|| AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR)
					|| AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE)
					|| AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND)) {
				// Special Integer field (WATCH OUT!!!: typeName is null)
				if (StringUtils.isNotEmpty(value) && !AgnUtils.isNumber(value)) {
					throw new InvalidDataException("Invalid data for integer field '" + name + "': '" + value + "'");
				} else {
					newCustomer.setCustParameters(name, value);
				}
			} else if (StringUtils.isBlank(typeName)) {
				throw new InvalidDataException("Unknown data field '" + name + "'");
			} else if (typeName.toUpperCase().startsWith("VARCHAR") || typeName.toUpperCase().startsWith("CHAR")) {
				// Alphanumeric field
				if (recipientDao.isOracleDB()) {
					// MySQL VARCHAR size is in bytes
					if (value != null && value.getBytes("UTF-8").length > profileField.getDataTypeLength()) {
						throw new InvalidDataException("Value size for alphanumeric field '" + name + "' exceeded. Maximum size: " + profileField.getDataTypeLength() + ", Actual size: " + value.length());
					}
				} else {
					// MySQL VARCHAR size is in characters
					if (value != null && value.length() > profileField.getDataTypeLength()) {
						throw new InvalidDataException("Value size for alphanumeric field '" + name + "' exceeded. Maximum size: " + profileField.getDataTypeLength() + ", Actual size: " + value.length());
					}
				}
				newCustomer.setCustParameters(name, value);
			} else if (typeName.toUpperCase().startsWith("CLOB")) {
				// CLOB field
				newCustomer.setCustParameters(name, value);
			} else if (typeName.toUpperCase().contains("DATE") || typeName.toUpperCase().contains("TIME")) {
				// Date field
				Date newValue = null;
				if (StringUtils.isNotEmpty(value)) {
					try {
						newValue = DateUtilities.parseIso8601DateTimeString(value);
					} catch (Exception e) {
						throw new InvalidDataException("Invalid data for date field '" + name + "': '" + newValue + "'", e);
					}
				}
				if (newValue != null) {
					Calendar calendar = new GregorianCalendar();
					calendar.setTime(newValue);
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(calendar.get(Calendar.YEAR)));
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(calendar.get(Calendar.MONTH) + 1));
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, Integer.toString(calendar.get(Calendar.MINUTE)));
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, Integer.toString(calendar.get(Calendar.SECOND)));
				} else {
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, "");
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, "");
					newCustomer.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, "");
				}
			} else {
				// Numeric field
				if (StringUtils.isNotEmpty(value) && !AgnUtils.isDouble(value)) {
					throw new InvalidDataException("Invalid data for " + typeName + " field '" + name + "': '" + value + "'");
				} else {
					newCustomer.setCustParameters(name, value);
				}
			}
		}

		supplySourceID(newCustomer, username, companyId);

		final String keyColumn = model.getKeyColumn().toLowerCase();
		final String keyColumnValue = (String) model.getParameters().get(model.getKeyColumn());

		if (model.isDoubleCheck()) {
			tmpCustID = recipientDao.findByColumn(newCustomer.getCompanyID(), keyColumn, keyColumnValue);
			if (tmpCustID == 0) {
				returnValue = recipientDao.insertNewCust(newCustomer);
				userActions.add(new UserAction("create recipient", keyColumnValue + " (" + returnValue + ")"));
			} else {
				returnValue = tmpCustID;
				if (model.isOverwrite()) {
					newCustomer.setCustomerID(tmpCustID);
					CaseInsensitiveMap<String, Object> dataFromDb = recipientDao.getCustomerDataFromDb(model.getCompanyId(), tmpCustID);
					Map<String, Object> parameters = newCustomer.getCustParameters();
					StringBuilder description = new StringBuilder();
					String value, oldValue;
					for (Entry<String, Object> entry : dataFromDb.entrySet()) {
						if (!parameters.containsKey(entry.getKey())) {
							parameters.put(entry.getKey(), entry.getValue());
						} else {
							value = (String) parameters.get(entry.getKey());
							oldValue = (String) entry.getValue();
							if (!value.equals(oldValue)) {
								description.setLength(0);
								description
										.append("Recipient ")
										.append(keyColumnValue)
										.append(" (")
										.append(returnValue)
										.append("). ")
										.append(entry.getKey())
										.append(" changed from ")
										.append(oldValue)
										.append(" to ")
										.append(value);
								userActions.add(new UserAction("edit recipient", description.toString()));
							}
						}
					}
					recipientDao.updateInDB(newCustomer);
				}
			}
		} else {
			returnValue = recipientDao.insertNewCust(newCustomer);
			userActions.add(new UserAction("create recipient", keyColumnValue + " (" + returnValue + ")"));
		}
		return returnValue;
	}

	@Override
	@Transactional
	@Validate("updateSubscriber")
	public boolean updateSubscriber(RecipientModel model, String username) throws Exception {
		recipientDao.checkParameters(model.getParameters(), model.getCompanyId());

		final ComAdmin admin = new ComAdminImpl();
		admin.setUsername(username);

		final Recipient aCust = this.recipientFactory.newRecipient(model.getCompanyId());
		aCust.setCustomerID(model.getCustomerId());

		Map<String, Object> data = recipientDao.getCustomerDataFromDb(aCust.getCompanyID(), model.getCustomerId());

		String email = model.getEmail();
		if (email != null) {
			model.setEmail(email.toLowerCase());
		}

		aCust.setCustParameters(data);
		aCust.setCustomerID(model.getCustomerId());

		Map<String, String> customerFieldTypes = aCust.getCustDBStructure();
		for (Entry<String, Object> entry : model.getParameters().entrySet()) {
			String name = entry.getKey();
			String value = (String) entry.getValue();
			
			String oldValue = (String) data.get(name);
			String typeName = customerFieldTypes.get(name);

			if (AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR)
					|| AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH)
					|| AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY)
					|| AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR)
					|| AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE)
					|| AgnUtils.endsWithIgnoreCase(name, ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND)) {
				// Special Integer field (WATCH OUT!!!: typeName is null)
				if (StringUtils.isNotEmpty(value) && !AgnUtils.isNumber(value)) {
					throw new InvalidDataException("Invalid data for integer field '" + name + "': '" + value + "'");
				} else if (!value.equals(oldValue)) {
					aCust.setCustParameters(name, value);
				}
			} else if (StringUtils.isBlank(typeName)) {
				throw new InvalidDataException("Unknown data field '" + name + "'");
			} else if (typeName.toUpperCase().startsWith("VARCHAR") || typeName.toUpperCase().startsWith("CHAR") || typeName.toUpperCase().startsWith("CLOB")) {
				// Alphanumeric field
				if (!value.equals(oldValue)) {
					aCust.setCustParameters(name, value);
				}
			} else if (typeName.toUpperCase().contains("DATE") || typeName.toUpperCase().contains("TIME")) {
				// Date field
				Date newValue = null;
				if (StringUtils.isNotEmpty(value)) {
					try {
						newValue = DateUtilities.parseIso8601DateTimeString(value);
					} catch (Exception e) {
						throw new InvalidDataException("Invalid data for " + typeName + " field '" + name + "': '" + value + "'");
					}
				}
				if (newValue != null) {
					if (!new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS).format(newValue).equals(oldValue)) {
						Calendar calendar = new GregorianCalendar();
						calendar.setTime(newValue);
						aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(calendar.get(Calendar.YEAR)));
						aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(calendar.get(Calendar.MONTH) + 1));
						aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
						aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
						aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, Integer.toString(calendar.get(Calendar.MINUTE)));
						aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, Integer.toString(calendar.get(Calendar.SECOND)));
					}
				} else if (StringUtils.isNotBlank(oldValue)) {
					aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
					aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
					aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
					aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, "");
					aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, "");
					aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, "");
				}
			} else {
				// Numeric field
				if (StringUtils.isNotEmpty(value) && !AgnUtils.isDouble(value)) {
					throw new InvalidDataException("Invalid data for " + typeName + " field '" + name + "': '" + value + "'");
				} else if (!value.equals(oldValue)) {
					aCust.setCustParameters(name, value);
				}
			}
		}

		return recipientDao.updateInDB(aCust, false);        // Don't set unspecified profile fields to null
	}


	@Override
	public final void updateRecipientWithEmailChangeConfiguration(final Recipient recipient, final int mailingID, final String profileFieldForConfirmationCode) throws Exception {
		final Map<String, Object> currentData = this.recipientDao.getCustomerDataFromDb(recipient.getCompanyID(), recipient.getCustomerID());
		
		final boolean emailChanged = !StringUtils.equals(currentData.get("email").toString(), recipient.getCustParametersNotNull("email"));
		
		if (emailChanged) {
			// If email changed, (1) backup new address...
			final String newEmailAddress = recipient.getCustParametersNotNull("email");
			
			// (2) ... restore current address...
			recipient.setCustParameters("email", currentData.get("email").toString());

			// (3) ... save recipient with current email address...
			if (!this.recipientDao.updateInDB(recipient)) {
				throw new Exception(String.format("Error saving recipient %d (company ID %d)", recipient.getCustomerID(), recipient.getCompanyID()));
			}
			
			// (4) ... store new address to pending table ...
			final String confirmationCode = recordEmailAddressChangeRequest(recipient.getCompanyID(), recipient.getCustomerID(), newEmailAddress, mailingID);
			
			// (5) ... and send confirmation mail
			final MailgunOptions mailgunOptions = new MailgunOptions()
					.withDifferentRecipientEmailAddress(newEmailAddress)
					.withProfileFieldValue(profileFieldForConfirmationCode, confirmationCode);
			
			this.sendActionbasedMailingService.sendActionbasedMailing(recipient.getCompanyID(), mailingID, recipient.getCustomerID(), 0, mailgunOptions);
		} else {
			this.recipientDao.updateInDB(recipient);
		}
	}
	
	private final String recordEmailAddressChangeRequest(final int companyID, final int customerID, final String newEmailAddress, final int mailingID) throws SendActionbasedMailingException {
		final UUID uuid = UUID.randomUUID();
		final String confirmationCode = uuid.toString();
		
		this.recipientDao.writeEmailAddressChangeRequest(companyID, customerID, newEmailAddress, confirmationCode);
		
		return confirmationCode;
	}

	@Override
	public final void confirmEmailAddressChange(final ComExtensibleUID uid, final String confirmationCode) throws Exception {
		final String newEmailAddress = readEmailAddressForPendingChangeRequest(uid, confirmationCode);

		if (newEmailAddress != null) {
			final Recipient recipient = this.getRecipient(uid.getCompanyID(), uid.getCustomerID());
			recipient.setCustParameters("email", newEmailAddress);
			
			if (recipient.updateInDB()) {
				deletePendingEmailAddressChangeRequest(uid, confirmationCode);
			} else {
				final String msg = String.format("Cannot change email address for pending change requence (customer %d, company %d, confirmation code '%s')", uid.getCustomerID(), uid.getCompanyID(), confirmationCode);
				logger.error(msg);
				
				throw new Exception(msg);
			}
		} else {
			final String msg = String.format("Request to change email to null (customer %d, company %d, confirmation code '%s')", uid.getCustomerID(), uid.getCompanyID(), confirmationCode);
			logger.error(msg);
			
			throw new Exception(msg);
		}
	}
	
	private final String readEmailAddressForPendingChangeRequest(final ComExtensibleUID uid, final String confirmationCode) throws Exception {
		final String address = this.recipientDao.readEmailAddressForPendingChangeRequest(uid.getCompanyID(), uid.getCustomerID(), confirmationCode);
		
		if (address == null) {
			throw new Exception(String.format("No pending change request for email address of customer %d (company %d, confirmation code '%s')", uid.getCustomerID(), uid.getCompanyID(), confirmationCode));
		}
		
		return address;
	}
	
	private final void deletePendingEmailAddressChangeRequest(final ComExtensibleUID uid, final String confirmationCode) {
		this.recipientDao.deletePendingEmailAddressChangeRequest(uid.getCompanyID(), uid.getCustomerID(), confirmationCode);
	}

	@Override
	@Transactional
	@Validate("deleteSubscriber")
	public void deleteSubscriber(RecipientModel model, List<UserAction> userActions) {
		final int companyId = model.getCompanyId();
		final int recipientId = model.getCustomerId();
		CaseInsensitiveMap<String, Object> customerData = recipientDao.getCustomerDataFromDb(companyId, recipientId, DEFAULT_COLUMNS);

		if (customerData.isEmpty()) {
			throw new IllegalStateException("Attempt to delete not existing recipient #"
					+ recipientId + " (company #" + companyId + ")");
		}

		recipientDao.deleteRecipients(companyId, Collections.singletonList(recipientId));

		userActions.add(new UserAction("delete recipient", RecipientUtils.getRecipientDescription(customerData)));
	}


	private void supplySourceID(Recipient recipient, final String username, final int companyID) {
		int defaultId = recipientDao.getDefaultDatasourceID(username, companyID);
		if (!recipient.hasCustParameter("DATASOURCE_ID")) {
			logger.trace("Set default datasource_id = " + defaultId + " for recipient with email  " + recipient.getEmail());
			recipient.getCustParameters().put("DATASOURCE_ID", String.valueOf(defaultId));
		} else {
			logger.trace("Set datasource_id = " + recipient.getCustParametersNotNull("DATASOURCE_ID") + " from parameter for recipient with email  " + recipient.getEmail());
		}
		recipient.getCustParameters().put("LATEST_DATASOURCE_ID", recipient.getCustParametersNotNull("DATASOURCE_ID"));
	}

	@Override
	public List<ComProfileField> getRecipientBulkFields(@VelocityCheck int companyId) {
		List<ComProfileField> profileFields = Collections.emptyList();
		try {
			Set<String> immutableField = new CaseInsensitiveSet(Arrays.asList(ComCompanyDaoImpl.GUI_BULK_IMMUTABALE_FIELDS));
			List<ComProfileField> allFields = profileFieldDao.getComProfileFields(companyId);
			if (allFields != null) {
				profileFields = allFields.stream()
						.filter(field -> field.getModeEdit() == ProfileField.MODE_EDIT_EDITABLE &&
								!immutableField.contains(field.getColumn()))
						.collect(Collectors.toList());
			}
		} catch (Exception e) {
			logger.error("Cannot get recipient bulk fields", e);
		}
		
		return profileFields;
	}
	
	@Override
	public int calculateRecipient(ComAdmin admin, int targetId, int mailinglistId) {
		int companyId = admin.getCompanyID();
		String targetExpression = targetService.getTargetSQLWithSimpleIfNotExists(targetId, companyId);
		return recipientDao.getRecipientsAmountForTargetGroup(companyId, admin.getAdminID(), mailinglistId, targetExpression);
	}

	@Override
	public boolean deleteRecipients(ComAdmin admin, Set<Integer> recipientIds) {
		if (recipientIds.isEmpty()) {
			return false;
		}
		recipientDao.deleteRecipients(admin.getCompanyID(), new ArrayList<>(recipientIds));
		return true;
	}
	
	@Override
	public boolean deleteDuplicateRecipients(ComAdmin admin, Set<Integer> recipientIds, String email) {
		if (CollectionUtils.isNotEmpty(recipientIds) && StringUtils.isNotBlank(email)) {
			try {
				recipientDao.deleteDuplicateRecipients(admin.getCompanyID(), new ArrayList<>(recipientIds), email);
				return true;
			} catch (Exception e) {
				logger.warn("Could not delete recipients!", e);
			}
		}
		return false;
	}
	
	private RecipientSqlOptions getDuplicateOptions(String sort, String order) {
		return RecipientSqlOptions.builder()
				.setCheckParenthesisBalance(true)
				.setSort(sort)
				.setDirection(order)
				.setDuplicateList(true)
				.setUserType(BindingEntry.UserType.World.getTypeCode())
				.setUserTypeEmpty(true)
				.build();
	}
	@Override
	public PaginatedListImpl<DynaBean> getPaginatedDuplicateList(ComAdmin admin, String sort, String order, int page, int rownums, Map<String, String> fields) throws Exception {
		sort = StringUtils.defaultIfEmpty(sort, "email");
		RecipientSqlOptions options = getDuplicateOptions(sort, order);
		SqlPreparedStatementManager sqlStatementManagerForDataSelect = recipientQueryBuilder.getDuplicateAnalysisSQLStatement(admin, options, true);
		String selectDataStatement = sqlStatementManagerForDataSelect.getPreparedSqlString()
				.replaceAll("cust[.]bind", "bind").replace("lower(cust.email)", "cust.email");
		logger.info("Recipient Select data SQL statement: " + selectDataStatement);
		
		PaginatedListImpl<Recipient> paginatedList = recipientDao.getDuplicatePaginatedRecipientList(admin.getCompanyID(), fields.keySet(), selectDataStatement,
				sqlStatementManagerForDataSelect.getPreparedSqlParameters(), sort, AgnUtils.sortingDirectionToBoolean(order), page, rownums);
		
		// Convert PaginatedListImpl of Recipient into PaginatedListImpl of DynaBean
		DynaProperty[] properties = fields.keySet().stream().map(column -> new DynaProperty(column.toLowerCase(), String.class)).toArray(DynaProperty[]::new);
		BasicDynaClass dynaClass = new BasicDynaClass("recipient", null, properties);
		List<DynaBean> partialList = new ArrayList<>();
		
		for (Recipient recipient : paginatedList.getList()) {
			DynaBean bean = dynaClass.newInstance();
			for (String column : fields.keySet()) {
				bean.set(column.toLowerCase(), recipient.getCustParametersNotNull(column.toUpperCase()));
			}
			partialList.add(bean);
		}

		return new PaginatedListImpl<>(partialList, paginatedList.getFullListSize(),
				paginatedList.getPageSize(),
				paginatedList.getPageNumber(),
				paginatedList.getSortCriterion(),
				paginatedList.getSortDirection());
	}
	
	@Override
	public File getDuplicateAnalysisCsv(ComAdmin admin, Map<String, String> fieldsMap, Set<String> selectedColumns, String sort, String order) throws Exception {
		sort = StringUtils.defaultIfEmpty(sort, COLUMN_EMAIL);
		String tempFileName = String.format("duplicate-recipients-%s.csv", UUID.randomUUID());
        File duplicateRecipientsExportTempDirectory = AgnUtils.createDirectory(AgnUtils.getTempDir() + File.separator + "DuplicateRecipientExport");
        File exportTempFile = new File(duplicateRecipientsExportTempDirectory, tempFileName);
		
		RecipientSqlOptions sqlOptions = getDuplicateOptions(sort, order);
		List<String> columns = new ArrayList<>(selectedColumns);
        columns.sort(RecipientUtils.getCsvColumnComparator(true));
        
		DuplicatedRecipientsExportWorker exportWorker = recipientWorkerFactory.getDuplicateRecipientsBuilderInstance(recipientQueryBuilder)
				.setAdmin(admin)
				.setSelectedColumns(columns)
				.setFieldsNames(fieldsMap)
				.setSqlOptions(sqlOptions)
				.setExportFile(exportTempFile.getAbsolutePath())
				.setDateFormat(admin.getDateFormat())
				.setDateTimeFormat(admin.getDateTimeFormatWithSeconds())
				.setExportTimezone(TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId())
				.build();
        exportWorker.call();

        return exportTempFile;
	}

	@Override
	public RecipientLightDto getRecipientDto(@VelocityCheck int companyId, int recipientId) {
		CaseInsensitiveMap<String, Object> dataFromDb = recipientDao.getCustomerDataFromDb(companyId, recipientId, DEFAULT_COLUMNS);
		int customerId = NumberUtils.toInt((String) dataFromDb.get(COLUMN_CUSTOMER_ID), 0);
		String firstName = (String) dataFromDb.get(COLUMN_FIRSTNAME);
		String lastName = (String) dataFromDb.get(COLUMN_LASTNAME);
		String email = (String) dataFromDb.get(COLUMN_EMAIL);
		return new RecipientLightDto(customerId, firstName, lastName, email);
	}
	
	@Override
	public List<RecipientLightDto> getDuplicateRecipients(ComAdmin admin, String email) throws Exception {
		RecipientSqlOptions options = RecipientSqlOptions.builder()
				.setCheckParenthesisBalance(true)
				.setDuplicateList(true)
				.setUserType(BindingEntry.UserType.World.getTypeCode())
				.setUserTypeEmpty(true)
				.setSingleMode(true)
				.setSearchEmail(email)
				.build();
		SqlPreparedStatementManager sqlStatement = recipientQueryBuilder.getDuplicateAnalysisSQLStatement(admin, options, DEFAULT_COLUMNS, true);
		String select = sqlStatement.getPreparedSqlString().replaceAll("cust[.]bind", "bind").replace("LOWER(cust.email)", "cust.email");
		List<Recipient> duplicateRecipients = recipientDao.getDuplicateRecipients(admin.getCompanyID(), email, select, sqlStatement.getPreparedSqlParameters());
		return duplicateRecipients.stream()
					.map(cust -> new RecipientLightDto(cust.getCustomerID(), cust.getFirstname(), cust.getLastname(), cust.getEmail()))
					.collect(Collectors.toList());
	}
	
	@Override
	public ServiceResult<FieldsSaveResults> saveBulkRecipientFields(ComAdmin admin, int targetId, int mailinglistId, Map<String, RecipientFieldDto> fieldChanges) {
		if (fieldChanges.isEmpty()) {
			return new ServiceResult<>(new FieldsSaveResults(), true, Collections.emptyList());
		}
		
		FieldsSaveResults result = new FieldsSaveResults();
		Set<String> immutableField = new CaseInsensitiveSet(Arrays.asList(ComCompanyDaoImpl.GUI_BULK_IMMUTABALE_FIELDS));
		Collection<String> immutableFieldsToChange = CollectionUtils.retainAll(fieldChanges.keySet(), immutableField);
		if (CollectionUtils.isNotEmpty(immutableFieldsToChange)) {
			return new ServiceResult<>(result, false,
					Message.of("error.bulkAction.field.immutable", StringUtils.join(immutableFieldsToChange, ", ")));
		}
		
		Locale locale = admin.getLocale();
		boolean success = true;
		List<Message> messages = new ArrayList<>();
		
		Map<String, Object> valuesForUpdate = new HashMap<>();
		for (RecipientFieldDto fieldChange : fieldChanges.values()) {
			ServiceResult<Object> validationResult =
					profileFieldValidationService.validateNewProfileFieldValue(admin, fieldChange);
			if (validationResult.isSuccess()) {
				valuesForUpdate.put(fieldChange.getShortname(), validationResult.getResult());
			} else {
				success = false;
				messages.addAll(validationResult.getErrorMessages());
			}
		}
		
		if (success) {
			try {
				int companyId = admin.getCompanyID();
				String targetExpression = targetService.getTargetSQLWithSimpleIfNotExists(targetId, companyId);
				
				int affectedRecipients = recipientDao.bulkUpdateEachRecipientsFields(companyId, admin.getAdminID(),
						mailinglistId, targetExpression, valuesForUpdate);
				
				result.setAffectedRecipients(affectedRecipients);
				result.setAffectedFields(valuesForUpdate);
				
				return new ServiceResult<>(result, true, Collections.emptyList());
			} catch (ProfileFieldBulkUpdateException e) {
				messages.add(Message.exact(String.format("%s: %s", e.getProfileFieldName(),
						I18nString.getLocaleString("error.bulkAction.exception", locale, e.getMessage()))));
			} catch (ImportException e) {
				messages.add(Message.of("error.bulkAction.exception",
						I18nString.getLocaleString(e.getErrorMessageKey(), locale, e.getAdditionalErrorData())));
			} catch (Exception e) {
				messages.add(Message.of("Error"));
			}
		}
		
		return new ServiceResult<>(result, false, messages);
	}
	
	@Override
	public List<ComRecipientLiteImpl> getAdminAndTestRecipients(@VelocityCheck int companyId, int mailinglistId) {
		return recipientDao.getAdminAndTestRecipients(companyId, mailinglistId);
	}

	@Override
	public void supplySourceID(Recipient recipient, int defaultId) {
		if (!recipient.hasCustParameter("DATASOURCE_ID")) {
			recipient.getCustParameters().put("DATASOURCE_ID", String.valueOf(defaultId));
		}
		recipient.getCustParameters().put("LATEST_DATASOURCE_ID", recipient.getCustParametersNotNull("DATASOURCE_ID"));
	}

	@Required
	public final void setRecipientDao(final ComRecipientDao dao) {
		this.recipientDao = Objects.requireNonNull(dao, "Recipient DAO cannot be null");
	}

	@Required
	public final void setUidService(final ExtensibleUIDService service) {
		this.uidService = Objects.requireNonNull(service, "UID service cannot be null");
	}

	@Required
	public final void setCompanyDao(final ComCompanyDao dao) {
		this.companyDao = Objects.requireNonNull(dao, "Company DAO cannot be null");
	}

	@Required
	public final void setRecipientFactory(final RecipientFactory factory) {
		this.recipientFactory = Objects.requireNonNull(factory, "Recipient factory cannot be null");
	}

	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}
	
	@Required
	public final void setSendActionbasedMailingService(final SendActionbasedMailingService service) {
		this.sendActionbasedMailingService = Objects.requireNonNull(service, "Service to send action-based mailings is null");
	}
	
	@Required
	public void setTargetService(ComTargetService targetService) {
		this.targetService = targetService;
	}
	
	@Required
	public void setProfileFieldDao(ComProfileFieldDao profileFieldDao) {
		this.profileFieldDao = profileFieldDao;
	}
	
	@Required
	public void setProfileFieldValidationService(ProfileFieldValidationService profileFieldValidationService) {
		this.profileFieldValidationService = profileFieldValidationService;
	}
}
