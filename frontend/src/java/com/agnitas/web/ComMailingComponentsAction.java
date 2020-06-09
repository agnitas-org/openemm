/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.impl.MailingComponentImpl;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.SFtpHelper;
import org.agnitas.util.SFtpHelperFactory;
import org.agnitas.util.SafeString;
import org.agnitas.util.ZipUtilities;
import org.agnitas.web.StrutsActionBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;
import com.agnitas.util.ImageUtils;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.forms.ComMailingComponentsForm;

public class ComMailingComponentsAction extends StrutsActionBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingComponentsAction.class);
	
	public static final int ACTION_SAVE_COMPONENTS = ACTION_LAST + 1;

	public static final int ACTION_SAVE_COMPONENT_EDIT = ACTION_LAST + 2;

	public static final int ACTION_BULK_DOWNLOAD_COMPONENT = ACTION_LAST + 3;

	public static final int ACTION_UPLOAD_ARCHIVE = ACTION_LAST + 4;
	
	public static final int ACTION_UPLOAD_SFTP = ACTION_LAST + 5;

    public static final int ACTION_UPDATE_HOST_IMAGE = ACTION_LAST + 6;
    
    public static final int ACTION_BULK_CONFIRM_DELETE = ACTION_LAST + 7;
    
    public static final int ACTION_BULK_DELETE = ACTION_LAST + 8;

    protected MailingDao mailingDao;
	
    protected ComTrackableLinkService linkService;

    protected ComMailingBaseService mailingBaseService;
	
	protected ConfigService configService;

    protected SFtpHelperFactory sFtpHelperFactory;
    
    private MaildropService maildropService;

    private MailinglistApprovalService mailinglistApprovalService;
    
    private PreviewImageService previewImageService;
    
    private ComMailingComponentsService mailingComponentService;
    
    // --------------------------------------------------------------------------- Dependency Injection

	@Required
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
	   
    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

	@Required
    public void setLinkService(ComTrackableLinkService linkService) {
        this.linkService = linkService;
    }

    @Required
    public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
        this.mailingBaseService = mailingBaseService;
    }

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

    @Required
    public void setsFtpHelperFactory(SFtpHelperFactory sFtpHelperFactory) {
        this.sFtpHelperFactory = sFtpHelperFactory;
    }
    
	@Required
	public void setPreviewImageService(PreviewImageService previewImageService) {
		this.previewImageService = previewImageService;
	}

	@Required
	public void setMailingComponentService( ComMailingComponentsService mailingComponentService) {
		this.mailingComponentService = mailingComponentService;
	}

    @Required
    public final void setMaildropService(final MaildropService service) {
        this.maildropService = service;
    }

    // --------------------------------------------------------------------------- Business Logic
	
    @Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
        case ACTION_SAVE_COMPONENTS:
            return "save_components";
        case ACTION_SAVE_COMPONENT_EDIT:
            return "save_component_edit";
        case ACTION_BULK_DOWNLOAD_COMPONENT:
            return "bulk_download_component";
        case ACTION_UPLOAD_ARCHIVE:
            return "upload_archive";
        case ACTION_UPLOAD_SFTP:
            return "upload_sftp";
        case ACTION_UPDATE_HOST_IMAGE:
            return "update_image";
        case ACTION_BULK_CONFIRM_DELETE:
            return "bulk_confirm_delete";
        case ACTION_BULK_DELETE:
            return "bulk_delete";
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
     * @param request
     * @param response
     * @param mapping The ActionMapping used to select this instance
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     * @return destination
     */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	 // Validate the request parameters specified by the user
        ComMailingComponentsForm aForm;
        ActionMessages errors = new ActionMessages();
    	ActionMessages messages = new ActionMessages();
    	ActionForward destination=null;
    	ComAdmin admin = AgnUtils.getAdmin(request);

        aForm = (ComMailingComponentsForm) form;
        if (logger.isInfoEnabled()) {
        	logger.info("Action: "+aForm.getAction());
        }
        int companyId = AgnUtils.getCompanyID(request);

        String mailingName = aForm.getShortname();
        try {
            switch(aForm.getAction()) {
                case ACTION_BULK_CONFIRM_DELETE:
                    if (CollectionUtils.isEmpty(aForm.getBulkIds())) {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("bulkAction.nothing.image"));
                        destination = mapping.findForward("messages");
                        break;
                    }
                    
                    aForm.setAction(ACTION_BULK_DELETE);
                    destination = mapping.findForward("bulk_delete_confirm");
                    break;
                    
                case ACTION_BULK_DELETE:
                    try {
                        mailingComponentService.deleteComponents(companyId, aForm.getMailingID(), aForm.getBulkIds());
                        if (StringUtils.isEmpty(mailingName)) {
                            mailingName = mailingBaseService.getMailingName(aForm.getMailingID(), companyId);
                        }
                        writeUserActivityLog(admin, "delete mailing components",
                                String.format("%s(%d), deleted components (IDS: %s)",
                                        mailingName,
                                        aForm.getMailingID(),
                                        StringUtils.join(aForm.getBulkIds(), ", ")
                                ));
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                    } catch (Exception e) {
                    	logger.error(e);
                    }
                    aForm.setAction(ACTION_LIST);
                    destination = mapping.findForward("list");
                    break;
                
                case ACTION_UPDATE_HOST_IMAGE:
                    int componentId = aForm.getComponentId();
                    int mailingId = aForm.getMailingID();

                    String imgBase64 = aForm.getImageFile();
                    if (StringUtils.isNotBlank(imgBase64)) {
                        String imgUri = imgBase64.split(",")[1];
                        byte[] imageBytes = Base64.decodeBase64(imgUri);
                        mailingComponentService.updateHostImage(mailingId, companyId, componentId, imageBytes);
                        if (StringUtils.isEmpty(mailingName)) {
                            mailingBaseService.getMailingName(mailingId, companyId);
                        }
                        writeUserActivityLog(admin, "update mailing component",
                                String.format("%s(%d), edited component (%d)", mailingName, mailingId, componentId));
                    }
                    destination = mapping.findForward("list");
                    break;

                case ACTION_SAVE_COMPONENTS:
                    destination = mapping.findForward("list");
                    Mailing mailing = mailingDao.getMailing(aForm.getMailingID(), companyId);
            		try {
                        boolean existArchiveFile = false;
                        int stored = 0;
                        int found = 0;
                        for (FormFile file : aForm.getAllFiles().values()) {
                            if (file != null && StringUtils.endsWithIgnoreCase(file.getFileName(), ".zip")) {
                                ComMailingComponentsService.UploadStatistics statistics = mailingComponentService.uploadZipArchive(mailing, file);
                                stored += statistics.getStored();
                                found += statistics.getFound();
                                existArchiveFile = true;
                            }
                        }
                        if (!existArchiveFile) {
		                    List<String> invalidLinksFilenameList = graphicLinkTargetsContainsInvalidCharacters(aForm);
		                    if (invalidLinksFilenameList.size() > 0) {
		                    	errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.error.invalidLinkTarget", invalidLinksFilenameList));
		                    	break;
		                    }
		                    
		                    if (!isValidPictureFiles(aForm)) {
		                        aForm.setAction(ACTION_SAVE_COMPONENTS);
		                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("grid.divchild.format.error"));
		                        break;
		                    }
		                    
		                    if (checkNewComponentSizes(aForm, errors, messages)) {
			                    if (!saveComponent(aForm, request, messages, errors)) {
			                        previewImageService.generateMailingPreview(request, aForm.getMailingID(), true);
			                    }
			                    aForm.setAction(ACTION_SAVE_COMPONENTS);
			                    Enumeration<String> parameterNames = request.getParameterNames();
			                    boolean aComponentWasJustDeleted = false;
			                    boolean aComponentWasJustUpdated = false;
			                    while (parameterNames.hasMoreElements()) {
			                    	String parameterString = parameterNames.nextElement();
			                        if (parameterString.startsWith("delete") && AgnUtils.parameterNotEmpty(request, parameterString)){
			                            aComponentWasJustDeleted = true;
			                            break;
			                        }
			                        if (parameterString.startsWith("update") && AgnUtils.parameterNotEmpty(request, parameterString)){
			                            aComponentWasJustUpdated = true;
			                            break;
			                        }
			                    }
			
			                    // Show "changes saved" or error message
			                    if (aComponentWasJustUpdated || aComponentWasJustDeleted) {
			                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			                    } else {
			                        Set<Integer> indices = aForm.getIndices();
			                        boolean componentsAreValid = !indices.isEmpty();
			                        if (componentsAreValid) {
			                            for (Integer index : indices) {
			                                FormFile file = aForm.getNewFile(index);
			                                if (StringUtils.isEmpty(file.getFileName())) {
			                                    componentsAreValid = false;
			                                    break;
			                                }
			                            }
			                        }
			
			                        if (componentsAreValid) {
			                            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			                        } else {
			                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.errors.no_component_file"));
			                        }
			                    }
		                    }
                        } else {
                            if (stored > 0) {
                                mailingDao.saveMailing(mailing, false);
                                mailingName = StringUtils.defaultString(mailing.getShortname());
                                writeUserActivityLog(admin, "upload archive", String.format("%s(%d), uploaded images from archive", mailingName, aForm.getMailingID()));
                    		}
                            if (found > 0) {
                                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("items_saved", stored, found));
                            }
                            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                        }
                    } catch (Exception e) {
                        logger.error("error uploading ZIP archive", e);
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.Graphics_Component.zipUploadFailed"));
                	}
                	
                	destination = mapping.findForward("list");
                	break;

                case ACTION_SAVE_COMPONENT_EDIT:
                    destination=mapping.findForward("component_edit");
                    saveComponent(aForm, request, messages, errors);
                    aForm.setAction(ACTION_SAVE_COMPONENTS);

                    // Show "changes saved"
                	messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                    break;
                case ACTION_BULK_DOWNLOAD_COMPONENT:
                    aForm.setAction(ACTION_SAVE_COMPONENTS);
                    destination = mapping.findForward("list");
                    if (CollectionUtils.isEmpty(aForm.getBulkIds())) {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("bulkAction.nothing.image"));
                        break;
                    }
                    
                    File zipFile = createComponentsZipFile(companyId, aForm.getMailingID(), aForm.getBulkIds());

                    if (zipFile != null) {
                        try (FileInputStream instream = new FileInputStream(zipFile)) {
                            String filename = getExportFilename(aForm) + ".zip";

                            response.setContentType("application/zip");
			                HttpUtils.setDownloadFilenameHeader(response, filename);
                            response.setContentLength((int) zipFile.length());

                            try (ServletOutputStream ostream = response.getOutputStream()) {
                                IOUtils.copy(instream, ostream);
                                return null;
                            }
                        } catch (Exception e) {
                            logger.error("Images archive creation failed", e);
                            ActionMessage msg = new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
                            errors.add(ActionMessages.GLOBAL_MESSAGE, msg);
                        } finally {
                        	if (zipFile.exists() && !zipFile.delete()) {
                                logger.error("Cannot delete temporary archive file");
                        	}
                        	writeUserActivityLog(AgnUtils.getAdmin(request), "do bulk download mailing component", "MailingID: " + aForm.getMailingID(), logger);
                        }
                    } else {
                        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.Graphics_Component.NoImage"));
                    }

                    aForm.setAction(ACTION_SAVE_COMPONENTS);
                    destination = mapping.findForward("list");
                    break;
                    
                case ACTION_UPLOAD_SFTP:
                	uploadSftp(request, aForm, errors, messages);
                	destination = mapping.findForward("list");
                	break;

                case ACTION_CONFIRM_DELETE:
                    MailingComponent component = mailingComponentService.getComponent(aForm.getComponentId(), companyId);
                    aForm.setComponentName(component.getComponentName());
                    aForm.setAction(ACTION_DELETE);
                    destination = mapping.findForward("delete");
                    break;

                case ACTION_DELETE:
                    MailingComponent mailingComponent = mailingComponentService.getComponent(aForm.getComponentId(), companyId);
                    if (mailingComponent != null) {
                        mailingComponentService.deleteComponent(mailingComponent);
                        if (StringUtils.isEmpty(mailingName)) {
                            mailingName = mailingBaseService.getMailingName(aForm.getMailingID(), aForm.getComponentId());
                        }
                        mailingName = StringUtils.defaultString(mailingName);
                        writeUserActivityLog(admin, "delete mailing component",
                                String.format("%s(%d), deleted component (%d, %s)",
                                        mailingName,
                                        aForm.getMailingID(),
                                        aForm.getComponentId(),
                                        mailingComponent.getComponentName() != null ? "'" + mailingComponent.getComponentName() + "'" : "unnamed"
                                ));
                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
                    } else {
                        logger.warn(String.format("Could not find component (ID: %d) to delete for mailing (ID: %d)", aForm.getComponentId(), aForm.getMailingID()));
                    }
                    aForm.setAction(ACTION_LIST);
                    destination = mapping.findForward("list");
                    break;

                case ACTION_LIST:
                    writeUserActivityLog(AgnUtils.getAdmin(request), "images list", "active tab - images");
	                //$FALL-THROUGH$
				default:
                    aForm.setAction(ACTION_LIST);
                    destination = mapping.findForward("list");
                    break;
            }

    		if (AgnUtils.allowed(request, Permission.MAILING_COMPONENTS_SFTP)) {
                final Locale locale = AgnUtils.getLocale(request);
    			try {
					String sftpServerAndCredentials = configService.getEncryptedValue(ConfigValue.DefaultSftpServerAndCredentials, companyId);
					String sftpPrivateKey = configService.getEncryptedValue(ConfigValue.DefaultSftpPrivateKey, companyId);
					if (StringUtils.isNotEmpty(sftpServerAndCredentials)) {
						try (SFtpHelper sFtpHelper = sFtpHelperFactory.createSFtpHelper(sftpServerAndCredentials)) {
			                if (StringUtils.isNotBlank(sftpPrivateKey)) {
			                	sFtpHelper.setPrivateSshKeyData(sftpPrivateKey);
			                }
							sFtpHelper.setAllowUnknownHostKeys(true);
							sFtpHelper.connect();
							String sftpServerWithoutCredentials = sFtpHelper.getSetUpDataWithoutString();
							if (!sftpServerWithoutCredentials.endsWith("/")) {
								sftpServerWithoutCredentials += "/";
							}
							request.setAttribute("sftpServer", sftpServerWithoutCredentials);
							List<String> sftpFileList = sFtpHelper.scanForFiles(".*\\.(jpg|jpeg|gif|bmp|tif|tiff|png|svg)", true);
							request.setAttribute("sftpFiles", sftpFileList);
						}
					} else {
						request.setAttribute("sftpServer", SafeString.getLocaleString("error.sftp_upload.no_server_config", locale));
						request.setAttribute("sftpFiles", new ArrayList<>());
					}
				} catch (Exception e) {
					logger.error("SFTP-server configuration is invalid: " + e.getMessage(), e);
					request.setAttribute("sftpServer", SafeString.getLocaleString("error.sftp_upload.invalid_server_config", locale));
				}
    		}
        } catch (Exception e) {
            logger.error("execute: "+e, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

		if (destination != null && "list".equals(destination.getName())) {
            loadMailing(aForm, request);
            loadAdditionalImagesData(aForm, request);
            
			List<MailingComponent> components = loadComponents(aForm, request);
			request.setAttribute("components", components);
			AgnUtils.setAdminDateTimeFormatPatterns(request);
			List<Integer> linkIds = components.stream().map(MailingComponent::getUrlID).collect(Collectors.toList());
			List<ComTrackableLink> links = linkService.getTrackableLinks(companyId, linkIds);
			request.setAttribute("componentLinks", links);
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
    
	private boolean checkNewComponentSizes(ComMailingComponentsForm form, ActionMessages errors, ActionMessages messages) {
		for (int index : form.getIndices()) {
			if (form.getNewFile(index).getFileSize() > configService.getIntegerValue(ConfigValue.MaximumUploadImageSize)) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("component.size.error", configService.getIntegerValue(ConfigValue.MaximumUploadImageSize) / 1024f / 1024));
				return false;
			} else if (form.getNewFile(index).getFileSize() > configService.getIntegerValue(ConfigValue.MaximumWarningImageSize)) {
				messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.component.size", configService.getIntegerValue(ConfigValue.MaximumWarningImageSize) / 1024f / 1024));
			}
		}
		return true;
	}
 
	private void uploadSftp(HttpServletRequest req, ComMailingComponentsForm aForm, ActionMessages errors, ActionMessages messages) throws Exception {
		String sftpFilePath = aForm.getSftpFilePath();

		try {
		    if (StringUtils.isEmpty(sftpFilePath)) {
		        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.errors.no_sftp_file"));
		    } else {
                final int companyId = AgnUtils.getCompanyID(req);
                final String serverAndCredentials = configService.getEncryptedValue(ConfigValue.DefaultSftpServerAndCredentials, companyId);
                final String privateKey = configService.getEncryptedValue(ConfigValue.DefaultSftpPrivateKey, companyId);

                Mailing mailing = mailingDao.getMailing(aForm.getMailingID(), companyId);
		        ComMailingComponentsService.UploadStatistics statistics = mailingComponentService.uploadSFTP(mailing, serverAndCredentials, privateKey, sftpFilePath);

                if (statistics.getStored() > 0) {
                    mailingDao.saveMailing(mailing, false);
                } else if (statistics.getFound() <= 0) {
                    throw new Exception("File(s) not found on SFTP server");
                }
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("items_saved", statistics.getStored(), statistics.getFound()));
		    }
		} catch (Exception e) {
		    logger.error("Error uploading SFTP archive", e);
		    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.errors.sftpUploadFailed"));
		}
    }
    
    protected void loadMailing(ComMailingComponentsForm form, HttpServletRequest req) {
        final int companyId = AgnUtils.getCompanyID(req);
        final int mailingId = form.getMailingID();

        if (form.getDescriptionByIndex(1) != null) {
            form.setDescriptionByIndex(1, null);
        }
        if (form.getLink(1) != null) {
            form.setLink(1, null);
        }
        
        Mailing aMailing = mailingDao.getMailing(mailingId, companyId);

        form.setShortname(aMailing.getShortname());
        form.setDescriptionByIndex(1, "");
        form.setIsTemplate(aMailing.isIsTemplate());
        form.setLink(1, "");
        form.setWorldMailingSend(this.maildropService.isActiveMailing(aMailing.getId(), companyId));

        if (mailingId > 0) {
            form.setIsMailingUndoAvailable(mailingBaseService.checkUndoAvailable(mailingId));
            form.setWorkflowId(mailingBaseService.getWorkflowId(mailingId, companyId));
        } else {
            form.setIsMailingUndoAvailable(false);
            form.setWorkflowId(0);
        }

        req.setAttribute("limitedRecipientOverview",
				form.isWorldMailingSend() &&
						!mailinglistApprovalService.isAdminHaveAccess(AgnUtils.getAdmin(req), aMailing.getMailinglistID()));
        
        if (logger.isInfoEnabled()) {
            logger.info("loadMailing: mailing loaded");
        }
    }
    
    private void loadAdditionalImagesData(ComMailingComponentsForm form, HttpServletRequest req) {
        DateFormat dateFormat = new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS);
		form.setFileSizes(mailingComponentService.getImageSizes(AgnUtils.getCompanyID(req), form.getMailingID()));
		form.setTimestamps(mailingComponentService.getImageTimestamps(AgnUtils.getCompanyID(req), form.getMailingID(), dateFormat));
	}
 
	protected void addUploadedFiles(ComMailingComponentsForm componentForm, Mailing mailing, HttpServletRequest req, ActionMessages messages, ActionMessages errors) throws Exception {
		Set<Integer> indices = componentForm.getIndices();

		int foundItemsToStore = 0;
		int successfullyStoredItems = 0;
		
		for (int index : indices) {
			// Check if any part of this item was filled at all
            FormFile file = componentForm.getNewFile(index);
            if (file != null && StringUtils.isNotBlank(file.getFileName()) && file.getFileName().toLowerCase().endsWith(".zip")) {
                ComMailingComponentsService.UploadStatistics statistics = mailingComponentService.uploadZipArchive(mailing, file);
                foundItemsToStore += statistics.getFound();
                successfullyStoredItems += statistics.getStored();
                writeUserActivityLog(AgnUtils.getAdmin(req), "upload mailing component file", "Mailing ID: " + mailing.getId() + ", type: ZIP, Name: " + file.getFileName() + ", found items to store: " + statistics.getFound() + ", successfully stored items: " + statistics.getStored());
            } else {
	            String link = componentForm.getLink(index);
	            String description = componentForm.getDescriptionByIndex(index);
	            String baseComponentForMobileComponent = componentForm.getMobileComponentBaseComponent(index);
	
				if ((file != null && StringUtils.isNotBlank(file.getFileName()))
						|| StringUtils.isNotBlank(link)
						|| StringUtils.isNotBlank(description)
						|| StringUtils.isNotBlank(baseComponentForMobileComponent)) {
					foundItemsToStore++;
					// Check if mandatory parts are missing
					if (file == null || StringUtils.isBlank(file.getFileName())) {
		                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.errors.no_component_file"));
		            } else {
						String newComponentName = null;
						if (StringUtils.isNotBlank(baseComponentForMobileComponent)) {
							newComponentName = ShowImageServlet.MOBILE_IMAGE_PREFIX + baseComponentForMobileComponent;
							if (StringUtils.isBlank(description)) {
	                            description = "Mobile component for " + baseComponentForMobileComponent;
							} else {
	                            description = description + " / Mobile component for " + baseComponentForMobileComponent;
							}
						}
		
						if (StringUtils.isBlank(newComponentName)) {
	                        newComponentName = file.getFileName();
	                    }
	
	                    try {
	                        if (file.getFileSize() > 0) {
	                        	MailingComponent component = mailing.getComponents().get(newComponentName);
								if (component != null && component.getType() == MailingComponent.TYPE_HOSTED_IMAGE) {
									// Update existing image
									component.setBinaryBlock(file.getFileData(), file.getContentType());
									component.setLink(link);
									component.setDescription(description);
								} else {
									// Store new image
									component = new MailingComponentImpl();
									component.setCompanyID(mailing.getCompanyID());
									component.setMailingID(componentForm.getMailingID());
									component.setType(MailingComponent.TYPE_HOSTED_IMAGE);
									component.setDescription(description);
									component.setComponentName(newComponentName);
									component.setBinaryBlock(file.getFileData(), file.getContentType());
									component.setLink(link);
									mailing.addComponent(component);
								}
							}
						} catch (Exception e) {
							logger.error("saveComponent: " + e);
						}
	
						if (componentForm.getAction() == ACTION_SAVE_COMPONENT_EDIT) {
							req.setAttribute("file_path",
									AgnUtils.getAdmin(req).getCompany().getRdirDomain() + "/image?ci=" + mailing.getCompanyID() + "&mi=" + componentForm.getMailingID() + "&name=" + file.getFileName());
						}
						
						// Reset MobileComponentBaseComponent for next upload request from gui
	                    componentForm.setMobileComponentBaseComponent(index, "");
						successfullyStoredItems++;
                        writeUserActivityLog(AgnUtils.getAdmin(req), "upload mailing component file", "Mailing ID: "+mailing.getId() + ", type: image, name: " + newComponentName);
		            }
				}
            }
		}
		
		if (foundItemsToStore > 0) {
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("items_saved", successfullyStoredItems, foundItemsToStore));
		}
	}
    
    private List<String> graphicLinkTargetsContainsInvalidCharacters(ComMailingComponentsForm form) {
    	List<String> filenames = new Vector<>();

    	Set<Integer> indices = form.getIndices();
    	for (int index : indices) {
    		String link = form.getLink(index);
    		
    		if (link == null || link.contains(" ") || link.contains("\"") || link.contains("'")) {
    			filenames.add(form.getNewFile(index).getFileName());
    		}
    	}
    	
    	return filenames;
    }
    
    private boolean isValidPictureFiles(ComMailingComponentsForm form) {
        for (FormFile file : form.getAllFiles().values()) {
            String name = file.getFileName();

            if (StringUtils.isEmpty(name)) {
                return false;
            }

            String extension = AgnUtils.getFileExtension(name);
            if (!ImageUtils.isValidImageFileExtension(extension) && !"zip".equalsIgnoreCase(extension)) {
                return false;
            }
        }
        return true;
    }
    
    protected List<MailingComponent> loadComponents(ComMailingComponentsForm form, HttpServletRequest request) {
        List<MailingComponent> components = mailingComponentService.getComponentsByType(AgnUtils.getCompanyID(request),
                form.getMailingID(), Arrays.asList(MailingComponent.TYPE_HOSTED_IMAGE, MailingComponent.TYPE_IMAGE));
        request.setAttribute("components", components);
        return components;
    }
    
    protected void checkAndRemoveUploadDuplicates(Mailing aMailing, ComMailingComponentsForm form) {
		// Delete previous images with same name as uploaded image
        for (MailingComponent storedComponent : aMailing.getComponents().values()) {
        	if (storedComponent.getType() == MailingComponent.TYPE_HOSTED_IMAGE) {
        		for (int index : form.getIndices()) {
        			String newComponentName = form.getNewFile(index).getFileName();
        			String baseComponentForMobileComponent = form.getMobileComponentBaseComponent(index);
        			if (StringUtils.isNotBlank(baseComponentForMobileComponent)) {
        				newComponentName = ShowImageServlet.MOBILE_IMAGE_PREFIX + baseComponentForMobileComponent;
        			}
        			
		        	if (storedComponent.getComponentName().equals(newComponentName)) {
		                mailingComponentService.deleteComponent(storedComponent);
		                break;
		            }
        		}
	        }
        }
	}
    
    /**
     * Saves components.
     * @throws Exception
     */
    protected boolean saveComponent(ComMailingComponentsForm aForm, HttpServletRequest req, ActionMessages messages, ActionMessages errors) throws Exception {
        boolean somethingDeleted = false;

        // Retrieves all the components
        Mailing aMailing = mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));
        Map<String, MailingComponent> componentsMap = aMailing.getComponents();
        Set<String> updatedComponents = new HashSet<>();

        for (MailingComponent storedComponent : componentsMap.values()) {
            switch (storedComponent.getType()) {
                case MailingComponent.TYPE_IMAGE:
                    if (AgnUtils.parameterNotEmpty(req, "update" + storedComponent.getId())) {
                        updatedComponents.add(storedComponent.getComponentName());
                        storedComponent.loadContentFromURL();
                    }
                    break;

                case MailingComponent.TYPE_HOSTED_IMAGE:
                    if (AgnUtils.parameterNotEmpty(req, "delete" + storedComponent.getId())) {
                        somethingDeleted = true;
                        mailingComponentService.deleteComponent(storedComponent);
                    }
                    break;
                    
				default:
					break;
            }
        }

		// Delete previous images with same name as uploaded image
        checkAndRemoveUploadDuplicates(aMailing, aForm);

        // Remove untouched components to avoid an unnecessary updating
        Iterator<Map.Entry<String, MailingComponent>> iterator = componentsMap.entrySet().iterator();
        while (iterator.hasNext()) {
            if (!updatedComponents.contains(iterator.next().getKey())) {
                iterator.remove();
            }
        }

        addUploadedFiles(aForm, aMailing, req, messages, errors);

        mailingDao.saveMailing(aMailing, false);

        return !somethingDeleted;
    }
    
    protected String getExportFilename(ComMailingComponentsForm aForm) {
        return "Uploaded_images_mailingId_"+ aForm.getMailingID()+ "_" + new SimpleDateFormat(DateUtilities.YYYYMD).format(new Date());
    }
    
    protected File createComponentsZipFile(int companyId, int mailingId, Set<Integer> selectedComponentIds) throws IOException {
        List<MailingComponent> components = mailingComponentService.getComponents(companyId, mailingId, selectedComponentIds);
        if (CollectionUtils.isEmpty(components)) { //no pictures connected with mailing
            return null;
        }
        File tempZipFile = File.createTempFile("GraphicComponents_" + companyId + "_" + mailingId + "_", ".zip", AgnUtils.createDirectory(AgnUtils.getTempDir()));
        List<String> writtenFilenames = new ArrayList<>();
        ZipOutputStream tempZipOutputStream = null;
        try {
            tempZipOutputStream = ZipUtilities.openNewZipOutputStream(tempZipFile);
            for (MailingComponent component : components) {
                String componentFileType = AgnUtils.getFileExtension(component.getComponentName());
                if (ImageUtils.isValidImageFileExtension(componentFileType)) {
                    String componentFileName = FilenameUtils.removeExtension(component.getComponentName());
                    byte[] bytes = component.getBinaryBlock();
                    if (bytes != null) {
                        String outputFilename = componentFileName + "." + componentFileType;
                        int index = 0;
                        while (writtenFilenames.contains(outputFilename) && index < 100) {
                            outputFilename = componentFileName + "_" + (++index) + "." + componentFileType;
                        }
                        ZipUtilities.addFileDataToOpenZipFileStream(bytes, outputFilename, tempZipOutputStream);
                        writtenFilenames.add(outputFilename);
                    } else {
                        logger.warn(String.format("Cannot add mailings (ID: %d) component %s to zip: fileData is missing",
                                mailingId, component.getComponentName()));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot create tempZipFile: " + tempZipFile.getAbsolutePath(), e);
        } finally {
            if (tempZipOutputStream != null) {
                try {
                    ZipUtilities.closeZipOutputStream(tempZipOutputStream);
                } catch (Exception e) {
                    // Do nothing
                    logger.error("Cannot close tempZipOutputStream: " + tempZipFile.getAbsolutePath(), e);
                }
            }
        }
        return tempZipFile;
    }
}
