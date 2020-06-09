/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.SalutationEntry;
import org.agnitas.beans.Title;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.beans.impl.TitleImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.SalutationListQueryWorker;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComTitleDao;

public final class SalutationAction extends StrutsActionBase {
	private static final transient Logger logger = Logger.getLogger(SalutationAction.class);

    public static final String FUTURE_TASK = "GET_SALUTATION_LIST";

	protected ConfigService configService;

	protected WebStorage webStorage;

	protected ComTitleDao titleDao;

	protected ExecutorService workerExecutorService = null;

	protected Map<String, Future<PaginatedListImpl<SalutationEntry>>> futureHolder;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setWebStorage(WebStorage webStorage) {
		this.webStorage = webStorage;
	}
	
	@Required
	public void setTitleDao(ComTitleDao titleDao) {
		this.titleDao = titleDao;
	}

	@Required
	public void setWorkerExecutorService(ExecutorService workerExecutorService) {
		this.workerExecutorService = workerExecutorService;
	}

	@Required
	public void setFutureHolder(Map<String, Future<PaginatedListImpl<SalutationEntry>>> futureHolder) {
		this.futureHolder = futureHolder;
	}

	// --------------------------------------------------------- Public Methods

	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it).
	 * Return an <code>ActionForward</code> instance describing where and how
	 * control should be forwarded, or <code>null</code> if the response has
	 * already been completed.
        * ACTION_LIST: calls a FutureHolder to get the list of entries.<br>
        * 		While FutureHolder is running destination is "loading".<br>
        * 		After FutureHolder is finished destination is "list".
        * <br><br>
        * ACTION_VIEW: loads data of chosen form of salutation into form,<br>
        *        forwards to form of salutation view page.
        * <br><br>
        * ACTION_SAVE:  save form of salutation in database.<br>
        *        calls a FutureHolder to get the list of entries.<br>
        * 	   While FutureHolder is running destination is "loading".<br>
        * 	   After FutureHolder is finished destination is "list".
        * <br><br>
        * ACTION_NEW: save new form of salutation in database, <br>
        *        calls a FutureHolder to get the list of entries.<br>
        * 	   While FutureHolder is running destination is "loading".<br>
        * 	   After FutureHolder is finished destination is "list".
        * <br><br>
        * ACTION_CONFIRM_DELETE: loads data of form of salutation into form, <br>
        *        forwards to jsp with question to confirm deletion.
        * <br><br>
        * ACTION_DELETE: delete the entry of form of salutation, <br>
        *        calls a FutureHolder to get the list of entries.<br>
        * 	   While FutureHolder is running destination is "loading".<br>
        * 	   After FutureHolder is finished destination is "list".
        * <br><br>
        * Any other ACTION_* calls a FutureHolder to get the list of entries.<br>
        * 	   While FutureHolder is running destination is "loading".<br>
        * 	   After FutureHolder is finished destination is "list".
        * <br><br>
	 * @param form
	 * @param req
	 * @param res
	 * @param mapping
	 *            The ActionMapping used to select this instance
	 * @exception IOException
	 *                if an input/output error occurs
	 * @exception ServletException
	 *                if a servlet exception occurs
	 * @return destination               vic
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest req, HttpServletResponse res)
			throws IOException, ServletException {

		// Validate the request parameters specified by the user
		SalutationForm aForm = null;
		ActionMessages errors = new ActionMessages();
		ActionMessages messages = new ActionMessages();
		ActionForward destination = null;

		if (!AgnUtils.isUserLoggedIn(req)) {
			return mapping.findForward("logon");
		}

		if (form != null) {
			aForm = (SalutationForm) form;
		} else {
			aForm = new SalutationForm();
		}

		if (logger.isInfoEnabled()) {
			logger.info("Action: " + aForm.getAction());
		}

		if (req.getParameter("delete.x") != null) {
			aForm.setAction(ACTION_CONFIRM_DELETE);
		}

		try {
			switch (aForm.getAction()) {
			case SalutationAction.ACTION_LIST:
                if (aForm.getColumnwidthsList() == null) {
                		aForm.setColumnwidthsList(getInitializedColumnWidthList(4));
                	}
				destination = prepareList(mapping, req, errors, aForm);
				break;

			case SalutationAction.ACTION_VIEW:
				boolean isSalutationLoaded = false;
				if (aForm.getSalutationID() != 0) {
					aForm.setAction(SalutationAction.ACTION_SAVE);
					isSalutationLoaded = loadSalutation(aForm, req);
				} else {
					aForm.setAction(SalutationAction.ACTION_NEW);
					aForm.setSalutationCompanyID(AgnUtils.getCompanyID(req));
					aForm.setSalutationID(0);
					isSalutationLoaded = true;
				}
				if (isSalutationLoaded) {
					destination = mapping.findForward("view");
				} else {
					destination = prepareList(mapping, req, errors, aForm);
				}
				break;
			case SalutationAction.ACTION_SAVE:
				if (AgnUtils.parameterNotEmpty(req, "save")) {
					boolean result = saveSalutation(aForm, req);
					destination = prepareList(mapping, req, errors, aForm);

					if (result) {
						// Show "changes saved"
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
					} else {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.salutation.change.permission"));
                		saveErrors(req, errors);
					}
				}
				break;

			case SalutationAction.ACTION_NEW:
				if (AgnUtils.parameterNotEmpty(req, "save")) {
					aForm.setSalutationID(0);
					boolean result = saveSalutation(aForm, req);
					aForm.setAction(SalutationAction.ACTION_SAVE);
					destination = prepareList(mapping, req, errors, aForm);

					if (result) {
						// Show "changes saved"
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
					} else {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.salutation.save"));
                		saveErrors(req, errors);
					}
				}
				break;

			case SalutationAction.ACTION_CONFIRM_DELETE:
				boolean result = checkSalutationForDeletion(aForm, req);
				if (!result) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.salutation.change.permission"));
            		saveErrors(req, errors);
            		aForm.setAction(SalutationAction.ACTION_LIST);
            		destination = prepareList(mapping, req, errors, aForm);
				} else {
					aForm.setShortname(aForm.getShortname());
					aForm.setAction(SalutationAction.ACTION_DELETE);
					destination = mapping.findForward("delete");
				}
				break;

			case SalutationAction.ACTION_DELETE:
				if (req.getParameter("kill") != null) {
					deleteSalutation(aForm, req);
					aForm.setAction(SalutationAction.ACTION_LIST);
					destination = prepareList(mapping, req, errors, aForm);

					// Show "changes saved"
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
				}
				break;

			default:
				aForm.setAction(SalutationAction.ACTION_LIST);
                if (aForm.getColumnwidthsList() == null) {
					aForm.setColumnwidthsList(getInitializedColumnWidthList(4));
				}
				destination = prepareList(mapping, req, errors, aForm);
			}

		} catch (Exception e) {
			logger.error("execute: " + e, e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
					"error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
		}

		// Report any errors we have discovered back to the original form
		if (!errors.isEmpty()) {
			saveErrors(req, errors);
		}

		// Report any message (non-errors) we have discovered
		if (!messages.isEmpty()) {
			saveMessages(req, messages);
		}

		return destination;
	}

	/**
	 * Loads salutation.
	 */
	protected boolean loadSalutation(SalutationForm aForm, HttpServletRequest request) {
		int companyID = AgnUtils.getCompanyID(request);
		int titleID = aForm.getSalutationID();
		Title title = titleDao.getTitle(titleID, companyID);

		Map<Integer, String> map = title.getTitleGender();

		aForm.setSalMale(map.get(Title.GENDER_MALE));
		aForm.setSalFemale(map.get(Title.GENDER_FEMALE));
		aForm.setSalUnknown(map.get(Title.GENDER_UNKNOWN));
		aForm.setSalMiss(map.get(Title.GENDER_MISS));
		aForm.setSalPractice(map.get(Title.GENDER_PRACTICE));
		aForm.setSalCompany(map.get(Title.GENDER_COMPANY));
		aForm.setShortname(title.getDescription());
		aForm.setSalutationCompanyID(title.getCompanyID());
		
		return true;
	}
	
	protected boolean checkSalutationForDeletion(SalutationForm aForm, HttpServletRequest request) {
		int companyID = AgnUtils.getCompanyID(request);
		int titleID = aForm.getSalutationID();
		Title title = titleDao.getTitle(titleID, companyID);

		if (title == null || (title.getCompanyID() < 1 && companyID != 1)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Saves salutation.
	 * @throws Exception 
	 */
	protected boolean saveSalutation(SalutationForm aForm, HttpServletRequest request) throws Exception {
		int companyID = AgnUtils.getCompanyID(request);
		int titelID = aForm.getSalutationID();
		Title title = titleDao.getTitle(titelID, companyID);
		
		if (title != null && title.getCompanyID() == 0 && companyID != 1) {
			return false;
		} else if (title == null && titelID > 0) {
			return false;
		}
		
		if (title == null) {
			title = new TitleImpl();
			title.setId(titelID);
			title.setCompanyID(companyID);
		}
		title.setDescription(aForm.getShortname());
		
		Map<Integer, String> map = new HashMap<>();
		map.put(Title.GENDER_MALE, aForm.getSalMale());
		map.put(Title.GENDER_FEMALE, aForm.getSalFemale());
		if (aForm.getSalUnknown() != null && aForm.getSalUnknown().length() > 0) {
			map.put(Title.GENDER_UNKNOWN, aForm.getSalUnknown());
		}
		if (aForm.getSalMiss() != null && aForm.getSalMiss().length() > 0) {
			map.put(Title.GENDER_MISS, aForm.getSalMiss());
		}
		if (aForm.getSalPractice() != null && aForm.getSalPractice().length() > 0) {
			map.put(Title.GENDER_PRACTICE, aForm.getSalPractice());
		}
		if (aForm.getSalCompany() != null && aForm.getSalCompany().length() > 0) {
			map.put(Title.GENDER_COMPANY, aForm.getSalCompany());
		}
		title.setTitleGender(map);
		
		titleDao.save(title);
		
		if (aForm.getSalutationID() == 0) {
			aForm.setSalutationID(title.getId());
		}
		return true;
	}

	/**
	 * Removes salutation.
	 */
	protected void deleteSalutation(SalutationForm aForm, HttpServletRequest request) {
		int companyID = AgnUtils.getCompanyID(request);
		int titleID = aForm.getSalutationID();
		Title title = titleDao.getTitle(titleID, companyID);
		if (title != null && title.getCompanyID() == companyID) {
			titleDao.delete(titleID, companyID);
		}
	}

    private ActionForward prepareList(ActionMapping mapping, HttpServletRequest req, ActionMessages errors, SalutationForm salutationForm) {
		ActionForward destination = null;
		ActionMessages messages;

        try {
			FormUtils.syncNumberOfRows(webStorage, WebStorage.SALUTATION_OVERVIEW, salutationForm);

            destination = mapping.findForward("loading");
            String key = FUTURE_TASK + "@" + req.getSession(false).getId();
            if (!futureHolder.containsKey(key)) {
            	Future<PaginatedListImpl<SalutationEntry>> salutationFuture = getSalutationlistFuture(req, salutationForm);
                futureHolder.put(key, salutationFuture);
            }
            if (futureHolder.containsKey(key) && futureHolder.get(key).isDone()) {
                req.setAttribute("salutationEntries", futureHolder.get(key).get());
                destination = mapping.findForward("list");
                futureHolder.remove(key);
                salutationForm.setRefreshMillis(RecipientForm.DEFAULT_REFRESH_MILLIS);
                messages = salutationForm.getMessages();

                if (messages != null && !messages.isEmpty()) {
                    saveMessages(req, messages);
                    salutationForm.setMessages(null);
                }
            } else {
                if (salutationForm.getRefreshMillis() < 1000) { // raise the refresh time
                    salutationForm.setRefreshMillis(salutationForm.getRefreshMillis() + 50);
                }
                salutationForm.setError(false);
            }
        } catch (Exception e) {
            logger.error("salutation: " + e, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
            salutationForm.setError(true); // do not refresh when an error has been occurred
        }

        return destination;
    }

    protected Future<PaginatedListImpl<SalutationEntry>> getSalutationlistFuture(HttpServletRequest request, StrutsFormBase aForm) throws NumberFormatException, IllegalAccessException, InstantiationException, InterruptedException, ExecutionException {
        String sort = getSort(request, aForm);
        String direction = request.getParameter("dir");

        int rownums = aForm.getNumberOfRows();
        if (direction == null) {
            direction = aForm.getOrder();
        } else {
            aForm.setOrder(direction);
        }

        String pageStr = request.getParameter("page");
        if (pageStr == null || "".equals(pageStr.trim())) {
            if (aForm.getPage() == null || "".equals(aForm.getPage().trim())) {
                aForm.setPage("1");
            }
            pageStr = aForm.getPage();
        } else {
            aForm.setPage(pageStr);
        }

        if (aForm.isNumberOfRowsChanged()) {
            aForm.setPage("1");
            aForm.setNumberOfRowsChanged(false);
            pageStr = "1";
        }

        int companyID = AgnUtils.getCompanyID(request);

        return workerExecutorService.submit(new SalutationListQueryWorker(titleDao, companyID, sort, direction, NumberUtils.toInt(pageStr), rownums));
    }
}
