/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.service.impl;

import static com.agnitas.emm.core.binding.service.BindingUtils.getRecipientTypeTitleByLetter;
import static com.agnitas.emm.core.target.TargetExpressionUtils.SIMPLE_TARGET_EXPRESSION;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.Recipient;
import com.agnitas.beans.RecipientHistory;
import com.agnitas.beans.RecipientMailing;
import com.agnitas.beans.RecipientReaction;
import com.agnitas.beans.Target;
import com.agnitas.beans.WebtrackingHistoryEntry;
import com.agnitas.beans.factory.BindingEntryFactory;
import com.agnitas.beans.factory.RecipientFactory;
import com.agnitas.beans.impl.AdminImpl;
import com.agnitas.beans.impl.BindingEntryImpl;
import com.agnitas.beans.impl.RecipientImpl;
import com.agnitas.beans.impl.RecipientLiteImpl;
import com.agnitas.beans.impl.ViciousFormDataException;
import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.binding.service.BindingUtils;
import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.profilefields.exception.ProfileFieldBulkUpdateException;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.recipient.dto.BindingAction;
import com.agnitas.emm.core.recipient.dto.RecipientBindingDto;
import com.agnitas.emm.core.recipient.dto.RecipientBindingsDto;
import com.agnitas.emm.core.recipient.dto.RecipientDto;
import com.agnitas.emm.core.recipient.dto.RecipientFieldDto;
import com.agnitas.emm.core.recipient.dto.RecipientLightDto;
import com.agnitas.emm.core.recipient.dto.RecipientSalutationDto;
import com.agnitas.emm.core.recipient.dto.RecipientSearchParamsDto;
import com.agnitas.emm.core.recipient.dto.SaveRecipientDto;
import com.agnitas.emm.core.recipient.exception.InvalidDataException;
import com.agnitas.emm.core.recipient.exception.ProfileFieldNotExistException;
import com.agnitas.emm.core.recipient.exception.RecipientException;
import com.agnitas.emm.core.recipient.exception.RecipientNotExistException;
import com.agnitas.emm.core.recipient.service.DuplicatedRecipientsExportWorker;
import com.agnitas.emm.core.recipient.service.FieldsSaveResults;
import com.agnitas.emm.core.recipient.service.RecipientModel;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.recipient.service.RecipientWorkerFactory;
import com.agnitas.emm.core.recipient.service.RecipientsModel;
import com.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import com.agnitas.emm.core.recipient.service.validation.RecipientModelValidator;
import com.agnitas.emm.core.recipient.utils.RecipientUtils;
import com.agnitas.emm.core.report.enums.fields.RecipientMutableFields;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCode;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.exception.UiMessageException;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ImportException;
import com.agnitas.service.RecipientDuplicateSqlOptions;
import com.agnitas.service.RecipientQueryBuilder;
import com.agnitas.service.RecipientSqlOptions;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserMessageException;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.Const;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.SqlPreparedStatementManager;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

public class RecipientServiceImpl implements RecipientService {

	private static final Logger logger = LogManager.getLogger(RecipientServiceImpl.class);
	
	public static final List<String> DEFAULT_COLUMNS = Arrays.asList(
			RecipientStandardField.CustomerID.getColumnName(),
			RecipientStandardField.Email.getColumnName(),
			RecipientStandardField.Firstname.getColumnName(),
			RecipientStandardField.Lastname.getColumnName()
	);
    private static final String INVALID_DATE_FIELD_EROR_CODE = "error.value.notADateForField";

	protected RecipientDao recipientDao;
	protected TargetService targetService;
	protected ConfigService configService;
	protected ColumnInfoService columnInfoService;
	protected RecipientQueryBuilder recipientQueryBuilder;
	protected AdminService adminService;
	protected BindingEntryDao bindingEntryDao;
	protected BindingEntryFactory bindingEntryFactory;
	protected ExtendedConversionService conversionService;
	protected SubscriberLimitCheck subscriberLimitCheck;
	protected TargetDao targetDao;
	protected EqlFacade eqlFacade;
	private CompanyDao companyDao;
	private ExtensibleUIDService uidService;
	private MailinglistDao mailinglistDao;
	private RecipientFactory recipientFactory;
	private SendActionbasedMailingService sendActionbasedMailingService;
	private ProfileFieldDao profileFieldDao;
	private ProfileFieldValidationService profileFieldValidationService;
	private RecipientWorkerFactory recipientWorkerFactory;
	private RecipientModelValidator recipientModelValidator;
	private MailinglistApprovalService mailinglistApprovalService;
	private BulkActionValidationService<Integer, RecipientLightDto> bulkActionValidationService;

	@Override
	public boolean isColumnsIndexed(List<String> columns, int companyId) {
		return recipientDao.isColumnsIndexed(columns, companyId);
	}

	@Override
	@Transactional
	public int findSubscriber(int companyId, String keyColumn, String value) {
		try {
			return recipientDao.findByColumn(companyId, keyColumn, value);
		} catch (RuntimeException e) {
			logger.error("Exception", e);
			throw e;
		}
	}

	@Override
	@Transactional
	public void checkColumnsAvailable(RecipientModel model) {
		CaseInsensitiveMap<String, RecipientFieldDescription> availableProfileFields = recipientDao.getAvailableProfileFields(model.getCompanyId());
		for (String columnName : model.getColumns()) {
			if (!availableProfileFields.containsKey(columnName)) {
				throw new ProfileFieldNotExistException(columnName);
			}
		}
	}

	@Override
	@Transactional
	public Map<String, Object> getSubscriber(RecipientModel model) {
	    recipientModelValidator.assertIsValidToGetOrDelete(model);
		Set<String> columns = model.getColumns();
		if (CollectionUtils.isEmpty(columns)) {
			return recipientDao.getCustomerDataFromDb(model.getCompanyId(), model.getCustomerId());
		} else {
			return recipientDao.getCustomerDataFromDb(model.getCompanyId(), model.getCustomerId(), columns);
		}
	}

	@Override
	@Transactional
	public Recipient getRecipient(RecipientModel model) {
	    recipientModelValidator.assertIsValidToGetOrDelete(model);
		return getRecipient(model.getCompanyId(), model.getCustomerId());
	}

	@Override
	@Transactional
	public Recipient getRecipient(int companyID, int customerID) {
		final Recipient recipient = recipientFactory.newRecipient(companyID);
		recipient.setCustomerID(customerID);

		recipient.setCustParameters(getCustomerDataFromDb(companyID, customerID, recipient.getDateFormat()));

		return recipient;
	}

	@Override
	public boolean isMailTrackingEnabled(int companyId) {
		return companyId > 0 && recipientDao.isMailtrackingEnabled(companyId);
	}

	@Override
	@Transactional
	public List<Integer> getSubscribers(RecipientsModel model) throws RecipientException {
		return model.getEql() == null
				? recipientDao.getCustomerDataFromDb(model.getCompanyId(), model.isMatchAll(), model.getCriteriaEquals())
				: recipientDao.getCustomerDataFromDb(model.getCompanyId(), model.getEql());
	}

	@Override
	@Transactional
	public int getSubscribersSize(RecipientsModel model) {
		return model.getEql() == null
				? recipientDao.getSizeOfCustomerDataFromDbList(model.getCompanyId(), model.isMatchAll(), model.getCriteriaEquals())
				: recipientDao.getSizeOfCustomerDataFromDbList(model.getCompanyId(), model.getEql());
	}

	@Override
	public int getNumberOfRecipients(int companyId) {
		return recipientDao.getNumberOfRecipients(companyId, true);
	}

	@Override
	public boolean hasBeenReachedLimitOnNonIndexedImport(int companyId) {
		int maxContentLines = configService.getIntegerValue(ConfigValue.MaximumContentLinesForUnindexedImport, companyId);
		if (maxContentLines < 0) {
			return false;
		}

		return getNumberOfRecipients(companyId) > maxContentLines;
	}

	@Override
	@Transactional
	public List<Map<String, Object>> getSubscriberMailings(RecipientModel model) {
		if (!recipientDao.exist(model.getCustomerId(), model.getCompanyId())) {
			throw new RecipientNotExistException();
		}

		List<RecipientMailing> recipientMailings = recipientDao.getMailingsDeliveredToRecipient(model.getCustomerId(), model.getCompanyId());
		if (CollectionUtils.isNotEmpty(recipientMailings)) {
			final int licenseID = this.configService.getLicenseID();
			final ExtensibleUID uid = UIDFactory.from(licenseID, model.getCompanyId(), model.getCustomerId());

			final String redirectUrl = prepareEmmFormRedirect(model.getCompanyId());

			return recipientMailings
					.stream()
					.map(mailing -> getBasicDataMap(mailing, uid, redirectUrl))
					.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	private Map<String, Object> getBasicDataMap(final RecipientMailing mailing, final ExtensibleUID uid, final String redirectUrl) {
		final Map<String, Object> basicDataMap = new HashMap<>();

		basicDataMap.put("mailing_id", mailing.getMailingId());
		basicDataMap.put("senddate", mailing.getSendDate());
		basicDataMap.put("mailing_type", mailing.getMailingType().getWebserviceCode());	// Return the result of getWebserviceCode(). Changes here will affect the SOAP interface!!!!
		basicDataMap.put("mailing_name", mailing.getShortName());
		basicDataMap.put("mailing_subject", mailing.getSubject());
		basicDataMap.put("openings", mailing.getNumberOfOpenings());
		basicDataMap.put("clicks", mailing.getNumberOfClicks());

		if (StringUtils.isNotEmpty(redirectUrl)) {
			final ExtensibleUID newUID = UIDFactory.copyWithNewMailingID(uid, mailing.getMailingId());

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

			return redirectUrl + "form.action?agnCI=" + companyId + "&agnFN=fullview";
		}
		return null;
	}

	private String buildUid(final ExtensibleUID uid) {
		try {
			return uidService.buildUIDString(uid);
		} catch (UIDStringBuilderException | RequiredInformationMissingException e) {
			logger.error("Error occurred on UID generation: " + e.getMessage(), e);
			return "";
		}
	}

	@Override
	@Transactional
	public int addSubscriber(RecipientModel model, final String username, final int companyId, List<UserAction> userActions) throws Exception {
	    recipientModelValidator.assertIsValidToAdd(model);
		recipientDao.checkParameters(model.getParameters(), model.getCompanyId());
		
		subscriberLimitCheck.checkSubscriberLimit(companyId);

		int returnValue;
		int tmpCustID;

		if (configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, model.getCompanyId())) {
			model.setEmail(model.getEmail());
		} else {
			model.setEmail(AgnUtils.normalizeEmail(model.getEmail()));
		}

		final Recipient newCustomer = recipientFactory.newRecipient(model.getCompanyId());

		Map<String, ProfileField> columnInfo = columnInfoService.getColumnInfoMap(model.getCompanyId());
		for (Entry<String, Object> entry : model.getParameters().entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue() instanceof String
					? (String) entry.getValue()
					: (entry.getValue() != null ? entry.getValue().toString() : null);

			ProfileField profileField = columnInfo.get(name);
			String typeName = profileField != null ? profileField.getDataType() : "";

			if (AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR)
					|| AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH)
					|| AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY)
					|| AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR)
					|| AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE)
					|| AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND)) {
				// Special Integer field (WATCH OUT!!!: typeName is null)
				if (StringUtils.isNotEmpty(value) && !AgnUtils.isNumber(value)) {
					throw new InvalidDataException("Invalid data for integer field '" + name + "': '" + value + "'");
				} else {
					newCustomer.setCustParameters(name, value);
				}
			} else if (profileField == null || StringUtils.isBlank(typeName)) {
				throw new InvalidDataException("Unknown data field '" + name + "'");
			} else if (typeName.toUpperCase().startsWith("VARCHAR") || typeName.toUpperCase().startsWith("CHAR")) {
				// Alphanumeric field
				if (recipientDao.isOracleDB()) {
					// MySQL VARCHAR size is in bytes
					if (value != null && value.getBytes(StandardCharsets.UTF_8).length > profileField.getDataTypeLength()) {
						throw new InvalidDataException("Value size for alphanumeric field '" + name + "' exceeded. Maximum size: " + profileField.getDataTypeLength() + ", Actual size: " + value.length());
					}
				} else {
					// MySQL VARCHAR size is in characters
					if (value != null && value.length() > profileField.getDataTypeLength()) {
						throw new InvalidDataException("Value size for alphanumeric field '" + name + "' exceeded. Maximum size: " + profileField.getDataTypeLength() + ", Actual size: " + value.length());
					}
				}
				newCustomer.setCustParameters(name, value);
			} else if (typeName.toUpperCase().startsWith("CLOB") || typeName.equalsIgnoreCase("TEXT") || typeName.equalsIgnoreCase("MEDIUMTEXT") || typeName.equalsIgnoreCase("LONGTEXT")) {
				// CLOB field
				newCustomer.setCustParameters(name, value);
			} else if (DbColumnType.GENERIC_TYPE_DATE.equalsIgnoreCase(typeName)
					|| DbColumnType.GENERIC_TYPE_DATETIME.equalsIgnoreCase(typeName) || DbColumnType.GENERIC_TYPE_TIMESTAMP.equalsIgnoreCase(typeName)) {
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
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(calendar.get(Calendar.YEAR)));
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(calendar.get(Calendar.MONTH) + 1));
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, Integer.toString(calendar.get(Calendar.MINUTE)));
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, Integer.toString(calendar.get(Calendar.SECOND)));
				} else {
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, "");
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, "");
					newCustomer.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, "");
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
				returnValue = recipientDao.insertNewCustWithException(newCustomer);
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
							if (!Objects.equals(value, oldValue)) {
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
					recipientDao.updateInDbWithException(newCustomer);
				}
			}
		} else {
			returnValue = recipientDao.insertNewCustWithException(newCustomer);
			userActions.add(new UserAction("create recipient", keyColumnValue + " (" + returnValue + ")"));
		}
		return returnValue;
	}

	@Override
	@Transactional
	public boolean updateSubscriber(RecipientModel model, String username) throws Exception {
	    recipientModelValidator.assertIsValidToUpdate(model);
		recipientDao.checkParameters(model.getParameters(), model.getCompanyId());

		final Admin admin = new AdminImpl();
		admin.setUsername(username);

		final Recipient aCust = this.recipientFactory.newRecipient(model.getCompanyId());
		aCust.setCustomerID(model.getCustomerId());

		Map<String, Object> data = recipientDao.getCustomerDataFromDb(aCust.getCompanyID(), model.getCustomerId());

		String email = model.getEmail();
		if (email != null) {
			if (configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, model.getCompanyId())) {
				model.setEmail(model.getEmail());
			} else {
				model.setEmail(AgnUtils.normalizeEmail(model.getEmail()));
			}
		}

		aCust.setCustParameters(data);
		aCust.setCustomerID(model.getCustomerId());

		Map<String, String> customerFieldTypes = aCust.getCustDBStructure();
		for (Entry<String, Object> entry : model.getParameters().entrySet()) {
			String name = entry.getKey();
			String value = (String) entry.getValue();

			String oldValue = (String) data.get(name);
			String typeName = customerFieldTypes.get(name);

			if (AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR)
					|| AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH)
					|| AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY)
					|| AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR)
					|| AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE)
					|| AgnUtils.endsWithIgnoreCase(name, RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND)) {
				// Special Integer field (WATCH OUT!!!: typeName is null)
				if (StringUtils.isNotEmpty(value) && !AgnUtils.isNumber(value)) {
					throw new InvalidDataException("Invalid data for integer field '" + name + "': '" + value + "'");
				} else if (!Objects.equals(value, oldValue)) {
					aCust.setCustParameters(name, value);
				}
			} else if (StringUtils.isBlank(typeName)) {
				throw new InvalidDataException("Unknown data field '" + name + "'");
			} else if (typeName.toUpperCase().startsWith("VARCHAR") || typeName.toUpperCase().startsWith("CHAR") || typeName.toUpperCase().startsWith("CLOB")
					|| typeName.equalsIgnoreCase("TEXT") || typeName.equalsIgnoreCase("MEDIUMTEXT") || typeName.equalsIgnoreCase("LONGTEXT")) {
				// Alphanumeric field

				if(!Objects.equals(value, oldValue)) {		// Objects.equals() is true if value and oldValue are null or value.equals(oldValue)
					aCust.setCustParameters(name, value);
				}
			} else if (DbColumnType.GENERIC_TYPE_DATE.equalsIgnoreCase(typeName)
					|| DbColumnType.GENERIC_TYPE_DATETIME.equalsIgnoreCase(typeName)
					|| DbColumnType.GENERIC_TYPE_TIMESTAMP.equalsIgnoreCase(typeName)) {
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
						aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(calendar.get(Calendar.YEAR)));
						aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(calendar.get(Calendar.MONTH) + 1));
						aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
						aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
						aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, Integer.toString(calendar.get(Calendar.MINUTE)));
						aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, Integer.toString(calendar.get(Calendar.SECOND)));
					}
				} else if (StringUtils.isNotBlank(oldValue)) {
					aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
					aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
					aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
					aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, "");
					aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, "");
					aCust.setCustParameters(name + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, "");
				}
			} else {
				// Numeric field
				if (StringUtils.isNotEmpty(value) && !AgnUtils.isDouble(value)) {
					throw new InvalidDataException("Invalid data for " + typeName + " field '" + name + "': '" + value + "'");
				} else if (!Objects.equals(value, oldValue)) {
					aCust.setCustParameters(name, value);
				}
			}
		}

		return recipientDao.updateInDbWithException(aCust, false);        // Don't set unspecified profile fields to null
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
			if (!this.recipientDao.updateInDbWithException(recipient)) {
				throw new Exception(String.format("Error saving recipient %d (company ID %d)", recipient.getCustomerID(), recipient.getCompanyID()));
			}

			// (4) ... store new address to pending table ...
			final String confirmationCode = recordEmailAddressChangeRequest(recipient.getCompanyID(), recipient.getCustomerID(), newEmailAddress);

			// (5) ... and send confirmation mail
			final MailgunOptions mailgunOptions = new MailgunOptions()
					.withDifferentRecipientEmailAddress(newEmailAddress)
					.withProfileFieldValue(profileFieldForConfirmationCode, confirmationCode);

			this.sendActionbasedMailingService.sendActionbasedMailing(recipient.getCompanyID(), mailingID, recipient.getCustomerID(), 0, mailgunOptions);
		} else {
			recipientDao.updateInDbWithException(recipient);
		}
	}

	private String recordEmailAddressChangeRequest(final int companyID, final int customerID, final String newEmailAddress) {
		final UUID uuid = UUID.randomUUID();
		final String confirmationCode = uuid.toString();

		this.recipientDao.writeEmailAddressChangeRequest(companyID, customerID, newEmailAddress, confirmationCode);

		return confirmationCode;
	}

	@Transactional
	@Override
	public void confirmEmailAddressChange(final ExtensibleUID uid, final String confirmationCode) throws Exception {
		final String newEmailAddress = readEmailAddressForPendingChangeRequest(uid, confirmationCode);

		assert newEmailAddress != null; // Ensured by implementation of readEmailAddressForPendingChangeRequest()

		final Recipient recipient = this.getRecipient(uid.getCompanyID(), uid.getCustomerID());
		recipient.setCustParameters("email", newEmailAddress);


		if (recipientDao.updateInDbWithException(recipient)) {
			deletePendingEmailAddressChangeRequest(uid, confirmationCode);
		} else {
			final String msg = String.format("Cannot change email address for pending change requence (customer %d, company %d, confirmation code '%s')", uid.getCustomerID(), uid.getCompanyID(), confirmationCode);
			logger.error(msg);

			throw new Exception(msg);
		}
	}

	private String readEmailAddressForPendingChangeRequest(final ExtensibleUID uid, final String confirmationCode) throws Exception {
		final String address = this.recipientDao.readEmailAddressForPendingChangeRequest(uid.getCompanyID(), uid.getCustomerID(), confirmationCode);

		if (address == null) {
			throw new Exception(String.format("No pending change request for email address of customer %d (company %d, confirmation code '%s')", uid.getCustomerID(), uid.getCompanyID(), confirmationCode));
		}

		return address;
	}

	private void deletePendingEmailAddressChangeRequest(final ExtensibleUID uid, final String confirmationCode) {
		this.recipientDao.deletePendingEmailAddressChangeRequest(uid.getCompanyID(), uid.getCustomerID(), confirmationCode);
	}

	@Override
	@Transactional
	public void deleteSubscriber(RecipientModel model, List<UserAction> userActions) {
	    recipientModelValidator.assertIsValidToGetOrDelete(model);
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
	public List<ProfileField> getRecipientBulkFields(int companyID, int adminID) {
		List<ProfileField> allFields = profileFieldDao.getComProfileFields(companyID, adminID);
		if (allFields != null) {
			Set<String> immutableField = RecipientStandardField.getBulkImmutableRecipientStandardFieldColumnNames();

			return allFields.stream()
					.filter(field -> field.getModeEdit() == ProfileFieldMode.Editable
							&& !immutableField.contains(field.getColumn()))
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	@Override
	public int calculateRecipient(Admin admin, int targetId, int mailinglistId) {
		int companyId = admin.getCompanyID();
		String targetExpression;
		if (targetId > 0) {
			targetExpression = getTargetGroupExpressionOrDefault(targetId, companyId);
		} else {
			targetExpression = SIMPLE_TARGET_EXPRESSION;
		}
		return recipientDao.getRecipientsAmountForTargetGroup(companyId, admin.getAdminID(), mailinglistId, targetExpression);
	}

	private String getTargetGroupExpressionOrDefault(int targetId, int companyId) {
        return targetService.getTargetGroup(targetId, companyId).getTargetSQL();
	}

	@Override
	public boolean deleteRecipients(Admin admin, Set<Integer> recipientIds) {
		if (CollectionUtils.isNotEmpty(recipientIds)) {
			try {
				recipientDao.deleteRecipients(admin.getCompanyID(), new ArrayList<>(recipientIds));
				return true;
			} catch (Exception e) {
				logger.warn("Could not delete recipients!", e);
			}
		}
		return false;
	}

	protected RecipientSqlOptions getRecipientOptions(Admin admin, String sort, String order, RecipientSearchParamsDto searchParams) {
		RecipientSqlOptions.Builder builder = RecipientSqlOptions.builder()
				.setDirection(order)
				.setSort(sort)
				.setListId(searchParams.getMailingListId())
				.setTargetId(searchParams.getTargetGroupId())
				.setUserStatus(searchParams.getUserStatus())
				.setUserTypes(searchParams.getUserTypes())
				.setSearchFirstName(searchParams.getFirstName())
				.setGender(searchParams.getGender())
				.setSearchLastName(searchParams.getLastName())
				.setSearchEmail(searchParams.getEmail())
				.setTargetEQL(searchParams.getEql());
        setRecipientSqlOptionsBuilderExtendedParams(admin, builder, searchParams);

        return builder.build();
	}

    protected void setRecipientSqlOptionsBuilderExtendedParams(Admin admin, RecipientSqlOptions.Builder builder, RecipientSearchParamsDto searchParams) {
        // nothing to do
    }

	protected RecipientDuplicateSqlOptions getDuplicateOptions(Admin admin, String sort, String order, String searchFieldName, boolean caseSensitive) {
		RecipientDuplicateSqlOptions.Builder builder = RecipientDuplicateSqlOptions.builder()
				.setCheckParenthesisBalance(true)
				.setSearchFieldName(searchFieldName)
				.setCaseSensitive(caseSensitive)
				.setSort(sort)
				.setDirection(order)
				.setUserTypes(List.of(BindingEntry.UserType.World.getTypeCode()))
				.setUserTypeEmpty(true);
        setRecipientDuplicateSqlOptionsBuilderExtendedParams(admin, builder);

		return builder.build();
	}

    protected void setRecipientDuplicateSqlOptionsBuilderExtendedParams(Admin admin, RecipientDuplicateSqlOptions.Builder builder) {
        // nothing to do
    }
	
	@Override
	public PaginatedList<RecipientDto> getPaginatedRecipientList(Admin admin, RecipientSearchParamsDto searchParams, String sortColumn, String order, int page, int rownums, Map<String, String> fields) throws Exception {
		String sort = StringUtils.defaultIfEmpty(sortColumn, "email");
		RecipientSqlOptions options = getRecipientOptions(admin, StringUtils.defaultIfEmpty(sort, "email"), order, searchParams);
		
		final int companyID = admin.getCompanyID();

        SqlPreparedStatementManager sqlStatementManagerForDataSelect;
        try {
			sqlStatementManagerForDataSelect = prepareBaseOverviewStatement(options, admin, false);
			addExtendedSearchOptions(admin, sqlStatementManagerForDataSelect, options);

            if (options.getTargetId() > 0) {
            	sqlStatementManagerForDataSelect.addWhereClause(targetDao.getTarget(options.getTargetId(), companyID).getTargetSQL());
            }

            final String eql = options.getTargetEQL();
            if (StringUtils.isNotEmpty(eql)) {
                final SqlCode sqlCode = eqlFacade.convertEqlToSql(eql, companyID);

                if (sqlCode != null) {
                    sqlStatementManagerForDataSelect.addWhereClause(sqlCode.getSql());
                }
            }

            if (options.getListId() != 0 || options.getUserStatus() != 0 || CollectionUtils.isNotEmpty(options.getUserTypes())) {
            	SqlPreparedStatementManager sqlCheckBinding = new SqlPreparedStatementManager("SELECT 1 FROM customer_" + companyID + "_binding_tbl bind");
                sqlCheckBinding.addWhereClause("bind.customer_id = cust.customer_id");

                if (options.getListId() == -1) {
					sqlStatementManagerForDataSelect.addWhereClause("NOT EXISTS (" + sqlCheckBinding.getPreparedSqlString() + ")");
				} else {
					if (options.getListId() != 0) {
						sqlCheckBinding.addWhereClause("bind.mailinglist_id = ?", options.getListId());
					}

					if (options.getUserStatus() != 0) {
						// Check for valid UserStatus code
						UserStatus.getByCode(options.getUserStatus());

						sqlCheckBinding.addWhereClause("bind.user_status = ?", options.getUserStatus());
					}

					if (CollectionUtils.isNotEmpty(options.getUserTypes())) {
						// Check for valid UserType code
						for (String userType : options.getUserTypes()) {
							BindingEntry.UserType.getUserTypeByString(userType);
						}

						sqlCheckBinding.addWhereClause("bind.user_type IN (" + AgnUtils.csvQMark(options.getUserTypes().size()) + ")", options.getUserTypes().toArray());
					}

					sqlStatementManagerForDataSelect.addWhereClause("EXISTS (" + sqlCheckBinding.getPreparedSqlString() + ")", sqlCheckBinding.getPreparedSqlParameters());
				}
            }
        } catch (final EqlParserException e) {
            logger.error("Unable to create SQL statement for recipient search", e);

            // In case of an error, return a statement that won't show recipients
            sqlStatementManagerForDataSelect = new SqlPreparedStatementManager("SELECT * FROM customer_" + companyID + "_tbl cust ");
            sqlStatementManagerForDataSelect.addWhereClause("1 = 0");
        }

		PaginatedList<Map<String, Object>> paginatedList = 
			recipientDao.getPaginatedRecipientsData(companyID, fields.keySet(),
				sqlStatementManagerForDataSelect.getPreparedSqlString().replaceAll("cust\\.bind", "bind"),
				sqlStatementManagerForDataSelect.getPreparedSqlParameters(),
				sort, AgnUtils.sortingDirectionToBoolean(order), page, rownums);

		if (searchParams.isUiFiltersSet()) {
			SqlPreparedStatementManager countStatement = prepareBaseOverviewStatement(options, admin, true);
			paginatedList.setNotFilteredFullListSize(recipientDao.getIntResult(countStatement));
		}

		return getRecipientDtoPaginatedList(admin, paginatedList);
	}

	private SqlPreparedStatementManager prepareBaseOverviewStatement(RecipientSqlOptions options, Admin admin, boolean useCountQuery) throws Exception {
        SqlPreparedStatementManager statementManager = new SqlPreparedStatementManager(
				"SELECT " + (useCountQuery ? "COUNT(*)" : "*") + " FROM customer_" + admin.getCompanyID() + "_tbl cust"
		);

		statementManager.addWhereClause(RecipientStandardField.Bounceload.getColumnName() + " = 0");

        if (configService.getBooleanValue(ConfigValue.RespectHideDataSign, admin.getCompanyID())) {
			statementManager.addWhereClause("hide <= 0 OR hide IS NULL");
		}

		addExtendedSearchRestrictions(admin, statementManager, options);
		return statementManager;
	}

	protected PaginatedList<RecipientDto> getRecipientDtoPaginatedList(Admin admin, PaginatedList<Map<String, Object>> paginatedList) {
		List<RecipientDto> partialList = new ArrayList<>();
		Map<String, ProfileField> dbColumns = getRecipientColumnInfos(admin).stream().collect(Collectors.toMap(ProfileField::getColumn, Function.identity()));
		for (Map<String, Object> parameters : paginatedList.getList()) {
			RecipientDto recipientDto = new RecipientDto();
			recipientDto.setId(((Number) parameters.get(RecipientStandardField.CustomerID.getColumnName())).intValue());
			recipientDto.setParameters(parameters);
			recipientDto.setDbColumns(dbColumns);

			partialList.add(recipientDto);
		}

		return new PaginatedList<>(partialList,
				paginatedList.getFullListSize(),
				paginatedList.getPageSize(),
				paginatedList.getPageNumber(),
				paginatedList.getSortCriterion(),
				paginatedList.getSortDirection(),
				paginatedList.getNotFilteredFullListSize());
	}

	@Override
	public PaginatedList<RecipientDto> getPaginatedDuplicateList(Admin admin, String searchFieldName, boolean caseSensitive, String sort, String order, int page, int rownums, Map<String, String> fields) {
		sort = StringUtils.defaultIfEmpty(sort, "email");
		RecipientDuplicateSqlOptions options = getDuplicateOptions(admin, sort, order, searchFieldName, caseSensitive);
		SqlPreparedStatementManager sqlStatementManagerForDataSelect = recipientQueryBuilder.getDuplicateAnalysisSQLStatement(admin, options, true);
		String selectDataStatement = sqlStatementManagerForDataSelect.getPreparedSqlString()
				.replaceAll("cust[.]bind", "bind").replace("lower(cust.email)", "cust.email");
		logger.info("Recipient Select data SQL statement: " + selectDataStatement);

		PaginatedList<Map<String, Object>> paginatedList = recipientDao.getPaginatedRecipientsData(admin.getCompanyID(), fields.keySet(), selectDataStatement,
				sqlStatementManagerForDataSelect.getPreparedSqlParameters(), sort, AgnUtils.sortingDirectionToBoolean(order), page, rownums);

		return getRecipientDtoPaginatedList(admin, paginatedList);
	}

	@Override
	public File getDuplicateAnalysisCsv(Admin admin, String searchFieldName, Map<String, String> fieldsMap, Set<String> selectedColumns, String sort, String order) throws Exception {
		sort = StringUtils.defaultIfEmpty(sort, RecipientStandardField.Email.getColumnName());
		String tempFileName = String.format("duplicate-recipients-%s.csv", UUID.randomUUID());
        File duplicateRecipientsExportTempDirectory = AgnUtils.createDirectory(AgnUtils.getTempDir() + File.separator + "DuplicateRecipientExport");
        File exportTempFile = new File(duplicateRecipientsExportTempDirectory, tempFileName);

		RecipientDuplicateSqlOptions sqlOptions = getDuplicateOptions(admin, sort, order, searchFieldName, false);
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
	public RecipientLightDto getRecipientLightDto(int companyId, int recipientId) {
		CaseInsensitiveMap<String, Object> dataFromDb = recipientDao.getCustomerDataFromDb(companyId, recipientId, DEFAULT_COLUMNS);
		int customerId = NumberUtils.toInt((String) dataFromDb.get(RecipientStandardField.CustomerID.getColumnName()), 0);
		String firstName = (String) dataFromDb.get(RecipientStandardField.Firstname.getColumnName());
		String lastName = (String) dataFromDb.get(RecipientStandardField.Lastname.getColumnName());
		String email = (String) dataFromDb.get(RecipientStandardField.Email.getColumnName());
		return new RecipientLightDto(customerId, firstName, lastName, email);
	}

	@Override
	public RecipientDto getRecipientDto(Admin admin, int recipientId) {
		RecipientDto recipient = new RecipientDto();
		recipient.setId(recipientId);
		try {
			Map<String, Object> data = recipientDao.getRecipientData(admin.getCompanyID(), recipientId, true);
			recipient.setParameters(data);

			Set<ProfileField> recipientColumnInfos = getRecipientColumnInfos(admin);
			recipient.setDbColumns(recipientColumnInfos.stream().collect(Collectors.toMap(ProfileField::getColumn, Function.identity())));

        } catch (Exception e) {
            logger.error("Could not collect recipient data", e);
        }

        return recipient;
	}

	@Override
	public Set<ProfileField> getRecipientColumnInfos(Admin admin) {
		return columnInfoService.getColumnInfos(admin.getCompanyID(), admin.getAdminID()).stream()
				.sorted(Comparator.comparing(ProfileField::getSort))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public ServiceResult<FieldsSaveResults> saveBulkRecipientFields(Admin admin, int targetId, int mailinglistId, Map<String, RecipientFieldDto> fieldChanges) {
		if (fieldChanges.isEmpty()) {
			return ServiceResult.success(new FieldsSaveResults());
		}

		ServiceResult<Map<String, Object>> valuesForUpdate = getValuesForSaveBulkRecipientFields(fieldChanges, admin);
		if (!valuesForUpdate.isSuccess()) {
			return ServiceResult.from(valuesForUpdate);
		}

		if (MapUtils.isEmpty(valuesForUpdate.getResult())) {
			return ServiceResult.success(new FieldsSaveResults());
		}

		int companyId = admin.getCompanyID();

		Map<String, Object> excludedImmutable = excludeImmutableFields(valuesForUpdate.getResult());
		if (excludedImmutable.isEmpty()) {
			logger.error("No field to change found. Maybe all fields given are immutable for company {}", companyId);
			return ServiceResult.errorKeys(ERROR_MSG);
		}

		String targetExpression = targetId > 0 ? getTargetGroupExpressionOrDefault(targetId, companyId) : null;

		try {
			int affectedRecipients = recipientDao.bulkUpdateEachRecipientsFields(companyId, admin.getAdminID(),
					mailinglistId, targetExpression, excludedImmutable);

			return new ServiceResult<>(
					new FieldsSaveResults(affectedRecipients, excludedImmutable),
					true,
					Collections.emptyList()
			);
		} catch (Exception e) {
			return ServiceResult.errorKeys(ERROR_MSG);
		}
	}

	@Override
	public ServiceResult<Integer> getAffectedRecipientsCountForBulkSaveFields(Admin admin, int targetId, int mailinglistId, Map<String, RecipientFieldDto> fieldChanges) {
		if (fieldChanges.isEmpty()) {
			return ServiceResult.success(0);
		}

		ServiceResult<Map<String, Object>> valuesForUpdate = getValuesForSaveBulkRecipientFields(fieldChanges, admin);
		if (!valuesForUpdate.isSuccess()) {
			return ServiceResult.from(valuesForUpdate);
		}

		if (MapUtils.isEmpty(valuesForUpdate.getResult())) {
			return ServiceResult.success(0);
		}

		int companyId = admin.getCompanyID();
		Locale locale = admin.getLocale();

		Map<String, Object> excludedImmutable = excludeImmutableFields(valuesForUpdate.getResult());
		if (excludedImmutable.isEmpty()) {
			logger.error("No field to change found. Maybe all fields given are immutable for company {}", companyId);
			return ServiceResult.errorKeys("Error");
		}

		try {
			String targetExpression = targetId > 0 ? getTargetGroupExpressionOrDefault(targetId, companyId) : null;

			int affectedRecipients = recipientDao.getAffectedRecipientsForBulkUpdateFields(companyId, admin.getAdminID(),
					mailinglistId, targetExpression, excludedImmutable);

			return ServiceResult.success(affectedRecipients);
		} catch (ProfileFieldBulkUpdateException e) {
			return ServiceResult.error(Message.exact(String.format("%s: %s", e.getProfileFieldName(),
					I18nString.getLocaleString("error.bulkAction.exception", locale, e.getMessage()))));
		} catch (ImportException e) {
			return ServiceResult.error(Message.of("error.bulkAction.exception",
					I18nString.getLocaleString(e.getErrorMessageKey(), locale, e.getAdditionalErrorData())));
		} catch (Exception e) {
			return ServiceResult.errorKeys("Error");
		}
	}

	private ServiceResult<Map<String, Object>> getValuesForSaveBulkRecipientFields(Map<String, RecipientFieldDto> fieldChanges, Admin admin) {
		Set<String> immutableField = RecipientStandardField.getBulkImmutableRecipientStandardFieldColumnNames();
		Collection<String> immutableFieldsToChange = CollectionUtils.retainAll(fieldChanges.keySet(), immutableField);
		if (CollectionUtils.isNotEmpty(immutableFieldsToChange)) {
			return ServiceResult.error(Message.of("error.bulkAction.field.immutable", StringUtils.join(immutableFieldsToChange, ", ")));
		}

		boolean success = true;
		List<Message> errors = new ArrayList<>();

		Map<String, Object> valuesForUpdate = new HashMap<>();
		for (RecipientFieldDto fieldChange : fieldChanges.values()) {
			ServiceResult<Object> validationResult =
					profileFieldValidationService.validateNewProfileFieldValue(admin, fieldChange.getShortname(), fieldChange.getType(), fieldChange.getNewValue(), fieldChange.isClear());
			if (validationResult.isSuccess()) {
				valuesForUpdate.put(fieldChange.getShortname(), validationResult.getResult());
			} else {
				success = false;
				errors.addAll(validationResult.getErrorMessages());
			}
		}

		return new ServiceResult<>(valuesForUpdate, success, errors);
	}

	private Map<String, Object> excludeImmutableFields(Map<String, Object> valuesForUpdate) {
		Set<String> guiBulkImmutableFields = RecipientStandardField.getBulkImmutableRecipientStandardFieldColumnNames();
		return valuesForUpdate.entrySet().stream()
				.filter(pair -> !guiBulkImmutableFields.contains(pair.getKey()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	@Override
	public List<RecipientLiteImpl> getAdminAndTestRecipients(int companyId, int mailinglistId) {
		return recipientDao.getAdminAndTestRecipients(companyId, mailinglistId);
	}

	@Override
	public final List<RecipientLiteImpl> listAdminAndTestRecipients(final Admin admin) {
		return recipientDao.listAdminAndTestRecipientsByAdmin(admin.getCompanyID(), admin.getAdminID());
	}

	@Override
	public List<RecipientSalutationDto> getAdminAndTestRecipientsSalutation(final Admin admin) {
		return recipientDao.getAdminAndTestRecipientsSalutation(admin.getCompanyID(), admin.getAdminID());
	}

	@Override
	public void supplySourceID(Recipient recipient, int defaultId) {
		if (!recipient.hasCustParameter("DATASOURCE_ID")) {
			recipient.getCustParameters().put("DATASOURCE_ID", String.valueOf(defaultId));
		}
		recipient.getCustParameters().put("LATEST_DATASOURCE_ID", recipient.getCustParametersNotNull("DATASOURCE_ID"));
	}

	@Override
	public List<Integer> listRecipientIdsByTargetGroup(int targetId, int companyId) {
		Target target = this.targetService.getTargetGroup(targetId, companyId);
		return this.recipientDao.listRecipientIdsByTargetGroup(companyId, target);
	}

	@Override
	public JSONArray getDeviceHistoryJson(int companyId, int recipientId) {
		JSONArray actionsJson = new JSONArray();
		for (RecipientReaction deviceHistory: recipientDao.getRecipientReactionsHistory(recipientId, companyId)) {
            JSONObject entry = new JSONObject();

            entry.put("timestamp", DateUtilities.toLong(deviceHistory.getTimestamp()));
			entry.put("mailingName", deviceHistory.getMailingName());
			entry.put("reactionType", deviceHistory.getReactionType().getMessageKey());
			entry.put("deviceType", deviceHistory.getDeviceClass());
			entry.put("deviceName", deviceHistory.getDeviceName());

			actionsJson.put(entry);
		}
		return actionsJson;
	}

    @Override
    public JSONArray getWebtrackingHistoryJson(Admin admin, int recipientId) {
		JSONArray actionsJson = new JSONArray();

		List<WebtrackingHistoryEntry> webtrackingHistoryEntries = recipientDao.getRecipientWebtrackingHistory(admin.getCompanyID(), recipientId);
		webtrackingHistoryEntries.sort(Collections.reverseOrder());

		for (WebtrackingHistoryEntry trackingHistory: webtrackingHistoryEntries) {
            JSONObject entry = new JSONObject();

            entry.put("timestamp", DateUtilities.toLong(trackingHistory.getDate()));
            entry.put("mailingTitle", String.format("%s (%d)", trackingHistory.getMailingName(),  trackingHistory.getMailingID()));
			entry.put("name", trackingHistory.getName());

			Object value = trackingHistory.getValue();
			if (trackingHistory.isLinkValue()) {
				value = I18nString.getLocaleString("default.Yes", admin.getLocale());
			}
			entry.put("value", value);

			actionsJson.put(entry);
		}
		return actionsJson;
    }

    @Override
    public JSONArray getContactHistoryJson(int companyId, int recipientId) {
		JSONArray data = new JSONArray();
		List<RecipientMailing> historyList = recipientDao.getMailingsDeliveredToRecipient(recipientId, companyId);
		List<RecipientMailing> sorted = historyList.stream().sorted((h1, h2) -> h2.getSendDate().compareTo(h1.getSendDate())).toList();
		for (RecipientMailing history: sorted) {
			JSONObject entry = new JSONObject();

			entry.put("sendDate", DateUtilities.toLong(history.getSendDate()));

			MailingType mailingType = history.getMailingType();
			String typeMessageKey = "";
			if (mailingType != null) {
				typeMessageKey = mailingType.getMessagekey();
			}
			entry.put("typeMessageKey", typeMessageKey);

			entry.put("mailingId", history.getMailingId());
			entry.put("mailingName", history.getShortName());
			entry.put("subject", history.getSubject());
			entry.put("deliveryDate", getContactHistoryDeliveryDate(history, recipientId, companyId));
			entry.put("openings", history.getNumberOfOpenings());
			entry.put("clicks", history.getNumberOfClicks());
			entry.put("numberOfDeliveries", history.getSendCount());

			data.put(entry);
		}

		return data;
    }

    private Object getContactHistoryDeliveryDate(RecipientMailing mailing, int recipientId, int companyId) {
        if (mailing.getDeliveryDate() != null) {
            return DateUtilities.toLong(mailing.getDeliveryDate());
        }
        int bounce = recipientDao.getBounceDetail(mailing.getMailingId(), recipientId, companyId);
        if (bounce <= 0) {
            return "";
        }
        return bounce < 510 ? "soft-bounce" : "hard-bounce";
    }

    @Override
	public void updateDataSource(Recipient recipient) {
		recipientDao.updateDataSource(recipient);
	}

	@Override
	public int findByKeyColumn(Recipient recipient, String keyColumn, String keyVal) {
		return recipientDao.findByKeyColumn(recipient, keyColumn, keyVal);
	}

	/**
	 * Load structure of Customer-Table for the given Company-ID in member
	 * variable "companyID". Load profile data into map. Has to be done before
	 * working with customer-data in class instance
	 *
	 * @return true on success
	 */
	@Override
	public Map<String, String> getRecipientDBStructure(int companyId) {
		Map<String, String> structure = new CaseInsensitiveMap<>();
		if (companyId > 0) {
			try {
				List<ProfileField> columns = columnInfoService.getColumnInfos(companyId);
				for (ProfileField field : columns) {
					structure.put(field.getColumn(), field.getDataType());
				}
			} catch (Exception e) {
				logger.warn("Could not load recipient column: " + e.getMessage());
			}

		}

		return structure;
	}

	/**
	 * Updates customer data by analyzing given HTTP-Request-Parameters
	 *
	 * @return true on success
	 * @param suffix
	 *            Suffix appended to Database-Column-Names when searching for
	 *            corresponding request parameters
	 * @param requestParameters
	 *            Map containing all HTTP-Request-Parameters as key-value-pair.
	 */
	@Override
	public boolean importRequestParameters(Recipient recipient, Map<String, Object> requestParameters, String suffix) {
		CaseInsensitiveMap<String, Object> caseInsensitiveParameters = new CaseInsensitiveMap<>(requestParameters);

		if (suffix == null) {
			suffix = "";
		}

		recipient.setCustDBStructure(getRecipientDBStructure(recipient.getCompanyID()));
		for (Entry<String, String> entry : recipient.getCustDBStructure().entrySet()) {
			String colType = entry.getValue();
			String name = entry.getKey().toUpperCase();

			if (!recipient.isAllowedName(entry.getKey())) {
				continue;
			}
			if (colType.equalsIgnoreCase(DbColumnType.GENERIC_TYPE_DATE) || colType.equalsIgnoreCase(DbColumnType.GENERIC_TYPE_DATETIME)) {
				if (StringUtils.isNotBlank((String) caseInsensitiveParameters.get(entry.getKey() + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT))) {
					String value = (String) caseInsensitiveParameters.get(entry.getKey());
					if (StringUtils.isNotBlank(value)) {
						try {
							SimpleDateFormat format = new SimpleDateFormat((String) caseInsensitiveParameters.get(entry.getKey() + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT));
							format.setLenient(false);
							GregorianCalendar date = new GregorianCalendar();
							date.setTime(format.parse(value));
							recipient.setCustParameters(entry.getKey() + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(date.get(Calendar.DAY_OF_MONTH)));
							recipient.setCustParameters(entry.getKey() + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(date.get(Calendar.MONTH) + 1));
							recipient.setCustParameters(entry.getKey() + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(date.get(Calendar.YEAR)));
							recipient.setCustParameters(entry.getKey() + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, Integer.toString(date.get(Calendar.HOUR_OF_DAY)));
							recipient.setCustParameters(entry.getKey() + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, Integer.toString(date.get(Calendar.MINUTE)));
							recipient.setCustParameters(entry.getKey() + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, Integer.toString(date.get(Calendar.SECOND)));
						} catch (ParseException e) {
							logger.error("Invalid value for customer field '" + entry.getKey() + "' with expected format '" + ((String) caseInsensitiveParameters.get(entry.getKey() + RecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT)) + "'");
						}
					}
				} else {
					recipient.copyDateFromRequest(caseInsensitiveParameters, entry.getKey(), suffix);
				}
			} else if (caseInsensitiveParameters.get(name + suffix) != null) {
				String aValue = (String) caseInsensitiveParameters.get(name + suffix);
				if (name.equalsIgnoreCase("EMAIL")) {
					if (aValue.length() == 0) {
						aValue = " ";
					}
					aValue = aValue.toLowerCase();
					aValue = aValue.trim();
				} else if (name.length() > 4) {
					if (name.substring(0, 4).equals("SEC_")
							|| name.equals("FIRSTNAME")
							|| name.equals("LASTNAME")) {
						if (!recipient.isSecure(aValue)) {
							return false;
						}
					}
				}
				if (name.equalsIgnoreCase("DATASOURCE_ID")) {
					if (!recipient.hasCustParameter(entry.getKey())) {
						recipient.setCustParameters(entry.getKey(), aValue);
					}
				} else {
					recipient.setCustParameters(entry.getKey(), aValue);
				}
			}
		}
		return true;
	}

	@Override
	public int findByUserPassword(int companyId, String keyColumn, String keyVal, String passColumn, String passVal) {
		return recipientDao.findByUserPassword(companyId, keyColumn, keyVal, passColumn, passVal);
	}

	/**
	 * Updates internal Datastructure for Mailinglist-Bindings of this customer
	 * by analyzing HTTP-Request-Parameters
	 *
//	 * @param tafWriteBack
	 *            if true, eventually existent TAF-Information will be written
	 *            back to source-customer
	 * @param params
	 *            Map containing all HTTP-Request-Parameters as key-value-pair.
	 * @param doubleOptIn
	 *            true means use Double-Opt-In
	 * @return true on success
	 */
	@Override
	public void updateBindingsFromRequest(Recipient recipient, Map<String, Object> params, boolean doubleOptIn, String remoteAddr, String referrer) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, Object> requestParameters = (Map<String, Object>) params.get("requestParameters"); // suppress warning for this cast
		int mailingID;

		try {
			Integer tmpNum = (Integer) params.get("mailingID");
			mailingID = tmpNum.intValue();
		} catch (Exception e) {
			mailingID = 0;
		}

		// Requests without any "agnSUBSCRIBE"-parameter are still valid
		// Those are used for updating the users data only without any new subscription
		for (Entry<String, Object> entry : requestParameters.entrySet()) {
			if (StringUtils.startsWithIgnoreCase(entry.getKey(), "agnSUBSCRIBE")) {
				String postfix = "";

				int mediatype;
				int subscribeStatus;
				BindingEntry aEntry = null;
				if (entry.getKey().length() > "agnSUBSCRIBE".length()) {
					postfix = entry.getKey().substring("agnSUBSCRIBE".length());
				}

				String agnSubscribeString = (String) entry.getValue();
				if (StringUtils.isBlank(agnSubscribeString)) {
					throw new Exception("Mandatory subscribeStatus (form-param: agnSUBSCRIBE) is missing: " + getRequestParameterString(params));
				}

				try {
					subscribeStatus = Integer.parseInt(agnSubscribeString);
				} catch (Exception e) {
					throw new Exception("Mandatory subscribeStatus (form-param: agnSUBSCRIBE) is missing or invalid: " + getRequestParameterString(params));
				}
				if (subscribeStatus != 0 && subscribeStatus != 1) {
					throw new Exception("Mandatory subscribeStatus (form-param: agnSUBSCRIBE) is invalid: " + getRequestParameterString(params));
				}

				String agnMailinglistString = (String) requestParameters.get("agnMAILINGLIST" + postfix);
				if (StringUtils.isBlank(agnMailinglistString)) {
					throw new Exception("Mandatory mailinglistID (form-param: agnMAILINGLIST) is missing: " + getRequestParameterString(params));
				}

				int mailinglistID;
				try {
					mailinglistID = Integer.parseInt(agnMailinglistString);
				} catch (NumberFormatException e) {
					throw new ViciousFormDataException("Mandatory mailinglistID (form-param: agnMAILINGLIST) is invalid: " + agnMailinglistString);
				} catch (Exception e) {
					throw new Exception("Mandatory mailinglistID (form-param: agnMAILINGLIST) is missing or invalid: " + getRequestParameterString(params));
				}
				if (mailinglistID <= 0) {
					throw new Exception("Mandatory mailinglistID (form-param: agnMAILINGLIST) is invalid: " + getRequestParameterString(params));
				}

				try {
					mediatype = Integer.parseInt((String) requestParameters.get("agnMEDIATYPE" + postfix));
				} catch (Exception e) {
					mediatype = MediaTypes.EMAIL.getMediaCode();
				}

				doUpdateBindings(recipient, doubleOptIn, mediatype, mailinglistID, mailingID, aEntry, subscribeStatus, remoteAddr, referrer);
			}
		}
	}

	private String getRequestParameterString(Map<String, Object> params) {
		String requestData;
		HttpServletRequest request = (HttpServletRequest) params.get("_request");
		if (request != null) {
			requestData = "\n" + request.getRequestURL().toString() + " \nParams: \n" + AgnUtils.mapToString(request.getParameterMap());
			requestData += "\nIP: " + request.getRemoteAddr();
		} else {
			requestData = "";
		}
		return requestData;
	}

	private void doUpdateBindings(Recipient recipient, boolean doubleOptIn, int mediatype, int mailinglistID, int mailingID, BindingEntry aEntry, int subscribeStatus, String remoteAddr, String referrer) {
		// find BindingEntry or create new one
		int companyID = recipient.getCompanyID();
		int customerID = recipient.getCustomerID();
		Map<Integer, BindingEntry> mList = recipient.getListBindings().get(mailinglistID);
		if (mList != null) {
			aEntry = mList.get(mediatype);
		}

		if (aEntry != null) {
			// put changes in db
			switch (UserStatus.getByCode(aEntry.getUserStatus())) {
				case AdminOut, Bounce, UserOut:
					if (subscribeStatus == 1) {
						// Subscribe this currently inactive recipient
						if (!doubleOptIn) {
							aEntry.setUserStatus(UserStatus.Active.getStatusCode());
							if (remoteAddr == null) {
								aEntry.setUserRemark("CSV Import");
							} else {
								aEntry.setUserRemark("Opt-In-IP: " + remoteAddr);
								aEntry.setReferrer(referrer);
							}
						} else {
							aEntry.setUserStatus(UserStatus.WaitForConfirm.getStatusCode());
							if (remoteAddr == null) {
								aEntry.setUserRemark("CSV Import");
							} else {
								aEntry.setUserRemark("Opt-In-IP: " + remoteAddr);
								aEntry.setReferrer(referrer);
							}
						}
						bindingEntryDao.updateStatus(aEntry, companyID);
					}
					break;
				case WaitForConfirm, Active:
					if (subscribeStatus == 0) {
						// Unsubscribe this currently active recipient
						aEntry.setUserStatus(UserStatus.UserOut.getStatusCode());
						if (remoteAddr == null) {
							aEntry.setUserRemark("CSV Import");
						} else {
							aEntry.setUserRemark("User-Opt-Out: " + remoteAddr);
							aEntry.setExitMailingID(mailingID);
						}
						bindingEntryDao.updateStatus(aEntry, companyID);
					}
					break;
				case Blacklisted:
					break;
				case Suspend:
					break;
				default:
					break;
			}
		} else {
			if (subscribeStatus == 1) {
				aEntry = bindingEntryFactory.newBindingEntry();
				aEntry.setCustomerID(customerID);
				aEntry.setMediaType(mediatype);
				aEntry.setMailinglistID(mailinglistID);
				aEntry.setUserType(BindingEntry.UserType.World.getTypeCode());

				if (!doubleOptIn) {
					aEntry.setUserStatus(UserStatus.Active.getStatusCode());
					if (remoteAddr == null) {
						aEntry.setUserRemark("CSV Import");
					} else {
						aEntry.setUserRemark("Opt-In-IP: " + remoteAddr);
						aEntry.setReferrer(referrer);
					}
				} else {
					aEntry.setUserStatus(UserStatus.WaitForConfirm.getStatusCode());
					if (remoteAddr == null) {
						aEntry.setUserRemark("CSV Import");
					} else {
						aEntry.setUserRemark("Opt-In-IP: " + remoteAddr);
						aEntry.setReferrer(referrer);
					}
				}

				aEntry.insertNewBindingInDB(companyID);
				if (mList == null) {
					mList = new HashMap<>();
					recipient.getListBindings().put(mailinglistID, mList);
				}
				mList.put(mediatype, aEntry);
			}
		}
	}

	/**
	 * Load structure of Customer-Table for the given Company-ID in member
	 * variable "companyID". Load profile data into map. Has to be done before
	 * working with customer-data in class instance
	 *
	 * @return true on success
	 */
	@Override
	public Map<String, Object> getCustomerDataFromDb(int companyId, int customerId, DateFormat dateFormat) {
		return recipientDao.getCustomerDataFromDb(companyId, customerId, dateFormat);
	}

	@Override
	public Map<Integer, Map<Integer, BindingEntry>> getMailinglistBindings(int companyId, int customerId) {
		return recipientDao.getAllMailingLists(customerId, companyId);
	}

	@Override
	public String getRecipientField(String value, int customerId, int companyId) {
		return recipientDao.getField(value, customerId, companyId);
	}

	@Override
	public List<Integer> getMailingRecipientIds(int companyID, int mailinglistID, MediaTypes post, String fullTargetSql, List<UserStatus> userstatusList) {
		return recipientDao.getMailingRecipientIds(companyID, mailinglistID, post, fullTargetSql, userstatusList);
	}

	@Override
	public void logMailingDelivery(int companyId, int id, int customerId, int mailingId) {
		recipientDao.logMailingDelivery(companyId, id, customerId, mailingId);
	}

	@Override
	public boolean updateRecipientInDB(Recipient recipient) {
		return recipientDao.updateInDbWithException(recipient);
	}

	@Override
	public void deleteCustomerDataFromDb(int companyId, int customerId) {
		recipientDao.deleteCustomerDataFromDb(companyId, customerId);
	}

	@Override
	public int saveNewCustomer(Recipient recipient) {
		return recipientDao.insertNewCustWithException(recipient);
	}

	@Override
	public List<Recipient> findRecipientByData(int companyID, Map<String, Object> dataMap) {
		return recipientDao.findByData(companyID, dataMap);
	}

	@Override
	public List<Recipient> findAllByEmailPart(String email, List<Integer> companiesIds) {
		return companiesIds.stream()
				.filter(recipientDao::tableExists)
				.flatMap(cId -> recipientDao.findAllByEmailPart(email, cId).stream())
                .collect(Collectors.toList());
	}

	@Override
	public List<Integer> getRecipientIds(int companyID, String keyColumn, String value) {
		return recipientDao.getRecipientIDs(companyID, keyColumn, value);
	}

    @Override
    public void updateBindings(List<BindingEntry> bindings, int companyId) {
        bindingEntryDao.updateBindings(companyId, bindings);
    }

	@Override
	public ServiceResult<Integer> saveRecipient(Admin admin, SaveRecipientDto recipientDto, List<UserAction> userActions) {
		int companyId = admin.getCompanyID();
		boolean isNew = recipientDto.getId() == 0;
		
		SubscriberLimitCheckResult subscriberLimitCheckResult = null;
		if (isNew) {
			subscriberLimitCheckResult = subscriberLimitCheck.checkSubscriberLimit(companyId);
		}

		CaseInsensitiveMap<String, Object> rowValues = new CaseInsensitiveMap<>();
		try {
			rowValues.putAll(collectRecipientData(admin, recipientDto));
		} catch (UserMessageException e) {
			return ServiceResult.error(Message.of(e));
		}

		try {
			if (isNew) {
				Recipient recipient = new RecipientImpl();
				recipient.setCompanyID(admin.getCompanyID());
				recipient.setCustParameters(rowValues);
				recipientDao.insertNewCustWithException(recipient);
				recipientDto.setId(recipient.getCustomerID());
			} else {
				int recipientId = recipientDao.saveRecipient(companyId, recipientDto.getId(), rowValues);
				recipientDto.setId(recipientId);
			}
			
			if (subscriberLimitCheckResult != null && subscriberLimitCheckResult.isWithinGraceLimitation()) {
				List<Message> warningMessages = new ArrayList<>();
				warningMessages.add(Message.of(
						"error.numberOfCustomersExceeded.graceful",
						subscriberLimitCheckResult.getMaximumNumberOfCustomers(),
						subscriberLimitCheckResult.getCurrentNumberOfCustomers(),
						subscriberLimitCheckResult.getGracefulLimitExtension()));
				return new ServiceResult<>(recipientDto.getId(), true, null, warningMessages, null);
			} else {
				return new ServiceResult<>(recipientDto.getId(), true);
			}
		} catch (UiMessageException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error occurred: {}", e.getMessage(), e);
			return ServiceResult.error(Message.of("error.recipient.create"));
		}
	}

	@Override
	public SimpleServiceResult isRecipientMatchAltgTarget(Admin admin, SaveRecipientDto recipient) {
        return new SimpleServiceResult(true);
	}

	public Map<String, Object> collectRecipientData(Admin admin, SaveRecipientDto recipient) throws UserMessageException {
		return collectRecipientData(admin, recipient, Collections.emptyMap());
	}

	protected Map<String, Object> collectRecipientData(Admin admin, SaveRecipientDto recipient, Map<String, Object> savedData) throws UserMessageException {
		boolean isNew = recipient.getId() == 0;

		Map<String, String> recipientRows = recipient.getFieldsToSave();

		int dataSourceId = companyDao.getCompanyDatasource(admin.getCompanyID());

		boolean replacedBySavedData = !isNew && !savedData.isEmpty();

		CaseInsensitiveMap<String, Object> rowValues = new CaseInsensitiveMap<>();
		for (ProfileField type: getRecipientColumnInfos(admin)) {
            String fieldName = type.getShortname();
			String column = type.getColumn();
			String value = recipientRows.get(column);

			if (column.equals(RecipientStandardField.DatasourceID.getColumnName())) {
				rowValues.put(RecipientStandardField.DatasourceID.getColumnName(), dataSourceId);
			} else if (column.equals(RecipientStandardField.LatestDatasourceID.getColumnName())) {
				rowValues.put(RecipientStandardField.LatestDatasourceID.getColumnName(), dataSourceId);
            } else if (column.equals(RecipientStandardField.ChangeDate.getColumnName())) {
                rowValues.put(RecipientStandardField.ChangeDate.getColumnName(), null);
			} else if (type.getModeEdit() != ProfileFieldMode.Editable && replacedBySavedData) {
				//if not new and saved data map is not empty add to rowValues saved data from db
				rowValues.put(column, savedData.get(column));
			} if (isNew || type.getModeEdit() == ProfileFieldMode.Editable) {
				//don't add not editable fields if update recipient
				if (!type.getNullable() && isEmptyColumnValue(type, value)) {
					value = type.getDefaultValue();
					rowValues.put(column, toDaoValue(admin, type, value));
				} else {
					switch (type.getSimpleDataType()) {
						case Numeric:
						case Float:
							if (StringUtils.isEmpty(value)) {
								rowValues.put(column, null);
								break;
							} else {
								value = AgnUtils.getNormalizedDecimalNumber(value, admin.getLocale());
                                if (AgnUtils.isDouble(value)) {
									if (type.getNumericPrecision() > 0 && value.length() > type.getNumericPrecision() && recipientDao.isOracleDB()) {
										throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
									}
									rowValues.put(column, AgnUtils.parseNumber(value));
								} else {
									throw new UserMessageException("error.value.notANumberForField", fieldName);
								}

								double doubleValue;
								try {
									doubleValue = Double.parseDouble(value);
								} catch (NumberFormatException e) {
									throw new UserMessageException("error.value.notANumberForField", fieldName);
								}

								if (recipientDao.isOracleDB()) {
									if ("integer".equalsIgnoreCase(type.getDataType())) {
										if (doubleValue > 2147483647) {
											throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
										} else if (doubleValue < -2147483648) {
											throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
										}
									} else if (type.getNumericPrecision() < Double.toString(doubleValue).length()) {
										throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
									}
								} else {
									if ("smallint".equalsIgnoreCase(type.getDataType())) {
										if (doubleValue > 32767) {
											throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
										} else if (doubleValue < -32768) {
											throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
										}
									} else if ("int".equalsIgnoreCase(type.getDataType()) || "integer".equalsIgnoreCase(type.getDataType())) {
										if (doubleValue > 2147483647) {
											throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
										} else if (doubleValue < -2147483648) {
											throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
										}
									} else if ("bigint".equalsIgnoreCase(type.getDataType())) {
										if (doubleValue > 9223372036854775807l) {
											throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
										} else if (doubleValue < -9223372036854775808l) {
											throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
										}
									}
								}
								break;
							}

						case Characters:
							if (StringUtils.isEmpty(value)) {
								rowValues.put(column, "");
								break;
							} else if (value.length() <= type.getMaxDataSize()) {
								rowValues.put(column, value);
								break;
							} else {
								throw new UserMessageException("error.value.toolargeforfield", fieldName);
							}

						case Date:
                            rowValues.put(column, getRecipientDateField(value, admin.getDateFormat()));
                            break;
                        case DateTime:
                            rowValues.put(column, getRecipientDateField(value, admin.getDateTimeFormat()));
                            break;
						default:
							throw new UnsupportedOperationException("Unsupported data type: " + type.getSimpleDataType());

					}
				}
			}
		}

		return rowValues;
	}

    private Object getRecipientDateField(String dateStr, SimpleDateFormat format) throws UserMessageException {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        } else if (DbUtilities.isNowKeyword(dateStr)) {
            return "CURRENT_TIMESTAMP";
        }
        return parseRecipientDateFromString(dateStr, format);
    }

    private Date parseRecipientDateFromString(String dateStr, SimpleDateFormat format) throws UserMessageException {
        try {
            return tryParseDate(dateStr, format);
        } catch (Exception e) {
            throw new UserMessageException(INVALID_DATE_FIELD_EROR_CODE, dateStr);
        }
    }

    private Date tryParseDate(String dateStr, SimpleDateFormat format) throws Exception {
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            return RecipientUtils.parseUnknownDateFormat(dateStr);
        }
    }

	@Override
	@Transactional
	public ServiceResult<List<BindingAction>> saveRecipientBindings(Admin admin, int recipientId, RecipientBindingsDto bindings, UserStatus newStatusForUnsubscribing) {
		int companyId = admin.getCompanyID();
		Map<Integer, Map<Integer, BindingEntry>> mailinglistBindings = getMailinglistBindings(companyId, recipientId);
		List<Integer> existingMailinglistIds = mailinglistDao.getMailinglistIds(companyId);
		List<BindingAction> bindingActions = new ArrayList<>();
		boolean oldEmailBlacklisted = bindings.isOldBlacklistedEmail();
        boolean newEmailBlacklisted = bindings.isNewBlacklistedEmail();

		try {
			List<BindingEntry> updateBindings = new ArrayList<>();
			List<BindingEntry> insertBindings = new ArrayList<>();
			for (RecipientBindingDto bindingDto : bindings.getBindings()) {
				if (existingMailinglistIds.contains(bindingDto.getMailinglistId())) {
					BindingEntry bindingForSave = mailinglistBindings
                            .getOrDefault(bindingDto.getMailinglistId(), new HashMap<>())
							.get(bindingDto.getMediaType().getMediaCode());

					changeBindingUserTypeIfNeeded(bindingDto, bindingForSave, admin);

					if (bindingForSave == null) {
                        if (bindingDto.isActiveStatus()) {
							UserStatus newUserStatus = getNewUserStatus(bindingDto.getStatus(), newStatusForUnsubscribing,
									null, newEmailBlacklisted, oldEmailBlacklisted);
							String description = getSaveBindingResultDescription(bindingDto, null, bindingDto.getUserType(),
									null, UserStatus.getByCode(newUserStatus.getStatusCode()));

							insertBindings.add(generateBindingToInsert(bindingDto, recipientId, admin, newUserStatus));

							bindingActions.add(new BindingAction(BindingAction.Type.CREATE, description));
                        }
					} else if (isBindingChanged(bindingForSave, bindingDto, newEmailBlacklisted, oldEmailBlacklisted, newStatusForUnsubscribing)) {
						String oldUserType = bindingForSave.getUserType();
						String newUserType = bindingDto.getUserType();
						UserStatus oldUserStatus = UserStatus.getByCode(bindingForSave.getUserStatus());
						UserStatus newUserStatus = getNewUserStatus(bindingDto.getStatus(), newStatusForUnsubscribing,
								oldUserStatus, newEmailBlacklisted, oldEmailBlacklisted);

						String description = getSaveBindingResultDescription(bindingDto, oldUserType, newUserType, oldUserStatus, newUserStatus);
						bindingActions.add(new BindingAction(BindingAction.Type.UPDATE, description));

                        updateBinding(bindingForSave, bindingDto, recipientId, admin, oldUserStatus, newUserStatus);
                        updateBindings.add(bindingForSave);
                    }
				}
			}

			bindingEntryDao.insertBindings(companyId, insertBindings);
			bindingEntryDao.updateBindings(companyId, updateBindings);

			return new ServiceResult<>(bindingActions, true);
		} catch (UiMessageException ume) {
			return ServiceResult.error(new ArrayList<>(ume.getErrors()));
		} catch (Exception e) {
			logger.error("Saving bindings failed: ", e);
		}

		return ServiceResult.error(Collections.emptyList());
	}

	protected void changeBindingUserTypeIfNeeded(RecipientBindingDto binding, BindingEntry existingBinding, Admin admin) {
		// in case when binding was deactivated then select with user type became disabled.
		// for this case we should prevent change of user type
		if (!binding.isActiveStatus() && existingBinding != null) {
			binding.setUserType(existingBinding.getUserType());
		}
	}

    private void updateBinding(BindingEntry bindingToUpdate, RecipientBindingDto bindingDto, int recipientId,
							   Admin admin, UserStatus oldUserStatus, UserStatus newUserStatus) {

		bindingToUpdate.setCustomerID(recipientId);
        bindingToUpdate.setMailinglistID(bindingDto.getMailinglistId());
        bindingToUpdate.setMediaType(bindingDto.getMediaType().getMediaCode());
        bindingToUpdate.setUserType(bindingDto.getUserType());

        if (StringUtils.isEmpty(bindingToUpdate.getUserRemark())) {
            bindingToUpdate.setUserRemark(BindingUtils.getUserRemarkForStatusByAdmin(admin, bindingToUpdate.getUserStatus()));
        }

        if (oldUserStatus != newUserStatus) {
            bindingToUpdate.setUserStatus(newUserStatus.getStatusCode());
            bindingToUpdate.setUserRemark(BindingUtils.getUserRemarkForStatusByAdmin(admin, newUserStatus));
        }
    }

    private boolean isBindingChanged(BindingEntry bindingToUpdate, RecipientBindingDto bindingDto,
                                     boolean newEmailBlacklisted, boolean oldEmailBlacklisted, UserStatus newStatusForUnsubscribing) {
    	UserStatus oldUserStatus = UserStatus.getByCode(bindingToUpdate.getUserStatus());
        UserStatus newUserStatus = getNewUserStatus(bindingDto.getStatus(), newStatusForUnsubscribing, oldUserStatus, newEmailBlacklisted, oldEmailBlacklisted);
        String oldUserType = bindingToUpdate.getUserType();
        String newUserType = bindingDto.getUserType();

        return bindingToUpdate.getExitMailingID() != bindingDto.getExitMailingId()
                || bindingToUpdate.getMediaType() != bindingDto.getMediaType().getMediaCode()
                || !StringUtils.equals(oldUserType, newUserType)
                || oldUserStatus != newUserStatus;
	}

    private String getSaveBindingResultDescription(RecipientBindingDto binding,
                                                   String oldUserType, String newUserType,
                                                   UserStatus oldUserStatus, UserStatus newUserStatus) {
        if (oldUserStatus != newUserStatus) {
            return String.format("Recipient %s type for mailinglist with ID: %d %s ",
                    binding.getMediaType().name(), binding.getMailinglistId(), newUserStatus == UserStatus.Active ? "switch on" : "switch off");
        } else {
            return String.format("Recipient %s type for mailinglist with ID: %d changed from %s to %s",
                    binding.getMediaType().name(),
                    binding.getMailinglistId(),
                    getRecipientTypeTitleByLetter(oldUserType), getRecipientTypeTitleByLetter(newUserType));
        }
    }

    private BindingEntry generateBindingToInsert(RecipientBindingDto bindingDto, int recipientId, Admin admin, UserStatus newUserStatus) {
        BindingEntry binding = new BindingEntryImpl();

        binding.setCustomerID(recipientId);
        binding.setMailinglistID(bindingDto.getMailinglistId());
        binding.setMediaType(bindingDto.getMediaType().getMediaCode());
        binding.setUserType(bindingDto.getUserType());
        if (Objects.nonNull(newUserStatus)) {
            binding.setUserStatus(newUserStatus.getStatusCode());
        }
        binding.setUserRemark(BindingUtils.getUserRemarkForStatusByAdmin(admin, newUserStatus));
        return binding;
    }

	@Override
	public List<String> fetchRecipientNames(Set<Integer> bulkIds, int companyID) {
		return recipientDao.fetchRecipientNames(bulkIds, companyID);
	}

	@Override
    public JSONArray getClicksJson(int recipientId, int mailingId, int companyId) {
        JSONArray jsonArray = new JSONArray();
        recipientDao.getRecipientClicksHistory(recipientId, mailingId, companyId).forEach(rowMap -> {
            JSONObject elem = new JSONObject();
            elem.put("full_url", rowMap.get("full_url"));
            elem.put("count", rowMap.get("count"));
            elem.put("last_time", ((Date) rowMap.get("last_time")).getTime());
            jsonArray.put(elem);
        });
        return jsonArray;
    }

    @Override
	public int getRecipientIdByAddress(Admin admin, int recipientId, String email) {
		return recipientDao.getRecipientIdByAddress(StringUtils.trimToEmpty(email), recipientId, admin.getCompanyID());
	}

    @Override
    public JSONArray getRecipientStatusChangesHistory(Admin admin, int recipientId) {
		int companyId = admin.getCompanyID();

		Map<Date, List<JSONObject>> groupedMap = new HashMap<>();
		Locale locale = admin.getLocale();

		for (RecipientHistory history : recipientDao.getRecipientBindingHistory(recipientId, companyId)) {
			JSONObject entry = new JSONObject();

			Date changeDate = history.getChangeDate();
			entry.put("changeDate", DateUtilities.toLong(changeDate));

			String fieldDescription = getBindingHistoryFieldDescription(history, locale);
			entry.put("fieldDescription", fieldDescription);

			String oldValueDescription = getBindingHistoryValueDescription(history, history.getOldValue(), locale, false);
			entry.put("oldValue", oldValueDescription);
			String newValueDescription = getBindingHistoryValueDescription(history, history.getNewValue(), locale, true);
			entry.put("newValue", newValueDescription);

			List<JSONObject> list = groupedMap.computeIfAbsent(changeDate, dateKey -> new ArrayList<>());
			list.add(entry);
		}

		for (RecipientHistory history : recipientDao.getRecipientProfileHistory(recipientId, companyId)) {
			JSONObject entry = new JSONObject();

			Date changeDate = history.getChangeDate();
			entry.put("changeDate", DateUtilities.toLong(changeDate));

			String description = history.getFieldName();
			RecipientMutableFields mutableField = RecipientMutableFields.getByCode(history.getFieldName());
			if (mutableField != null) {
				description = I18nString.getLocaleString(mutableField.getTranslationKey(), locale);
			}
			entry.put("fieldDescription", description);

			String oldValueDescription = getProfileFieldHistoryValueDescription(history, history.getOldValue(), locale);
			entry.put("oldValue", oldValueDescription);
			String newValueDescription = getProfileFieldHistoryValueDescription(history, history.getNewValue(), locale);
			entry.put("newValue", newValueDescription);

			List<JSONObject> list = groupedMap.computeIfAbsent(changeDate, dateKey -> new ArrayList<>());
			list.add(entry);
		}
        groupedMap.remove(null);
		JSONArray data = new JSONArray();

		//sort map by date DESC and compute group index for each entry by change date
		AtomicInteger groupIndex = new AtomicInteger(0);
		groupedMap.entrySet().stream()
				.sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
				.forEach(pair -> {
					int index = groupIndex.getAndAdd(1);
					pair.getValue().forEach(entry -> {
						entry.put("groupIndex", index);
						data.put(entry);
					});
				});

		return data;
    }

	private String getBindingHistoryFieldDescription(RecipientHistory history, Locale locale) {
		List<String> descriptions = new ArrayList<>();
		descriptions.add(String.format("%s %s", I18nString.getLocaleString("Mailinglist", locale), history.getMailingList()));
		if (history.getMediaType() != null) {
			descriptions.add(String.format("Medium: %s", I18nString.getLocaleString("mailing.MediaType." + history.getMediaType(), locale)));
		}

		String fieldTitle = I18nString.getLocaleString("Field", locale);
		String description = history.getFieldName();
		RecipientMutableFields mutableField = RecipientMutableFields.getByCode(history.getFieldName());

		if (mutableField != null) {
			switch (mutableField) {
				case USER_TYPE:
				case EXIT_MAILING_ID:
				case USER_REMARK:
				case USER_STATUS:
				case EMAIL:
					description = String.format("%s %s: %s",
							StringUtils.join(descriptions, " "),
							fieldTitle,
							I18nString.getLocaleString(mutableField.getTranslationKey(), locale));
					break;
				case MAILINGLIST_DELETED:
					description = String.format("%s %s",
							I18nString.getLocaleString(mutableField.getTranslationKey(), locale),
							history.getMailingList());
					break;
				case CUSTOMER_BINDING_DELETED:
					description = String.format("%s %s %s",
							I18nString.getLocaleString(RecipientMutableFields.MAILINGLIST_DELETED.getTranslationKey(), locale),
							I18nString.getLocaleString(mutableField.getTranslationKey(), locale),
							history.getMailingList());
					break;
				default:
					//nothing do
			}
		}

		return description;
	}

	private String getProfileFieldHistoryValueDescription(RecipientHistory history, Object value, Locale locale) {
		String description = value == null ? "" : value.toString();
		RecipientMutableFields mutableField = RecipientMutableFields.getByCode(history.getFieldName());
		if (mutableField != null) {
			switch (mutableField) {
				case GENDER:
					int genderValue = value != null ? ((Number) value).intValue() : 0;
					description = I18nString.getLocaleString("recipient.gender." + genderValue + ".short", locale);
					break;
				case MAIL_TYPE:
					int mailTypeValue = value != null ? ((Number) value).intValue() : 0;
					description = I18nString.getLocaleString("Mailtype" + mailTypeValue, locale);
					break;
				default:
					//nothing do
			}
		}

		return description;
	}

	private String getBindingHistoryValueDescription(RecipientHistory history, Object value, Locale locale, boolean isNewValue) {
		String description = value == null ? "" : value.toString();
		RecipientMutableFields mutableField = RecipientMutableFields.getByCode(history.getFieldName());

		if (mutableField != null) {
			switch (mutableField) {
				case USER_TYPE:
					description = getRecipientTypeTitleByLetter((String) value);
					break;
				case USER_STATUS:
					int statusValue = value != null ? ((Number) value).intValue() : 0;
					if (statusValue == 0) {
						description = I18nString.getLocaleString("recipient.NewRecipient", locale);
					} else {
						description = I18nString.getLocaleString("recipient.MailingState" + statusValue, locale);
					}
					break;
				case MAILINGLIST_DELETED:
				case CUSTOMER_BINDING_DELETED:
					if (isNewValue) {
						description =
								I18nString.getLocaleString(mutableField.getTranslationKey(), locale) + " " +
										I18nString.getLocaleString("target.Deleted", locale).toUpperCase();
					}
					break;
				default:
					//nothing do
			}
		}

		return description;
	}

	private UserStatus getNewUserStatus(UserStatus status, UserStatus newStatusForUnsubscribing, UserStatus oldUserStatus, boolean newEmailBlacklisted, boolean oldEmailBlacklisted) {
		if (status == null) {
			status = oldUserStatus == UserStatus.Active ? newStatusForUnsubscribing : oldUserStatus;
		}
		if (newEmailBlacklisted) {
			return UserStatus.Blacklisted;
		} else if (oldEmailBlacklisted && status == UserStatus.Blacklisted) {
			return UserStatus.AdminOut;
		} else {
			return status;
		}
	}

	private Object toDaoValue(Admin admin, ProfileField type, String value) throws UserMessageException {
		if (StringUtils.isEmpty(value)) {
			return null;
		}

        String fieldName = type.getShortname();
        switch (type.getSimpleDataType()) {
			case Numeric:
			case Float:
				if (AgnUtils.isDouble(value)) {
					if (type.getNumericPrecision() > 0 && value.length() > type.getNumericPrecision() && recipientDao.isOracleDB()) {
						throw new UserMessageException("error.value.numbertoolargeforfield", fieldName);
					}
					return value;
				} else {
					throw new UserMessageException("error.value.notANumberForField", fieldName);
				}

			case Characters:
				if (value.length() <= type.getMaxDataSize()) {
					return value;
				} else {
					throw new UserMessageException("error.value.toolargeforfield", fieldName);
				}

			case Date:
                return getRecipientDateField(value, admin.getDateFormat());
			case DateTime:
				return getRecipientDateField(value, admin.getDateTimeFormat());
			case Blob:
			default:
				throw new UnsupportedOperationException("Unsupported data type: " + type.getSimpleDataType());
		}
	}

	private boolean isEmptyColumnValue(ProfileField type, String value) {
		return isEmptyColumnValue(type.getSimpleDataType(), value);
	}

	private boolean isEmptyColumnValue(DbColumnType.SimpleDataType columnType, String value) {
		if (columnType == DbColumnType.SimpleDataType.Characters) {
			return StringUtils.isEmpty(value);
		} else {
			return StringUtils.isBlank(value);
		}
	}

    @Override
	public boolean isRecipientTrackingAllowed(int companyID, int recipientID) {
    	return this.recipientDao.isRecipientTrackingAllowed(companyID, recipientID);
	}

	@Override
    public Map<String, ProfileField> getEditableColumns(Admin admin) {
		Set<ProfileField> columns = getRecipientColumnInfos(admin);
		Map<String, ProfileField> map = new CaseInsensitiveMap<>();
		for (ProfileField column : columns) {
			if (column.getModeEdit() == ProfileFieldMode.Editable) {
				map.put(column.getShortname(), column);
			}
		}
		return map;
    }

	public final void setRecipientDao(final RecipientDao dao) {
		this.recipientDao = Objects.requireNonNull(dao, "Recipient DAO cannot be null");
	}

	public final void setUidService(final ExtensibleUIDService service) {
		this.uidService = Objects.requireNonNull(service, "UID service cannot be null");
	}

	public final void setCompanyDao(final CompanyDao dao) {
		this.companyDao = Objects.requireNonNull(dao, "Company DAO cannot be null");
	}

	public final void setRecipientFactory(final RecipientFactory factory) {
		this.recipientFactory = Objects.requireNonNull(factory, "Recipient factory cannot be null");
	}

	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}

	public final void setSendActionbasedMailingService(final SendActionbasedMailingService service) {
		this.sendActionbasedMailingService = Objects.requireNonNull(service, "Service to send action-based mailings is null");
	}

	public void setTargetService(TargetService targetService) {
		this.targetService = targetService;
	}

	public void setProfileFieldDao(ProfileFieldDao profileFieldDao) {
		this.profileFieldDao = profileFieldDao;
	}

	public void setProfileFieldValidationService(ProfileFieldValidationService profileFieldValidationService) {
		this.profileFieldValidationService = profileFieldValidationService;
	}

	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}

	public void setBindingEntryDao(BindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
	}

	public void setBindingEntryFactory(BindingEntryFactory bindingEntryFactory) {
		this.bindingEntryFactory = bindingEntryFactory;
	}

	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Override
	public BindingEntry getMailinglistBinding(int companyID, int customerID, int mailinglistID, int mediaCode) {
		return recipientDao.getMailinglistBinding(companyID, customerID, mailinglistID, mediaCode);
	}

	@Override
	public int getMinimumCustomerId(int companyID) {
		return recipientDao.getMinimumCustomerId(companyID);
	}

    @Override
    public String getEmail(int recipientId, int companyId) {
        return recipientDao.getEmail(companyId, recipientId);
    }
    
	protected void addExtendedSearchOptions(Admin admin, SqlPreparedStatementManager sqlStatement, RecipientSqlOptions options) {
		// Do nothing
	}

	protected void addExtendedSearchRestrictions(Admin admin, SqlPreparedStatementManager sqlStatement, RecipientSqlOptions options) {
		// Do nothing
	}

	@Override
	public boolean recipientExists(int companyID, int customerID) {
		return recipientDao.exist(customerID, companyID);
	}

	@Override
	public boolean existsWithEmail(String email, int companyId) {
		return !findIdsByEmail(email, companyId).isEmpty();
	}

	@Override
	public List<Integer> findIdsByEmail(String email, int companyId) {
		return recipientDao.findIdsByEmail(email, companyId);
	}

	@Override
	public List<CaseInsensitiveMap<String, Object>> getMailinglistRecipients(int companyID, int id, MediaTypes email, String targetSql, List<String> profileFieldsToShow, List<UserStatus> userstatusList, TimeZone timeZone) {
		return recipientDao.getMailinglistRecipients(companyID, id, email, targetSql, profileFieldsToShow, userstatusList, timeZone);
	}

	@Override
	public void updateEmail(String newEmail, int id, int companyId) {
		recipientDao.updateEmail(newEmail, id, companyId);
	}

	@Override
	public ServiceResult<List<RecipientLightDto>> getAllowedForDeletion(Set<Integer> ids, Admin admin) {
		return bulkActionValidationService.checkAllowedForDeletion(ids, id -> getRecipientForDeletion(id, admin.getCompanyID(), admin));
	}

	@Override
	public ServiceResult<UserAction> delete(Set<Integer> ids, Admin admin) {
		List<Integer> allowedIds = ids.stream()
				.map(id -> getRecipientForDeletion(id, admin.getCompanyID(), admin))
				.filter(ServiceResult::isSuccess)
				.map(r -> r.getResult().getCustomerId())
				.toList();

		allowedIds.forEach(id -> recipientDao.deleteCustomerDataFromDb(admin.getCompanyID(), id));

		return ServiceResult.success(
				new UserAction(
						"delete recipients",
						String.format("Recipients IDs %s", StringUtils.join(allowedIds, ", "))
				),
				Message.of(Const.Mvc.SELECTION_DELETED_MSG)
		);
	}

	@Override
	public SimpleServiceResult delete(int id, int companyId, Admin admin) {
		ServiceResult<RecipientLightDto> recipient = getRecipientForDeletion(id, companyId, admin);

		if (recipient.isSuccess()) {
			recipientDao.deleteCustomerDataFromDb(companyId, id);
		}

		return SimpleServiceResult.of(recipient);
	}

	private ServiceResult<RecipientLightDto> getRecipientForDeletion(int id, int companyId, Admin admin) {
		if (mailinglistApprovalService.hasAnyDisabledRecipientBindingsForAdmin(admin, id)) {
			return ServiceResult.errorKeys("error.access.limit.mailinglist");
		}

		RecipientLightDto recipient = getRecipientLightDto(companyId, id);
		if (recipient == null) {
			return ServiceResult.errorKeys("error.general.missing");
		}

		return ServiceResult.success(recipient);
	}

	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}

	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}

	public void setRecipientQueryBuilder(RecipientQueryBuilder recipientQueryBuilder) {
		this.recipientQueryBuilder = recipientQueryBuilder;
	}

	public void setRecipientWorkerFactory(RecipientWorkerFactory recipientWorkerFactory) {
		this.recipientWorkerFactory = recipientWorkerFactory;
	}

	public void setRecipientModelValidator(RecipientModelValidator recipientModelValidator) {
		this.recipientModelValidator = recipientModelValidator;
	}

	public void setTargetDao(TargetDao targetDao) {
		this.targetDao = targetDao;
	}

	public void setEqlFacade(EqlFacade eqlFacade) {
		this.eqlFacade = eqlFacade;
	}

	public final void setSubscriberLimitCheck(final SubscriberLimitCheck check) {
		this.subscriberLimitCheck = Objects.requireNonNull(check, "subscriberLimitCheck");
	}

	public void setMailinglistApprovalService(MailinglistApprovalService mailinglistApprovalService) {
		this.mailinglistApprovalService = mailinglistApprovalService;
	}

	public void setBulkActionValidationService(BulkActionValidationService<Integer, RecipientLightDto> bulkActionValidationService) {
		this.bulkActionValidationService = bulkActionValidationService;
	}
}
