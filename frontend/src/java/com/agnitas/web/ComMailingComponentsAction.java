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
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.dao.MailingDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
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
import org.apache.commons.text.StringEscapeUtils;
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
import com.agnitas.emm.core.components.dto.NewFileDto;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.components.service.ComMailingComponentsService.ImportStatistics;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.trackablelinks.service.ComTrackableLinkService;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ServiceResult;
import com.agnitas.util.ImageUtils;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.forms.ComMailingComponentsForm;

public class ComMailingComponentsAction extends StrutsActionBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingComponentsAction.class);
	
	public static final int ACTION_SAVE_COMPONENTS = ACTION_LAST + 1;

	public static final int ACTION_BULK_DOWNLOAD_COMPONENT = ACTION_LAST + 3;

	public static final int ACTION_UPLOAD_ARCHIVE = ACTION_LAST + 4;
	
	public static final int ACTION_UPLOAD_SFTP = ACTION_LAST + 5;

    public static final int ACTION_RELOAD_IMAGE = ACTION_LAST + 9;

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
        case ACTION_BULK_DOWNLOAD_COMPONENT:
            return "bulk_download_component";
        case ACTION_UPLOAD_ARCHIVE:
            return "upload_archive";
        case ACTION_UPLOAD_SFTP:
            return "upload_sftp";
        case ACTION_RELOAD_IMAGE:
            return "reload_image";
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

    	assert admin != null;

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
                        if (mailingComponentService.deleteHostedImages(companyId, aForm.getMailingID(), aForm.getBulkIds())) {
                            previewImageService.generateMailingPreview(admin, request.getSession().getId(), aForm.getMailingID(), true);
                        }

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

                case ACTION_RELOAD_IMAGE:
                    reloadImage(admin, aForm, request.getSession().getId(), messages, errors);
                    aForm.setAction(ACTION_LIST);
                    destination = mapping.findForward("list");
                    break;

                case ACTION_UPDATE_HOST_IMAGE:
                    updateHostImage(admin, aForm, request.getSession().getId(), messages, errors);
                    aForm.setAction(ACTION_LIST);
                    destination = mapping.findForward("list");
                    break;

                case ACTION_SAVE_COMPONENTS:
                    saveComponents(admin, aForm, request.getSession().getId(), messages, errors);
                    aForm.setAction(ACTION_LIST);
                	destination = mapping.findForward("list");
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
                            String filename = getExportFilename(admin, aForm) + ".zip";

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
                    if (mailingComponent != null && mailingComponent.getType() == MailingComponent.TYPE_HOSTED_IMAGE) {
                        mailingComponentService.deleteComponent(mailingComponent);
                        previewImageService.generateMailingPreview(admin, request.getSession().getId(), aForm.getMailingID(), true);

                        if (StringUtils.isEmpty(mailingName)) {
                            mailingName = mailingBaseService.getMailingName(aForm.getMailingID(), companyId);
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

    private List<NewFileDto> getNewFiles(ComMailingComponentsForm form) {
	    List<NewFileDto> newImages = new ArrayList<>();

	    for (int index : form.getIndices()) {
	        FormFile file = form.getNewFile(index);

	        if (StringUtils.isNotEmpty(file.getFileName())) {
                newImages.add(new NewFileDto(
                        file,
                        form.getLink(index),
                        form.getDescriptionByIndex(index),
                        form.getMobileComponentBaseComponent(index)
                ));
            }
        }

	    return newImages;
    }

    private boolean containsArchives(List<NewFileDto> newImages) {
	    for (NewFileDto newFile : newImages) {
	        if (StringUtils.endsWithIgnoreCase(newFile.getFile().getFileName(), ".zip")) {
	            return true;
            }
        }

	    return false;
    }

    private void saveComponents(ComAdmin admin, ComMailingComponentsForm form, String sessionId, ActionMessages messages, ActionMessages errors) {
	    // Make sure that all uploaded files are allowed images or zip-archives.
	    if (validateFileExtensions(admin, form, errors)) {
	        // Validate links for images (not applicable for zip-archives).
            if (validateFileLinks(form, errors)) {
                List<NewFileDto> newImages = getNewFiles(form);

                if (newImages.size() > 0) {
                    List<UserAction> userActions = new ArrayList<>();
                    ServiceResult<ImportStatistics> result = mailingComponentService.importImagesBulk(admin, form.getMailingID(), newImages, userActions);

                    if (result.isSuccess()) {
                        previewImageService.generateMailingPreview(admin, sessionId, form.getMailingID(), true);

                        // Files found / files effectively stored.
                        ImportStatistics statistics = result.getResult();

                        // Show numbers if multiple files or at least one archive uploaded (otherwise show default success message).
                        if (statistics.getFound() > 1 || containsArchives(newImages)) {
                            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("items_saved", statistics.getStored(), statistics.getFound()));
                        } else {
                            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                        }

                        userActions.forEach(userAction -> writeUserActivityLog(admin, userAction));
                    }

                    result.extractMessagesTo(messages, errors);
                } else {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.errors.no_component_file"));
                }
            }
        }
    }

    private void uploadSftp(HttpServletRequest req, ComMailingComponentsForm aForm, ActionMessages errors, ActionMessages messages) {
		String sftpFilePath = aForm.getSftpFilePath();

		try {
		    if (StringUtils.isEmpty(sftpFilePath)) {
		        errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.errors.no_sftp_file"));
		    } else {
		        final ComAdmin admin = AgnUtils.getAdmin(req);
                final int companyId = AgnUtils.getCompanyID(req);
                final String serverAndCredentials = configService.getEncryptedValue(ConfigValue.DefaultSftpServerAndCredentials, companyId);
                final String privateKey = configService.getEncryptedValue(ConfigValue.DefaultSftpPrivateKey, companyId);

                List<UserAction> userActions = new ArrayList<>();
                ServiceResult<ImportStatistics> result = mailingComponentService.importImagesFromSftp(admin, aForm.getMailingID(), serverAndCredentials, privateKey, sftpFilePath, userActions);

                if (result.isSuccess()) {
                    previewImageService.generateMailingPreview(admin, req.getSession().getId(), aForm.getMailingID(), true);

                    // Files found / files effectively stored.
                    ImportStatistics statistics = result.getResult();
                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("items_saved", statistics.getStored(), statistics.getFound()));

                    userActions.forEach(userAction -> writeUserActivityLog(admin, userAction));
                }

                result.extractMessagesTo(messages, errors);
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

    private boolean validateFileLinks(ComMailingComponentsForm form, ActionMessages errors) {
    	List<String> filenames = new Vector<>();

    	for (int index : form.getIndices()) {
    	    FormFile file = form.getNewFile(index);
    	    String filename = file.getFileName();

    	    String extension = AgnUtils.getFileExtension(filename);

    	    // Links are not applicable for zip-archives so ignore.
    	    if (!extension.equalsIgnoreCase("zip")) {
                String link = form.getLink(index);

                if (StringUtils.isNotEmpty(link) && (link.contains(" ") || link.contains("\"") || link.contains("'"))) {
                    filenames.add(filename);
                }
            }
    	}

    	if (filenames.size() > 0) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("mailing.error.invalidLinkTarget", filenamesToHtml(filenames)));
            return false;
        }

        return true;
    }

    private boolean validateFileExtensions(ComAdmin admin, ComMailingComponentsForm form, ActionMessages errors) {
	    Set<String> filenames = new LinkedHashSet<>();

        for (FormFile file : form.getAllFiles().values()) {
            String name = file.getFileName();

            if (StringUtils.isEmpty(name)) {
                filenames.add("<blank>");
            } else {
                String extension = AgnUtils.getFileExtension(name);
                if (!ImageUtils.isValidImageFileExtension(extension) && !"zip".equalsIgnoreCase(extension)) {
                    filenames.add(name);
                }
            }
        }

        if (filenames.size() > 0) {
            String message = I18nString.getLocaleString("grid.divchild.format.error", admin.getLocale()) + filenamesToHtml(filenames);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(message, false));
            return false;
        }

        return true;
    }

    private String filenamesToHtml(Collection<String> filenames) {
        StringBuilder sb = new StringBuilder();

        for (String filename : filenames) {
            sb.append("<br><b>").append(StringEscapeUtils.escapeHtml4(filename)).append("</b>");
        }

        return sb.toString();
    }

    protected List<MailingComponent> loadComponents(ComMailingComponentsForm form, HttpServletRequest request) {
        List<MailingComponent> components = mailingComponentService.getComponentsByType(AgnUtils.getCompanyID(request),
                form.getMailingID(), Arrays.asList(MailingComponent.TYPE_HOSTED_IMAGE, MailingComponent.TYPE_IMAGE));
        request.setAttribute("components", components);
        return components;
    }

    private void reloadImage(ComAdmin admin, ComMailingComponentsForm form, String sessionId, ActionMessages messages, ActionMessages errors) {
	    int mailingId = form.getMailingID();
	    int componentId = form.getComponentId();
	    String mailingName = form.getShortname();

        ServiceResult<Boolean> result = mailingComponentService.reloadImage(admin, mailingId, componentId);

        if (result.isSuccess()) {
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));

            if (result.getResult()) {
                // Re-generate mailing preview iff component has been changed.
                previewImageService.generateMailingPreview(admin, sessionId, mailingId, true);
            }

            if (StringUtils.isEmpty(mailingName)) {
                mailingName = mailingBaseService.getMailingName(mailingId, admin.getCompanyID());
            }

            writeUserActivityLog(admin, "update mailing component",
                    String.format("%s(%d), reloaded component (%d)", mailingName, mailingId, componentId));
        }

        result.extractMessagesTo(messages, errors);
    }

    private void updateHostImage(ComAdmin admin, ComMailingComponentsForm form, String sessionId, ActionMessages messages, ActionMessages errors) {
        int mailingId = form.getMailingID();
        int componentId = form.getComponentId();
        String mailingName = form.getShortname();

        String imgBase64 = form.getImageFile();
        if (StringUtils.isNotBlank(imgBase64)) {
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));

            if (mailingComponentService.updateHostImage(mailingId, admin.getCompanyID(), componentId, Base64.decodeBase64(imgBase64))) {
                // Re-generate mailing preview iff component has been changed.
                previewImageService.generateMailingPreview(admin, sessionId, mailingId, true);
            }

            if (StringUtils.isEmpty(mailingName)) {
                mailingName = mailingBaseService.getMailingName(mailingId, admin.getCompanyID());
            }

            writeUserActivityLog(admin, "update mailing component",
                    String.format("%s(%d), edited component (%d)", mailingName, mailingId, componentId));
        } else {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));
        }
    }
    
    protected String getExportFilename(ComAdmin admin, ComMailingComponentsForm aForm) {
	    SimpleDateFormat dateFormat = DateUtilities.getFormat(DateUtilities.YYYYMD, TimeZone.getTimeZone(admin.getAdminTimezone()));
        return "Uploaded_images_mailingId_"+ aForm.getMailingID()+ "_" + dateFormat.format(new Date());
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
