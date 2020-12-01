/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import static com.agnitas.beans.ProfileField.MODE_EDIT_EDITABLE;
import static com.agnitas.web.ComRecipientAction.ACTION_SAVE_BACK_TO_LIST;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_CUSTOMER_ID;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_EMAIL;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FIRSTNAME;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_GENDER;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_LASTNAME;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_MAILTYPE;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_TITLE;
import static org.agnitas.emm.core.recipient.RecipientUtils.MAX_SELECTED_FIELDS_COUNT;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.factory.BindingEntryFactory;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.beans.impl.ViciousFormDataException;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.recipient.RecipientUtils;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.RecipientQueryBuilder;
import org.agnitas.service.RecipientSqlOptions;
import org.agnitas.service.WebStorage;
import org.agnitas.target.ConditionalOperator;
import org.agnitas.target.PseudoColumn;
import org.agnitas.target.TargetFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.forms.FormSearchParams;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.ArrayUtils;
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
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.delivery.service.DeliveryService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.codegen.DateValueFormatFaultyCodeException;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.util.html.xssprevention.HtmlXSSPreventer;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.emm.util.html.xssprevention.http.DefaultHtmlCheckErrorToActionErrorsMapper;
import com.agnitas.emm.util.streams.struts.ActionMessageCollector;
import com.agnitas.util.MapUtils;

/**
 * Handles all actions on recipients profile.
 */
public class RecipientAction extends StrutsActionBase {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(RecipientAction.class);

	public static final String SEARCH_PARAMS = "recipients_search_params";

	public static final String FUTURE_TASK = "GET_RECIPIENT_LIST";
	public static final int ACTION_SEARCH = ACTION_LAST + 1;
	public static final int ACTION_OVERVIEW_START = ACTION_LAST + 2;
	public static final int ACTION_VIEW_WITHOUT_LOAD = ACTION_LAST + 3;
	public static final int ORG_ACTION_LAST = ACTION_LAST + 3;

	protected Map<String, Future<PaginatedListImpl<DynaBean>>> futureHolder = null;
	protected ComMailinglistService mailinglistService;
	protected MailingService mailingService;

    protected RecipientService recipientService;
	protected ComRecipientDao recipientDao;
	protected BlacklistService blacklistService;
	protected ExecutorService workerExecutorService;
	protected RecipientQueryBuilder recipientQueryBuilder;
	protected ColumnInfoService columnInfoService;
	protected RecipientFactory recipientFactory;
	protected BindingEntryFactory bindingEntryFactory;
	protected ConfigService configService;
    protected TargetFactory targetFactory;
    protected ComTargetService targetService;
    protected DeliveryService deliveryService;
    /** Facade providing full EQL functionality. */
    protected EqlFacade eqlFacade;
    protected WebStorage webStorage;

	/**
	 * Process the specified HTTP request, and create the corresponding HTTP response (or forward to another web component that will create it). Return an <code>ActionForward</code> instance
	 * describing where and how control should be forwarded, or <code>null</code> if the response has already been completed.<br>
	 * <br>
	 * ACTION_LIST: initializes the list of columns-widths according to number of columns selected by user;<br>
	 * checks if the number of selected columns is less or equal than max-value (currently 8):<br>
	 * - if max value exceeded - puts error to page and restores the list of selected columns from previous time<br>
	 * - if the selected column number <= max-value - stores the current selection to be used for future calls<br>
	 * forwards to list. <br>
	 * <br>
	 * ACTION_VIEW: loads recipient to form, puts list of mailinglists to request and forwards to "view" <br>
	 * <br>
	 * ACTION_SAVE: If the request parameter "cancel.x" is set - just forwards to "list". In other case saves changed or new recipient to the database; also saves the recipient bindings to
	 * mailinglists <br>
	 * <br>
	 * ACTION_NEW: if the request parameter "cancel.x" is set - just forwards to "list", otherwise forwards to the page where user can fill the data for a new recipient <br>
	 * <br>
	 * ACTION_CONFIRM_DELETE: loads recipient into form and forwards to jsp with confirmation about deletion of current recipient <br>
	 * <br>
	 * ACTION_DELETE: if the request parameter "kill" is set - removes the recipient from database (otherwise returns to previous page) <br>
	 * <br>
	 * ACTION_VIEW_WITHOUT_LOAD: forwards to view page without loading the recipient data from DB. Used as input page for struts-action in struts-config.xml. If the error occurs while saving the
	 * recipient - this action is used (as we don't need to load recipient data again) <br>
	 * <br>
	 * Any other ACTION_* would cause a forward to "list" <br>
	 * <br>
	 * If the destination is "list" - calls a FutureHolder to get the list of recipients. While FutureHolder is running destination is "loading". After FutureHolder is finished destination is "list".
	 *
	 * @param form
	 *            data for the action filled by the jsp
	 * @param request
	 *            request from jsp.
	 * @param res
	 *            response
	 * @param mapping
	 *            The ActionMapping used to select this instance
	 *
	 * @exception IOException
	 *                if an input/output error occurs
	 * @exception ServletException
	 *                if a servlet exception occurs
	 *
	 * @return destination specified in struts-config.xml to forward to next jsp
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse res) throws IOException, ServletException {
		// Validate the request parameters specified by the user
		RecipientForm aForm = null;
		ActionMessages errors = new ActionErrors();
        ActionMessages rulesValidationErrors = new ActionMessages();
		ActionMessages messages = new ActionMessages();
		ActionForward destination = null;
		final HttpSession session = request.getSession();

		if (form != null) {
			aForm = (RecipientForm) form;
		} else {
			aForm = new RecipientForm();
		}
		if (AgnUtils.parameterNotEmpty(request, "resetSearch")) {
			session.removeAttribute(SEARCH_PARAMS);
			aForm.resetSearch();
		} else if(BooleanUtils.toBoolean(request.getParameter(FormSearchParams.RESTORE_PARAM_NAME))){
			final RecipientSearchParams searchParams = (RecipientSearchParams) session.getAttribute(SEARCH_PARAMS);
			if(searchParams != null) {
				aForm.restoreSearchParams(searchParams);
			}
		}
		this.updateRecipientFormProperties(request, aForm);

		if (aForm.getDelete().isSelected()) {
			aForm.setAction(ACTION_CONFIRM_DELETE);
		}
		
		ComAdmin admin = AgnUtils.getAdmin(request);
		Objects.requireNonNull(admin);

		try {
			switch (aForm.getAction()) {
			case ACTION_LIST:
				setPseudoRuleAttributes(request);
				session.setAttribute(SEARCH_PARAMS, aForm.generateSearchParams());
				saveTargetGroupIfNecessary(aForm, errors, messages, admin, rulesValidationErrors);
				AgnUtils.setAdminDateTimeFormatPatterns(request);

				validateSelectedFields(aForm, errors);
				destination = mapping.findForward("list");
				break;

			case ACTION_VIEW:
				if (request.getParameter("recipientID") != null) {
					loadRecipient(aForm, admin);
					aForm.setAction(RecipientAction.ACTION_SAVE);
				} else {
					loadDefaults(aForm, request);
					aForm.setAction(RecipientAction.ACTION_NEW);
				}
				defineMailinglistAttributes(request, admin);
				
				AgnUtils.setAdminDateTimeFormatPatterns(request);
				
				destination = mapping.findForward("view");
				break;

			case ACTION_SAVE:
				destination = saveRecipientAndGetDestination(request, errors, messages, mapping, aForm, this::loadDataAndForwardToView);

				if("messages".equals(destination.getName()) || "confirm_save".equals(destination.getName())) {
					return destination;
				}
				break;
			case ACTION_SAVE_BACK_TO_LIST:
				destination = saveRecipientAndGetDestination(request, errors, messages, mapping, aForm, this::forwardToList);

				if("messages".equals(destination.getName())) {
					return destination;
				}
				break;

			case ACTION_NEW:
				if (request.getParameter("cancel.x") == null) {
					validateForm(admin, aForm, errors);
					
					if (!errors.isEmpty()) {
						saveErrors(request, errors);
						return mapping.findForward("messages");
					}
		            
					aForm.setRecipientID(0);
					final ActionForward saveDestination = saveRecipient(aForm, request, errors, mapping);
					if (saveDestination == null) {
						aForm.setAction(RecipientAction.ACTION_LIST);
	                	AgnUtils.setAdminDateTimeFormatPatterns(request);
						destination = mapping.findForward("list");
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
					} else {
						saveErrors(request, errors);
						return saveDestination;
					}
					defineMailinglistAttributes(request, admin);
				} else {
                	AgnUtils.setAdminDateTimeFormatPatterns(request);
					destination = mapping.findForward("list");
				}
				break;

			case ACTION_CONFIRM_DELETE:
				loadRecipient(aForm, admin);
				destination = mapping.findForward("delete");
				break;

			case ACTION_DELETE:
				if (request.getParameter("kill") != null) {
					deleteRecipient(aForm, admin);
					aForm.setAction(RecipientAction.ACTION_LIST);
                	AgnUtils.setAdminDateTimeFormatPatterns(request);
					destination = mapping.findForward("list");
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
				}
				break;
			case ACTION_VIEW_WITHOUT_LOAD:
				defineMailinglistAttributes(request, admin);
				AgnUtils.setAdminDateTimeFormatPatterns(request);
				destination = mapping.findForward("view");
				aForm.setAction(RecipientAction.ACTION_SAVE);
				break;

			default:
				aForm.setAction(RecipientAction.ACTION_LIST);
            	AgnUtils.setAdminDateTimeFormatPatterns(request);
				destination = mapping.findForward("list");
				if (aForm.getColumnwidthsList() == null) {
					int lengthSelectedFields = aForm.getSelectedFields().length;
					aForm.setColumnwidthsList(getInitializedColumnWidthList(lengthSelectedFields + 1));
				}
			}
		} catch (ViciousFormDataException e) {
			logger.error("RecipientAction execute: " + e.getMessage(), e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.recipient.viciousData", e.getMessage()));
			aForm.setAction(RecipientAction.ACTION_SAVE);
			defineMailinglistAttributes(request, admin);
			AgnUtils.setAdminDateTimeFormatPatterns(request);
			destination = mapping.findForward("view");
		} catch (Exception e) {
			logger.error("RecipientAction execute: " + e.getMessage(), e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			aForm.setAction(RecipientAction.ACTION_SAVE);
			defineMailinglistAttributes(request, admin);
			AgnUtils.setAdminDateTimeFormatPatterns(request);
			destination = mapping.findForward("view");
		}
		
		destination = prepareListOverview(aForm, destination, mapping, request, errors, messages);

		// this is a hack for the recipient-search / recipient overview.
		if (destination != null && "list".equals(destination.getName())) {
			// check if we are in search-mode
			if (!aForm.isOverview()) {
				// check if it is the last element in filter
				if (aForm.getNumTargetNodes() == 0 && aForm.getListID() == 0 && aForm.getTargetID() == 0 &&
						aForm.isDefaultUserType() && aForm.getUser_status() == 0) {
					
					aForm.setAction(7);
                	
                	AgnUtils.setAdminDateTimeFormatPatterns(request);
                	
					destination = mapping.findForward("search");
				}
			}
		}

		if (destination != null && "view".equals(destination.getName())) {
			request.setAttribute("isRecipientEmailInUseWarningEnabled", configService.getBooleanValue(ConfigValue.RecipientEmailInUseWarning, AgnUtils.getCompanyID(request)));
		}

		List<LightweightMailing> lightweightIntervalMailings = mailingService.getLightweightIntervalMailings(admin);
		request.setAttribute("interval_mailings", lightweightIntervalMailings);

        request.setAttribute("rulesValidationErrors", rulesValidationErrors);

		// Report any errors we have discovered back to the original form
		if (!errors.isEmpty()) {
			saveErrors(request, errors);
			// return new ActionForward(mapping.getForward());
		}

		// Report any message (non-errors) we have discovered
		if (!messages.isEmpty()) {
			saveMessages(request, messages);
		}

        aForm.setSaveTargetVisible(false);

        return destination;
	}
	
	protected void setPseudoRuleAttributes(final HttpServletRequest request) {
		request.setAttribute("COLUMN_INTERVAL_MAILING", PseudoColumn.INTERVAL_MAILING);
	}
	
	private boolean validateForm(ComAdmin admin, RecipientForm form, ActionMessages errors) throws Exception {
		if (!configService.getBooleanValue(ConfigValue.AllowEmptyEmail, admin.getCompanyID()) && StringUtils.isBlank(form.getEmail())) {
			errors.add("email", new ActionMessage("error.invalid.email"));
		}
		if (StringUtils.isNotBlank(form.getEmail()) && !AgnUtils.isEmailValid(form.getEmail())) {
			errors.add("email", new ActionMessage("error.invalid.email"));
		}
		if (form.getTitle().length() > 100) {
			errors.add("title", new ActionMessage("error.recipient.title.tooLong"));
		}
		if (form.getFirstname().length() > 100) {
			errors.add("firstname", new ActionMessage("error.recipient.firstname.tooLong"));
		}
		if (form.getLastname().length() > 100) {
			errors.add("lastname", new ActionMessage("error.recipient.lastname.tooLong"));
		}
		if (!errors.isEmpty()) {
			return false;
		}
		
		if (!admin.permissionAllowed(Permission.RECIPIENT_PROFILEFIELD_HTML_ALLOWED)) {
			List<ProfileField> columnInfos = columnInfoService.getColumnInfos(admin.getCompanyID(), admin.getAdminID());
			//validate profile fields with string type
			List<String> columnNames = getEditableColumnNames(columnInfos, DbColumnType.GENERIC_TYPE_VARCHAR);
			return validateIfFieldsDoesNotContainHtmlTags(form, columnNames, errors);
		}
		return true;
	}
	
	private boolean validateIfFieldsDoesNotContainHtmlTags(RecipientForm form, List<String> columnNames, ActionMessages errors) {
		for (String name : columnNames) {
			String value = getColumnValue(form, name);
			if (StringUtils.isNotBlank(value)) {
				try {
					HtmlXSSPreventer.checkString(value, ArrayUtils.EMPTY_STRING_ARRAY);
				} catch (final XSSHtmlException e) {
					DefaultHtmlCheckErrorToActionErrorsMapper mapper = new DefaultHtmlCheckErrorToActionErrorsMapper();
					ActionMessages errorCollections = e.getErrors().stream()
							.map(mapper::mapToActionError)
							.collect(new ActionMessageCollector(ActionMessages.GLOBAL_MESSAGE));
					errors.add(errorCollections);
					return false;
				}
			}
		}
		return true;
	}
	
	private String getColumnValue(RecipientForm form, String name) {
		switch (name) {
			case COLUMN_TITLE:
				return form.getTitle();
			case COLUMN_FIRSTNAME:
				return form.getFirstname();
			case COLUMN_LASTNAME:
				return form.getLastname();
			case COLUMN_EMAIL:
				return form.getEmail();
			default:
				return (String) form.getColumnMap().get(name);
		}
	}
	
	private List<String> getEditableColumnNames(List<ProfileField> columnInfos, String dataType) {
		return columnInfos.stream()
				.filter(column -> StringUtils.equalsIgnoreCase(dataType, column.getDataType())
						&& column.getModeEdit() == MODE_EDIT_EDITABLE)
				.map(ProfileField::getShortname)
				.collect(Collectors.toList());
	}
	
	protected ActionForward prepareListOverview(RecipientForm aForm, ActionForward destination, ActionMapping mapping, HttpServletRequest request, ActionMessages errors, ActionMessages messages) {
		if (destination != null && ("list".equals(destination.getName()))) {
			try {
				destination = processListOverviewLoading(aForm, mapping, request, errors, messages);
				aForm.setDatePickerFormat(AgnUtils.getAdmin(request).getDateFormat().toPattern());
			} catch (Exception e) {
				logger.error("recipientList: " + e, e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
				aForm.setError(true); // do not refresh when an error has been occurred
			}

			if (aForm.isDeactivatePagination()) {
				int maxRecipients = AgnUtils.getCompanyMaxRecipients(request);
				request.setAttribute("countOfRecipients", maxRecipients);
			}
		}
		return destination;
	}
	
	protected void validateSelectedFields(RecipientForm aForm, ActionMessages errors) {
		int columnsCount = ArrayUtils.getLength(aForm.getSelectedFields());
		if (columnsCount < 1 || columnsCount > MAX_SELECTED_FIELDS_COUNT) {
			String[] columns = webStorage.get(WebStorage.RECIPIENT_OVERVIEW)
					.getSelectedFields()
					.toArray(ArrayUtils.EMPTY_STRING_ARRAY);

			aForm.setSelectedFields(columns);
		}

		if (columnsCount > MAX_SELECTED_FIELDS_COUNT) {
			logger.error("Error in RecipientAction: error.maximum.recipient.columns: count > " + MAX_SELECTED_FIELDS_COUNT);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.maximum.recipient.columns"));
			aForm.addErrors(errors);
		}
	}
	
	private ActionForward processListOverviewLoading(RecipientForm aForm, ActionMapping mapping, HttpServletRequest request, ActionMessages errors, ActionMessages messages) throws Exception {
		final ComAdmin admin = AgnUtils.getAdmin(request);
		ActionForward destination = mapping.findForward("loading");
		String key = FUTURE_TASK + "@" + request.getSession(false).getId();
		
		Map<String, String> fieldsMap = getRecipientFieldsNames(admin.getCompanyID(), admin.getAdminID());
		Set<String> recipientDbColumns = fieldsMap.keySet();
		request.setAttribute("fieldsMap", fieldsMap);

		Future<PaginatedListImpl<DynaBean>> future = futureHolder.get(key);
		if(future == null){
			try {
				future = getRecipientListFuture(request, aForm, recipientDbColumns);
				futureHolder.put(key, future);
			} catch (DateValueFormatFaultyCodeException e) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.norule"));
				return mapping.findForward("list");
			} catch (Exception e) {
				throw new RuntimeException("Failure of getting RecipientListFuture", e);
			}
		}

		//if we perform AJAX request (load next/previous page) we have to wait for preparing data
		if (HttpUtils.isAjax(request)) {
			try {
				waitForFuture(future, aForm);
			} finally {
				aForm.setRefreshMillis(RecipientForm.DEFAULT_REFRESH_MILLIS);
				futureHolder.remove(key);
			}
		}

		if (future != null && future.isDone()) {
			try {
				PaginatedListImpl<DynaBean> resultingList = future.get();

				request.setAttribute("recipientList", resultingList);
				defineMailinglistAttributes(request, admin);
				request.setAttribute("targets", targetService.getTargetLights(admin));
				
				AgnUtils.setAdminDateTimeFormatPatterns(request);

				destination = mapping.findForward("list");
				if (resultingList == null) {
					aForm.setDeactivatePagination(false);
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.errorneous_recipient_search"));
				} else {
					// check the max recipients for company and change visualisation if needed
					int maxRecipients = AgnUtils.getCompanyMaxRecipients(request);
					if (maxRecipients > 0 && resultingList.getFullListSize() > maxRecipients) {
						aForm.setPage("1");
						aForm.setNumberOfRowsChanged(false);
						aForm.setDeactivatePagination(true);
					} else {
						aForm.setDeactivatePagination(false);
					}

					//add to messages only new from form
					messages.add(getMissedMessages(aForm, messages));
					aForm.setMessages(null);
					errors.add(aForm.getErrors());
					aForm.resetErrors();
				}
			} finally {
				aForm.setRefreshMillis(RecipientForm.DEFAULT_REFRESH_MILLIS);
				futureHolder.remove(key);
			}
		} else {
			if (aForm.getRefreshMillis() < 1000) { // raise the refresh time
				aForm.setRefreshMillis(aForm.getRefreshMillis() + 50);
			}
			aForm.setError(false);
		}
		
		return destination;
	}

	private void saveTargetGroupIfNecessary(RecipientForm aForm, ActionMessages errors, ActionMessages messages, ComAdmin admin, ActionMessages rulesValidationErrors) throws Exception {
		if (aForm.isNeedSaveTargetGroup()) {
			if (!targetService.checkIfTargetNameIsValid(aForm.getTargetShortname())) {
				errors.clear();
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.namenotallowed"));
			} else if (targetService.checkIfTargetNameAlreadyExists(admin.getCompanyID(), aForm.getTargetShortname(), 0)) {
				errors.clear();
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.namealreadyexists"));
			} else if (saveTargetGroup(aForm, admin, errors, rulesValidationErrors) > 0) {
			   messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			} else {
			   errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.saving"));
			}
	
			aForm.setNeedSaveTargetGroup(false);
		}
	}
	
	protected void defineMailinglistAttributes(final HttpServletRequest request, final ComAdmin admin) {
		request.setAttribute("hasAnyDisabledMailingLists", false);
		request.setAttribute("mailinglists", mailinglistService.getMailinglists(admin.getCompanyID()));

	}

	private void waitForFuture(Future<?> future, RecipientForm form) throws ExecutionException, InterruptedException {
		while (!future.isDone()) {
			// Raise the refresh time up to 1 second
			if (form.getRefreshMillis() < 1000) {
				form.setRefreshMillis(form.getRefreshMillis() + 50);
			}

			try {
				future.get(form.getRefreshMillis(), TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				// Do nothing, keep waiting
			}
		}
	}

    private ActionMessages getMissedMessages(RecipientForm aForm, ActionMessages messages) {
        ActionMessages missedMessages = new ActionMessages();
        if (aForm.getMessages() != null) {
            @SuppressWarnings("unchecked")
			Iterator<String> propertiesIterator = aForm.getMessages().properties();
            while (propertiesIterator.hasNext()) {
                String property = propertiesIterator.next();
                @SuppressWarnings("unchecked")
    			Iterator<ActionMessage> formMessageIterator = aForm.getMessages().get(property);
                while (formMessageIterator.hasNext()) {
                    ActionMessage formMessage = formMessageIterator.next();
                    @SuppressWarnings("unchecked")
        			Iterator<ActionMessage> messageIterator = messages.get(property);
                    boolean messageExists = false;
                    while (messageIterator.hasNext()) {
                        ActionMessage message = messageIterator.next();
                        if (formMessage.getKey().equals(message.getKey())) {
                            messageExists = true;
                        }
                    }

                    if (!messageExists) {
                        missedMessages.add(property, formMessage);
                    }
                }
            }
        }
        return missedMessages;
    }

    /**
	 * Loads recipient data into a form. Uses recipientID property of aForm to identify the customer.
	 *
	 * @param aForm
	 *            form to put recipient data into
	 * @param admin
	 *            HTTP request
     * @throws Exception
	 */
	protected void loadRecipient(RecipientForm aForm, ComAdmin admin) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Loads recipient columns list for specific admin (identified by adminID property of form) and puts it to form.
	 *
	 * @param aForm
	 *            form
	 * @param request
	 *            HTTP request
	 */
	protected void loadDefaults(RecipientForm aForm, HttpServletRequest request) {
		aForm.clearColumns();

		try {
			List<ProfileField> fields = columnInfoService.getColumnInfos(AgnUtils.getCompanyID(request), AgnUtils.getAdminId(request));
			LocalDate now = LocalDate.now();

			for (ProfileField field : fields) {
				boolean isDateField = DbColumnType.GENERIC_TYPE_DATE.equalsIgnoreCase(field.getDataType()) || DbColumnType.GENERIC_TYPE_DATETIME.equalsIgnoreCase(field.getDataType());
				if (isDateField && DbUtilities.isNowKeyword(field.getDefaultValue())) {
					aForm.setColumn(field.getColumn() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(now.getDayOfMonth()));
					aForm.setColumn(field.getColumn() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(now.getMonthValue()));
					aForm.setColumn(field.getColumn() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(now.getYear()));
				} else {
					aForm.setColumn(field.getColumn(), field.getDefaultValue());
				}
			}
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		}

		aForm.setFirstname(StringUtils.trimToEmpty(request.getParameter("firstname")));
		aForm.setLastname(StringUtils.trimToEmpty(request.getParameter("lastname")));
		aForm.setEmail(StringUtils.trimToEmpty(request.getParameter("email")));
	}

	/**
	 * Saves recipient bindings to mailinglists set by user on view-page.<br>
	 * The bindings-data is taken from recipientForm.
	 *
	 * @param recipientForm
	 *            form
	 * @param request
	 *            HTTP request
     * @param isNewRecipient
	 * @throws Exception
	 */
	protected void saveBindings(RecipientForm recipientForm, HttpServletRequest request, boolean isNewRecipient,
			final boolean newEmailBlacklisted, final boolean oldEmailBlacklisted) throws Exception {
		ComAdmin admin = AgnUtils.getAdmin(request);
		int companyID = AgnUtils.getCompanyID(request);
		int customerID = recipientForm.getRecipientID();
		Map<Integer, Map<Integer, BindingEntry>> bindings = recipientForm.getAllBindings();
		Map<Integer, Map<Integer, BindingEntry>> customerMailingLists = recipientDao.getAllMailingLists(customerID, companyID);

		for (Map<Integer, BindingEntry> bindingEntriesByMediaType : bindings.values()) {
			for (Entry<Integer, BindingEntry> entry : bindingEntriesByMediaType.entrySet()) {
				BindingEntry bindingEntry = entry.getValue();

				if (bindingEntry.getUserStatus() != 0) {
					bindingEntry.setCustomerID(customerID);
					bindingEntry.setUserStatus(getNewUserStatus(bindingEntry.getUserStatus(), newEmailBlacklisted, oldEmailBlacklisted));
					int newUserStatus = bindingEntry.getUserStatus();
					int mailingListId = bindingEntry.getMailinglistID();
					int existingUserStatus = 3;
					String existingUserType = "";
					String newUserType = bindingEntry.getUserType();

					try {
						if (customerMailingLists.get(mailingListId) != null && customerMailingLists.get(mailingListId).get(entry.getKey()) != null) {
							BindingEntry existingBindingEntry = customerMailingLists.get(mailingListId).get(entry.getKey());
							existingUserType = existingBindingEntry.getUserType();
							existingUserStatus = existingBindingEntry.getUserStatus();
						}
					} catch (Exception e) {
						logger.error("saveBindings: Existed binding could not be read", e);
					}

					if (!isNewRecipient) {
						if (!(newUserType.equals(existingUserType))) {
							writeRecipientChangeLog(admin, recipientForm, "Recipient type for mailingList with ID = " + mailingListId +
									" changed from " + getRecipientTypeByLetter(existingUserType) +
									" to " + getRecipientTypeByLetter(newUserType));
						}

						if (newUserStatus != existingUserStatus) {
							writeRecipientChangeLog(admin, recipientForm, "Recipient Email for mailingList with ID = " +
									mailingListId + (newUserStatus < existingUserStatus ? " switch on" : " switch off"));
						}
					}

					// this should be removed after refactoring of BindingEntry class
					if (bindingEntry.getBindingEntryDao() == null) {
						bindingEntry.setBindingEntryDao(bindingEntryFactory.getBindingEntryDao());
					}
					if (!bindingEntry.saveBindingInDB(companyID, customerMailingLists, admin)) {
						logger.error("saveBindings: Binding could not be saved");
					}
				}
			}
		}
	}

	private int getNewUserStatus(final int setUserStatus, final boolean newEmailBlacklisted, final boolean oldEmailBlacklisted) {
		if(newEmailBlacklisted) {
			return UserStatus.Blacklisted.getStatusCode();
		}
		if(oldEmailBlacklisted && setUserStatus == UserStatus.Blacklisted.getStatusCode()) {
			return UserStatus.AdminOut.getStatusCode();
		}
		return setUserStatus;
	}

	/**
	 * If customerID of aForm is not 0 - saves changed recipient to DB<br>
	 * If customerID is 0 - creates new recipient in DB (before that checks if max number of recipients is reached)<br>
	 * Recipient data is taken from form properties.<br>
	 * Also invokes method for saving recipient bindings to mailinglists.
	 *
	 * @param aForm
	 *            form
	 * @param request
	 *            HTTP request
	 * @throws Exception
	 */
	protected ActionForward saveRecipient(RecipientForm aForm, HttpServletRequest request, ActionMessages errors, ActionMapping actionMapping) throws Exception {
        throw new UnsupportedOperationException();
	}

    /**
     * Get a text representation of mail format
     *
     * @param mailFormatId mail format id from database
     * @return text representation of mail format
     */
    protected String getMailFormatById(int mailFormatId){
        switch(mailFormatId) {
            case 0:
                return "Text";
            case 1:
                return "HTML";
            case 2:
                return "OfflineHTML";
            default:
                return "not set";
        }
    }

    /**
     *  Get a text representation of salutation
     *
     * @param genderId gender id from database
     * @return salutation text
     */
    protected String getSalutationById(int genderId){
        throw new UnsupportedOperationException();
    }

    /**
     *  Get a text representation of recipient type
     *
     * @param letter recipient type letter
     * @return text representation of recipient type
     */
    protected String getRecipientTypeByLetter(String letter){
		throw new UnsupportedOperationException();
    }

	/**
	 * Method for storing specific fields of recipient. Can be overridden in subclass. For OpenEMM the method currently doesn't do anything.
	 *
	 * @param aForm
	 *            current form
	 * @param customerFields
	 *            the map of recipients fields which will be later used for saving recipient to DB
     * @param admin
     *            current form
	 */
	protected void storeSpecificFields(RecipientForm aForm, Map<String, Object> customerFields, ComAdmin admin) {
		// nothing to do
	}

	/**
	 * Gets the list of recipient fields
	 *
	 * @param companyId
	 *            current company ID
	 * @return recipient fields in a form of map: column -> column-shortname
	 * @throws Exception
	 *             if the exception happens in columnInfoService class
	 */
	protected Map<String, String> getRecipientFieldsNames(int companyId, int adminId) throws Exception {
		final CaseInsensitiveMap<String, ProfileField> columnInfoMap = columnInfoService.getColumnInfoMap(companyId, adminId);
		final LinkedHashMap<String, String> fieldsMap = new LinkedHashMap<>();
		
		// we need predefined order for default columns: gender, firstname, lastname.
		fieldsMap.put(COLUMN_GENDER, columnInfoMap.get(COLUMN_GENDER).getShortname());
		fieldsMap.put(COLUMN_FIRSTNAME, columnInfoMap.get(COLUMN_FIRSTNAME).getShortname());
		fieldsMap.put(COLUMN_LASTNAME, columnInfoMap.get(COLUMN_LASTNAME).getShortname());
		
		columnInfoMap.remove(COLUMN_GENDER);
		columnInfoMap.remove(COLUMN_FIRSTNAME);
		columnInfoMap.remove(COLUMN_LASTNAME);

		columnInfoMap.forEach((key, value) -> fieldsMap.put(key, value.getShortname()));
		
		MapUtils.reorderLinkedHashMap(fieldsMap, RecipientUtils.getFieldOrderComparator());

		return fieldsMap;
	}


	/**
	 * Updates customer bindings with the data taken from request
	 *
	 * @param cust
	 *            recipient bean
	 * @param request
	 *            HTTP request holding parameters for binding entries
	 */
	@Deprecated
	public boolean updateCustBindingsFromAdminReq(Recipient cust, HttpServletRequest request) {
		String newKey = null;
		String aParam = null;
		int aMailinglistID;
		int oldSubStatus, newSubStatus;
		String tmpUT = null;
		String tmpOrgUT = null;
		BindingEntry bindingEntry = bindingEntryFactory.newBindingEntry();

		for (String key : request.getParameterMap().keySet()) {
			if (key.startsWith("AGN_0_ORG_MT")) {
				oldSubStatus = Integer.parseInt(request.getParameter(key));
				aMailinglistID = Integer.parseInt(key.substring(12));
				newKey = "AGN_0_MTYPE" + aMailinglistID;
				aParam = request.getParameter(newKey);
				if (aParam != null) {
					newSubStatus = 1;
				} else {
					newSubStatus = 0;
				}

				newKey = "AGN_0_MLUT" + aMailinglistID;
				tmpUT = request.getParameter(newKey);
				newKey = "AGN_0_ORG_UT" + aMailinglistID;
				tmpOrgUT = request.getParameter(newKey);

				if ((newSubStatus != oldSubStatus) || (tmpUT.compareTo(tmpOrgUT) != 0)) {
					bindingEntry.setMediaType(0);
					bindingEntry.setCustomerID(cust.getCustomerID());
					bindingEntry.setMailinglistID(aMailinglistID);
					bindingEntry.setUserType(tmpUT);
					if (newSubStatus == 0) { // Opt-Out
						bindingEntry.setUserStatus(UserStatus.AdminOut.getStatusCode());
						// bindingEntry.setUserRemark("Opt-Out by ADMIN");
					} else { // Opt-In
						bindingEntry.setUserStatus(UserStatus.Active.getStatusCode());
						// bindingEntry.setUserRemark("Opt-In by ADMIN");
					}
					if (!bindingEntry.updateBindingInDB(cust.getCompanyID())) {
						// bindingEntry.setUserType(BindingEntry.USER_TYPE_WORLD); // Bei Neu-Eintrag durch User entsprechenden Typ setzen
						if (newSubStatus == 1) {
							bindingEntry.insertNewBindingInDB(cust.getCompanyID());
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Removes recipient from a database. The ID of recipient is taken from form property recipientID.
	 *
	 * @param aForm
	 *            form
	 * @param admin
	 */
	protected void deleteRecipient(RecipientForm aForm, ComAdmin admin) {
		recipientDao.deleteCustomerDataFromDb(admin.getCompanyID(), aForm.getRecipientID());
		writeUserActivityLog(admin, "delete recipient", getRecipientDescription(aForm));
	}

	public Future<PaginatedListImpl<DynaBean>> getRecipientListFuture(HttpServletRequest request, RecipientForm aForm, Set<String> recipientDbColumns) throws Exception {
		int pageNumber = 1;
		if (StringUtils.isNotBlank(aForm.getPage())) {
			pageNumber = Integer.parseInt(aForm.getPage());
		}
		if (aForm.isNumberOfRowsChanged()) {
			aForm.setNumberOfRowsChanged(false);
			pageNumber = 1;
		}
		String sort = getSort(request, aForm);
		String direction = request.getParameter("dir");

		webStorage.access(WebStorage.RECIPIENT_OVERVIEW, storage -> {
			if (aForm.getNumberOfRows() > 0) {
				storage.setRowsCount(aForm.getNumberOfRows());
				storage.setSelectedFields(Arrays.asList(aForm.getSelectedFields()));
			} else {
				aForm.setNumberOfRows(storage.getRowsCount());
				aForm.setSelectedFields(storage.getSelectedFields().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
			}
		});

		int rownums = aForm.getNumberOfRows();
		if (direction == null) {
			direction = aForm.getOrder();
		} else {
			aForm.setOrder(direction);
		}

		return workerExecutorService.submit(recipientService.getRecipientWorker(request, aForm, recipientDbColumns, sort, direction, pageNumber, rownums));
	}

	protected RecipientSqlOptions.Builder prepareBuilder(HttpServletRequest request, RecipientForm form) {
		RecipientSqlOptions.Builder builder = RecipientSqlOptions.builder();
		builder.setCheckParenthesisBalance(form.checkParenthesisBalance());

		String sort = request.getParameter("sort");
		if (sort == null) {
			sort = form.getSort();

			if (logger.isDebugEnabled()) {
				logger.debug("request parameter sort = null");
				logger.debug("using form parameter sort = " + sort);
			}
		}

		String direction = request.getParameter("dir");
		if (direction == null) {
			direction = form.getOrder();

			if (logger.isDebugEnabled()) {
				logger.debug("request parameter dir = null");
				logger.debug("using form parameter order = " + direction);
			}
		}

		builder.setSort(sort)
				.setDirection(direction)
				.setUseAdvancedSearch(true, form);

		if (request.getParameter("listID") != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("parameter listID = " + request.getParameter("listID"));
			}

			form.setListID(NumberUtils.toInt(request.getParameter("listID")));
		}
		builder.setListId(form.getListID());


		if (request.getParameter("targetID") != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("parameter targetID = " + request.getParameter("targetID"));
			}

			form.setTargetID(Integer.parseInt(request.getParameter("targetID")));
		}
		builder.setTargetId(form.getTargetID());

		if (request.getParameter("user_type") != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("parameter user_type = " + request.getParameter("user_type"));
			}
			form.setUser_type(request.getParameter("user_type"));
		}
		builder.setUserType(form.getUser_type());

		if (request.getParameter("searchFirstName") != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("parameter searchFirstName = " + request.getParameter("searchFirstName"));
			}

			form.setSearchFirstName(request.getParameter("searchFirstName"));
		}
		builder.setSearchFirstName(form.getSearchFirstName());

		if (request.getParameter("searchLastName") != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("parameter searchLastName = " + request.getParameter("searchLastName"));
			}

			form.setSearchLastName(request.getParameter("searchLastName"));
		}
		builder.setSearchLastName(form.getSearchLastName());

		if (request.getParameter("searchEmail") != null) {
			form.setSearchEmail(request.getParameter("searchEmail"));
		}
		builder.setSearchEmail(form.getSearchEmail());

		if (request.getParameter("user_status") != null) {
			form.setUser_status(Integer.parseInt(request.getParameter("user_status")));
		}
		builder.setUserStatus(form.getUser_status());

		return builder;
	}

	protected RecipientSqlOptions getOptions(HttpServletRequest request, RecipientForm form) {
		return prepareBuilder(request, form).build();
	}
	
	private boolean updateRecipientFormProperties(HttpServletRequest request, RecipientForm form) {
		form.cleanRulesForBasicSearch();

		int lastIndex = form.getNumTargetNodes();
		int removeIndex = -1;

		// If "add" was clicked, add new rule
		if (AgnUtils.parameterNotEmpty(request, "addTargetNode")  || (AgnUtils.parameterNotEmpty(request, "Update") && !StringUtils.isEmpty(form.getPrimaryValueNew()))) {
			form.setColumnAndType(lastIndex, form.getColumnAndTypeNew());
			form.setChainOperator(lastIndex, form.getChainOperatorNew());
			form.setParenthesisOpened(lastIndex, form.getParenthesisOpenedNew());
			form.setPrimaryOperator(lastIndex, form.getPrimaryOperatorNew());
			form.setPrimaryValue(lastIndex, form.getPrimaryValueNew());
			form.setParenthesisClosed(lastIndex, form.getParenthesisClosedNew());
			form.setDateFormat(lastIndex, form.getDateFormatNew());
			form.setSecondaryOperator(lastIndex, form.getSecondaryOperatorNew());
			form.setSecondaryValue(lastIndex, form.getSecondaryValueNew());

			// clearing fields in advance search
			form.clearNewAdvancedSearch();

			lastIndex++;
		}

		lastIndex = createRulesFromBasicSearch(form, lastIndex);

		int nodeToRemove = -1;
		String nodeToRemoveStr = request.getParameter("targetNodeToRemove");
		if (AgnUtils.parameterNotEmpty(request, "targetNodeToRemove")) {
			nodeToRemove = Integer.parseInt(nodeToRemoveStr);
		}
		// Iterate over all target rules
		for (int index = 0; index < lastIndex; index++) {
			if (index != nodeToRemove) {
				String column = form.getColumnAndType(index);
				if (column.contains("#")) {
					column = column.substring(0, column.indexOf('#'));
				}
				String type = "unknownType";
				if ("CURRENT_TIMESTAMP".equalsIgnoreCase(column)) {
        			type = DbColumnType.GENERIC_TYPE_DATE;
				} else if (PseudoColumn.INTERVAL_MAILING.isThisPseudoColumn(column)) {
        			type = "INTERVAL_MAILING";
        		} else {
					try {
						type = columnInfoService.getColumnInfo(AgnUtils.getCompanyID(request), column).getDataType();
					} catch (Exception e) {
						logger.error("Cannot find fieldtype for companyId " + AgnUtils.getCompanyID(request) + " and column '" + column + "'", e);
					}
        		}

				form.setColumnName(index, column);

				if (type.equalsIgnoreCase(DbColumnType.GENERIC_TYPE_VARCHAR) || type.equalsIgnoreCase("VARCHAR2") || type.equalsIgnoreCase("CHAR")) {
					form.setValidTargetOperators(index, ConditionalOperator.getValidOperatorsForString());
					form.setColumnType(index, TargetForm.COLUMN_TYPE_STRING);
				} else if (type.equalsIgnoreCase(DbColumnType.GENERIC_TYPE_INTEGER) || type.equalsIgnoreCase("DOUBLE") || type.equalsIgnoreCase("NUMBER")) {
					form.setValidTargetOperators(index, ConditionalOperator.getValidOperatorsForNumber());
					form.setColumnType(index, TargetForm.COLUMN_TYPE_NUMERIC);
				} else if (type.equalsIgnoreCase(DbColumnType.GENERIC_TYPE_DATE) || type.equalsIgnoreCase(DbColumnType.GENERIC_TYPE_DATETIME)) {
					form.setValidTargetOperators(index, ConditionalOperator.getValidOperatorsForDate());
					form.setColumnType(index, TargetForm.COLUMN_TYPE_DATE);
				} else if (type.equalsIgnoreCase("INTERVAL_MAILING")) {
					form.setValidTargetOperators(index, ConditionalOperator.getValidOperatorsForMailingOperators());
					form.setColumnType(index, TargetForm.COLUMN_TYPE_INTERVAL_MAILING);
				}
			} else {
				if (removeIndex != -1) {
					throw new RuntimeException("duplicate remove??? (removeIndex = " + removeIndex + ", index = " + index + ")");
				}
				removeIndex = index;
			}
		}

		if (removeIndex != -1) {
			form.removeRule(removeIndex);
			return true;
		} else {
			return false;
		}
	}

	private int createRulesFromBasicSearch(RecipientForm form, int lastIndex) {
		if(!form.isAdvancedSearch()) {	// User is doing basic search
			final int firstnameConditionIndex = form.findConditionalIndex("FIRSTNAME");
			if(firstnameConditionIndex != -1) {
				form.removeRule(firstnameConditionIndex);
				lastIndex--;
			}
			
			if (StringUtils.isNotBlank(form.getSearchFirstName())) {
				form.setColumnAndType(lastIndex, "FIRSTNAME");
				form.setChainOperator(lastIndex, form.getChainOperatorNew());
				form.setParenthesisOpened(lastIndex, form.getParenthesisOpenedNew());
				form.setPrimaryOperator(lastIndex, 5);
				form.setPrimaryValue(lastIndex, StringUtils.trim(form.getSearchFirstName()));
				form.setParenthesisClosed(lastIndex, form.getParenthesisClosedNew());
				form.setDateFormat(lastIndex, form.getDateFormatNew());
				form.setSecondaryOperator(lastIndex, form.getSecondaryOperatorNew());
				form.setSecondaryValue(lastIndex, form.getSecondaryValueNew());
				lastIndex++;
			}
			
	
			
			final int lastnameConditionIndex = form.findConditionalIndex("LASTNAME");
			if(lastnameConditionIndex != -1) {
				form.removeRule(lastnameConditionIndex);
				lastIndex--;
			}
			
			if (StringUtils.isNotBlank(form.getSearchLastName())) {
				form.setColumnAndType(lastIndex, "LASTNAME");
				form.setChainOperator(lastIndex, form.getChainOperatorNew());
				form.setParenthesisOpened(lastIndex, form.getParenthesisOpenedNew());
				form.setPrimaryOperator(lastIndex, 5);
				form.setPrimaryValue(lastIndex, StringUtils.trim(form.getSearchLastName()));
				form.setParenthesisClosed(lastIndex, form.getParenthesisClosedNew());
				form.setDateFormat(lastIndex, form.getDateFormatNew());
				form.setSecondaryOperator(lastIndex, form.getSecondaryOperatorNew());
				form.setSecondaryValue(lastIndex, form.getSecondaryValueNew());
				lastIndex++;
			}
	
			
			final int emailConditionIndex = form.findConditionalIndex("EMAIL");
			if(emailConditionIndex != -1) {
				form.removeRule(emailConditionIndex);
				lastIndex--;
			}

			if(StringUtils.isNotBlank(form.getSearchEmail())) {
				form.setColumnAndType(lastIndex, "EMAIL");
				form.setChainOperator(lastIndex, form.getChainOperatorNew());
				form.setParenthesisOpened(lastIndex, form.getParenthesisOpenedNew());
				form.setPrimaryOperator(lastIndex, 5);
				form.setPrimaryValue(lastIndex, StringUtils.trim(form.getSearchEmail()));
				form.setParenthesisClosed(lastIndex, form.getParenthesisClosedNew());
				form.setDateFormat(lastIndex, form.getDateFormatNew());
				form.setSecondaryOperator(lastIndex, form.getSecondaryOperatorNew());
				form.setSecondaryValue(lastIndex, form.getSecondaryValueNew());
				lastIndex++;
			}
		}
		
		return lastIndex;
	}
	

    /**
     * Compare existed and new recipient data and write changes in user log
     *
     * @param aForm the form passed from the jsp
     * @param data existed recipient data
     * @param admin Admin
     */
    protected void writeRecipientChangesLog(Map<String, Object> data, RecipientForm aForm, ComAdmin admin) {
        try {
            ArrayList<String> handledKeys = new ArrayList<>();
            String recipientId = (String) data.get(COLUMN_CUSTOMER_ID);
            Map<String, Object> column = aForm.getColumnMap();

            //Log salutation changes
            int existedGender = Integer.valueOf((String) data.get(COLUMN_GENDER));
            handledKeys.add(COLUMN_GENDER);
            int newGender = aForm.getGender();
            if (existedGender != aForm.getGender()) {
				writeRecipientChangeLog(admin, aForm, "Salutation changed from " +
						getSalutationById(existedGender) + " to " + getSalutationById(newGender));
            }

            //Log title changes
            String existedTitle = (String) data.get(COLUMN_TITLE);
            handledKeys.add(COLUMN_TITLE);
            String newTitle = aForm.getTitle();
            if (AgnUtils.stringValueChanged(existedTitle, newTitle)) {
				if (StringUtils.isEmpty(existedTitle)) {
					if (StringUtils.isNotEmpty(newTitle)) {
						writeRecipientChangeLog(admin, aForm, "Title " + newTitle + " added");
					}
				} else {
					if (StringUtils.isEmpty(newTitle)) {
						writeRecipientChangeLog(admin, aForm, "Title " + existedTitle + " removed");
					} else {
						writeRecipientChangeLog(admin, aForm, "Title changed from " + existedTitle + " to " + newTitle);
					}
				}
            }

            //Log first name changes
            String existedFirstName = (String) data.get(COLUMN_FIRSTNAME);
            handledKeys.add(COLUMN_FIRSTNAME);
            String newFirstName = aForm.getFirstname();
            if (AgnUtils.stringValueChanged(existedFirstName, newFirstName)) {
				if (StringUtils.isEmpty(existedFirstName)) {
					if (StringUtils.isNotEmpty(newFirstName)) {
						writeRecipientChangeLog(admin, aForm, "First name " + newFirstName + " added");
					}
				} else {
					if (StringUtils.isEmpty(newFirstName)) {
						writeRecipientChangeLog(admin, aForm, "First name " + existedFirstName + " removed");
					} else {
						writeRecipientChangeLog(admin, aForm, "First name changed from " + existedFirstName + " to " + newFirstName);
					}
				}
            }

            //Log last name changes
            String existedLastName = (String) data.get(COLUMN_LASTNAME);
            handledKeys.add(COLUMN_LASTNAME);
            String newLastName = aForm.getLastname();
            if (AgnUtils.stringValueChanged(existedLastName, newLastName)) {
				if (StringUtils.isEmpty(existedLastName)) {
					if (StringUtils.isNotEmpty(newLastName)) {
						writeRecipientChangeLog(admin, aForm, "Last name " + newLastName + " added");
					}
				} else {
					if (StringUtils.isEmpty(newLastName)) {
						writeRecipientChangeLog(admin, aForm, "Last name " + existedLastName + " removed");
					} else {
						writeRecipientChangeLog(admin, aForm, "Last name changed from " + existedLastName + " to " + newLastName);
					}
				}
            }

            //Log email changes
            String existedEmail = (String) data.get(COLUMN_EMAIL);
            handledKeys.add(COLUMN_EMAIL);
            String newEmail = aForm.getEmail();
            if (AgnUtils.stringValueChanged(existedEmail, newEmail)) {
				writeRecipientChangeLog(admin, aForm, "Email changed from " + existedEmail + " to " + newEmail);
            }

            //Log mailType changes
            int existedMailType = Integer.valueOf((String)  data.get(COLUMN_MAILTYPE));
            handledKeys.add(COLUMN_MAILTYPE);
            int newMailType = aForm.getMailtype();
            if (existedMailType != newMailType) {
				writeRecipientChangeLog(admin, aForm, "Mailtype changed from " + getMailFormatById(existedMailType) + " to " + getMailFormatById(newMailType));
            }

            //Log additional data changes
            for (Entry<String, Object> entry : column.entrySet()) {
                if (!handledKeys.contains(entry.getKey())) {
                	String newValue = (String) entry.getValue();
                    newValue = newValue == null ? null : newValue.replaceAll("\\s","");
                    String existedValue = (String) data.get(entry.getKey());
                    existedValue = existedValue == null ? null : existedValue.replaceAll("\\s","");
                    StringBuilder description = new StringBuilder();

					if (AgnUtils.stringValueChanged(existedValue, newValue)) {
						if (StringUtils.isEmpty(existedValue)) {
							description.append("Recipient ");
							description.append(entry.getKey());
							description.append(" ");
							description.append(RecipientUtils.cutRecipientDescription(newValue));
							description.append(" added");
							writeRecipientChangeLog(admin, aForm, description.toString());
						} else if (StringUtils.isEmpty(newValue)) {
							description.append("Recipient ");
							description.append(entry.getKey());
							description.append(" ");
							description.append(RecipientUtils.cutRecipientDescription(existedValue));
							description.append(" removed");
							writeRecipientChangeLog(admin, aForm, description.toString());
						} else {
							description.append("Recipient ");
							description.append(entry.getKey());
							description.append(" changed from ");
							description.append(RecipientUtils.cutRecipientDescription(existedValue));
							description.append(" to ");
							description.append(RecipientUtils.cutRecipientDescription(newValue));
							writeRecipientChangeLog(admin, aForm, description.toString());
						}
                    }
                }
            }
            if (logger.isInfoEnabled()){
                logger.info("save recipient: save recipient " + recipientId);
            }
        } catch (NumberFormatException e) {
            if (logger.isInfoEnabled()){
                logger.error("Log Recipient changes error" + e);
            }
        }
    }

    private ActionForward loadDataAndForwardToView(final HttpServletRequest request, final ActionMapping actionMapping, final RecipientForm form) {
        form.setAction(RecipientAction.ACTION_SAVE);
        AgnUtils.setAdminDateTimeFormatPatterns(request);
        final ComAdmin admin = AgnUtils.getAdmin(request);
        try {
            loadRecipient(form, admin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        defineMailinglistAttributes(request, admin);
        return actionMapping.findForward("view");
    }

    private ActionForward forwardToList(final HttpServletRequest request, final ActionMapping actionMapping, final RecipientForm form) {
        form.setAction(RecipientAction.ACTION_LIST);
        AgnUtils.setAdminDateTimeFormatPatterns(request);
        return actionMapping.findForward("list");
    }

    private ActionForward saveRecipientAndGetDestination(HttpServletRequest request, ActionMessages errors,
                                                         ActionMessages messages, ActionMapping mapping, RecipientForm form,
                                                         GetSuccessfulDestinationFunction successfulDestinationFunction) throws Exception {
        if (StringUtils.isNotEmpty(request.getParameter("cancel.x"))) {
            return forwardToList(request, mapping, form);
        }

        final ComAdmin admin = AgnUtils.getAdmin(request);
        validateForm(admin, form, errors);
        if (errors.isEmpty()) {
            final ActionForward saveForward = saveRecipient(form, request, errors, mapping);
            if(saveForward != null) {
            	saveErrors(request, errors);
            	return saveForward;
			}
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
            return successfulDestinationFunction.apply(request, mapping, form);
        } else {
            saveErrors(request, errors);
            return mapping.findForward("messages");
        }
    }

    private int saveTargetGroup(RecipientForm aForm, ComAdmin admin, ActionMessages errors, ActionMessages validationErrors) throws Exception {
        final int errorCount = errors.size();
        if (!aForm.checkParenthesisBalance()) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.bracketbalance"));
        }
        if (aForm.getTargetShortname() != null && aForm.getTargetShortname().length() < 1 ) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.name.too.short"));
        }
        if (aForm.getNumTargetNodes() == 0) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.norule"));
        }
        if (errorCount != errors.size()) {
            return 0;
        }
        
        final ComTarget newTarget = targetFactory.newTarget();
        newTarget.setId(0);
        newTarget.setTargetName(aForm.getTargetShortname());
        newTarget.setTargetDescription(aForm.getTargetDescription());
        newTarget.setCompanyID(admin.getCompanyID());
        
    	final String eqlFromForm = recipientQueryBuilder.createEqlFromForm(aForm, admin.getCompanyID());
    	newTarget.setEQL(eqlFromForm);

		final ComTarget target = loadTargetGroupOrNull(aForm.getTargetID(), admin.getCompanyID());
		return targetService.saveTarget(admin, newTarget, target, validationErrors, this::writeUserActivityLog);
    }
    
    private final ComTarget loadTargetGroupOrNull(final int targetId, final int companyId) {
    	try {
    		return targetService.getTargetGroup(targetId, companyId);
    	} catch(final UnknownTargetGroupIdException e) {
    		return null;
    	}
    }

	protected void writeRecipientChangeLog(ComAdmin admin, RecipientForm form, String description) {
		writeRecipientChangeLog(admin, form.getRecipientID(), form.getFirstname(), form.getLastname(), form.getEmail(), description);
	}
	
	protected void writeRecipientChangeLog(ComAdmin admin, Map<String, Object> data, String description) {
		writeUserActivityLog(admin, "edit recipient", RecipientUtils.getRecipientDescription(data) + ". " + description);
	}
	
	protected void writeRecipientChangeLog(ComAdmin admin, int id, String firstName, String lastName, String email, String description) {
		writeUserActivityLog(admin, "edit recipient", RecipientUtils.getRecipientDescription(id, firstName, lastName, email) + ". " + description);
	}
	
	protected String getRecipientDescription(RecipientForm form) {
		return RecipientUtils.getRecipientDescription(form.getRecipientID(), form.getFirstname(), form.getLastname(), form.getEmail());
	}
	
	protected String getRecipientDescription(Recipient cust) {
		return RecipientUtils.getRecipientDescription(cust.getCustomerID(), cust.getFirstname(), cust.getLastname(), cust.getEmail());
	}

 
	//------------- setters for bindings

    @Required
	public void setFutureHolder(Map<String, Future<PaginatedListImpl<DynaBean>>> futureHolder) {
		this.futureHolder = futureHolder;
	}

    @Required
	public void setMailinglistService(ComMailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}

    @Required
	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}

    @Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

    @Required
	public void setWorkerExecutorService(ExecutorService workerExecutorService) {
		this.workerExecutorService = workerExecutorService;
	}

    @Required
	public void setTargetFactory(TargetFactory targetFactory) {
		this.targetFactory = targetFactory;
	}

	@Required
	public void setRecipientQueryBuilder(RecipientQueryBuilder recipientQueryBuilder) {
		this.recipientQueryBuilder = recipientQueryBuilder;
	}

    @Required
	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}

    @Required
	public void setRecipientFactory(RecipientFactory recipientFactory) {
		this.recipientFactory = recipientFactory;
	}

    @Required
	public void setBindingEntryFactory(BindingEntryFactory bindingEntryFactory) {
		this.bindingEntryFactory = bindingEntryFactory;
	}

    /**
     * Set service dealing with target groups.
     *
     * @param targetService service dealing with target groups
     */
    @Required
    public void setTargetService(ComTargetService targetService) {
        this.targetService = targetService;
    }
    
    @Required
    public void setEqlFacade(EqlFacade eqlFacade) {
    	this.eqlFacade = eqlFacade;
    }

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setWebStorage(WebStorage webStorage) {
		this.webStorage = webStorage;
	}
	
	@Required
	public void setBlacklistService(BlacklistService blacklistService) {
		this.blacklistService = blacklistService;
	}
	
	@Required
	public void setRecipientService(RecipientService recipientService) {
		this.recipientService = recipientService;
	}

	@Required
    public void setDeliveryService(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @FunctionalInterface
    private interface GetSuccessfulDestinationFunction {
        ActionForward apply(HttpServletRequest request, ActionMapping actionMapping, RecipientForm recipientForm);
    }
}
