/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.web;

import static com.agnitas.beans.FormComponent.FormComponentType.IMAGE;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.forms.PaginationForm;
import org.agnitas.web.forms.SimpleActionForm;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import com.agnitas.beans.Admin;
import com.agnitas.beans.FormComponent;
import com.agnitas.emm.core.components.dto.FormUploadComponentDto;
import com.agnitas.emm.core.components.form.FormUploadComponentsForm;
import com.agnitas.emm.core.components.form.FormZipUploadComponentsForm;
import com.agnitas.emm.core.components.service.ComComponentService;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.emm.core.userform.util.WebFormUtils;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/webform")
@PermissionMapping("userform.components")
public class UserFormComponentController implements XssCheckAware {
	
	private static final Logger logger = LogManager.getLogger(UserFormComponentController.class);

	private final ComUserformService userformService;
	private final ComComponentService componentService;
	private final ConfigService configService;
	private final UserActivityLogService userActivityLogService;
	private final ExtendedConversionService conversionService;

	public UserFormComponentController(ComUserformService userformService, ComComponentService componentService, ConfigService configService, UserActivityLogService userActivityLogService, ExtendedConversionService conversionService) {
		this.userformService = userformService;
		this.componentService = componentService;
		this.configService = configService;
		this.userActivityLogService = userActivityLogService;
		this.conversionService = conversionService;
	}

	@RequestMapping("/{formId:\\d+}/components/list.action")
	public String list(Admin admin, @PathVariable int formId, @ModelAttribute("form") PaginationForm form, Model model) {
		UserFormDto userForm = userformService.getUserForm(admin.getCompanyID(), formId);
		String userFormInfo = StringUtils.join(Arrays.asList(userForm.getName(), StringUtils.trimToNull(userForm.getDescription())), " | ");

		model.addAttribute("userFormInfo", userFormInfo);
		model.addAttribute("components", componentService.getFormImageComponents(admin.getCompanyID(), formId));
		model.addAttribute("userFormName", userformService.getUserFormName(formId, admin.getCompanyID()));
		model.addAttribute("locale", admin.getLocale());
		model.addAttribute("formId", formId);


		setComponentsSrcPatterns(admin, formId, model);
		AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
		return "form_components";
	}


	@GetMapping("/{formId:\\d+}/components/{compName}/confirmDelete.action")
	public String confirmDelete(Admin admin, @PathVariable int formId, @PathVariable String compName,
								SimpleActionForm form, Model model, Popups popups) {
		FormComponent formComponent = componentService.getFormComponent(formId, admin.getCompanyID(), compName, IMAGE);
		if (formComponent == null) {
			popups.alert("bulkAction.nothing.userform");
			return "messages";

		}

		model.addAttribute("formId", formId);
		form.setShortname(formComponent.getName());
		return "formcomponents_delete_ajax";
	}

	@RequestMapping(value = "/{formId:\\d+}/components/delete.action",  method = {RequestMethod.POST, RequestMethod.DELETE})
	public String delete(Admin admin, @PathVariable int formId, SimpleActionForm form, Popups popups) {
		boolean deleted = componentService.deleteFormComponent(formId, admin.getCompanyID(), form.getShortname());
		if (deleted) {
			popups.success("default.selection.deleted");
			return redirectToComponentList(formId);
		}

		popups.alert("changes_not_saved");
		return "messages";
	}

    private String redirectToComponentList(@PathVariable int formId) {
        return "redirect:/webform/" + formId + "/components/list.action";
    }

	@GetMapping(value = "/{formId:\\d+}/components/bulkDownload.action")
	@PermissionMapping("download")
	public Object bulkDownload(Admin admin, @PathVariable int formId, Popups popups) {
		int companyId = admin.getCompanyID();
		Map<String, byte[]> component = componentService.getImageComponentsData(companyId, formId);
		if (MapUtils.isEmpty(component)) {
			popups.alert("error.default.nothing_selected");
			return redirectToComponentList(formId);
		}

		String zipName = "FormComponents_" + formId + ".zip";
		File zipFile = componentService.getComponentArchive(zipName, component);

        if (zipFile == null) {
			return redirectToComponentList(formId);
		}

        writeUserActivityLog(admin, new UserAction("download form components", String.format("Form ID: %d", formId)));
        return ResponseEntity.ok()
                .contentLength(zipFile.length())
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(zipName))
                .body(new DeleteFileAfterSuccessReadResource(zipFile));
	}

	@PostMapping(value = "/{formId:\\d+}/components/upload.action")
	public String upload(Admin admin, @PathVariable int formId, FormUploadComponentsForm form, Popups popups) {
		List<FormUploadComponentDto> components = new ArrayList<>(form.getComponents().values());
		if (!validate(components, popups)) {
			return "messages";
		}

		try {
			List<UserAction> userActions = new ArrayList<>();

			List<FormComponent> formComponents = conversionService.convert(components, FormUploadComponentDto.class, FormComponent.class);
			SimpleServiceResult result = componentService.saveFormComponents(admin, formId, formComponents, userActions);

			if (result.isSuccess()) {
				popups.success("default.changes_saved");
				result.getWarningMessages().forEach(popups::warning);

				userActions.forEach(action -> writeUserActivityLog(admin, action));
			} else {
				result.getErrorMessages().forEach(popups::alert);
			}

			return redirectToComponentList(formId);
		} catch (Exception e) {
			logger.error("Upload user form coponents failed!", e);
		}
		popups.alert("Error");
		return "messages";
	}

	@PostMapping(value = "/{formId:\\d+}/components/uploadZip.action")
	@PermissionMapping("upload")
	public String uploadZip(Admin admin, @PathVariable int formId, FormZipUploadComponentsForm form, Popups popups) {
		if (!checkIfFileExists(form.getZipFile(), popups)) {
			return "messages";
		}

		List<UserAction> userActions = new ArrayList<>();

		SimpleServiceResult result = componentService.saveComponentsFromZipFile(admin, formId, form.getZipFile(), userActions, form.isOverwriteExisting());

		if (result.isSuccess()) {
			popups.success("default.changes_saved");
			result.getWarningMessages().forEach(popups::warning);

			userActions.forEach(action -> writeUserActivityLog(admin, action));
		} else {
			result.getErrorMessages().forEach(popups::alert);
		}

		return redirectToComponentList(formId);
	}

	private boolean validate(List<FormUploadComponentDto> components, Popups popups) {
		for (FormUploadComponentDto component : components) {
			if (!checkIfFileExists(component.getFile(), popups)) {
				return false;
			}
		}

		List<Message> errors = componentService.validateComponents(components, true);
		errors.forEach(popups::alert);

		return !popups.hasAlertPopups();
	}

	private boolean checkIfFileExists(MultipartFile file, Popups popups) {
		if (file == null || file.isEmpty()) {
			popups.alert("error.file.missingOrEmpty");
			return false;
		}
		return true;
	}

	private void setComponentsSrcPatterns(Admin admin, int formId, Model model) {
		String redirectDomain = AgnUtils.getRedirectDomain(admin.getCompany());
		int licenceId = configService.getLicenseID();

		model.addAttribute("imageSrcPatternNoCache", WebFormUtils.getImageSrcPattern(redirectDomain, licenceId, admin.getCompanyID(), formId, true));
		model.addAttribute("imageSrcPattern", WebFormUtils.getImageSrcPattern(redirectDomain, licenceId, admin.getCompanyID(), formId, false));
		model.addAttribute("imageThumbnailPattern", WebFormUtils.getImageThumbnailPattern(redirectDomain, licenceId, admin.getCompanyID(), formId));

		WebFormUtils.getImageSrcPattern(redirectDomain, licenceId, admin.getCompanyID(), formId, true);
	}

	private void writeUserActivityLog(Admin admin, UserAction userAction) {
		if (userActivityLogService != null) {
			userActivityLogService.writeUserActivityLog(admin, userAction, logger);
		} else {
			logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
			logger.info("Userlog: " + admin.getUsername() + " " + userAction.getAction() + " " +  userAction.getDescription());
		}
	}
}
