/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.beans.ComRecipientMailing;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ComColumnInfoService;
import net.sf.json.JSONObject;
import org.agnitas.beans.ProfileField;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.beans.impl.RecipientImpl;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.RecipientAction;
import org.agnitas.web.RecipientForm;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

/**
 * Handles all actions on profile fields.
 */
public class ComRecipientAction extends RecipientAction {

    public static final String COLUMN_DATASOURCE_ID = "datasource_id";

	public static final int ACTION_MAILINGS_VIEW = ORG_ACTION_LAST + 1;

    public static final int ACTION_BULK_CONFIRM_DELETE = ORG_ACTION_LAST + 2;

    public static final int ACTION_BULK_DELETE = ORG_ACTION_LAST + 3;

    public static final int ACTION_HISTORY_VIEW = ORG_ACTION_LAST + 4;

    public static final int ACTION_HISTORY_TRACKING = ORG_ACTION_LAST + 5;

    public static final int ACTION_REACTIONS_HISTORY = ORG_ACTION_LAST + 6;

    public static final int ACTION_CHECK_ADDRESS = ORG_ACTION_LAST + 10;

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComRecipientAction.class);
	/** DAO for accessing company data. */
	protected ComCompanyDao companyDao;
	protected ComProfileFieldDao profileFieldDao;
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
    public void setProfileFieldDao(ComProfileFieldDao profileFieldDao) {
		this.profileFieldDao = profileFieldDao;
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
        case ACTION_MAILINGS_VIEW:
            return "mailings_view";
        case ACTION_HISTORY_VIEW:
            return "history_view";
        case ACTION_BULK_CONFIRM_DELETE:
            return "bulk_confirm_delete";
        case ACTION_BULK_DELETE:
            return "bulk_delete";
        case ACTION_VIEW_WITHOUT_LOAD:
            return "view_without_load";
        case ACTION_HISTORY_TRACKING:
            return "history_tracking";
        case ACTION_REACTIONS_HISTORY:
            return "reactions_history";
        case ACTION_CHECK_ADDRESS:
            return "check_address";
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
        
		if (recipientDao.isMailtrackingEnabled(admin.getCompanyID())) {
			req.setAttribute("mailtracking", true);
		}

        try {
            switch(aForm.getAction()) {
            	case ACTION_OVERVIEW_START:
            		// all we do here is to set the overview parameter
            		aForm.setOverview(true);
            		aForm.setAction(ACTION_LIST);
                    aForm.setAdminId(admin.getAdminID());
                    aForm.resetSearch();
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

                case ACTION_MAILINGS_VIEW:
                    PaginatedListImpl<ComRecipientMailing> mailingsSentToRecipient = null;
                    if (aForm.getRecipientID() > 0) {
                        FormUtils.syncNumberOfRows(webStorage, WebStorage.RECIPIENT_MAILING_HISTORY_OVERVIEW, aForm);
                        mailingsSentToRecipient = recipientDao.getMailingsSentToRecipient(aForm.getRecipientID(), admin.getCompanyID(), aForm.getPageNumber(), aForm.getNumberOfRows(), aForm.getSort(), AgnUtils.sortingDirectionToBoolean(aForm.getDir(), false));
                        CaseInsensitiveMap<String, Object> customer = getRecipientDataForDescription(admin.getCompanyID(), aForm.getRecipientID());
                        if (customer.containsKey(COLUMN_EMAIL)) {
                            writeUserActivityLog(admin, "view mailing history", "For: " + customer.get(COLUMN_EMAIL) + ". " + getRecipientDescription(customer));
                        }
                    }
                    req.setAttribute("recipientMailings", mailingsSentToRecipient);
                    destination = mapping.findForward("mailings");
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

                        CaseInsensitiveMap<String, Object> customer = getRecipientDataForDescription(companyId, aForm.getRecipientID());
                        if (customer.containsKey(COLUMN_EMAIL)) {
                            writeUserActivityLog(admin, "view recipient status history", "For: " + customer.get(COLUMN_EMAIL) + ". " + getRecipientDescription(customer));
                        }
                    }
                    destination = mapping.findForward("history");
                    break;

                case ACTION_HISTORY_TRACKING:
                    destination = recipientRetargetingHistoryOverview(req, mapping, aForm, res);
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

                case ACTION_REACTIONS_HISTORY:
                    destination = recipientEndDeviceOverview(req, mapping, aForm, res);
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
        aForm.setSaveTargetVisible(false);

        return destination;
    }
    
	protected ActionForward recipientRetargetingHistoryOverview(HttpServletRequest request, ActionMapping mapping, ComRecipientForm form, HttpServletResponse response) throws IOException, ServletException {
        form.setAction(ACTION_VIEW);
        return super.execute(mapping, form, request, response);
    }
    
    protected ActionForward recipientEndDeviceOverview(HttpServletRequest request, ActionMapping mapping, ComRecipientForm form, HttpServletResponse response) throws IOException, ServletException {
        form.setAction(ACTION_VIEW);
        return super.execute(mapping, form, request, response);
    }
    
    private JSONObject checkAddress(ComAdmin admin, String email, int recipientId) {
        JSONObject data = new JSONObject();
        data.element("address", email);
        data.element("inUse", recipientDao.checkAddressInUse(StringUtils.trimToEmpty(email), recipientId, admin.getCompanyID()));
        data.element("isBlacklisted", blacklistService.blacklistCheck(StringUtils.trimToEmpty(email), admin.getCompanyID()));
        return data;
    }

    protected String getDateFormatPattern(ComAdmin admin) {
        // Configure date display format
        SimpleDateFormat dateTimeFormat = AgnUtils.getDateTimeFormat(DateFormat.MEDIUM, DateFormat.MEDIUM, admin);
        return dateTimeFormat.toPattern();
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
        String mailinglistPrefix = I18nString.getLocaleString("Mailinglist", AgnUtils.getLocale(req));
        for (ComRecipientHistory comRecipientHistory : recipientChangesHistory) {
            String fieldName = comRecipientHistory.getFieldName();
            StringBuilder mailinglist = new StringBuilder(mailinglistPrefix);
            mailinglist.append(" ").append(comRecipientHistory.getMailingList());
            if (comRecipientHistory.getMediaType() != null) {
                mailinglist.append(" Medium: ").append(I18nString.getLocaleString("mailing.MediaType." + comRecipientHistory.getMediaType(), AgnUtils.getLocale(req)));
            }
            mailinglist.append(" ").append(I18nString.getLocaleString("Field", AgnUtils.getLocale(req))).append(": ");
            switch(fieldName){
                case ComRecipientHistory.USER_TYPE:
                    comRecipientHistory.setFieldName(mailinglist.append(I18nString.getLocaleString("recipient.history.usertype", AgnUtils.getLocale(req))).toString());
                    String oldType = ((String) comRecipientHistory.getOldValue());
                    comRecipientHistory.setOldValue(getRecipientTypeByLetter(oldType));
                    String newType = ((String) comRecipientHistory.getNewValue());
                    comRecipientHistory.setNewValue(getRecipientTypeByLetter(newType));
                    break;
                case ComRecipientHistory.EXIT_MAILING_ID:
                    comRecipientHistory.setFieldName(mailinglist.append(I18nString.getLocaleString("recipient.history.mailingid", AgnUtils.getLocale(req))).toString());
                    break;
                case ComRecipientHistory.USER_REMARK:
                    comRecipientHistory.setFieldName(mailinglist.append(I18nString.getLocaleString("recipient.Remark", AgnUtils.getLocale(req))).toString());
                    break;
                case ComRecipientHistory.USER_STATUS:
                    comRecipientHistory.setFieldName(mailinglist.append(I18nString.getLocaleString("recipient.Status", AgnUtils.getLocale(req))).toString());
                    Number oldStatus = ((Number) comRecipientHistory.getOldValue());
                    if (oldStatus.intValue() == 0) {
                    	comRecipientHistory.setOldValue(I18nString.getLocaleString("recipient.NewRecipient", AgnUtils.getLocale(req)));
                    } else {
                    	comRecipientHistory.setOldValue(I18nString.getLocaleString("recipient.MailingState" + oldStatus, AgnUtils.getLocale(req)));
                    }
                    Number newStatus = ((Number) comRecipientHistory.getNewValue());
                    comRecipientHistory.setNewValue(I18nString.getLocaleString("recipient.MailingState" + newStatus, AgnUtils.getLocale(req)));
                    break;
                case ComRecipientHistory.EMAIL:
                    comRecipientHistory.setFieldName(mailinglist.append(I18nString.getLocaleString("mailing.MediaType.0", AgnUtils.getLocale(req))).toString());
                    break;
                case ComRecipientHistory.MAILINGLIST_DELETED:
                    comRecipientHistory.setFieldName(mailinglistPrefix + " " + comRecipientHistory.getMailingList());
                    comRecipientHistory.setNewValue(I18nString.getLocaleString("Mailinglist", AgnUtils.getLocale(req)) + " " + I18nString.getLocaleString("Deleted", AgnUtils.getLocale(req)).toUpperCase());
                    break;
                case ComRecipientHistory.CUSTOMER_BINDING_DELETED:
                    comRecipientHistory.setFieldName(mailinglistPrefix + " " + comRecipientHistory.getMailingList());
                    comRecipientHistory.setNewValue(I18nString.getLocaleString("Binding", AgnUtils.getLocale(req)) + " " + I18nString.getLocaleString("Deleted", AgnUtils.getLocale(req)).toUpperCase());
                    break;
            }
        }
    }

	@Override
    protected void loadDefaults(RecipientForm aForm, HttpServletRequest req) {
        ComRecipientForm comForm = (ComRecipientForm) aForm;
//        List<String> availableSM = new ArrayList<>();
//        List<ProfileField> columns = null;
//        try {
//			columns = columnInfoService.getColumnInfos(AgnUtils.getCompanyID(req));
//            for (ProfileField column : columns) {
//                String columnName = column.getColumn().toLowerCase();
//                for (String socialMedia : comForm.getAllSocialMedia()) {
//                    String socialMediaStatus = socialMedia + "_status";
//                    if (columnName.equals(socialMediaStatus)) {
//                        availableSM.add(socialMedia);
//                        break;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.error("execute: " + e, e);
//        }
//        comForm.setOldSocialValues(new HashMap<>());
//        comForm.setActiveSocialMedia(new String[0]);
//        comForm.setAvailableSocialMedia(availableSM.toArray(new String[availableSM.size()]));
        comForm.setAdminId(AgnUtils.getAdminId(req));
        super.loadDefaults(aForm, req);
    }

    @Override
    protected void loadRecipient(RecipientForm aForm, HttpServletRequest request) throws Exception {
    	int customerIdToLoad = aForm.getRecipientID();
        ComRecipientForm comRecipientForm = (ComRecipientForm) aForm;
        comRecipientForm.clearRecipientData();
		Map<String, Object> data = recipientDao.getCustomerDataFromDb(AgnUtils.getCompanyID(request), customerIdToLoad, true);
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
        
		ComAdmin admin = AgnUtils.getAdmin(request);
        Map<String, Object> dataMap = aForm.getColumnMap();
        List<ProfileField> profileFields = columnInfoService.getColumnInfos(admin.getCompanyID(), admin.getAdminID());
        for (ProfileField profileField : profileFields) {
        	if (profileField.getSimpleDataType() == SimpleDataType.Date && profileField.getModeEdit() == ProfileField.MODE_EDIT_READONLY) {
                convertDateColumnInDataMap(admin.getDateTimeFormat(), dataMap, profileField.getColumn());
        	}
        }
		
		comRecipientForm.setTrackingVeto(RecipientImpl.isDoNotTrackMe(data));

        writeUserActivityLog(AgnUtils.getAdmin(request), "view recipient", getRecipientDescription(aForm));
    }

    private void convertDateColumnInDataMap(SimpleDateFormat dateFormat, Map<String, Object> dataMap, String columnName) throws Exception {
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY);
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH);
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR);
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR);
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE);
		dataMap.remove(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND);
		String dateString = (String) dataMap.get(columnName);
		if (StringUtils.isNotBlank(dateString)) {
			Date Value = new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT).parse(dateString);
			String valueString = dateFormat.format(Value);
			dataMap.put(columnName, valueString);
		}
	}

//    @Override
//    protected void storeSpecificFields(RecipientForm aForm, Map<String, Object> customerFields, Admin admin) {
        // save the social media statuses
//        String availableMediaStatus, oldStatus, mediaStatus;
//        Integer recipientID = aForm.getRecipientID();
//        ComRecipientForm comForm = (ComRecipientForm) aForm;
//        List<String> active = Arrays.asList(comForm.getActiveSocialMedia());

//        for (String availableMedia : comForm.getAvailableSocialMedia()) {
//            availableMediaStatus = availableMedia + "_status";
//            oldStatus = (String) customerFields.get(availableMediaStatus);
//            if (active.contains(availableMedia)) {
//                mediaStatus = String.valueOf(ComRecipientForm.SOCIAL_MEDIA_STATUS_ACTIVE);
//            } else {
//                mediaStatus = String.valueOf(ComRecipientForm.SOCIAL_MEDIA_STATUS_INACTIVE);
//            }
//            customerFields.put(availableMediaStatus, mediaStatus);
//
//            if (recipientID != 0 && !oldStatus.equals(mediaStatus)) {
//                writeRecipientChangeLog(admin, customerFields, "Recipient network " + getNetworkName(availableMediaStatus) +
//                        ("1".equals(mediaStatus) ? " checked" : " unchecked"));
//            }
//        }
//    }

    @Override
    protected boolean saveRecipient(RecipientForm aForm, HttpServletRequest req) throws Exception {
        final int companyId = AgnUtils.getCompanyID(req);
        final ComAdmin admin = AgnUtils.getAdmin(req);
        final boolean isNewRecipient = aForm.getRecipientID() == 0;

        Recipient cust = recipientFactory.newRecipient();
        cust.setCompanyID(companyId);

        String defaultDatasourceID = Integer.toString(companyDao.getCompanyDatasource(companyId));
		if (!isNewRecipient) {
			cust.setCustomerID(aForm.getRecipientID());

			Map<String, Object> data = recipientDao.getCustomerDataFromDb(companyId, cust.getCustomerID());

            writeRecipientChangesLog(data, aForm, admin);

            Map<String, Object> column = aForm.getColumnMap();
            for (Entry<String, Object> entry : column.entrySet()) {
                data.put(entry.getKey(), entry.getValue());
            }

			data.put(COLUMN_GENDER, Integer.toString(aForm.getGender()));
			data.put(COLUMN_TITLE, aForm.getTitle().trim());
			data.put(COLUMN_FIRSTNAME, aForm.getFirstname().trim());
			data.put(COLUMN_LASTNAME, aForm.getLastname().trim());
			data.put(COLUMN_EMAIL, aForm.getEmail().trim());
			data.put(COLUMN_MAILTYPE, Integer.toString(aForm.getMailtype()));
			data.put(ComCompanyDaoImpl.STANDARD_FIELD_DO_NOT_TRACK, ((ComRecipientForm) aForm).isTrackingVeto() ? "1" : "0");
			
			if (!configService.getBooleanValue(ConfigValue.DontWriteLatestDatasourceId)) {
				data.put(COLUMN_LATEST_DATASOURCE_ID, defaultDatasourceID);
			}

//			storeSpecificFields(aForm, data, admin);
			cust.setCustParameters(data);
			recipientDao.updateInDB(cust);
		} else {
			if (!recipientDao.mayAdd(companyId, 1)) {
				return false;
			}

			Map<String, Object> data = recipientDao.getCustomerDataFromDb(companyId, aForm.getRecipientID());
			Map<String, Object> column = aForm.getColumnMap();
			for (Entry<String, Object> entry : column.entrySet()) {
				data.put(entry.getKey(), entry.getValue());
			}
			data.put(COLUMN_GENDER, Integer.toString(aForm.getGender()));
			data.put(COLUMN_TITLE, aForm.getTitle().trim());
			data.put(COLUMN_FIRSTNAME, aForm.getFirstname().trim());
			data.put(COLUMN_LASTNAME, aForm.getLastname().trim());
			data.put(COLUMN_EMAIL, aForm.getEmail().trim());
			data.put(COLUMN_MAILTYPE, Integer.toString(aForm.getMailtype()));
			data.put(ComCompanyDaoImpl.STANDARD_FIELD_DO_NOT_TRACK, ((ComRecipientForm) aForm).isTrackingVeto() ? "1" : "0");
            if (StringUtils.isEmpty((String) data.get(COLUMN_DATASOURCE_ID))){
                data.put(COLUMN_DATASOURCE_ID, defaultDatasourceID);

    			if (!configService.getBooleanValue(ConfigValue.DontWriteLatestDatasourceId)) {
    				data.put(COLUMN_LATEST_DATASOURCE_ID, defaultDatasourceID);
    			}
            }
			storeSpecificFields(aForm, data, admin);
			cust.setCustParameters(data);
			cust.setCustomerID(recipientDao.insertNewCust(cust));
			aForm.setRecipientID(cust.getCustomerID());

            writeUserActivityLog(admin, "create recipient", getRecipientDescription(cust));
		}
		aForm.setRecipientID(cust.getCustomerID());

		saveBindings(aForm, req, isNewRecipient);
		updateCustBindingsFromAdminReq(cust, req);
		return true;
	}

    private boolean deleteRecipientsBulk(ComRecipientForm form, HttpServletRequest req) {
        final Set<Integer> ids = form.getBulkIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            final ComAdmin admin = AgnUtils.getAdmin(req);
            final int companyId = AgnUtils.getCompanyID(req);
            final Set<String> columns = new HashSet<>();
            columns.add(COLUMN_CUSTOMER_ID);
            columns.add(COLUMN_FIRSTNAME);
            columns.add(COLUMN_LASTNAME);
            columns.add(COLUMN_EMAIL);

            List<String> descriptions = new ArrayList<>();
            for (int customerId : ids) {
                Map<String, Object> customer = recipientDao.getCustomerDataFromDb(companyId, customerId, columns);
                if (MapUtils.isNotEmpty(customer) && customer.containsKey(COLUMN_EMAIL)) {
                    descriptions.add(getRecipientDescription(customer));
                }
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
        switch (genderId){
            case 0:
                return "Mr.";
            case 1:
                return "Mrs.";
            case 2:
                return "Unknown";
            case 3:
                return "Miss";
            case 4:
                return "Practice";
            case 5:
                return "Company";
            default:
                return "not set";
        }
    }

    /**
     *  Get a text representation of recipient type
     *
     * @param letter recipient type letter
     * @return text representation of recipient type
     */
    @Override
    protected String getRecipientTypeByLetter(String letter){
        switch (letter){
            case "E":
                return "All";
            case "A":
                return "Administrator";
            case "T":
                return "Test recipient";
            case "t":
                return "Test VIP";
            case "W":
                return "Normal recipient";
            case "w":
                return "Normal VIP recipient";
            default:
                return "not set";
        }
    }
}
