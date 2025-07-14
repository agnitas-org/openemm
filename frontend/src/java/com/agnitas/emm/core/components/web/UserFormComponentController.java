/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.web;

import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static com.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.FormComponent;
import com.agnitas.emm.core.components.dto.FormUploadComponentDto;
import com.agnitas.emm.core.components.form.FormUploadComponentsForm;
import com.agnitas.emm.core.components.form.FormZipUploadComponentsForm;
import com.agnitas.emm.core.components.service.ComponentService;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.form.UserFormImagesFormSearchParams;
import com.agnitas.emm.core.userform.form.UserFormImagesOverviewFilter;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.emm.core.userform.util.WebFormUtils;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.util.ImageUtils;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.SimpleActionForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/webform")
@PermissionMapping("userform.components")
@SessionAttributes(types = UserFormImagesFormSearchParams.class)
public class UserFormComponentController implements XssCheckAware {

	private static final Logger logger = LogManager.getLogger(UserFormComponentController.class);

	private final UserformService userformService;
	private final ComponentService componentService;
	private final ConfigService configService;
	private final UserActivityLogService userActivityLogService;
	private final ExtendedConversionService conversionService;
	private final WebStorage webStorage;

	public UserFormComponentController(UserformService userformService, ComponentService componentService, ConfigService configService, UserActivityLogService userActivityLogService, ExtendedConversionService conversionService, WebStorage webStorage) {
		this.userformService = userformService;
		this.componentService = componentService;
		this.configService = configService;
		this.userActivityLogService = userActivityLogService;
		this.conversionService = conversionService;
		this.webStorage = webStorage;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder, Admin admin) {
		binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
	}

	@ModelAttribute
	public UserFormImagesFormSearchParams getSearchParams() {
		return new UserFormImagesFormSearchParams();
	}

	@RequestMapping("/{formId:\\d+}/components/list.action")
	public String list(@PathVariable int formId, @ModelAttribute("form") UserFormImagesOverviewFilter filter, @ModelAttribute UserFormImagesFormSearchParams searchParams,
					   @RequestParam(required = false) boolean restoreSort, Admin admin, Model model) {
		if (admin.isRedesignedUiUsed()) {
			FormUtils.syncSearchParams(searchParams, filter, true);
		}
		FormUtils.syncPaginationData(webStorage, WebStorage.USERFORM_IMAGES_OVERVIEW, filter, restoreSort);
		UserFormDto userForm = userformService.getUserForm(admin.getCompanyID(), formId);
		String userFormInfo = StringUtils.join(Arrays.asList(userForm.getName(), StringUtils.trimToNull(userForm.getDescription())), " | ");

		model.addAttribute("userFormInfo", userFormInfo);
		model.addAttribute("components", componentService.getFormImageComponents(filter, admin.getCompanyID(), formId));
		model.addAttribute("userFormName", userformService.getUserFormName(formId, admin.getCompanyID()));
		model.addAttribute("locale", admin.getLocale());
		model.addAttribute("formId", formId);
		model.addAttribute("imagesMimetypes", ImageUtils.ALLOWED_MIMETYPES);

		setComponentsSrcPatterns(admin, formId, model);
		AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
		model.addAttribute("adminDateTimeFormat", admin.getDateTimeFormat());
		return "form_components";
	}

	@GetMapping("/{formId:\\d+}/components/search.action")
	public String search(@PathVariable int formId, @ModelAttribute UserFormImagesOverviewFilter filter, @ModelAttribute UserFormImagesFormSearchParams searchParams, RedirectAttributes model) {
		FormUtils.syncSearchParams(searchParams, filter, false);
		return redirectToComponentList(formId);
	}

	@GetMapping("/{formId:\\d+}/components/{id:\\d+}/confirmDelete.action")
	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
	public String confirmDelete(Admin admin, @PathVariable int formId, @PathVariable int id,
								SimpleActionForm form, Model model, Popups popups) {
		List<String> names = componentService.getComponentFileNames(Set.of(id), formId, admin.getCompanyID());
		if (CollectionUtils.isEmpty(names)) {
			popups.alert("bulkAction.nothing.userform");
			return MESSAGES_VIEW;
		}

		model.addAttribute("formId", formId);
		form.setShortname(names.get(0));
		return "formcomponents_delete_ajax";
	}

	@RequestMapping(value = "/{formId:\\d+}/components/delete.action",  method = {RequestMethod.POST, RequestMethod.DELETE})
	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
	public String delete(Admin admin, @PathVariable int formId, SimpleActionForm form, Popups popups) {
		boolean deleted = componentService.deleteFormComponent(formId, admin.getCompanyID(), form.getShortname());
		if (deleted) {
			popups.success("default.selection.deleted");
			return redirectToComponentList(formId);
		}

		popups.alert("changes_not_saved");
		return MESSAGES_VIEW;
	}

	@GetMapping(value = "/{formId:\\d+}/components/deleteRedesigned.action")
	@PermissionMapping("delete")
	public String confirmDeleteRedesigned(@PathVariable int formId, @RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Model model) {
		if (CollectionUtils.isEmpty(bulkIds)) {
			throw new RequestErrorException(NOTHING_SELECTED_MSG);
		}

		List<String> items = componentService.getComponentFileNames(bulkIds, formId, admin.getCompanyID());
		MvcUtils.addDeleteAttrs(model, items,
                "mailing.Graphics_Component.delete", "image.delete.question",
                "bulkAction.delete.image", "bulkAction.delete.image.question");
		return DELETE_VIEW;
	}

	@RequestMapping(value = "/{formId:\\d+}/components/deleteRedesigned.action", method = {RequestMethod.POST, RequestMethod.DELETE})
	@PermissionMapping("delete")
	public String deleteRedesigned(@PathVariable int formId, @RequestParam(required = false) Set<Integer> bulkIds, Admin admin, Popups popups) {
		componentService.delete(bulkIds, formId, admin);
		popups.success(SELECTION_DELETED_MSG);
		return redirectToComponentList(formId);
	}

    private String redirectToComponentList(int formId) {
        return "redirect:/webform/" + formId + "/components/list.action?restoreSort=true";
    }

	@GetMapping(value = "/{formId:\\d+}/components/all/download.action")
	@PermissionMapping("download")
	public Object allDownload(Admin admin, @PathVariable int formId, UserFormImagesOverviewFilter filter, Popups popups) {
		int companyId = admin.getCompanyID();
		Map<String, byte[]> component = componentService.getImageComponentsData(filter, null, companyId, formId);
		if (MapUtils.isEmpty(component)) {
			popups.alert(NOTHING_SELECTED_MSG);
			return redirectToComponentList(formId);
		}

		return downloadZipFile(component, formId, admin);
	}

	@GetMapping(value = "/{formId:\\d+}/components/bulk/download.action")
	@PermissionMapping("download")
	public Object bulkDownload(@PathVariable int formId, @RequestParam(required = false) Set<Integer> bulkIds, UserFormImagesOverviewFilter filter, Admin admin, Popups popups) {
		if (CollectionUtils.isEmpty(bulkIds)) {
			popups.alert(NOTHING_SELECTED_MSG);
			return redirectToComponentList(formId);
		}

		Map<String, byte[]> component = componentService.getImageComponentsData(filter, bulkIds, admin.getCompanyID(), formId);
		return downloadZipFile(component, formId, admin);
	}

	private Object downloadZipFile(Map<String, byte[]> component, int formId, Admin admin) {
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
		List<FormUploadComponentDto> components = getUploadedComponents(form);
		if (!validate(components, popups)) {
			return MESSAGES_VIEW;
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
		return MESSAGES_VIEW;
	}

	private List<FormUploadComponentDto> getUploadedComponents(FormUploadComponentsForm form) {
		List<FormUploadComponentDto> components = new ArrayList<>();
		for (FormUploadComponentDto component : form.getComponents().values()) {
			if (StringUtils.endsWithIgnoreCase(component.getFileName(), ".zip")) {
				ServiceResult<List<FormUploadComponentDto>> componentReadResult = componentService.readComponentsFromZipFile(component.getFile());
				if (!componentReadResult.isSuccess()) {
					throw new RequestErrorException(new HashSet<>(componentReadResult.getErrorMessages()));
				}

				List<FormUploadComponentDto> zipComponents = componentReadResult.getResult();
				zipComponents.forEach(comp -> comp.setOverwriteExisting(component.isOverwriteExisting()));
				components.addAll(zipComponents);
			} else {
				components.add(component);
			}
		}

		return components;
	}

	@PostMapping(value = "/{formId:\\d+}/components/uploadZip.action")
	@PermissionMapping("upload")
	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
	public String uploadZip(Admin admin, @PathVariable int formId, FormZipUploadComponentsForm form, Popups popups) {
		if (!checkIfFileExists(form.getZipFile(), popups)) {
			return MESSAGES_VIEW;
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
			if (component.getData().length == 0 && !checkIfFileExists(component.getFile(), popups)) {
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
		if (!admin.isRedesignedUiUsed()) {
			model.addAttribute("imageThumbnailPattern", WebFormUtils.getImageThumbnailPattern(redirectDomain, licenceId, admin.getCompanyID(), formId));
		}

		WebFormUtils.getImageSrcPattern(redirectDomain, licenceId, admin.getCompanyID(), formId, true);
	}

	private void writeUserActivityLog(Admin admin, UserAction userAction) {
		userActivityLogService.writeUserActivityLog(admin, userAction, logger);
	}
}
