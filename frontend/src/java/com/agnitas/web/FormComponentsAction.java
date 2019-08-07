/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.component.service.ComponentService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.ZipUtilities;
import org.agnitas.web.DispatchBaseAction;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.FormComponent;
import com.agnitas.dao.FormComponent.FormComponentType;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.service.MimeTypeService;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.util.ImageUtils;
import com.agnitas.web.forms.FormComponentsForm;

public class FormComponentsAction extends DispatchBaseAction {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(FormComponentsAction.class);
    
	protected ComCompanyDao companyDao;
	
	protected ConfigService configService;
	
	protected UserFormDao userFormDao;
	
    protected ComponentService componentService;
    
    protected MimeTypeService mimeTypeService;

	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setUserFormDao(UserFormDao userFormDao) {
		this.userFormDao = userFormDao;
	}

	@Required
	public void setComponentService(ComponentService componentService) {
		this.componentService = componentService;
	}

	@Required
	public void setMimeTypeService(MimeTypeService mimeTypeService) {
		this.mimeTypeService = mimeTypeService;
	}
	
	@Override
	public ActionForward unspecified(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!AgnUtils.isUserLoggedIn(request)) {
			return mapping.findForward("logon");
		} else {
			return mapping.getInputForward();
		}
	}
	
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionMessages errors = new ActionMessages();
    	ActionMessages messages = new ActionMessages();
    	
    	if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }

		FormComponentsForm actionForm = (FormComponentsForm) form;
		final int companyID = AgnUtils.getCompanyID(request);

		if (!AgnUtils.allowed(request, Permission.MAILING_COMPONENTS_CHANGE)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
        } else {
        	UserForm userForm = userFormDao.getUserForm(actionForm.getFormID(), companyID);
			request.setAttribute("userformNameAndDescription", userForm.getFormName() + (StringUtils.isBlank(userForm.getDescription()) ? "" : " | " + userForm.getDescription()));
			request.setAttribute("userForm", userForm);

			loadImagesData(actionForm, request);

			// Set overwrite default value (is reseted in form when sending 
	        actionForm.setOverwriteExisting(true);
        }
		
		if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }
        if (!messages.isEmpty()) {
        	saveMessages(request, messages);
        }

		loadFormData(actionForm, request);
		
		AgnUtils.setAdminDateTimeFormatPatterns(request);

		return mapping.findForward("list");
	}
	
	public ActionForward upload(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionMessages errors = new ActionMessages();
    	ActionMessages messages = new ActionMessages();

    	if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
		}

		FormComponentsForm actionForm = (FormComponentsForm) form;

		if (!AgnUtils.allowed(request, Permission.MAILING_COMPONENTS_CHANGE)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
        } else {
            saveComponents(actionForm, request, messages, errors);

            // Always show "changes saved"
        	messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));

			loadImagesData(actionForm, request);

			// Set overwrite default value (is reseted in form when sending
	        actionForm.setOverwriteExisting(true);
        }

		if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }
        if (!messages.isEmpty()) {
        	saveMessages(request, messages);
        }

		loadFormData(actionForm, request);
		
		AgnUtils.setAdminDateTimeFormatPatterns(request);

		return mapping.findForward("list");
	}

    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }
        ((FormComponentsForm) form).setMethod("deleteconfirm");

        return mapping.findForward("delete");
    }

	public ActionForward deleteconfirm(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionMessages errors = new ActionMessages();
    	ActionMessages messages = new ActionMessages();
    	
    	if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }

		FormComponentsForm actionForm = (FormComponentsForm) form;

		if (!AgnUtils.allowed(request, Permission.MAILING_COMPONENTS_CHANGE)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
        } else {
    		boolean success = componentService.deleteFormComponent(actionForm.getFormID(), AgnUtils.getCompanyID(request), actionForm.getFilename());
        	if (!success) {
        		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("changes_not_saved"));
        	} else {
        		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
        	}
        	
			loadImagesData(actionForm, request);
        }
		
		if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }
        if (!messages.isEmpty()) {
        	saveMessages(request, messages);
        }

		loadFormData(actionForm, request);
		
		AgnUtils.setAdminDateTimeFormatPatterns(request);

		return mapping.findForward("list");
	}

	public ActionForward uploadArchive(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionMessages errors = new ActionMessages();
    	ActionMessages messages = new ActionMessages();

    	if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }

		FormComponentsForm actionForm = (FormComponentsForm) form;

		if (!AgnUtils.allowed(request, Permission.MAILING_COMPONENTS_CHANGE)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
        } else {
        	List<String> errorneousFiles = saveComponentsFromZipArchive(actionForm, request, actionForm.isOverwriteExisting());
        	if (!errorneousFiles.isEmpty()) {
        		errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("FilesWithError", StringUtils.join(errorneousFiles, ", ")));
        	} else {
        		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
        	}

			loadImagesData(actionForm, request);

			// Set overwrite default value (is reseted in form when sending
	        actionForm.setOverwriteExisting(true);
        }

		if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }
        if (!messages.isEmpty()) {
        	saveMessages(request, messages);
        }

		loadFormData(actionForm, request);
		
		AgnUtils.setAdminDateTimeFormatPatterns(request);

		return mapping.findForward("list");
	}

	public ActionForward downloadArchive(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionMessages errors = new ActionMessages();
    	ActionMessages messages = new ActionMessages();

    	if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }

		FormComponentsForm actionForm = (FormComponentsForm) form;

		if (!AgnUtils.allowed(request, Permission.MAILING_COMPONENTS_CHANGE)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.permissionDenied"));
        } else {
        	writeAllComponentsZipArchiveToResponse(actionForm, request, response);
			return null;
        }
		
		if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }
        if (!messages.isEmpty()) {
        	saveMessages(request, messages);
        }

		loadFormData(actionForm, request);
		
		AgnUtils.setAdminDateTimeFormatPatterns(request);

		return mapping.findForward("list");
	}

	private void saveComponents(FormComponentsForm actionForm, HttpServletRequest request, ActionMessages messages, ActionMessages errors) throws Exception {
		int companyID = AgnUtils.getCompanyID(request);
		
		// check if a given filename is valid
		List<String> errorneousFiles = getInvalidFilenames(actionForm);
		if (errorneousFiles.size() > 0) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("FilenameNotValid", StringUtils.join(errorneousFiles, ", ")));
			return;
		}

		// check if file list contains duplicates
		errorneousFiles = getDuplicateFilenames(actionForm);
		if (errorneousFiles.size() > 0) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("FilenameDuplicate", StringUtils.join(errorneousFiles, ", ")));
			return;
		}

		// check if a given file (its extension) has a valid format for images
		errorneousFiles = getInvalidFileformats(actionForm);
		if (errorneousFiles.size() > 0) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("FileformatNotValid", StringUtils.join(errorneousFiles, ", ")));
			return;
		}
		
		for (Entry<Integer, FormFile> entry : actionForm.getAllNewFiles().entrySet()) {
			FormFile file = entry.getValue();
			
			// Remove existing component
			FormComponent existingComponent = componentService.getFormComponent(actionForm.getFormID(), companyID, file.getFileName(), null);
			if (existingComponent != null && existingComponent.getFormID() != 0) {
				componentService.deleteFormComponent(existingComponent.getFormID(), companyID, existingComponent.getName());
			}
			
			// Save new component
			FormComponent imageFormComponent = new FormComponent();
			imageFormComponent.setCompanyID(AgnUtils.getCompanyID(request));
			imageFormComponent.setFormID(actionForm.getFormID());
			imageFormComponent.setName(file.getFileName());
			imageFormComponent.setDescription(actionForm.getAllDescriptions().get(entry.getKey()));
			imageFormComponent.setType(FormComponentType.IMAGE);
			imageFormComponent.setMimeType(mimeTypeService.getMimetypeForFile(file.getFileName()));
			imageFormComponent.setData(file.getFileData());
			
			componentService.saveFormComponent(imageFormComponent);

			writeUserActivityLog(AgnUtils.getAdmin(request), "upload form component zip", "FormComponent ID: "+imageFormComponent.getId() + ", component name: "+file.getFileName());
		}
	}

	private List<String> getInvalidFilenames(FormComponentsForm actionForm) {
		ArrayList<String> invalidFilenames = new ArrayList<>();

		// Characters from a-z, A-Z, 0-9, "_", "." and "-" are allowed.
		Pattern validFilenamePattern = Pattern.compile("[a-zA-Z0-9_\\.\\-]*");

		for (Map.Entry<Integer, FormFile> entry : actionForm.getAllNewFiles().entrySet()) {
			String filename = entry.getValue().getFileName();
			// Skip empty entries
			if (StringUtils.isNotBlank(filename)) {
				Matcher matcher = validFilenamePattern.matcher(filename);
				if (!matcher.matches()) {
					invalidFilenames.add(filename);
				}
			}
		}

		return invalidFilenames;
	}

	private List<String> getDuplicateFilenames(FormComponentsForm actionForm) {
		ArrayList<String> validFilenames = new ArrayList<>();
		ArrayList<String> duplicateFilenames = new ArrayList<>();

		for (Map.Entry<Integer, FormFile> entry : actionForm.getAllNewFiles().entrySet()) {
			String filename = entry.getValue().getFileName();
			// Skip empty entries
			if (StringUtils.isNotBlank(filename)) {
				if (validFilenames.contains(filename)) {
					duplicateFilenames.add(filename);
				} else {
					validFilenames.add(filename);
				}
			}
		}

		return duplicateFilenames;
	}

	private List<String> getInvalidFileformats(FormComponentsForm actionForm) {
		return actionForm.getAllNewFiles()
				.values()
				.stream()
				.map(FormFile::getFileName)
				.filter(name -> !ImageUtils.isValidImageFileExtension(AgnUtils.getFileExtension(name)))
				.collect(Collectors.toList());
	}

	private void loadFormData(FormComponentsForm actionForm, HttpServletRequest request) {
		String formName = userFormDao.getUserFormName(actionForm.getFormID(), AgnUtils.getCompanyID(request));
		actionForm.setFormName(formName);
	}

	private void loadImagesData(FormComponentsForm actionForm, HttpServletRequest request) {
		int companyID = AgnUtils.getCompanyID(request);
		String rdirDomain = companyDao.getCompany(companyID).getRdirDomain();
		
		List<FormComponent> formImageComponentsWithoutData = componentService.getFormComponentDescriptions(companyID, actionForm.getFormID(), FormComponentType.IMAGE);
		request.setAttribute("components", formImageComponentsWithoutData);
				
		Map<Integer, Map<String, String>> imageData = new HashMap<>();
		for (FormComponent formComponent : formImageComponentsWithoutData) {
			Map<String, String> imageDataItem = new HashMap<>();
			imageDataItem.put("standardRdirUrl", ShowFormImageServlet.getFormImageLink(rdirDomain, companyID, actionForm.getFormID(), formComponent.getName(), false));
			imageDataItem.put("nocacheUrl", ShowFormImageServlet.getFormImageLink("", companyID, actionForm.getFormID(), formComponent.getName(), true));
			imageDataItem.put("thumbnailUrl", ShowFormImageServlet.getFormImageThumbnailLink("", companyID, actionForm.getFormID(), formComponent.getName()));
			imageDataItem.put("changeDate", DateUtilities.formatLocalized(formComponent.getCreationDate(), request.getLocale()));
			imageDataItem.put("fileSize", AgnUtils.getHumanReadableNumber(formComponent.getDataSize(), "B", false, request.getLocale()));
			imageDataItem.put("description", formComponent.getDescription());
			imageData.put(formComponent.getId(), imageDataItem);
		}
		request.setAttribute("imageData", imageData);
	}
	
	private void writeAllComponentsZipArchiveToResponse(FormComponentsForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			int companyID = AgnUtils.getCompanyID(request);
			
			response.setContentType("application/zip");
            HttpUtils.setDownloadFilenameHeader(response, "FormComponents_" + actionForm.getFormID() + ".zip");
			
			try (ZipOutputStream zipOutputStream = ZipUtilities.openNewZipOutputStream(response.getOutputStream())) {
				for (FormComponent formComponent : componentService.getFormComponents(companyID, actionForm.getFormID())) {
					if (formComponent.getType() != FormComponentType.THUMBNAIL) {
						ZipUtilities.addFileDataToOpenZipFileStream(formComponent.getData(), formComponent.getName(), zipOutputStream);
					}
				}
			}
		} finally {
			writeUserActivityLog(AgnUtils.getAdmin(request), "download form components", "Form ID: " + actionForm.getFormID());
		}
	}
	
	private List<String> saveComponentsFromZipArchive(FormComponentsForm actionForm, HttpServletRequest request, boolean overwriteExisting) throws Exception {
		List<String> errorneousFiles = new ArrayList<>();
		int companyID = AgnUtils.getCompanyID(request);

		try (ZipInputStream zipInputStream = ZipUtilities.openZipInputStream(actionForm.getArchiveFile().getInputStream())) {
			ZipEntry nextZipEntry;
			while ((nextZipEntry = zipInputStream.getNextEntry()) != null) {
				String componentName = nextZipEntry.getName();
				byte[] componentData = IOUtils.toByteArray(zipInputStream);
				
				if (overwriteExisting) {
					FormComponent existingComponent = componentService.getFormComponent(actionForm.getFormID(), companyID, componentName, null);
					if (existingComponent != null && existingComponent.getFormID() != 0) {
						componentService.deleteFormComponent(existingComponent.getFormID(), companyID, existingComponent.getName());
					}
				}
				
				FormComponent imageFormComponent = new FormComponent();
				imageFormComponent.setCompanyID(companyID);
				imageFormComponent.setFormID(actionForm.getFormID());
				imageFormComponent.setName(componentName);
				imageFormComponent.setDescription("Zip upload");
				imageFormComponent.setType(FormComponentType.IMAGE);
				imageFormComponent.setMimeType(mimeTypeService.getMimetypeForFile(componentName));
				imageFormComponent.setData(componentData);
				
				if (!componentService.saveFormComponent(imageFormComponent)) {
					errorneousFiles.add(componentName);
				} else {
					writeUserActivityLog(AgnUtils.getAdmin(request), "upload form component zip", "FormComponent ID: "+imageFormComponent.getId() + ", component name: "+componentName);
				}
			}
		}
		
		return errorneousFiles;
	}
}
