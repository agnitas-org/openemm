/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.scriptvalidator.IllegalVelocityDirectiveException;
import org.agnitas.service.UserFormExporter;
import org.agnitas.service.UserFormImporter;
import org.agnitas.service.UserFormImporter.UserFormImportResult;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.FileUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.UserFormEditAction;
import org.agnitas.web.UserFormEditForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComCompany;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.LinkService;
import com.agnitas.emm.core.LinkService.LinkScanResult;
import com.agnitas.emm.core.workflow.web.ComWorkflowAction;
import com.agnitas.messages.I18nString;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.bean.impl.UserFormImpl;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;
import com.agnitas.userform.trackablelinks.bean.impl.ComTrackableUserFormLinkImpl;
import com.agnitas.web.forms.ComUserFormEditForm;

/**
 * Extended Action class for UserFormEditAction
 * The extension is needed for trackable formula links within EMM
 */
public class ComUserFormEditAction extends UserFormEditAction {
	private static final transient Logger logger = Logger.getLogger(ComUserFormEditAction.class);

    public static final int ACTION_BULK_CONFIRM_DELETE = ACTION_SECOND_LAST + 1;
    public static final int ACTION_BULK_DELETE = ACTION_SECOND_LAST + 2;
    public static final int ACTION_SAVE_ACTIVENESS = ACTION_SECOND_LAST + 3;
    public static final int ACTION_IMPORT = ACTION_SECOND_LAST + 4;
    public static final int ACTION_EXPORT = ACTION_SECOND_LAST + 5;

    private ComCompanyDao companyDao;
    
    private LinkService linkService;
    
    private UserFormImporter userFormImporter;
    
    private UserFormExporter userFormExporter;

    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
        case ACTION_CLONE_FORM:
            return "clone_form";
        case ACTION_VIEW_WITHOUT_LOAD:
            return "view_without_load";
        case ACTION_BULK_CONFIRM_DELETE:
            return "bulk_confirm_delete";
        case ACTION_BULK_DELETE:
            return "bulk_delete";
        case ACTION_SAVE_ACTIVENESS:
            return "save_activeness";

        case ACTION_IMPORT:
            return "import";
        case ACTION_EXPORT:
            return "export";
            
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
     * @return the action to forward to.
     * @throws Exception 
     */
    @Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComAdmin admin = AgnUtils.getAdmin(request);
        assert admin != null;

        ComUserFormEditForm userFormEditForm = (ComUserFormEditForm) form;
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();

        // change in EMM - save also the measurable links
        switch (userFormEditForm.getAction()) {
            case ACTION_SAVE:
                try {
                	if (userformService.isValidFormName(userFormEditForm.getFormName())) {
                        if (!userformService.isFormNameInUse(userFormEditForm.getFormName(), userFormEditForm.getFormID(), admin.getCompanyID())) {
							saveUserForm(userFormEditForm, request, messages, errors);
							
							// Show "changes saved"
		                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
		                    saveMessages(request, messages);
                    	} else {
                    		errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.form.name_in_use"));
                    	}
                	} else {
                		errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.form.invalid_name"));
                	}
                } catch (IllegalVelocityDirectiveException e) {
                	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.form.illegal_directive", e.getDirective()));
				} catch (Exception e) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));
				}
                generateFormUrl(userFormEditForm, request);
                userFormEditForm.setFormUrl(userFormEditForm.getFormUrl() + userFormEditForm.getFormName());
                loadEmmActions(request);

                if (!errors.isEmpty()) {
                    saveErrors(request, errors);
                } else {
                    if (userFormEditForm.getWorkflowId() != 0) {
                        return createWorkflowForwardRedirect(request, mapping, userFormEditForm.getFormID());
                    }
                }
                return mapping.findForward("success");

            case ACTION_BULK_CONFIRM_DELETE:
                if (userFormEditForm.getBulkIds().size() == 0) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("bulkAction.nothing.userform"));
                    saveErrors(request, errors);
                    userFormEditForm.setAction(ACTION_LIST);
                    return super.execute(mapping, form, request, response);
                } else {
                    userFormEditForm.setAction(ACTION_BULK_DELETE);
                    return mapping.findForward("bulk_delete_confirm");
                }

            case ACTION_BULK_DELETE:
                if (deleteUserFormsBulk(userFormEditForm, request)) {
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                    saveMessages(request, messages);
                }

                userFormEditForm.setAction(ACTION_LIST);
                return super.execute(mapping, form, request, response);
            	
            case ACTION_VIEW:
                updateForwardParameters(request);
                Integer forwardTargetItemId = (Integer) request.getSession().getAttribute(ComWorkflowAction.WORKFLOW_FORWARD_TARGET_ITEM_ID);
                if (forwardTargetItemId != null && forwardTargetItemId != 0) {
                    userFormEditForm.setFormID(forwardTargetItemId);
                }
            	generateFormUrl(userFormEditForm, request);
            	userFormEditForm.setFormUrl(userFormEditForm.getFormUrl() + userFormEditForm.getFormName());
            	return super.execute(mapping, userFormEditForm, request, response);
            	
            case ACTION_NEW:
                updateForwardParameters(request);
                Integer forwardTargetItemIdNew = (Integer) request.getSession().getAttribute(ComWorkflowAction.WORKFLOW_FORWARD_TARGET_ITEM_ID);
                if (forwardTargetItemIdNew != null && forwardTargetItemIdNew != 0) {
                    userFormEditForm.setFormID(forwardTargetItemIdNew);
                }
            	generateFormUrl(userFormEditForm, request);
            	userFormEditForm.setFormUrl(userFormEditForm.getFormUrl() + userFormEditForm.getFormName());
            	return super.execute(mapping, userFormEditForm, request, response);

            case ACTION_SAVE_ACTIVENESS:
                UserAction userAction = userformService.setActiveness(admin.getCompanyID(), userFormEditForm.getActivenessMap());
                if (Objects.nonNull(userAction)) {
                    writeUserActivityLog(admin, userAction, logger);
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                    saveMessages(request, messages);
                }
                userFormEditForm.setAction(ACTION_LIST);
                return super.execute(mapping, form, request, response);
                
            case ACTION_IMPORT:
            	if (userFormEditForm.getUploadFile() != null) {
            		if (userFormEditForm.getUploadFile().getFileSize() == 0) {
            			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.file.missingOrEmpty"));
            			return mapping.findForward("import");
            		} else {
                		try (InputStream input = userFormEditForm.getUploadFile().getInputStream()) {
                			// Import userform data from upload file
                			UserFormImportResult result = userFormImporter.importUserFormFromJson(admin.getCompanyID(), input);
	                		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.imported"));
	                		for (String warningKey : result.getWarningKeys()) {
	                			messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage(warningKey));
	                		}
	                		writeUserActivityLog(AgnUtils.getAdmin(request), "import userform", result.getUserFormID());
	                		
	                		// View the imported userform
                			userFormEditForm.setFormID(result.getUserFormID());
	                        loadUserForm(userFormEditForm, request);
	                        loadEmmActions(request);
	                        userFormEditForm.setAction(UserFormEditAction.ACTION_SAVE);
	                        return mapping.findForward("view");
                		} catch (Exception e) {
                			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.userform.import"));
                			return mapping.findForward("import");
						}
            		}
            	} else {
            		return mapping.findForward("import");
            	}

            case ACTION_EXPORT:
            	UserForm userForm = userFormDao.getUserForm(userFormEditForm.getFormID(), admin.getCompanyID());
            	String fileFriendlyUserFormName = userForm.getFormName().replace("/", "_");
				String filename = "UserForm_" + fileFriendlyUserFormName + "_" + admin.getCompanyID() + "_" + userFormEditForm.getFormID() + FileUtils.JSON_EXTENSION;
				File tmpFile = File.createTempFile("UserForm_" + admin.getCompanyID() + "_" + userFormEditForm.getFormID(), FileUtils.JSON_EXTENSION);
                try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
					userFormExporter.exportUserFormToJson(admin.getCompanyID(), userFormEditForm.getFormID(), outputStream);
				} catch (Exception e) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.userform.export"));
					return mapping.findForward("view");
				}
                if (errors.isEmpty()) {
					try (FileInputStream inputStream = new FileInputStream(tmpFile)) {
						response.setContentType("application/json");
			            HttpUtils.setDownloadFilenameHeader(response, filename);
						IOUtils.copy(inputStream, response.getOutputStream());
	                    writeUserActivityLog(AgnUtils.getAdmin(request), "export userform", userFormEditForm.getFormID());
	                    tmpFile.delete();
						return null;
					} catch (Exception e) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.userform.export"));
						return mapping.findForward("view");
					}
                } else {
                	return null;
                }

            default:
                generateFormUrl(userFormEditForm, request);

                if (Objects.isNull(userFormEditForm.getFormName())) {
                    userFormEditForm.setFormUrl(userFormEditForm.getFormUrl());
                } else {
                    userFormEditForm.setFormUrl(userFormEditForm.getFormUrl() + userFormEditForm.getFormName());
                }

                return super.execute(mapping, form, request, response);
        }
    }

    /**
     * Save a user form.
     * Writes the data of a form to the database.
     *
     * @param aForm contains the data of the form.
     * @throws Exception 
     */
    protected void saveUserForm(UserFormEditForm aForm, HttpServletRequest req, ActionMessages actionMessages, ActionMessages errors) throws Exception {
        logger.debug("Starting saveUserForm");
        
        checkVelocityScripts( aForm);

        final int companyId = AgnUtils.getCompanyID(req);
        UserForm aUserForm = new UserFormImpl();

        // formula values
        aUserForm.setCompanyID(companyId);
        aUserForm.setId(aForm.getFormID());
        aUserForm.setFormName(aForm.getFormName());
        aUserForm.setDescription(aForm.getDescription());
        aUserForm.setStartActionID(aForm.getStartActionID());
        aUserForm.setEndActionID(aForm.getEndActionID());
        aUserForm.setSuccessTemplate(aForm.getSuccessTemplate());
        aUserForm.setErrorTemplate(aForm.getErrorTemplate());
        aUserForm.setSuccessUrl(aForm.getSuccessUrl());
        aUserForm.setErrorUrl(aForm.getErrorUrl());
        aUserForm.setSuccessUseUrl(aForm.isSuccessUseUrl());
        aUserForm.setErrorUseUrl(aForm.isErrorUseUrl());
        aUserForm.setActive(aForm.getIsActive());

        logger.debug("Start Link handling");
        
        // Check for valid htmlLink-Syntax
        Integer invalidHrefInLineSuccess = linkService.getLineNumberOfFirstInvalidLink(aForm.getSuccessTemplate());
		if (invalidHrefInLineSuccess != null) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid_link", "SUCCESS", invalidHrefInLineSuccess));
		}
        
		Integer invalidHrefInLineError = linkService.getLineNumberOfFirstInvalidLink(aForm.getErrorTemplate());
		if (invalidHrefInLineError != null) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid_link", "ERROR", invalidHrefInLineError));
		}
        
        // link handling
        Map<String, ComTrackableUserFormLink> trackableLinks = new HashMap<>();
        LinkScanResult successLinks = linkService.scanForLinks(aForm.getSuccessTemplate(), companyId);
        List<ComTrackableLink> sucessTemplateLinks = successLinks.getTrackableLinks();
        LinkScanResult errorLinks = linkService.scanForLinks(aForm.getErrorTemplate(), companyId);
        List<ComTrackableLink> errorTemplateLinks = errorLinks.getTrackableLinks();
        sucessTemplateLinks.addAll(errorTemplateLinks);
        
		// Check for not measurable links
		if (successLinks.getNotTrackableLinks().size() > 0) {
		    actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.agntag", "success", StringEscapeUtils.escapeHtml(successLinks.getNotTrackableLinks().get(0))));
		}
		if (errorLinks.getNotTrackableLinks().size() > 0) {
		    actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.agntag", "error", StringEscapeUtils.escapeHtml(errorLinks.getNotTrackableLinks().get(0))));
		}

		// Check for not errorneous links
		if (successLinks.getErrorneousLinks().size() > 0) {
			errors.add(ActionMessages.GLOBAL_MESSAGE,
				new ActionMessage("error.mailing.links.errorneous",
		    		successLinks.getErrorneousLinks().size(),
		    		"success",
		    		StringEscapeUtils.escapeHtml(successLinks.getErrorneousLinks().get(0).getLinkText()),
		    		I18nString.getLocaleString(successLinks.getErrorneousLinks().get(0).getErrorMessageKey(), AgnUtils.getLocale(req))));
		}
		if (errorLinks.getErrorneousLinks().size() > 0) {
			errors.add(ActionMessages.GLOBAL_MESSAGE,
		    	new ActionMessage("error.mailing.links.errorneous",
	    			errorLinks.getErrorneousLinks().size(),
		    		"error",
		    		StringEscapeUtils.escapeHtml(errorLinks.getErrorneousLinks().get(0).getLinkText()),
		    		I18nString.getLocaleString(errorLinks.getErrorneousLinks().get(0).getErrorMessageKey(), AgnUtils.getLocale(req))));
		}

        // let's see what trackable links we already have
        Map<String,ComTrackableUserFormLink> linksAlreadyInDB = userFormDao.getUserFormTrackableLinks(aUserForm.getId(), companyId);

        // links that should be deleted
        List<ComTrackableUserFormLink> linksToDelete = new ArrayList<>();
        // already existing links
        List<ComTrackableUserFormLink> linksAlreadyExisting = new ArrayList<>();
        boolean shouldDeleteLink;
        for (ComTrackableUserFormLink comTrackableUserFormLinkCurr : linksAlreadyInDB.values()) {
            shouldDeleteLink = true;
            for (ComTrackableLink trackableLink : sucessTemplateLinks) {
            	String link = trackableLink.getFullUrl();
                if (comTrackableUserFormLinkCurr.getFullUrl().equals(link)) {
                    shouldDeleteLink = false;
                    break;
                }
            }
            if (shouldDeleteLink) {
                linksToDelete.add(comTrackableUserFormLinkCurr);
            } else {
                linksAlreadyExisting.add(comTrackableUserFormLinkCurr);
            }
        }

        boolean newLink;
        // Trackable links for the formular
        for (ComTrackableLink trackableLink : sucessTemplateLinks) {
        	String link = trackableLink.getFullUrl();
            newLink = true;
            for (ComTrackableUserFormLink comTrackableUserFormLinkExists : linksAlreadyExisting) {
                if (link.equals(comTrackableUserFormLinkExists.getFullUrl())) {
                    // link already exists
                    trackableLinks.put(link, comTrackableUserFormLinkExists);
                    newLink = false;
                    break;
                }
            }
            if (newLink) {
                // new link
            	ComTrackableUserFormLink comTrackableUserFormLink = new ComTrackableUserFormLinkImpl();
            	
                comTrackableUserFormLink.setFullUrl(link);
                comTrackableUserFormLink.setCompanyID(companyId);
                comTrackableUserFormLink.setFormID(aUserForm.getId());                    // Warning! This sets form ID 0 to new links! Must be updated when saving the form!!!!
                comTrackableUserFormLink.setUsage(ComTrackableUserFormLink.TRACKABLE_NO);
                comTrackableUserFormLink.setActionID(0);
                comTrackableUserFormLink.setShortname("");
                
                // Extend links with the default company extension if set
                setDefaultExtension(comTrackableUserFormLink);
                
                trackableLinks.put(link, comTrackableUserFormLink);
            }
        }
        logger.debug("Finished Link handling");

        aUserForm.setTrackableLinks(trackableLinks);
        
        int formID = userFormDao.storeUserForm(aUserForm);
        
        if (aForm.getFormID() == 0) {
            writeUserActivityLog(AgnUtils.getAdmin(req), "create user form", aForm.getFormName());
        } else {
            writeUserActivityLog(AgnUtils.getAdmin(req), "edit user form", aForm.getFormName());
        }

        // save form
        aForm.setFormID(formID);

        // delete no longer needed links
        // COMMENTED THIS OUT, SINCE OLD LINKS MAY STILL BE APPLICABLE FOR STATISTICS
        // deleteLinks(linksToDelete);

        logger.debug("Finished saveUserForm");
    }

    /**
     * Helper function of deleting trackable links.
     *
     * @param linksToDelete - list of trackable links to be deleted
     */
    protected void deleteLinks(List<ComTrackableUserFormLink> linksToDelete) {
    	logger.debug("Starting deleteLinks");

        for (ComTrackableUserFormLink comTrackableUserFormLinkCurr : linksToDelete) {
        	userFormDao.deleteUserFormTrackableLink(comTrackableUserFormLinkCurr.getId(),
                    comTrackableUserFormLinkCurr.getCompanyID());
        }

        logger.debug("Starting deleteLinks");
	}

    private boolean deleteUserFormsBulk(ComUserFormEditForm form, HttpServletRequest req) {
        Set<Integer> ids = form.getBulkIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            final int companyId = AgnUtils.getCompanyID(req);

            Map<Integer, String> descriptions = new HashMap<>();
            for (int formId : ids) {
                descriptions.put(formId, userformService.getUserFormName(formId, companyId));
            }

            userformService.bulkDelete(ids, companyId);

            for (int formId : ids) {
                writeUserActivityLog(AgnUtils.getAdmin(req), "delete user form", descriptions.get(formId));
            }
            return true;
        }
        return false;
    }

	private void setDefaultExtension(ComTrackableUserFormLink link) throws UnsupportedEncodingException {
		String defaultExtensionString = configService.getValue(ConfigValue.DefaultLinkExtension, link.getCompanyID());
		if (StringUtils.isNotBlank(defaultExtensionString)) {
			if (defaultExtensionString.startsWith("?")) {
				defaultExtensionString = defaultExtensionString.substring(1);
			}
			String[] extensionProperties = defaultExtensionString.split("&");
			for (String extensionProperty : extensionProperties) {
				final int eqIndex = extensionProperty.indexOf('=');
				final String[] extensionPropertyData = (eqIndex == -1) ? new String[] { extensionProperty, "" } : new String[] { extensionProperty.substring(0, eqIndex), extensionProperty.substring(eqIndex + 1) };
				
				String extensionPropertyName = URLDecoder.decode(extensionPropertyData[0], "UTF-8");
				String extensionPropertyValue = "";
				if (extensionPropertyData.length > 1) {
					extensionPropertyValue = URLDecoder.decode(extensionPropertyData[1], "UTF-8");
				}
		
				// Change link properties
				List<LinkProperty> properties = link.getProperties();
				boolean changedProperty = false;
				for (LinkProperty property : properties) {
					if (property.getPropertyType() == PropertyType.LinkExtension && property.getPropertyName().equals(extensionPropertyName)) {
						property.setPropertyValue(extensionPropertyValue);
						changedProperty = true;
					}
				}
				if (!changedProperty) {
					LinkProperty newProperty = new LinkProperty(PropertyType.LinkExtension, extensionPropertyName, extensionPropertyValue);
					properties.add(newProperty);
				}
			}
		}
	}
	
	protected void generateFormUrl(ComUserFormEditForm aForm, HttpServletRequest request) {
		ComCompany company = companyDao.getCompany(AgnUtils.getCompanyID(request));
		try {
			loadUserForm(aForm, request);
		} catch (Exception e) {
			logger.info("could not load userform");
		}
		String formUrl = company.getRdirDomain() + "/form.do?agnCI=" + AgnUtils.getCompanyID(request) + "&agnFN=";
		aForm.setFormUrl(formUrl);
	}

    private ActionRedirect createWorkflowForwardRedirect(HttpServletRequest req, ActionMapping mapping, int formId){
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("workflow_view"));
        redirect.addParameter("workflowId", req.getSession().getAttribute(ComWorkflowAction.WORKFLOW_ID));
        redirect.addParameter("forwardParams", req.getSession().getAttribute(ComWorkflowAction.WORKFLOW_FORWARD_PARAMS).toString()
                + ";elementValue=" + Integer.toString(formId));
        return redirect;
    }

    private void updateForwardParameters(HttpServletRequest req) {
        String forwardTargetItemId = req.getParameter(ComWorkflowAction.WORKFLOW_FORWARD_TARGET_ITEM_ID);
        if (forwardTargetItemId == null || "".equals(forwardTargetItemId)) {
            forwardTargetItemId = "0";
        }
        req.getSession().setAttribute(ComWorkflowAction.WORKFLOW_FORWARD_TARGET_ITEM_ID, Integer.valueOf(forwardTargetItemId));

        String workflowId = req.getParameter(ComWorkflowAction.WORKFLOW_ID);
        if (workflowId == null || "".equals(workflowId)) {
            workflowId = "0";
        }
        req.getSession().setAttribute(ComWorkflowAction.WORKFLOW_ID, Integer.valueOf(workflowId));

        req.getSession().setAttribute(ComWorkflowAction.WORKFLOW_FORWARD_PARAMS,
                req.getParameter(ComWorkflowAction.WORKFLOW_FORWARD_PARAMS));
    }

	@Required
    public void setComCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }

	@Required
    public void setLinkService(LinkService linkService) {
        this.linkService = linkService;
    }

	@Required
    public void setUserFormImporter(UserFormImporter userFormImporter) {
        this.userFormImporter = userFormImporter;
    }

	@Required
    public void setUserFormExporter(UserFormExporter userFormExporter) {
        this.userFormExporter = userFormExporter;
    }
}
