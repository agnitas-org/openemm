/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.actions.EmmAction;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityDirectiveScriptUtil;
import org.agnitas.emm.core.velocity.scriptvalidator.ScriptValidationException;
import org.agnitas.emm.core.velocity.scriptvalidator.VelocityDirectiveScriptValidator;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.SafeString;
import org.agnitas.util.Tuple;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.userform.bean.UserForm;

/**
 * Implementation of <strong>Action</strong> that handle input of user forms.
 */
public class UserFormEditAction extends StrutsActionBase {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(UserFormEditAction.class);

    public static final int ACTION_VIEW_WITHOUT_LOAD = ACTION_LAST + 1;
    public static final int ACTION_CLONE_FORM = ACTION_LAST + 2;
    public static final int ACTION_SECOND_LAST = ACTION_LAST + 2;

    protected ConfigService configService;
    protected WebStorage webStorage;

    // --------------------------------------------------------- Public Methods

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     * <br>
	 * ACTION_LIST: loads list of user forms into request;<br>
     *     forwards to user form list page.
	 * <br><br>
	 * ACTION_SAVE: saves user form data in database, sets user form id into form;<br>
     *     loads lists of user forms and actions that has ACTION_TYPE 0 or 9 in database into request;<br>
     *     sets destination = "success",<br>
     *     forwards to user form view page.
     * <br><br>
     * ACTION_VIEW: loads data of chosen user form data into form;<br>
     *    loads list of actions that has ACTION_TYPE 0 or 9 in database into request;<br>
     *      forwards to user form view page
     * <br><br>
     * ACTION_VIEW_WITHOUT_LOADING: is used after failing form validation<br>
     *      for loading essential data into request before returning to the view page;<br>
     *      does not reload form data.
     * <br><br>
     *ACTION_CONFIRM_DELETE:  loads data of chosen user form into form, <br>
     *      forwards to jsp with question to confirm deletion.
     * <br><br>
     * ACTION_DELETE: deletes the entry of certain user form;<br>
     * loads list of actions that has ACTION_TYPE 0 or 9 in database into request;<br>
     *          forwards to user form list page.
	 * <br><br>
	 * Any other ACTION_* would cause a forward to "list"
     * <br><br>
     * @param form
     * @param request
     * @param res
     * @param mapping The ActionMapping used to select this instance
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     * @return the action to forward to.
     * @throws Exception 
     */
    @Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse res) throws IOException, ServletException, Exception {
        // Validate the request parameters specified by the user
        UserFormEditForm aForm = null;
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        ActionForward destination=null;

        if(!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }

        aForm=(UserFormEditForm)form;
        if (logger.isInfoEnabled()) logger.info("Action: "+aForm.getAction());

        try {
            switch(aForm.getAction()) {
                case UserFormEditAction.ACTION_LIST:
                    destination=mapping.findForward("list");
                    break;

                case UserFormEditAction.ACTION_VIEW:
                    loadUserForm(aForm, request);
                    loadEmmActions(request);
                    aForm.setAction(UserFormEditAction.ACTION_SAVE);
                    destination=mapping.findForward("view");
                    break;

                case UserFormEditAction.ACTION_NEW:
                    loadUserForm(aForm, request);
                    loadEmmActions(request);
                    aForm.setAction(UserFormEditAction.ACTION_SAVE);
                    destination=mapping.findForward("view");
                    break;

                case UserFormEditAction.ACTION_CLONE_FORM:
                    loadUserForm(aForm, request);
                    aForm.setFormID(0);
                    String newFormName = SafeString.getLocaleString("mailing.CopyOf", AgnUtils.getLocale(request)) + " " + aForm.getFormName();
                    aForm.setFormName(newFormName.replaceAll(" ", "_"));
                    loadEmmActions(request);
                    aForm.setAction(UserFormEditAction.ACTION_SAVE);
                    destination=mapping.findForward("view");
                    break;

                case UserFormEditAction.ACTION_CONFIRM_DELETE:
                    loadUserForm(aForm, request);
                    aForm.setAction(UserFormEditAction.ACTION_DELETE);
                    destination=mapping.findForward("delete");
                    break;

                case UserFormEditAction.ACTION_DELETE:
                    deleteUserForm(aForm, request);
                    aForm.setAction(UserFormEditAction.ACTION_LIST);
                    destination=mapping.findForward("list");
                    // Show "changes saved"
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                    break;

                case UserFormEditAction.ACTION_VIEW_WITHOUT_LOAD:
                    loadEmmActions(request);
                    aForm.setAction(UserFormEditAction.ACTION_SAVE);
                    destination=mapping.findForward("view");
                    break;

                default:
                    aForm.setAction(UserFormEditAction.ACTION_LIST);
                    destination=mapping.findForward("list");
            }
        } catch (Exception e) {
            logger.error("execute", e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

		if (destination != null && ("list".equals(destination.getName()) || "success".equals(destination.getName()))) {
			if (aForm.getColumnwidthsList() == null) {
				aForm.setColumnwidthsList(getInitializedColumnWidthList(3));
			}

			try {
				String direction = request.getParameter("dir");
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
				Integer page = Integer.parseInt(pageStr);

                FormUtils.syncNumberOfRows(webStorage, WebStorage.USERFORM_OVERVIEW, aForm);
				int rownums = aForm.getNumberOfRows();

                Boolean activenessFilter = null;
                String activenessFilterString = request.getParameter("activenessFilter");
                if (StringUtils.equalsIgnoreCase(activenessFilterString, "active")) {
                    activenessFilter = Boolean.TRUE;
                } else if (StringUtils.equalsIgnoreCase(activenessFilterString, "inactive")) {
                    activenessFilter = Boolean.FALSE;
                }

				PaginatedListImpl<UserForm> userForms = userFormDao.getUserFormsWithActionIDs(getSort(request, aForm), direction, page, rownums, activenessFilter, AgnUtils.getCompanyID(request));

				ArrayList<Integer> allUsedActionIDs = new ArrayList<>();
				for (UserForm userForm : userForms.getList()) {
					if (userForm.getStartActionID() > 0) {
						allUsedActionIDs.add(userForm.getStartActionID());
					}
					if (userForm.getEndActionID() > 0) {
						allUsedActionIDs.add(userForm.getEndActionID());
					}
				}

				Map<Integer, String> allUsedActionNames = new HashMap<>();
				List<Tuple<Integer, String>> allUsedActionNamesList = emmActionDao
						.getEmmActionNames(AgnUtils.getCompanyID(request), allUsedActionIDs);
				for (Tuple<Integer, String> tuple : allUsedActionNamesList) {
					allUsedActionNames.put(tuple.getFirst(), tuple.getSecond());
				}

				for (UserForm userForm : userForms.getList()) {
					if (userForm.getStartActionID() > 0 && userForm.getEndActionID() > 0) {
						userForm.setActionNames(allUsedActionNames.getOrDefault(userForm.getStartActionID(), "") + ", " + allUsedActionNames.getOrDefault(userForm.getEndActionID(), ""));
					} else if (userForm.getStartActionID() > 0) {
						userForm.setActionNames(allUsedActionNames.get(userForm.getStartActionID()));
					} else if (userForm.getEndActionID() > 0) {
						userForm.setActionNames(allUsedActionNames.get(userForm.getEndActionID()));
					}
				}
				request.setAttribute("userformlist", userForms);
			} catch (Exception e) {
				logger.error("userformlist", e);
				errors.add(ActionMessages.GLOBAL_MESSAGE,
						new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}
		}
        
        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }

        // Report any message (non-errors) we have discovered
        if (!messages.isEmpty()) {
        	saveMessages(request, messages);
        }

        return destination;
    }

    /**
     * Load a user form data into form.
     * Retrieves the data of a form from the database.
     * @param aForm on input contains the id of the form.
     *              On exit contains the data read from the database.
     * @param req request
     * @throws Exception 
     */
    protected void loadUserForm(UserFormEditForm aForm, HttpServletRequest req) throws Exception {
    	if (aForm.getFormID() != 0) {
    		UserForm aUserForm = userFormDao.getUserForm(aForm.getFormID(), AgnUtils.getCompanyID(req));
	        if (aUserForm != null && aUserForm.getId() != 0) {
	            aForm.setFormName(aUserForm.getFormName());
	            aForm.setDescription(aUserForm.getDescription());
	            aForm.setStartActionID(aUserForm.getStartActionID());
	            aForm.setEndActionID(aUserForm.getEndActionID());
	            aForm.setSuccessTemplate(aUserForm.getSuccessTemplate());
	            aForm.setErrorTemplate(aUserForm.getErrorTemplate());
	            aForm.setSuccessUrl(aUserForm.getSuccessUrl());
	            aForm.setErrorUrl(aUserForm.getErrorUrl());
	            aForm.setSuccessUseUrl(aUserForm.isSuccessUseUrl());
	            aForm.setErrorUseUrl(aUserForm.isErrorUseUrl());
                aForm.setIsActive(aUserForm.isActive());
                aForm.setSuccessUseVelocity(VelocityDirectiveScriptUtil.containsAnyStatement(aUserForm.getSuccessTemplate()));
                aForm.setErrorUseVelocity(VelocityDirectiveScriptUtil.containsAnyStatement(aUserForm.getErrorTemplate()));
	            if (logger.isInfoEnabled()) {
	            	logger.info("loadUserForm: form "+aForm.getFormID()+" loaded");
	            }
	            writeUserActivityLog(AgnUtils.getAdmin(req), "view user form", aForm.getFormName());
	        } else {
	            logger.warn("loadUserForm: could not load userform" + aForm.getFormID());
	        }
    	}
    }
    
    protected void checkVelocityScripts( UserFormEditForm form) throws ScriptValidationException {
    	this.velocityDirectiveScriptValidator.validateScript( form.getSuccessTemplate());
    	this.velocityDirectiveScriptValidator.validateScript( form.getErrorTemplate());
    }

    /**
     * Delete a user form.
     * Removes the data of a form from the database.
     * @param aForm contains the id of the form.
     * @param  req request
     */
    protected void deleteUserForm(UserFormEditForm aForm, HttpServletRequest req) {
        userFormDao.deleteUserForm(aForm.getFormID(), AgnUtils.getCompanyID(req));
        writeUserActivityLog(AgnUtils.getAdmin(req), "delete user form", aForm.getFormName());
    }

    protected void loadEmmActions(HttpServletRequest req){
        List<EmmAction> emmActions = emmActionDao.getEmmNotLinkActions(AgnUtils.getCompanyID(req), false);
        req.setAttribute("emm_actions", emmActions);
    }

    // --------------------------------------------------------------------------------- Dependency Injection
    protected UserFormDao userFormDao;
    protected EmmActionDao emmActionDao;
    private VelocityDirectiveScriptValidator velocityDirectiveScriptValidator;
    
    /** Service layer for userform data. */
    protected ComUserformService userformService;
    
    public void setUserFormDao(UserFormDao userFormDao) {
        this.userFormDao = userFormDao;
    }

    public void setEmmActionDao(EmmActionDao emmActionDao) {
        this.emmActionDao = emmActionDao;
    }
    
    public void setVelocityDirectiveScriptValidator(VelocityDirectiveScriptValidator validator) {
    	this.velocityDirectiveScriptValidator = validator;
    }
    
    /**
     * Set service layer for userform data.
     * 
     * @param userformService service layer for userform data
     */
    public void setUserformService(ComUserformService userformService) {
    	this.userformService = userformService;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Required
    public void setWebStorage(WebStorage webStorage) {
        this.webStorage = webStorage;
    }
}
