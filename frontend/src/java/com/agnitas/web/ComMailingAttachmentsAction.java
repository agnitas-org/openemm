/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.factory.MailingComponentFactory;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.web.StrutsActionBase;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mimetypes.service.MimeTypeWhitelistService;
import com.agnitas.emm.core.upload.bean.UploadData;
import com.agnitas.emm.core.upload.dao.ComUploadDao;
import com.agnitas.web.forms.ComMailingAttachmentsForm;


/**
 * Implementation of <strong>Action</strong> that validates a user logon.
 */
public final class ComMailingAttachmentsAction extends StrutsActionBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingAttachmentsAction.class);

    // --------------------------------------------------------- Public Methods
	
	/** DAO accessing mailing data. */
    private ComMailingDao mailingDao;
	
	/** DAO accessing target group data. */
    private ComTargetDao targetDao;
	
	/** DAO accessing mailing component data. */
	private ComMailingComponentDao componentDao;
	
	/** DAO accessing upload data. */
	private ComUploadDao uploadDao;
	
	/** Factory for mailing components. */
    private MailingComponentFactory mailingComponentFactory;

    private ComMailingBaseService mailingBaseService;

	/** Service for configuration data. */
 	protected ConfigService configService;
 	
 	private MaildropService maildropService;
 	
 	private MimeTypeWhitelistService mimetypeWhitelistService;

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
            HttpServletResponse res) {

        // Validate the request parameters specified by the user
        ComMailingAttachmentsForm aForm = null;
        ActionMessages errors = new ActionMessages();
      	ActionMessages messages = new ActionMessages();
      	ActionForward destination=null;
      	ComAdmin admin = AgnUtils.getAdmin(req);

        if(!AgnUtils.isUserLoggedIn(req)) {
            return mapping.findForward("logon");
        }

        aForm = (ComMailingAttachmentsForm) form;

		req.setAttribute("targetGroups", targetDao.getTargetLights(AgnUtils.getCompanyID(req)));
		req.setAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(AgnUtils.getAdmin(req), aForm.getMailingID()));

        try {
            switch(aForm.getAction()) {
                case ACTION_LIST:
                    destination = mapping.findForward("list");
                    writeUserActivityLog(AgnUtils.getAdmin(req), "attachments list", "active tab - attachments");
                    break;

                case ACTION_SAVE:
                	if (!newAttachmentHasName(aForm)) {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.errors.no_attachment_name"));
	                    destination = mapping.findForward("list");
                	} else if (aForm.getNewAttachment() == null || checkNewAttachmentSize(aForm, errors, messages)) {
                		if(aForm.getNewAttachment() != null && !this.mimetypeWhitelistService.isMimeTypeWhitelisted(aForm.getNewAttachment().getContentType()) ) {
                			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.errors.attachment.invalidMimeType", aForm.getNewAttachment().getContentType()));
     	                    destination = mapping.findForward("list");	
                		} else {
		                    if (!statusChanged(aForm,req)) {
		                        saveAttachment(aForm, req, errors);
		                    }
		                    loadMailing(aForm, req);
		                    List<MailingComponent> attachments = loadAttachments(aForm,req);
		                    loadTargets(req);
		                    loadPdfUploads(req);
		                    aForm.setAction(ACTION_SAVE);
		                    
		                    if (!newAttachmentAllowed(aForm, attachments)) {
		                    	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.mailing.attachment.unique"));
			                    destination = mapping.findForward("view");
		                    } else {
			                    destination = mapping.findForward("list");
			                    Enumeration<String> parameterNames = req.getParameterNames();
			                    boolean aComponentWasJustAdded = false;
			                    while (parameterNames.hasMoreElements()) {
			                    	String parameter = parameterNames.nextElement();
		                            if (parameter.startsWith("add") && AgnUtils.parameterNotEmpty(req,parameter)) {
		                                aComponentWasJustAdded = true;
		                                break;
			                        }
			                    }
			
			                    // Show "changes saved"
			                    if (!statusChanged(aForm, req)) {
			                        if (!aForm.isUsePdfUpload()) {
			                            if (aComponentWasJustAdded && (aForm.getNewAttachment() == null || aForm.getNewAttachment().getFileName() == null || "".equals(aForm.getNewAttachment().getFileName()))) {
			                                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.errors.no_attachment_file"));
			                            } else {
			                                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			                            }
			                        } else {
			                            if (aForm.getAttachmentPdfFileID() == 0) {
			                                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.errors.no_attachment_pdf_file"));
			                            } else {
			                                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			                            }
			                        }
			                    } else {
			                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("status_changed"));
			                    }
		                    }
                		}
                	}
                    break;

                case ComMailingComponentsAction.ACTION_CONFIRM_DELETE:
                    MailingComponent component = componentDao.getMailingComponent(aForm.getAttachmentId(), AgnUtils.getCompanyID(req));
                    aForm.setAttachmentName(component.getComponentName());
                    aForm.setAction(ComMailingComponentsAction.ACTION_DELETE);
                    destination = mapping.findForward("delete");
                    break;

                case ComMailingComponentsAction.ACTION_DELETE:
                    MailingComponent mailingComponent =componentDao.getMailingComponent(aForm.getAttachmentId(), AgnUtils.getCompanyID(req));
                    componentDao.deleteMailingComponent(mailingComponent);
                    String mailingName = mailingBaseService.getMailingName(aForm.getMailingID(), AgnUtils.getCompanyID(req));
                    mailingName = mailingName == null ? StringUtils.EMPTY : mailingName;
                    writeUserActivityLog(admin, "delete attachment",
                            String.format("%s(%d), deletion of an attachment (%d)", mailingName, aForm.getMailingID(), aForm.getAttachmentId()));
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                    aForm.setAction(ComMailingComponentsAction.ACTION_LIST);
                    destination = mapping.findForward("list");
                    break;
            }

            if (destination != null && "list".equals(destination.getName())) {
                req.setAttribute("isMailingGrid", req.getParameter("isMailingGrid"));
                req.setAttribute("templateId", req.getParameter("templateId"));
                loadMailing(aForm, req);
                loadAttachments(aForm, req);
                loadTargets(req);
                loadPdfUploads(req);
                aForm.setAction(ACTION_SAVE);
            }

        } catch (Exception e) {
            logger.error("execute: " + e.getMessage(), e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(req, errors);
            return (new ActionForward(mapping.getInput()));
        }

        // Report any message (non-errors) we have discovered
        if (!messages.isEmpty()) {
        	saveMessages(req, messages);
        }

        return destination;

    }

	private boolean newAttachmentAllowed(ComMailingAttachmentsForm aForm, List<MailingComponent> attachments) {
		// Same attachment name for other target group is not allowed by now
		for (MailingComponent attachment : attachments) {
			if (attachment.getComponentName().equals(aForm.getNewAttachmentName()) && attachment.getTargetID() != aForm.getAttachmentTargetID()) {
				return false;
			}
		}
		return true;
	}

	private static boolean newAttachmentHasName(ComMailingAttachmentsForm form) {
        FormFile file = form.getNewAttachment();
		if (file == null || file.getFileSize() <= 0) {
            // No upload file (size <= 0)? Then we don't need a name.
			return true;
		} else {
			return StringUtils.isNotBlank(form.getNewAttachmentName());
		}
	}

    private boolean statusChanged(ComMailingAttachmentsForm aForm, HttpServletRequest req) {
        if (AgnUtils.allowed(req, Permission.MAILING_CONTENT_CHANGE_ALWAYS)) {
            return false;
        } else {
            Mailing mailing = mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));
            
            return this.maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID());
        }
    }

    /**
     * Loads mailing
     */
    protected void loadMailing(ComMailingAttachmentsForm aForm, HttpServletRequest req) throws Exception {
        final int companyId = AgnUtils.getCompanyID(req);
        final int mailingId = aForm.getMailingID();

        Mailing aMailing = mailingDao.getMailing(mailingId, companyId);

        aForm.setShortname(aMailing.getShortname());
        aForm.setDescription(aMailing.getDescription());
        aForm.setIsTemplate(aMailing.isIsTemplate());
        aForm.setWorldMailingSend(this.maildropService.isActiveMailing(aMailing.getId(), aMailing.getCompanyID()));

        aForm.setIsMailingUndoAvailable(mailingBaseService.checkUndoAvailable(mailingId));
        aForm.setWorkflowId(mailingBaseService.getWorkflowId(mailingId, companyId));

        if (logger.isInfoEnabled()) logger.info("loadMailing: mailing loaded");
    }

    protected List<MailingComponent> loadAttachments(ComMailingAttachmentsForm aForm, HttpServletRequest req){
        List<MailingComponent> attachments = componentDao.getPreviewHeaderComponents(aForm.getMailingID(), AgnUtils.getCompanyID(req));
		req.setAttribute("attachments", attachments);
		return attachments;
    }

    protected void loadTargets(HttpServletRequest req){
        List<TargetLight> targets = targetDao.getTargetLights(AgnUtils.getCompanyID(req));
        List<TargetLight> allTargets = targetDao.getTargetLights(AgnUtils.getCompanyID(req),true);
		req.setAttribute("targets", targets);
        req.setAttribute("allTargets", allTargets);
    }

    protected void loadPdfUploads(HttpServletRequest req){
        List<String> extentions = new ArrayList<>();
        extentions.add("pdf");
		req.setAttribute("pdfFiles", getOverviewListByExtention(req, extentions));
    }

    private List<UploadData> getOverviewListByExtention(HttpServletRequest req, List<String> extentions) {
    	return uploadDao.getOverviewListByExtention(AgnUtils.getAdmin(req), extentions);
    }

    /**
	 * Saves attachement
	 */
    protected void saveAttachment(ComMailingAttachmentsForm aForm, HttpServletRequest req, ActionMessages errors) {
        MailingComponent aComp=null;
        String aParam=null;
        UploadData uploadData = null;
        Vector<MailingComponent> deleteEm=new Vector<>();

        Mailing aMailing=mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));

        FormFile background = aForm.getNewAttachmentBackground();
        try {
            if (aForm.isUsePdfUpload()) {
                if (aForm.getAttachmentPdfFileID() != 0) {
                    aComp = mailingComponentFactory.newMailingComponent();
                    aComp.setCompanyID(AgnUtils.getCompanyID(req));
                    aComp.setMailingID(aForm.getMailingID());
                    uploadData = uploadDao.loadData(aForm.getAttachmentPdfFileID());
                    if (aForm.getNewAttachmentType() == 0) {
                        aComp.setType(MailingComponent.TYPE_ATTACHMENT);
                        aComp.setComponentName(uploadData.getFilename());
                        aComp.setBinaryBlock(uploadData.getData(), "application/pdf");
                    } else {
                        aComp.setType(MailingComponent.TYPE_PERSONALIZED_ATTACHMENT);
                        aComp.setComponentName(uploadData.getFilename());
                        aComp.setBinaryBlock(background.getFileData(), "application/pdf");
                        aMailing.findDynTagsInTemplates(new String(uploadData.getData(), "UTF-8"), getApplicationContext(req));
                    }
                    aComp.setTargetID(aForm.getAttachmentTargetID());
                    aMailing.addComponent(aComp);
                }
            } else {
                FormFile newAttachment = aForm.getNewAttachment();
                if (newAttachment != null && newAttachment.getFileSize() != 0) {
                    aComp = mailingComponentFactory.newMailingComponent();
                    aComp.setCompanyID(AgnUtils.getCompanyID(req));
                    aComp.setMailingID(aForm.getMailingID());
                    if (aForm.getNewAttachmentType() == 0) {
                        aComp.setType(MailingComponent.TYPE_ATTACHMENT);
                        aComp.setComponentName(aForm.getNewAttachmentName());
                        aComp.setBinaryBlock(newAttachment.getFileData(), newAttachment.getContentType());
                    } else {
                        aComp.setType(MailingComponent.TYPE_PERSONALIZED_ATTACHMENT);
                        aComp.setComponentName(aForm.getNewAttachmentName());
                        aComp.setBinaryBlock(background.getFileData(), "application/pdf");
                        aMailing.findDynTagsInTemplates(new String(newAttachment.getFileData(), "UTF-8"), getApplicationContext(req));
                    }
                    aComp.setTargetID(aForm.getAttachmentTargetID());
                    aMailing.addComponent(aComp);
                    writeUserActivityLog(AgnUtils.getAdmin(req), "upload mailing attachment", "Mailing ID: " + aForm.getMailingID() + ", attachment name: " + aForm.getNewAttachmentName());
                }
            }
        } catch (Exception e) {
            logger.error("saveAttachment: " + e.getMessage(), e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

        for (MailingComponent component : aMailing.getComponents().values()) {
            switch (component.getType()) {
                case MailingComponent.TYPE_PERSONALIZED_ATTACHMENT:
                case MailingComponent.TYPE_ATTACHMENT:
                    aParam = req.getParameter("delete" + component.getId());
                    if (aParam != null && aParam.equals("delete")) {
                        deleteEm.add(component);
                    }
                    aParam = req.getParameter("target" + component.getId());
                    if (aParam != null) {
                        component.setTargetID(Integer.parseInt(aParam));
                    }
                    break;
            }
        }

        Enumeration<MailingComponent> en=deleteEm.elements();
        while(en.hasMoreElements()) {

        	MailingComponent mailingComponent = en.nextElement();
        	componentDao.deleteMailingComponent(mailingComponent);
        	aMailing.getComponents().remove( mailingComponent.getComponentName());
        }

        mailingDao.saveMailing(aMailing, false);
    }

	private boolean checkNewAttachmentSize(ComMailingAttachmentsForm form, ActionMessages errors, ActionMessages messages) {
		if (form.getNewAttachment().getFileSize() > configService.getIntegerValue(ConfigValue.MaximumUploadAttachmentSize)) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("component.size.error", configService.getIntegerValue(ConfigValue.MaximumUploadAttachmentSize) / 1024f / 1024));
			return false;
		} else if (form.getNewAttachment().getFileSize() > configService.getIntegerValue(ConfigValue.MaximumWarningAttachmentSize)) {
			messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.component.size", configService.getIntegerValue(ConfigValue.MaximumWarningAttachmentSize) / 1024f / 1024));
		}
		return true;
	}

    private WebApplicationContext getApplicationContext(HttpServletRequest req){					// TODO: Remove this method to reduce dependency to Spring!
        return WebApplicationContextUtils.getRequiredWebApplicationContext(req.getSession().getServletContext());
    }

    public ComMailingDao getMailingDao() {
        return mailingDao;
    }

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    public ComTargetDao getTargetDao() {
        return targetDao;
    }

    @Required
    public void setTargetDao(ComTargetDao targetDao) {
        this.targetDao = targetDao;
    }

    public ComMailingComponentDao getComponentDao() {
        return componentDao;
    }

    @Required
    public void setComponentDao(ComMailingComponentDao componentDao) {
        this.componentDao = componentDao;
    }

    public MailingComponentFactory getMailingComponentFactory() {
        return mailingComponentFactory;
    }

    @Required
    public void setMailingComponentFactory(MailingComponentFactory mailingComponentFactory) {
        this.mailingComponentFactory = mailingComponentFactory;
    }

    @Required
    public void setUploadDao(ComUploadDao uploadDao) {
        this.uploadDao = uploadDao;
    }

    /**
     * Set service for reading configuration values.
     *
     * @param configService service for reading configuration values
     */
    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Required
    public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
        this.mailingBaseService = mailingBaseService;
    }
    
    @Required
    public final void setMaildropService(final MaildropService service) {
    	this.maildropService = service;
    }
    
    @Required
    public final void setMimeTypeWhitelistService(final MimeTypeWhitelistService service) {
    	this.mimetypeWhitelistService = Objects.requireNonNull(service, "Mimetype whitelist service is null");
    }
}
