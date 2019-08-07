/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.ProfileField;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.factory.BindingEntryFactory;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.RecipientBeanQueryWorker;
import org.agnitas.service.RecipientQueryBuilder;
import org.agnitas.service.WebStorage;
import org.agnitas.target.TargetFactory;
import org.agnitas.target.TargetNodeFactory;
import org.agnitas.target.TargetRepresentationFactory;
import org.agnitas.target.impl.TargetNodeDate;
import org.agnitas.target.impl.TargetNodeIntervalMailing;
import org.agnitas.target.impl.TargetNodeNumeric;
import org.agnitas.target.impl.TargetNodeString;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.util.MapUtils;

/**
 * Handles all actions on recipients profile.
 */
public class RecipientAction extends StrutsActionBase {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(RecipientAction.class);

	public static final String COLUMN_GENDER = "gender";
	public static final String COLUMN_FIRSTNAME = "firstname";
	public static final String COLUMN_LASTNAME = "lastname";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_EMAIL = "email";
	public static final String COLUMN_MAILTYPE = "mailtype";
	public static final String COLUMN_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_LATEST_DATASOURCE_ID = "latest_datasource_id";

    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_CREATION_DATE = "creation_date";
    public static final String COLUMN_LASTOPEN_DATE = "lastopen_date";
    public static final String COLUMN_LASTCLICK_DATE = "lastclick_date";
    public static final String COLUMN_LASTSEND_DATE = "lastsend_date";

	public static final int MAX_SELECTED_FIELDS_COUNT = 8;

    public static int COLUMN_GENDER_ORDER = 1;
    public static int COLUMN_FIRSTNAME_ORDER = 2;
    public static int COLUMN_LASTNAME_ORDER = 3;
    public static int COLUMN_OTHER_ORDER = 4;

	public static final String FUTURE_TASK = "GET_RECIPIENT_LIST";
	public static final int ACTION_SEARCH = ACTION_LAST + 1;
	public static final int ACTION_OVERVIEW_START = ACTION_LAST + 2;
	public static final int ACTION_VIEW_WITHOUT_LOAD = ACTION_LAST + 3;
	public static final int ORG_ACTION_LAST = ACTION_LAST + 3;

	protected Map<String, Future<PaginatedListImpl<DynaBean>>> futureHolder = null;
	protected ComMailinglistService mailinglistService;
	protected MailingDao mailingDao;

	/** DAO accessing target groups. */
	protected ComTargetDao targetDao;
	protected ComRecipientDao recipientDao;
	protected BlacklistService blacklistService;
	protected TargetRepresentationFactory targetRepresentationFactory;
	protected TargetNodeFactory targetNodeFactory;
	protected ExecutorService workerExecutorService;
	protected RecipientQueryBuilder recipientQueryBuilder;
	protected ColumnInfoService columnInfoService;
	protected RecipientFactory recipientFactory;
	protected BindingEntryFactory bindingEntryFactory;
	protected DataSource dataSource;
	protected ConfigService configService;
    protected TargetFactory targetFactory;
    protected ComTargetService targetService;
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
	 * @param req
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
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		// Validate the request parameters specified by the user
		RecipientForm aForm = null;
		ActionMessages errors = new ActionErrors();
        ActionMessages rulesValidationErrors = new ActionMessages();
		ActionMessages messages = new ActionMessages();
		ActionForward destination = null;

		if (form != null) {
			aForm = (RecipientForm) form;
		} else {
			aForm = new RecipientForm();
		}
		if (AgnUtils.parameterNotEmpty(req, "resetSearch")) {
			aForm.resetSearch();
		}
		this.updateRecipientFormProperties(req, aForm);

		if (aForm.getDelete().isSelected()) {
			aForm.setAction(ACTION_CONFIRM_DELETE);
		}

		try {
			switch (aForm.getAction()) {
			case ACTION_LIST:
                if (aForm.isNeedSaveTargetGroup()) {
                	if (!targetService.checkIfTargetNameIsValid(aForm.getTargetShortname())) {
            			errors = new ActionErrors();
            			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.namenotallowed"));
            			saveErrors(req, errors);
            		} else if (targetService.checkIfTargetNameAlreadyExists(AgnUtils.getCompanyID(req), aForm.getTargetShortname(), 0)) {
            			errors = new ActionErrors();
            			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.namealreadyexists"));
            			saveErrors(req, errors);
            		} else if (saveTargetGroup(aForm, req, errors, rulesValidationErrors) > 0) {
                       messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                    } else {
                       errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.saving"));
                    }

                    aForm.setNeedSaveTargetGroup(false);
                }

            	AgnUtils.setAdminDateTimeFormatPatterns(req);

				destination = mapping.findForward("list");

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
				break;

			case ACTION_VIEW:
				if (req.getParameter("recipientID") != null) {
					loadRecipient(aForm, req);
					aForm.setAction(RecipientAction.ACTION_SAVE);
				} else {
					loadDefaults(aForm, req);
					aForm.setAction(RecipientAction.ACTION_NEW);
				}
				defineMailinglistAttributes(req, AgnUtils.getAdmin(req));
				
				AgnUtils.setAdminDateTimeFormatPatterns(req);
				
				destination = mapping.findForward("view");
				break;

			case ACTION_SAVE:
				if (req.getParameter("cancel.x") == null) {
					if (StringUtils.isNotBlank(aForm.getEmail()) && !AgnUtils.isEmailValid(aForm.getEmail())) {
		                errors.add("email",new ActionMessage("error.invalid.email"));
		            }
		            if (aForm.getTitle().length() > 100) {
		                errors.add("title", new ActionMessage("error.recipient.title.tooLong"));
		            }
		            if (aForm.getFirstname().length() > 100) {
		                errors.add("firstname", new ActionMessage("error.recipient.firstname.tooLong"));
		            }
		            if (aForm.getLastname().length() > 100) {
		                errors.add("lastname", new ActionMessage("error.recipient.lastname.tooLong"));
		            }
					
					if (errors.isEmpty()) {
						saveRecipient(aForm, req);
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
						aForm.setAction(RecipientAction.ACTION_LIST);
	                	AgnUtils.setAdminDateTimeFormatPatterns(req);
						destination = mapping.findForward("list");
					} else {
						aForm.setAction(RecipientAction.ACTION_SAVE);
						defineMailinglistAttributes(req, AgnUtils.getAdmin(req));
						AgnUtils.setAdminDateTimeFormatPatterns(req);
						destination = mapping.findForward("view");
					}
				} else {
					aForm.setAction(RecipientAction.ACTION_LIST);
                	AgnUtils.setAdminDateTimeFormatPatterns(req);
					destination = mapping.findForward("list");
				}
				break;

			case ACTION_NEW:
				if (req.getParameter("cancel.x") == null) {
					aForm.setRecipientID(0);
					if (saveRecipient(aForm, req)) {
						aForm.setAction(RecipientAction.ACTION_LIST);
	                	AgnUtils.setAdminDateTimeFormatPatterns(req);
						destination = mapping.findForward("list");
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
					} else {
						errors.add("NewRecipient", new ActionMessage("error.subscriber.insert_in_db_error"));
						aForm.setAction(RecipientAction.ACTION_VIEW);
						AgnUtils.setAdminDateTimeFormatPatterns(req);
						destination = mapping.findForward("view");
					}
					defineMailinglistAttributes(req, AgnUtils.getAdmin(req));
				} else {
                	AgnUtils.setAdminDateTimeFormatPatterns(req);
					destination = mapping.findForward("list");
				}
				break;

			case ACTION_CONFIRM_DELETE:
				loadRecipient(aForm, req);
				destination = mapping.findForward("delete");
				break;

			case ACTION_DELETE:
				if (req.getParameter("kill") != null) {
					deleteRecipient(aForm, req);
					aForm.setAction(RecipientAction.ACTION_LIST);
                	AgnUtils.setAdminDateTimeFormatPatterns(req);
					destination = mapping.findForward("list");
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
				}
				break;
			case ACTION_VIEW_WITHOUT_LOAD:
				defineMailinglistAttributes(req, AgnUtils.getAdmin(req));
				AgnUtils.setAdminDateTimeFormatPatterns(req);
				destination = mapping.findForward("view");
				aForm.setAction(RecipientAction.ACTION_SAVE);
				break;

			default:
				aForm.setAction(RecipientAction.ACTION_LIST);
            	AgnUtils.setAdminDateTimeFormatPatterns(req);
				destination = mapping.findForward("list");
				if (aForm.getColumnwidthsList() == null) {
					int lengthSelectedFields = aForm.getSelectedFields().length;
					aForm.setColumnwidthsList(getInitializedColumnWidthList(lengthSelectedFields + 1));
				}
			}
		} catch (Exception e) {
			logger.error("RecipientAction execute: " + e.getMessage(), e);
			// errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(Value.SupportEmergencyUrl)));
		}

		if (destination != null && "list".equals(destination.getName())) {
			try {
				Map<String, String> fieldsMap = getRecipientFieldsNames(AgnUtils.getCompanyID(req), aForm.getAdminId());
				Set<String> recipientDbColumns = fieldsMap.keySet();
				req.setAttribute("fieldsMap", fieldsMap);

				destination = mapping.findForward("loading");
				String key = FUTURE_TASK + "@" + req.getSession(false).getId();

				Future<PaginatedListImpl<DynaBean>> future = futureHolder.get(key);
				if (future == null) {
					future = getRecipientListFuture(req, aForm, recipientDbColumns);
					futureHolder.put(key, future);
				}

                //if we perform AJAX request (load next/previous page) we have to wait for preparing data
                if (HttpUtils.isAjax(req)) {
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
						int companyID = AgnUtils.getAdmin(req).getCompanyID();
						int adminID = AgnUtils.getAdmin(req).getAdminID();

						req.setAttribute("recipientList", resultingList);
						defineMailinglistAttributes(req, AgnUtils.getAdmin(req));
						req.setAttribute("targets", targetDao.getTargetLights(AgnUtils.getCompanyID(req)));
	                	
	                	AgnUtils.setAdminDateTimeFormatPatterns(req);

						destination = mapping.findForward("list");
						if (resultingList == null) {
							aForm.setDeactivatePagination(false);
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.errorneous_recipient_search"));
						} else {
							// check the max recipients for company and change visualisation if needed
							int maxRecipients = AgnUtils.getCompanyMaxRecipients(req);
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
				aForm.setDatePickerFormat(AgnUtils.getDatePickerFormatPattern(AgnUtils.getAdmin(req)));
			} catch (Exception e) {
				logger.error("recipientList: " + e, e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
				aForm.setError(true); // do not refresh when an error has been occurred
			}

			if (aForm.isDeactivatePagination()) {
				int maxRecipients = AgnUtils.getCompanyMaxRecipients(req);
				req.setAttribute("countOfRecipients", maxRecipients);
			}
		}

		// this is a hack for the recipient-search / recipient overview.
		if (destination != null && "list".equals(destination.getName())) {
			// check if we are in search-mode
			if (!aForm.isOverview()) {
				// check if it is the last element in filter
				if (aForm.getNumTargetNodes() == 0 && aForm.getListID() == 0 && aForm.getTargetID() == 0 && aForm.getUser_type().equals("E") && aForm.getUser_status() == 0) {
					aForm.setAction(7);
                	
                	AgnUtils.setAdminDateTimeFormatPatterns(req);
                	
					destination = mapping.findForward("search");
				}
			}
		}

		if (destination != null && "view".equals(destination.getName())) {
			req.setAttribute("isRecipientEmailInUseWarningEnabled", configService.getBooleanValue(ConfigValue.RecipientEmailInUseWarning, AgnUtils.getCompanyID(req)));
		}

		List<LightweightMailing> lightweightIntervalMailings = mailingDao.getLightweightIntervalMailings(AgnUtils.getCompanyID(req));
		req.setAttribute("interval_mailings", lightweightIntervalMailings);

        req.setAttribute("rulesValidationErrors", rulesValidationErrors);

		// Report any errors we have discovered back to the original form
		if (!errors.isEmpty()) {
			saveErrors(req, errors);
			// return new ActionForward(mapping.getForward());
		}

		// Report any message (non-errors) we have discovered
		if (!messages.isEmpty()) {
			saveMessages(req, messages);
		}

        aForm.setSaveTargetVisible(false);

        return destination;
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
	 * @param req
	 *            HTTP request
     * @throws Exception 
	 */
	protected void loadRecipient(RecipientForm aForm, HttpServletRequest req) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Loads recipient columns list for specific admin (identified by adminID property of form) and puts it to form.
	 * 
	 * @param aForm
	 *            form
	 * @param req
	 *            HTTP request
	 */
	protected void loadDefaults(RecipientForm aForm, HttpServletRequest req) {
		aForm.clearColumns();

		try {
			List<ProfileField> list = columnInfoService.getColumnInfos(AgnUtils.getCompanyID(req), aForm.getAdminId());
			for (ProfileField profileField : list) {
				if ("DATE".equalsIgnoreCase(profileField.getDataType()) && "sysdate".equalsIgnoreCase(profileField.getDefaultValue())) {
					GregorianCalendar calendar = new GregorianCalendar();
					aForm.setColumn(profileField.getColumn() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(calendar.get(GregorianCalendar.DAY_OF_MONTH)));
					aForm.setColumn(profileField.getColumn() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(calendar.get(GregorianCalendar.MONTH) + 1));
					aForm.setColumn(profileField.getColumn() + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(calendar.get(GregorianCalendar.YEAR)));
				} else {
					aForm.setColumn(profileField.getColumn(), profileField.getDefaultValue());
				}
			}
		} catch (Exception e) {
			// nothing to do
		}
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
	protected void saveBindings(RecipientForm recipientForm, HttpServletRequest request, boolean isNewRecipient) throws Exception {
		ComAdmin admin = AgnUtils.getAdmin(request);
		int companyID = AgnUtils.getCompanyID(request);
		int customerID = recipientForm.getRecipientID();
		Map<Integer, Map<Integer, BindingEntry>> bindings = recipientForm.getAllBindings();
		Map<Integer, Map<Integer, BindingEntry>> customerMailingLists = recipientDao.getAllMailingLists(customerID, companyID);

		for (Map<Integer, BindingEntry> mailing : bindings.values()) {
			for (Entry<Integer, BindingEntry> entry : mailing.entrySet()) {
				BindingEntry bindingEntry = entry.getValue();

				if (bindingEntry.getUserStatus() != 0) {
					bindingEntry.setCustomerID(customerID);
					BindingEntry existingBindingEntry;
					int newUserStatus = bindingEntry.getUserStatus();
					int mailingListId = bindingEntry.getMailinglistID();
					int existingUserStatus = 3;
					String existingUserType = "";
					String newUserType = bindingEntry.getUserType();

					try {
						if (customerMailingLists.get(mailingListId) != null) {
							existingBindingEntry = customerMailingLists.get(mailingListId).get(entry.getKey());
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
					if (!bindingEntry.saveBindingInDB(companyID, customerMailingLists, AgnUtils.getAdmin(request))) {
						logger.error("saveBindings: Binding could not be saved");
					}
				}
			}
		}
	}

	/**
	 * If customerID of aForm is not 0 - saves changed recipient to DB<br>
	 * If customerID is 0 - creates new recipient in DB (before that checks if max number of recipients is reached)<br>
	 * Recipient data is taken from form properties.<br>
	 * Also invokes method for saving recipient bindings to mailinglists.
	 * 
	 * @param aForm
	 *            form
	 * @param req
	 *            HTTP request
	 * @throws Exception 
	 */
	protected boolean saveRecipient(RecipientForm aForm, HttpServletRequest req) throws Exception {
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
		
		final Comparator<Map.Entry<String, String>> comparator = (o1, o2) -> {
            //Some columns should always be first
            int firstOrder = columnOrder(o1.getKey());
            int secondOrder = columnOrder(o2.getKey());

            if(firstOrder == COLUMN_OTHER_ORDER && secondOrder == COLUMN_OTHER_ORDER){
                return o1.getValue().compareToIgnoreCase(o2.getValue());
            }

			return firstOrder - secondOrder;
		};

		columnInfoMap.entrySet().forEach(entry -> fieldsMap.put(entry.getKey(), entry.getValue().getShortname()));

		MapUtils.reorderLinkedHashMap(fieldsMap, comparator);

		return fieldsMap;
	}

	private int columnOrder(String columnName){
        if(COLUMN_GENDER.equals(columnName)){
            return COLUMN_GENDER_ORDER;
        }
        if(COLUMN_FIRSTNAME.equals(columnName)){
            return COLUMN_FIRSTNAME_ORDER;
        }
        if(COLUMN_LASTNAME.equals(columnName)){
            return COLUMN_LASTNAME_ORDER;
        }
        return COLUMN_OTHER_ORDER;
    }

	/**
	 * Updates customer bindings with the data taken from request
	 * 
	 * @param cust
	 *            recipient bean
	 * @param req
	 *            HTTP request holding parameters for binding entries
	 */
	@Deprecated
	public boolean updateCustBindingsFromAdminReq(Recipient cust, HttpServletRequest req) {
		String newKey = null;
		String aParam = null;
		int aMailinglistID;
		int oldSubStatus, newSubStatus;
		String tmpUT = null;
		String tmpOrgUT = null;
		BindingEntry bindingEntry = bindingEntryFactory.newBindingEntry();

		for (String key : req.getParameterMap().keySet()) {
			if (key.startsWith("AGN_0_ORG_MT")) {
				oldSubStatus = Integer.parseInt(req.getParameter(key));
				aMailinglistID = Integer.parseInt(key.substring(12));
				newKey = "AGN_0_MTYPE" + aMailinglistID;
				aParam = req.getParameter(newKey);
				if (aParam != null) {
					newSubStatus = 1;
				} else {
					newSubStatus = 0;
				}

				newKey = "AGN_0_MLUT" + aMailinglistID;
				tmpUT = req.getParameter(newKey);
				newKey = "AGN_0_ORG_UT" + aMailinglistID;
				tmpOrgUT = req.getParameter(newKey);

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
					if (bindingEntry.updateBindingInDB(cust.getCompanyID()) == false) {
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
	 * @param req
	 *            HTTP request
	 */
	protected void deleteRecipient(RecipientForm aForm, HttpServletRequest req) {
		int companyID = AgnUtils.getCompanyID(req);
		recipientDao.deleteCustomerDataFromDb(companyID, aForm.getRecipientID());
		writeUserActivityLog(AgnUtils.getAdmin(req), "delete recipient", getRecipientDescription(aForm));
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

		SqlPreparedStatementManager sqlStatementManagerForDataSelect = recipientQueryBuilder.getSQLStatement(request, aForm, targetRepresentationFactory, targetNodeFactory);
		String selectDataStatement = sqlStatementManagerForDataSelect.getPreparedSqlString().replaceAll("cust[.]bind", "bind").replace("lower(cust.email)", "cust.email");
		if (logger.isInfoEnabled()) {
			logger.info("Recipient Select data SQL statement: " + selectDataStatement);
		}

		return workerExecutorService.submit(new RecipientBeanQueryWorker(
			recipientDao,
			AgnUtils.getCompanyID(request),
			recipientDbColumns,
			selectDataStatement,
			sqlStatementManagerForDataSelect.getPreparedSqlParameters(),
			sort,
			AgnUtils.sortingDirectionToBoolean(direction),
			pageNumber,
			rownums
		));
	}

	private boolean updateRecipientFormProperties(HttpServletRequest req, RecipientForm form) {
		cleanRulesForBasicSearch(form);

		int lastIndex = form.getNumTargetNodes();
		int removeIndex = -1;

		// If "add" was clicked, add new rule
		if (AgnUtils.parameterNotEmpty(req, "addTargetNode") || (AgnUtils.parameterNotEmpty(req, "Update") && !StringUtils.isEmpty(form.getPrimaryValueNew()))) {
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
		String nodeToRemoveStr = req.getParameter("targetNodeToRemove");
		if (AgnUtils.parameterNotEmpty(req, "targetNodeToRemove")) {
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
        			type = "DATE";
        		} else if (TargetNodeIntervalMailing.PSEUDO_COLUMN_NAME.equalsIgnoreCase(column)) {
        			type = "INTERVAL_MAILING";
        		} else {
					try {
						type = columnInfoService.getColumnInfo(AgnUtils.getCompanyID(req), column).getDataType();
					} catch (Exception e) {
						logger.error("Cannot find fieldtype for companyId " + AgnUtils.getCompanyID(req) + " and column '" + column + "'", e);
					}
        		}

				form.setColumnName(index, column);

				if (type.equalsIgnoreCase("VARCHAR") || type.equalsIgnoreCase("VARCHAR2") || type.equalsIgnoreCase("CHAR")) {
					form.setValidTargetOperators(index, TargetNodeString.getValidOperators());
					form.setColumnType(index, TargetForm.COLUMN_TYPE_STRING);
				} else if (type.equalsIgnoreCase("INTEGER") || type.equalsIgnoreCase("DOUBLE") || type.equalsIgnoreCase("NUMBER")) {
					form.setValidTargetOperators(index, TargetNodeNumeric.getValidOperators());
					form.setColumnType(index, TargetForm.COLUMN_TYPE_NUMERIC);
				} else if (type.equalsIgnoreCase("DATE")) {
					form.setValidTargetOperators(index, TargetNodeDate.getValidOperators());
					form.setColumnType(index, TargetForm.COLUMN_TYPE_DATE);
				} else if (type.equalsIgnoreCase("INTERVAL_MAILING")) {
					form.setValidTargetOperators(index, TargetNodeIntervalMailing.getValidOperators());
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
			final int firstnameConditionIndex = conditionIndex("FIRSTNAME", form);
			if(firstnameConditionIndex != -1) {
				removeRuleFromForm(form, firstnameConditionIndex);
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
				form.setSearchFirstName(null);
				lastIndex++;
			}
			
	
			
			final int lastnameConditionIndex = conditionIndex("LASTNAME", form);
			if(lastnameConditionIndex != -1) {
				removeRuleFromForm(form, lastnameConditionIndex);
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
				form.setSearchLastName(null);
				lastIndex++;
			}
	
			
			final int emailConditionIndex = conditionIndex("EMAIL", form);
			if(emailConditionIndex != -1) {
				removeRuleFromForm(form, emailConditionIndex);
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
				form.setSearchEmail(null);
				lastIndex++;
			}
		}
		
		return lastIndex;
	}
	
	private static final void removeRuleFromForm(final RecipientForm form, final int ruleIndexToRemove) {
		form.removeRule(ruleIndexToRemove);
	}

	private void cleanRulesForBasicSearch(RecipientForm form) {
		if (StringUtils.isNotBlank(form.getSearchFirstName()) || StringUtils.isNotBlank(form.getSearchLastName()) || StringUtils.isNotBlank(form.getSearchEmail())) {
			for (int index = form.getNumTargetNodes(); index >= 0; index--) {
				form.removeRule(index);
			}
		}
	}

	private int conditionIndex (String field, final RecipientForm form) {
		final List<String> columns = form.getAllColumnsAndTypes();
		
		int index = 0;
		for (String column : columns) {
			if (column.equals(field)) {
				return index;
			}
			index++;
		}
		
		return -1;
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
							description.append(cutString(newValue));
							description.append(" added");
							writeRecipientChangeLog(admin, aForm, description.toString());
						} else if (StringUtils.isEmpty(newValue)) {
							description.append("Recipient ");
							description.append(entry.getKey());
							description.append(" ");
							description.append(cutString(existedValue));
							description.append(" removed");
							writeRecipientChangeLog(admin, aForm, description.toString());
						} else {
							description.append("Recipient ");
							description.append(entry.getKey());
							description.append(" changed from ");
							description.append(cutString(existedValue));
							description.append(" to ");
							description.append(cutString(newValue));
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

    @Required
	public void setFutureHolder(Map<String, Future<PaginatedListImpl<DynaBean>>> futureHolder) {
		this.futureHolder = futureHolder;
	}

	public void setMailinglistService(ComMailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	/**
	 * Sets DAO accessing target groups.
	 * 
	 * @param targetDao DAO accessing target groups
	 */
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	public void setTargetRepresentationFactory(TargetRepresentationFactory targetRepresentationFactory) {
		this.targetRepresentationFactory = targetRepresentationFactory;
	}

	public void setTargetNodeFactory(TargetNodeFactory targetNodeFactory) {
		this.targetNodeFactory = targetNodeFactory;
	}

	public void setWorkerExecutorService(ExecutorService workerExecutorService) {
		this.workerExecutorService = workerExecutorService;
	}

	@Required
	public void setRecipientQueryBuilder(RecipientQueryBuilder recipientQueryBuilder) {
		this.recipientQueryBuilder = recipientQueryBuilder;
	}

	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}

	public void setRecipientFactory(RecipientFactory recipientFactory) {
		this.recipientFactory = recipientFactory;
	}

	public void setBindingEntryFactory(BindingEntryFactory bindingEntryFactory) {
		this.bindingEntryFactory = bindingEntryFactory;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

    private int saveTargetGroup(RecipientForm aForm, HttpServletRequest request, ActionMessages errors, ActionMessages validationErrors) throws Exception {
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
        ComAdmin admin = AgnUtils.getAdmin(request);
        ComTarget newTarget = targetFactory.newTarget();
        newTarget.setId(0);
        newTarget.setTargetName(aForm.getTargetShortname());
        newTarget.setTargetDescription(aForm.getTargetDescription());
        newTarget.setTargetStructure(recipientQueryBuilder.createTargetRepresentationFromForm(aForm, targetRepresentationFactory, targetNodeFactory, AgnUtils.getCompanyID(request)));
        newTarget.setEQL(eqlFacade.convertTargetRepresentationToEql(newTarget.getTargetStructure(), admin.getCompanyID()));
        newTarget.setCompanyID(admin.getCompanyID());

		ComTarget target = targetDao.getTarget(aForm.getTargetID(), admin.getCompanyID());
		return targetService.saveTarget(admin, newTarget, target, validationErrors, this::writeUserActivityLog);
    }

	protected CaseInsensitiveMap<String, Object> getRecipientDataForDescription(int companyId, int recipientId) {
		Set<String> columns = new HashSet<>();
		columns.add(COLUMN_CUSTOMER_ID);
		columns.add(COLUMN_EMAIL);
		columns.add(COLUMN_FIRSTNAME);
		columns.add(COLUMN_LASTNAME);
		
		return recipientDao.getCustomerDataFromDb(companyId, recipientId, columns);
	}

	protected void writeRecipientChangeLog(ComAdmin admin, RecipientForm form, String description) {
		writeRecipientChangeLog(admin, form.getRecipientID(), form.getFirstname(), form.getLastname(), form.getEmail(), description);
	}

	protected void writeRecipientChangeLog(ComAdmin admin, Map<String, Object> data, String description) {
		writeUserActivityLog(admin, "edit recipient", getRecipientDescription(data) + ". " + description);
	}

	protected void writeRecipientChangeLog(ComAdmin admin, int id, String firstName, String lastName, String email, String description) {
		writeUserActivityLog(admin, "edit recipient", getRecipientDescription(id, firstName, lastName, email) + ". " + description);
	}

	protected String getRecipientDescription(RecipientForm form) {
		return getRecipientDescription(form.getRecipientID(), form.getFirstname(), form.getLastname(), form.getEmail());
	}

	protected String getRecipientDescription(Recipient cust) {
		return getRecipientDescription(cust.getCustomerID(), cust.getFirstname(), cust.getLastname(), cust.getEmail());
	}

	protected String getRecipientDescription(Map<String, Object> data) {
		final int customerId = NumberUtils.toInt((String) data.get(COLUMN_CUSTOMER_ID), 0);
		final String firstName = (String) data.get(COLUMN_FIRSTNAME);
		final String lastName = (String) data.get(COLUMN_LASTNAME);
		final String email = (String) data.get(COLUMN_EMAIL);
		return getRecipientDescription(customerId, firstName, lastName, email);
	}

	protected String getRecipientDescription(int id, String firstName, String lastName, String email) {
		final boolean hasFirstName = StringUtils.isNotBlank(firstName);
		final boolean hasLastName = StringUtils.isNotBlank(lastName);
		if (hasFirstName || hasLastName) {
			if (hasFirstName && hasLastName) {
				return firstName + " " + lastName + " (" + id + ")";
			} else {
				if (hasFirstName) {
					return firstName + " (" + id + ")";
				} else {
					return lastName + " (" + id + ")";
				}
			}
		} else {
			return email + " (" + id + ")";
		}
	}

    public void setTargetFactory(TargetFactory targetFactory) {
        this.targetFactory = targetFactory;
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
	
	/**
     *  If string length is more than 500 characters cut it and add "..." in the end.
     *
     * @param string recipient type letter
     * @return cut string
     */
    private String cutString(String string){
        try {
            int MAX_DESCRIPTION_LENGTH = 500;
            return StringUtils.abbreviate(string, MAX_DESCRIPTION_LENGTH);
        } catch (IllegalArgumentException e) {
            logger.error("RecipientAction.cutDescription: " + e, e);
            return string;
        }
    }
}
