/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_CUSTOMER_ID;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_EMAIL;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FIRSTNAME;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FREQUENCY_COUNTER_WEEK;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FREQUENCY_COUNT_DAY;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FREQUENCY_COUNT_MONTH;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_GENDER;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_LASTNAME;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_LATEST_DATASOURCE_ID;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_MAILTYPE;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_TITLE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.RecipientImpl;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.RecipientUtils;
import org.agnitas.emm.core.recipient.dto.RecipientFrequencyCounterDto;
import org.agnitas.emm.core.recipient.dto.RecipientLightDto;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.UserMessageException;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.RecipientAction;
import org.agnitas.web.RecipientForm;
import org.agnitas.web.forms.FormSearchParams;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.binding.service.BindingUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ComColumnInfoService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;

/**
 * Handles all actions on profile fields.
 */
public class ComRecipientAction extends RecipientAction {

    public static final String COLUMN_DATASOURCE_ID = "datasource_id";

    public static final int ACTION_BULK_CONFIRM_DELETE = ORG_ACTION_LAST + 2;

    public static final int ACTION_BULK_DELETE = ORG_ACTION_LAST + 3;

    public static final int ACTION_HISTORY_VIEW = ORG_ACTION_LAST + 4;

    public static final int ACTION_CHECK_ADDRESS = ORG_ACTION_LAST + 10;
    
    public static final int ACTION_SAVE_BACK_TO_LIST = ORG_ACTION_LAST + 16;

    public static final int ACTION_CHECK_LIMITACCESS = ORG_ACTION_LAST + 17;

    /** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComRecipientAction.class);
	/** DAO for accessing company data. */
	protected ComCompanyDao companyDao;
	protected AdminService adminService;
	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

    @Override
    public void setColumnInfoService(ColumnInfoService columnInfoService) {
    	if (!(columnInfoService instanceof ComColumnInfoService)) {
    		throw new RuntimeException("Invalid ColumnInfoService type for ComRecipientAction (expects ComColumnInfoService)");
    	}
        this.columnInfoService = columnInfoService;
    }
    
    /**
     * Set DAO for access company data.
     * 
     * @param companyDao DAO for accessing company data
     */
    public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	@Required
    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    // ----------------------------------------------------------------------------------------------------------------
	// Business Injection

    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
        case ACTION_OVERVIEW_START:
            return "overview_start";
        case ACTION_SEARCH:
            return "search";
        case ACTION_HISTORY_VIEW:
            return "history_view";
        case ACTION_BULK_CONFIRM_DELETE:
            return "bulk_confirm_delete";
        case ACTION_BULK_DELETE:
            return "bulk_delete";
        case ACTION_VIEW_WITHOUT_LOAD:
            return "view_without_load";
        case ACTION_CHECK_ADDRESS:
            return "check_address";
        case ACTION_SAVE_BACK_TO_LIST:
             return "save_back_to_list";
        case ACTION_CHECK_LIMITACCESS:
             return "check_limit_access";
        default:
            return super.subActionMethodName(subAction);
        }
    }

	/**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param form
     * @param req
     * @param res
     * @param mapping The ActionMapping used to select this instance
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     * @return destination
     */
    @Override
	public ActionForward execute(ActionMapping mapping,
	    ActionForm form,
	    HttpServletRequest req,
	    HttpServletResponse res)
	    throws IOException, ServletException {

        // Validate the request parameters specified by the user
        ComRecipientForm aForm = null;
        ActionMessages errors = new ActionErrors();
        ActionMessages messages = new ActionMessages();
        ActionForward destination = null;

        if (form != null) {
            aForm = (ComRecipientForm)form;
        } else {
            aForm = new ComRecipientForm();
        }
        ComAdmin admin = AgnUtils.getAdmin(req);
        assert Objects.nonNull(admin);

        if (aForm.getAction() == ACTION_CHECK_LIMITACCESS) {
            HttpUtils.responseJson(res, "{\"accessAllowed\" : " + isRecipientMatchAltgTarget(admin, aForm.getRecipientID()) + "}");

            return null;
        }

        req.setAttribute("mailtracking", AgnUtils.isMailTrackingAvailable(admin));

        try {
            if (!isRecipientMatchAltgTarget(admin, aForm.getRecipientID())) {
                logger.error(String.format("Recipient(ID: %d) doesn't match limit access target group - Admin (ID: %d)", aForm.getRecipientID(), admin.getAdminID()));
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.access.limit.targetgroup"));

                aForm.setAction(ACTION_LIST);
                aForm.setRecipientID(0);
            }

            switch(aForm.getAction()) {
            	case ACTION_OVERVIEW_START:
            		// all we do here is to set the overview parameter
            		aForm.setOverview(true);
            		aForm.setAction(ACTION_LIST);
                    aForm.setAdminId(admin.getAdminID());
                    if(!BooleanUtils.toBoolean(req.getParameter(FormSearchParams.RESTORE_PARAM_NAME))) {
                        aForm.resetSearch();
                    }
                    destination = super.execute(mapping, form, req, res);
            		break;
                case ACTION_SEARCH:
                	aForm.setOverview(false);
                	
                	AgnUtils.setAdminDateTimeFormatPatterns(req);
                	
                    destination = mapping.findForward("search");
                    break;

                case ACTION_VIEW:
					destination = super.execute(mapping, form, req, res);
					break;

                case ACTION_HISTORY_VIEW:
                    if (aForm.getRecipientID() > 0) {
                        List<ComRecipientHistory> recipientChangesHistory = new LinkedList<>();
                        int companyId = AgnUtils.getCompanyID(req);

                        List<ComRecipientHistory> bindingHistories = recipientDao.getRecipientBindingHistory(aForm.getRecipientID(), companyId);
                        convertMailinglistData(bindingHistories, req);
                        recipientChangesHistory.addAll(bindingHistories);

                        List<ComRecipientHistory> recipientProfileHistories = recipientDao.getRecipientProfileHistory(aForm.getRecipientID(), companyId);
                        convertRecipientProfileData(recipientProfileHistories, req);
                        recipientChangesHistory.addAll(recipientProfileHistories);

                        recipientChangesHistory.sort(Collections.reverseOrder());

                        FormUtils.syncNumberOfRows(webStorage, WebStorage.RECIPIENT_STATUS_HISTORY_OVERVIEW, aForm);
                        req.setAttribute("recipientHistory", recipientChangesHistory);

                        RecipientLightDto recipientDto = recipientService.getRecipientLightDto(companyId, aForm.getRecipientID());
                        if (StringUtils.isNotEmpty(recipientDto.getEmail())) {
                            writeUserActivityLog(admin, "view recipient status history", "For: " + recipientDto.getEmail() + ". " + RecipientUtils.getRecipientDescription(recipientDto));
                        }
                    }
                    destination = mapping.findForward("history");
                    break;

                case ACTION_BULK_CONFIRM_DELETE:
                    if (aForm.getBulkIds().size() == 0) {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("bulkAction.nothing.recipient"));
                        destination = super.execute(mapping, form, req, res);
                    } else {
                        aForm.setAction(ACTION_BULK_DELETE);
                        destination = mapping.findForward("bulk_delete_confirm");
                    }
                    break;

                case ACTION_BULK_DELETE:
                    if (deleteRecipientsBulk(aForm, req)) {
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                    }
                    destination = super.execute(mapping, form, req, res);
                    break;

                case ACTION_CHECK_ADDRESS:
                    HttpUtils.responseJson(res, checkAddress(admin, aForm.getEmail(), aForm.getRecipientID()));
                    return null;

                default:
                    aForm.setAdminId(admin.getAdminID());
                    destination = super.execute(mapping, form, req, res);
            }

		} catch (Exception e) {
			logger.error("ComRecipientAction execute: " + e.getMessage(), e);
			// errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(Value.SupportEmergencyUrl)));
		}

        // Report any messages we have discovered back to the original form
        if (!messages.isEmpty()) {
            if (getMessages(req).isEmpty()) {
                saveMessages(req, messages);
            }
            else {
                getMessages(req).add(messages);
            }
        }

        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            if (getErrors(req).isEmpty()) {
                saveErrors(req, errors);
            }
            else {
                getErrors(req).add(errors);
            }
        }

        return destination;
    }

    private JSONObject checkAddress(ComAdmin admin, String email, int recipientId) {
        JSONObject data = new JSONObject();
        data.element("address", email);
        int existingRecipientId = recipientDao.getRecipientIdByAddress(StringUtils.trimToEmpty(email), recipientId, admin.getCompanyID());
        data.element("inUse", existingRecipientId > 0);
        data.element("existingRecipientId", existingRecipientId);
        data.element("isBlacklisted", blacklistService.blacklistCheck(StringUtils.trimToEmpty(email), admin.getCompanyID()));
        return data;
    }

    /**
     * Translate DB signature in recipient profile history records to human-readable wording
     * @param profileHistories
     * @param req
     */
    private void convertRecipientProfileData(List<ComRecipientHistory> profileHistories, HttpServletRequest req) {
        for (ComRecipientHistory profileHistory : profileHistories) {
            String fieldName = profileHistory.getFieldName();
            switch (fieldName) {
                case ComRecipientHistory.EMAIL:
                    profileHistory.setFieldName(I18nString.getLocaleString("mailing.MediaType.0", AgnUtils.getLocale(req)));
                    break;
                case ComRecipientHistory.FIRSTNAME:
                    profileHistory.setFieldName(I18nString.getLocaleString("recipient.Firstname", AgnUtils.getLocale(req)));
                    break;
                case ComRecipientHistory.LASTNAME:
                    profileHistory.setFieldName(I18nString.getLocaleString("recipient.Lastname", AgnUtils.getLocale(req)));
                    break;
                case ComRecipientHistory.GENDER:
                    profileHistory.setFieldName(I18nString.getLocaleString("Gender", AgnUtils.getLocale(req)));
                    Number oldGender = (Number) profileHistory.getOldValue();
                    profileHistory.setOldValue(I18nString.getLocaleString("recipient.gender." + oldGender + ".short", AgnUtils.getLocale(req)));
                    Number newGender = (Number) profileHistory.getNewValue();
                    profileHistory.setNewValue(I18nString.getLocaleString("recipient.gender." + newGender + ".short", AgnUtils.getLocale(req)));
                    break;
                case ComRecipientHistory.MAILTYPE:
                    profileHistory.setFieldName(I18nString.getLocaleString("Mailtype", AgnUtils.getLocale(req)));
                    Number oldMailtype = (Number) profileHistory.getOldValue();
                    profileHistory.setOldValue(I18nString.getLocaleString("MailType." + oldMailtype, AgnUtils.getLocale(req)));
                    Number newMailtype = (Number) profileHistory.getNewValue();
                    profileHistory.setNewValue(I18nString.getLocaleString("MailType." + newMailtype, AgnUtils.getLocale(req)));
                    break;
                case ComRecipientHistory.TITLE:
                    profileHistory.setFieldName(I18nString.getLocaleString("Title", AgnUtils.getLocale(req)));
                    break;
                case ComRecipientHistory.DATASOURCE_ID:
                    profileHistory.setFieldName(I18nString.getLocaleString("recipient.DatasourceId", AgnUtils.getLocale(req)));
                    break;
				default:
					break;
            }
        }
    }

    /**
     * Translate DB signature in social status change records to human-readable wording
     * @param locale
     * @param profileHistory
     */
//    private void convertSocialStatus(Locale locale, ComRecipientHistory profileHistory) {
//        int oldStatus = ((Number)profileHistory.getOldValue()).intValue();
//        if(oldStatus == 0) {
//            profileHistory.setOldValue(I18nString.getLocaleString("workflow.view.status.inActive", locale));
//        }
//        else if(oldStatus == 1){
//            profileHistory.setOldValue(I18nString.getLocaleString("default.status.active", locale));
//        }
//        int newStatus = ((Number)profileHistory.getNewValue()).intValue();
//        if(newStatus == 0) {
//            profileHistory.setNewValue(I18nString.getLocaleString("workflow.view.status.inActive", locale));
//        }
//        else if(newStatus == 1){
//            profileHistory.setNewValue(I18nString.getLocaleString("default.status.active", locale));
//        }
//    }

    /**
     * Translate DB signature in recipient binding history records to human-readable wording
     * @param recipientChangesHistory
     * @param req
     */
    private void convertMailinglistData(List<ComRecipientHistory> recipientChangesHistory, HttpServletRequest req) {
        Locale locale = AgnUtils.getLocale(req);
        String mailinglistPrefix = I18nString.getLocaleString("Mailinglist", locale);
        String mediatypeDesc = "";
        String fieldPrefix = I18nString.getLocaleString("Field", locale);
        for (ComRecipientHistory comRecipientHistory : recipientChangesHistory) {
            String fieldName = comRecipientHistory.getFieldName();

            Number mediaType = comRecipientHistory.getMediaType();
            if (comRecipientHistory.getMediaType() != null) {
                mediatypeDesc = "Medium: " + I18nString.getLocaleString("mailing.MediaType." + mediaType, locale);
            }

            String mailinglist = String.format("%s %s %s %s: ", mailinglistPrefix, comRecipientHistory.getMailingList(), mediatypeDesc, fieldPrefix);

            switch(fieldName){
                case ComRecipientHistory.USER_TYPE:
                    comRecipientHistory.setFieldName(mailinglist + I18nString.getLocaleString("recipient.history.usertype", locale));
                    String oldType = ((String) comRecipientHistory.getOldValue());
                    comRecipientHistory.setOldValue(getRecipientTypeByLetter(oldType));
                    String newType = ((String) comRecipientHistory.getNewValue());
                    comRecipientHistory.setNewValue(getRecipientTypeByLetter(newType));
                    break;
                case ComRecipientHistory.EXIT_MAILING_ID:
                    comRecipientHistory.setFieldName(mailinglist + I18nString.getLocaleString("recipient.history.mailingid", locale));
                    break;
                case ComRecipientHistory.USER_REMARK:
                    comRecipientHistory.setFieldName(mailinglist + I18nString.getLocaleString("recipient.Remark", locale));
                    break;
                case ComRecipientHistory.USER_STATUS:
                    comRecipientHistory.setFieldName(mailinglist + I18nString.getLocaleString("recipient.Status", locale));
                    Number oldStatus = ((Number) comRecipientHistory.getOldValue());
                    if (oldStatus.intValue() == 0) {
                    	comRecipientHistory.setOldValue(I18nString.getLocaleString("recipient.NewRecipient", locale));
                    } else {
                    	comRecipientHistory.setOldValue(I18nString.getLocaleString("recipient.MailingState" + oldStatus, locale));
                    }
                    Number newStatus = ((Number) comRecipientHistory.getNewValue());
                    comRecipientHistory.setNewValue(I18nString.getLocaleString("recipient.MailingState" + newStatus, locale));
                    break;
                case ComRecipientHistory.EMAIL:
                    comRecipientHistory.setFieldName(mailinglist + I18nString.getLocaleString("mailing.MediaType.0", locale));
                    break;
                case ComRecipientHistory.MAILINGLIST_DELETED:
                    comRecipientHistory.setFieldName(mailinglistPrefix + " " + comRecipientHistory.getMailingList());
                    comRecipientHistory.setNewValue(I18nString.getLocaleString("Mailinglist", locale) + " " + I18nString.getLocaleString("target.Deleted", locale).toUpperCase());
                    break;
                case ComRecipientHistory.CUSTOMER_BINDING_DELETED:
                    comRecipientHistory.setFieldName(mailinglistPrefix + " " + comRecipientHistory.getMailingList());
                    comRecipientHistory.setNewValue(I18nString.getLocaleString("Binding", locale) + " " + I18nString.getLocaleString("target.Deleted", locale).toUpperCase());
                    break;
				default:
					break;
            }
        }
    }

	@Override
    protected void loadDefaults(RecipientForm aForm, HttpServletRequest req) {
        aForm.setAdminId(AgnUtils.getAdminId(req));
        super.loadDefaults(aForm, req);
    }

    @Override
    protected void loadRecipient(RecipientForm aForm, ComAdmin admin) throws Exception {
    	int customerIdToLoad = aForm.getRecipientID();
        ComRecipientForm comRecipientForm = (ComRecipientForm) aForm;
        comRecipientForm.clearRecipientData();
		Map<String, Object> data = recipientDao.getCustomerDataFromDb(admin.getCompanyID(), customerIdToLoad, true);
		
		if (!data.containsKey(COLUMN_CUSTOMER_ID)) {
		    aForm.setRecipientID(customerIdToLoad);
            logger.warn("Could not find customer_id column in customer data. RecipientID set customerIdToLoad: " + customerIdToLoad);
        }
        RecipientFrequencyCounterDto frequencyCounterDto = new RecipientFrequencyCounterDto();
		for (Entry<String, Object> dataEntry : data.entrySet()) {
			String key = dataEntry.getKey();
            switch (key) {
                case COLUMN_CUSTOMER_ID:
                    aForm.setRecipientID(Integer.parseInt((String) dataEntry.getValue()));
                    aForm.setColumn(key, dataEntry.getValue());
                    break;
                case COLUMN_GENDER:
                    try {
                        aForm.setGender(Integer.parseInt((String) dataEntry.getValue()));
                    } catch (Exception e) {
                        aForm.setGender(2);
                    }
                    break;
                case COLUMN_TITLE:
                    aForm.setTitle((String) dataEntry.getValue());
                    break;
                case COLUMN_FIRSTNAME:
                    aForm.setFirstname((String) dataEntry.getValue());
                    break;
                case COLUMN_LASTNAME:
                    aForm.setLastname((String) dataEntry.getValue());
                    break;
                case COLUMN_FREQUENCY_COUNT_DAY:
                    frequencyCounterDto.setDayFrequencyCounter(NumberUtils.toInt((String) dataEntry.getValue()));
                    break;
                case COLUMN_FREQUENCY_COUNTER_WEEK:
                    frequencyCounterDto.setWeekFrequencyCounter(NumberUtils.toInt((String) dataEntry.getValue()));
                    break;
                case COLUMN_FREQUENCY_COUNT_MONTH:
                    frequencyCounterDto.setMonthFrequencyCounter(NumberUtils.toInt((String) dataEntry.getValue()));
                    break;
                case COLUMN_EMAIL:
                    aForm.setEmail((String) dataEntry.getValue());
                    break;
                case COLUMN_MAILTYPE:
                    try {
                        aForm.setMailtype(Integer.parseInt((String) data.get(COLUMN_MAILTYPE)));
                    } catch (Exception e) {
                        aForm.setMailtype(1);
                    }
                    break;
                default:
                	aForm.setColumn(key, dataEntry.getValue());
                    break;
            }
        }
        aForm.setFrequencyCounterDto(frequencyCounterDto);
        Map<String, Object> dataMap = aForm.getColumnMap();
        List<ProfileField> profileFields = columnInfoService.getColumnInfos(admin.getCompanyID(), admin.getAdminID());
        for (ProfileField profileField : profileFields) {
            String columnName = profileField.getColumn();
            String value = dataMap.get(columnName) != null ? dataMap.get(columnName).toString() : "";

            if (RecipientUtils.hasSupplementalSuffix(columnName)) {
                dataMap.remove(columnName);
                break;
            }

            String formattedValue = value;
            if (profileField.getSimpleDataType() == SimpleDataType.Date) {
                removeColumnWithSupplementalPrefix(columnName, dataMap);
                formattedValue = RecipientUtils.formatRecipientDateValue(admin, value);
        	} else if (profileField.getSimpleDataType() == SimpleDataType.DateTime) {
                removeColumnWithSupplementalPrefix(columnName, dataMap);
                formattedValue = RecipientUtils.formatRecipientDateTimeValue(admin, value);
        	} else if (profileField.getSimpleDataType() == SimpleDataType.Float) {
                formattedValue = RecipientUtils.formatRecipientDoubleValue(admin, value);
        	}
            dataMap.put(columnName, formattedValue);
        }
		
		comRecipientForm.setTrackingVeto(RecipientImpl.isDoNotTrackMe(data));

        writeUserActivityLog(admin, "view recipient", getRecipientDescription(aForm));
    }

    private void removeColumnWithSupplementalPrefix(String columnName, Map<String, Object> dataMap) {
        dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY);
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH);
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR);
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR);
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE);
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND);
    }

    @Override
    protected ActionForward saveRecipient(RecipientForm aForm, HttpServletRequest req, ActionMessages errors, ActionMapping mapping) throws Exception {
        final int companyId = AgnUtils.getCompanyID(req);
        final ComAdmin admin = AgnUtils.getAdmin(req);
        final boolean isNewRecipient = aForm.getRecipientID() == 0;
        final String newEmail = aForm.getEmail().trim();
        final boolean newEmailBlacklisted = blacklistService.blacklistCheck(newEmail, companyId),
                oldEmailBlacklisted;
        final ActionForward destination;
        Map<String, ProfileField> profileFieldsMap = columnInfoService.getColumnInfoMap(companyId, admin.getAdminID());

        Recipient cust = recipientFactory.newRecipient();
        cust.setCompanyID(companyId);

        String defaultDatasourceID = Integer.toString(companyDao.getCompanyDatasource(companyId));
		if (!isNewRecipient) {
			cust.setCustomerID(aForm.getRecipientID());

			Map<String, Object> data = recipientDao.getCustomerDataFromDb(companyId, cust.getCustomerID());

			oldEmailBlacklisted = blacklistService.blacklistCheck((String) data.get(COLUMN_EMAIL), companyId);

            Map<String, Object> column = aForm.getColumnMap();
            for (Entry<String, Object> entry : column.entrySet()) {
                try {
					data.put(entry.getKey(), entry.getValue());
					if (profileFieldsMap.containsKey(entry.getKey()) && profileFieldsMap.get(entry.getKey()).getSimpleDataType() == SimpleDataType.Date && profileFieldsMap.get(entry.getKey()).getModeEdit() == ProfileField.MODE_EDIT_EDITABLE) {
						if (StringUtils.isNotBlank((String) entry.getValue())) {
					    	GregorianCalendar newDateValue = new GregorianCalendar();
					    	newDateValue.setTime(admin.getDateFormat().parse((String) entry.getValue()));
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, newDateValue.get(Calendar.DAY_OF_MONTH));
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, newDateValue.get(Calendar.MONTH) + 1);
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, newDateValue.get(Calendar.YEAR));
						} else {
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
						}
					} else if (profileFieldsMap.containsKey(entry.getKey()) && profileFieldsMap.get(entry.getKey()).getSimpleDataType() == SimpleDataType.DateTime && profileFieldsMap.get(entry.getKey()).getModeEdit() == ProfileField.MODE_EDIT_EDITABLE) {
						if (StringUtils.isNotBlank((String) entry.getValue())) {
					    	GregorianCalendar newDateValue = new GregorianCalendar();
					    	newDateValue.setTime(admin.getDateTimeFormat().parse((String) entry.getValue()));
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, newDateValue.get(Calendar.DAY_OF_MONTH));
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, newDateValue.get(Calendar.MONTH) + 1);
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, newDateValue.get(Calendar.YEAR));
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, newDateValue.get(Calendar.HOUR_OF_DAY));
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, newDateValue.get(Calendar.MINUTE));
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, newDateValue.get(Calendar.SECOND));
						} else {
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, "");
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, "");
							data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, "");
						}
					} else if (profileFieldsMap.containsKey(entry.getKey()) && (profileFieldsMap.get(entry.getKey()).getSimpleDataType() == SimpleDataType.Float || profileFieldsMap.get(entry.getKey()).getSimpleDataType() == SimpleDataType.Numeric) && profileFieldsMap.get(entry.getKey()).getModeEdit() == ProfileField.MODE_EDIT_EDITABLE) {
						if (StringUtils.isNotBlank((String) entry.getValue())) {
							double doubleValue;
							try {
								doubleValue = Double.parseDouble((String) entry.getValue());
							} catch (NumberFormatException e) {
								throw new UserMessageException("error.value.notANumberForField", entry.getKey());
							}
							
							if (recipientDao.isOracleDB()) {
								if ("integer".equalsIgnoreCase(profileFieldsMap.get(entry.getKey()).getDataType())) {
									if (doubleValue > 2147483647) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									} else if (doubleValue < -2147483648) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									}
								} else if (profileFieldsMap.get(entry.getKey()).getNumericPrecision() < Double.toString(doubleValue).length()) {
									throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
								}
							} else {
								if ("smallint".equalsIgnoreCase(profileFieldsMap.get(entry.getKey()).getDataType())) {
									if (doubleValue > 32767) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									} else if (doubleValue < -32768) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									}
								} else if ("int".equalsIgnoreCase(profileFieldsMap.get(entry.getKey()).getDataType()) || "integer".equalsIgnoreCase(profileFieldsMap.get(entry.getKey()).getDataType())) {
									if (doubleValue > 2147483647) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									} else if (doubleValue < -2147483648) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									}
								} else if ("bigint".equalsIgnoreCase(profileFieldsMap.get(entry.getKey()).getDataType())) {
									if (doubleValue > 9223372036854775807l) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									} else if (doubleValue < -9223372036854775808l) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									}
								}
							}
						}
						
						data.put(entry.getKey(), AgnUtils.normalizeNumber(admin.getLocale(), (String) entry.getValue()));
					}
				} catch (UserMessageException e) {
					logger.error("error during save of recipient: " + e);
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorMessageKey(), e.getAdditionalErrorData()));
				} catch (Exception e) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.invalidDataForField", entry.getKey()));
					logger.error("error during save of recipient: " + e);
				}
            }

			data.put(COLUMN_GENDER, Integer.toString(aForm.getGender()));
			data.put(COLUMN_TITLE, aForm.getTitle().trim());
			data.put(COLUMN_FIRSTNAME, aForm.getFirstname().trim());
			data.put(COLUMN_LASTNAME, aForm.getLastname().trim());
			data.put(COLUMN_EMAIL, newEmail);
			data.put(COLUMN_MAILTYPE, Integer.toString(aForm.getMailtype()));
			data.put(ComCompanyDaoImpl.STANDARD_FIELD_DO_NOT_TRACK, ((ComRecipientForm) aForm).isTrackingVeto() ? "1" : "0");
			data.put(COLUMN_LATEST_DATASOURCE_ID, defaultDatasourceID);

//			storeSpecificFields(aForm, data, admin);
			cust.setCustParameters(data);

			destination = getConfirmationPage(req, cust, mapping);
			if (destination != null) {
			    return destination;
            }

			recipientDao.updateInDbWithException(cust);
            writeRecipientChangesLog(data, aForm, admin);
		} else {
		    oldEmailBlacklisted = false;
			if (!recipientDao.mayAdd(companyId, 1)) {
				int allowedRecipientNumber = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfCustomers);
				if (allowedRecipientNumber < 0) {
					allowedRecipientNumber = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfCustomers, companyId);
				}
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.maxcustomersexceeded", recipientDao.getNumberOfRecipients(companyId), allowedRecipientNumber));
				return mapping.findForward("messages");
			}

			Map<String, Object> data = recipientDao.getCustomerDataFromDb(companyId, aForm.getRecipientID());
			Map<String, Object> column = aForm.getColumnMap();
			for (Entry<String, Object> entry : column.entrySet()) {
				try {
					data.put(entry.getKey(), entry.getValue());
	                if (profileFieldsMap.containsKey(entry.getKey()) && profileFieldsMap.get(entry.getKey()).getSimpleDataType() == SimpleDataType.Date && profileFieldsMap.get(entry.getKey()).getModeEdit() == ProfileField.MODE_EDIT_EDITABLE) {
	                	if (StringUtils.isNotBlank((String) entry.getValue())) {
		                	GregorianCalendar newDateValue = new GregorianCalendar();
		                	newDateValue.setTime(admin.getDateFormat().parse((String) entry.getValue()));
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, newDateValue.get(Calendar.DAY_OF_MONTH));
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, newDateValue.get(Calendar.MONTH) + 1);
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, newDateValue.get(Calendar.YEAR));
	                	} else if (!data.containsKey(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY) && !data.containsKey(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH) && !data.containsKey(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR)) {
	                		// Only put empty default data in map, if there was no default value in customer_field_tbl
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
	                	}
	                } else if (profileFieldsMap.containsKey(entry.getKey()) && profileFieldsMap.get(entry.getKey()).getSimpleDataType() == SimpleDataType.DateTime && profileFieldsMap.get(entry.getKey()).getModeEdit() == ProfileField.MODE_EDIT_EDITABLE) {
	                	if (StringUtils.isNotBlank((String) entry.getValue())) {
		                	GregorianCalendar newDateValue = new GregorianCalendar();
		                	newDateValue.setTime(admin.getDateTimeFormat().parse((String) entry.getValue()));
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, newDateValue.get(Calendar.DAY_OF_MONTH));
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, newDateValue.get(Calendar.MONTH) + 1);
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, newDateValue.get(Calendar.YEAR));
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, newDateValue.get(Calendar.HOUR_OF_DAY));
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, newDateValue.get(Calendar.MINUTE));
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, newDateValue.get(Calendar.SECOND));
	                	} else if (!data.containsKey(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY) && !data.containsKey(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH) && !data.containsKey(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR)
	                			 && !data.containsKey(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR) && !data.containsKey(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE) && !data.containsKey(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND)) {
	                		// Only put empty default data in map, if there was no default value in customer_field_tbl
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, "");
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, "");
		            		data.put(entry.getKey() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, "");
	                	}
	                } else if (profileFieldsMap.containsKey(entry.getKey()) && (profileFieldsMap.get(entry.getKey()).getSimpleDataType() == SimpleDataType.Float || profileFieldsMap.get(entry.getKey()).getSimpleDataType() == SimpleDataType.Numeric) && profileFieldsMap.get(entry.getKey()).getModeEdit() == ProfileField.MODE_EDIT_EDITABLE) {
	                	if (StringUtils.isNotBlank((String) entry.getValue())) {
		                	double doubleValue;
							try {
								doubleValue = Double.parseDouble((String) entry.getValue());
							} catch (NumberFormatException e) {
								throw new UserMessageException("error.value.notANumberForField", entry.getKey());
							}
							
							if (recipientDao.isOracleDB()) {
								if ("integer".equalsIgnoreCase(profileFieldsMap.get(entry.getKey()).getDataType())) {
									if (doubleValue > 2147483647) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									} else if (doubleValue < -2147483648) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									}
								} else if (profileFieldsMap.get(entry.getKey()).getNumericPrecision() < Double.toString(doubleValue).length()) {
									throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
								}
							} else {
								if ("smallint".equalsIgnoreCase(profileFieldsMap.get(entry.getKey()).getDataType())) {
									if (doubleValue > 32767) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									} else if (doubleValue < -32768) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									}
								} else if ("int".equalsIgnoreCase(profileFieldsMap.get(entry.getKey()).getDataType()) || "integer".equalsIgnoreCase(profileFieldsMap.get(entry.getKey()).getDataType())) {
									if (doubleValue > 2147483647) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									} else if (doubleValue < -2147483648) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									}
								} else if ("bigint".equalsIgnoreCase(profileFieldsMap.get(entry.getKey()).getDataType())) {
									if (doubleValue > 9223372036854775807l) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									} else if (doubleValue < -9223372036854775808l) {
										throw new UserMessageException("error.value.numbertoolargeforfield", entry.getKey());
									}
								}
							}
	                	}
						
	                	data.put(entry.getKey(), AgnUtils.normalizeNumber(admin.getLocale(), (String) entry.getValue()));
	                }
				} catch (UserMessageException e) {
					logger.error("error during save of recipient: " + e);
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getErrorMessageKey(), e.getAdditionalErrorData()));
				} catch (Exception e) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.invalidDataForField", entry.getKey()));
					logger.error("error during save of recipient: " + e);
				}
			}
			data.put(COLUMN_GENDER, Integer.toString(aForm.getGender()));
			data.put(COLUMN_TITLE, aForm.getTitle().trim());
			data.put(COLUMN_FIRSTNAME, aForm.getFirstname().trim());
			data.put(COLUMN_LASTNAME, aForm.getLastname().trim());
			data.put(COLUMN_EMAIL, newEmail);
			data.put(COLUMN_MAILTYPE, Integer.toString(aForm.getMailtype()));
			data.put(ComCompanyDaoImpl.STANDARD_FIELD_DO_NOT_TRACK, ((ComRecipientForm) aForm).isTrackingVeto() ? "1" : "0");
            if (StringUtils.isEmpty((String) data.get(COLUMN_DATASOURCE_ID))){
                data.put(COLUMN_DATASOURCE_ID, defaultDatasourceID);
				data.put(COLUMN_LATEST_DATASOURCE_ID, defaultDatasourceID);
            }
			storeSpecificFields(aForm, data, admin);
			cust.setCustParameters(data);

			destination = getConfirmationPage(req, cust, mapping);
			if (destination != null) {
			    return destination;
            }

			cust.setCustomerID(recipientDao.insertNewCustWithException(cust));
			
			if (cust.getCustomerID() <= 0) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.recipient.create"));
			}
			
			aForm.setRecipientID(cust.getCustomerID());

            writeUserActivityLog(admin, "create recipient", getRecipientDescription(cust));
		}
		aForm.setRecipientID(cust.getCustomerID());

		saveBindings(aForm, req, isNewRecipient, newEmailBlacklisted, oldEmailBlacklisted);
		updateCustBindingsFromAdminReq(cust, req);
		return null;
	}

    protected boolean isRecipientMatchAltgTarget(ComAdmin admin, int recipientId) {
        final int altgId = adminService.getAccessLimitTargetId(admin);
		if (recipientId <= 0 || altgId <= 0) {
			return true;
		}

        if (logger.isDebugEnabled()) {
            logger.debug("Recipient with id: " + recipientId + " has to be checked for ALTG: " + altgId);
        }

        final boolean isMatch = targetService.isRecipientMatchTarget(admin, altgId, recipientId);

        if (!isMatch) {
            logger.warn("Recipient with id: " + recipientId + " is not allowed for ALTG: " + altgId);
        }

        return isMatch;
    }

    protected ActionForward getConfirmationPage(HttpServletRequest request, Recipient recipient, ActionMapping mapping) {
        ComAdmin admin = AgnUtils.getAdmin(request);
        int altgId = adminService.getAccessLimitTargetId(admin);
        if(altgId > 0 && !"true".equals(request.getParameter("ignoreConfirm"))) {
			boolean isMatch = targetService.isRecipientMatchTarget(admin, altgId, recipient);
			if (!isMatch) {
                return mapping.findForward("confirm_save");
			}
		}

        return null;
    }

    private boolean deleteRecipientsBulk(ComRecipientForm form, HttpServletRequest req) {
        final Set<Integer> ids = form.getBulkIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            final ComAdmin admin = AgnUtils.getAdmin(req);
            final int companyId = AgnUtils.getCompanyID(req);
            
            List<String> descriptions = new ArrayList<>();
            for (int customerId : ids) {
                RecipientLightDto dto = recipientService.getRecipientLightDto(companyId, customerId);
                descriptions.add(RecipientUtils.getRecipientDescription(dto));
            }

            recipientDao.deleteRecipients(AgnUtils.getCompanyID(req), new ArrayList<>(ids));

            for (String description : descriptions) {
                writeUserActivityLog(admin, "delete recipient", description);
            }

            return true;
        }
        return false;
    }

    /**
     *  Get a text representation of salutation
     *
     * @param genderId gender id from database
     * @return salutation text
     */
    @Override
    protected String getSalutationById(int genderId){
        return RecipientUtils.getSalutationByGenderId(genderId);
    }

    /**
     *  Get a text representation of recipient type
     *
     * @param letter recipient type letter
     * @return text representation of recipient type
     */
    @Override
    protected String getRecipientTypeByLetter(String letter){
        return BindingUtils.getRecipientTypeTitleByLetter(letter);
    }
}
