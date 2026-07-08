/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.LinkProperty;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty;
import com.agnitas.emm.core.trackablelinks.dto.FormTrackableLinkDto;
import com.agnitas.emm.core.trackablelinks.form.FormTrackableLinkForm;
import com.agnitas.emm.core.trackablelinks.form.FormTrackableLinksForm;
import com.agnitas.emm.core.trackablelinks.service.FormTrackableLinkService;
import com.agnitas.emm.core.userform.form.UserformStatsForm;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.LinkUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.InputStreamResource;
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

@Controller
@RequestMapping("/webform")
public class UserFormTrackableLinkController implements XssCheckAware {

	private static final Logger logger = LogManager.getLogger(UserFormTrackableLinkController.class);

	private final LinkService linkService;
	private final FormTrackableLinkService trackableLinkService;
	private final UserformService userformService;
	private final ConfigService configService;
	private final ExtendedConversionService conversionService;
	private final UserActivityLogService userActivityLogService;

	public UserFormTrackableLinkController(
			LinkService linkService,
			FormTrackableLinkService trackableLinkService,
			UserformService userformService,
			ConfigService configService,
			ExtendedConversionService conversionService,
			UserActivityLogService userActivityLogService
	) {
		this.linkService = linkService;
		this.trackableLinkService = trackableLinkService;
		this.userformService = userformService;
        this.configService = configService;
		this.conversionService = conversionService;
		this.userActivityLogService = userActivityLogService;
	}

	@GetMapping("/{formId:\\d+}/trackablelink/list.action")
	@RequiredPermission("forms.change")
	public String list(Admin admin, Model model, @PathVariable int formId, @ModelAttribute("form") FormTrackableLinksForm form, Popups popups) {
		try {
			int companyId = admin.getCompanyID();
			List<FormTrackableLinkDto> trackableLinks = trackableLinkService.getFormTrackableLinks(admin, formId);
			form.setLinks(trackableLinks);

			List<LinkProperty> commonExtensions = trackableLinkService.getFormTrackableLinkCommonExtensions(admin, formId);
			form.setCommonExtensions(conversionService.convert(commonExtensions, LinkProperty.class, ExtensionProperty.class));

            addDefaultExtensionsModelAttr(model, companyId);
            loadUserFormData(companyId, formId, model);

			return "userform_trackablelink_list";
		} catch (Exception e) {
			logger.error("Gettings user form trackable link list failed!", e);
			alertError(popups);
		}
		return MESSAGES_VIEW;
	}

	private void alertError(Popups popups) {
		popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
	}

    private void addDefaultExtensionsModelAttr(Model model, int companyId) {
        List<LinkProperty> defaultExtensions = linkService.getDefaultExtensions(companyId);
        model.addAttribute("defaultExtensions", conversionService.convert(defaultExtensions, LinkProperty.class, ExtensionProperty.class));
    }

    private void loadUserFormData(int companyId, int formId, Model model) {
		model.addAttribute("userFormId", formId);
		model.addAttribute("userFormName", userformService.getUserFormName(formId, companyId));
	}

	@PostMapping("/{formId:\\d+}/trackablelink/bulkSave.action")
	@RequiredPermission("forms.change")
	public String bulkSave(Admin admin, @PathVariable int formId, @ModelAttribute("form") FormTrackableLinksForm form, Popups popups) {
		try {
			List<LinkProperty> commonExtensions = conversionService.convert(form.getCommonExtensions(), ExtensionProperty.class, LinkProperty.class);
			trackableLinkService.bulkUpdateTrackableLinks(admin, formId, form.getLinks(), form.isTrackable(), commonExtensions);
			popups.changesSaved();

			return redirectToList(formId);

		} catch (Exception e) {
			logger.error("Could not bulk save links!", e);
			alertError(popups);
		}

		return MESSAGES_VIEW;
	}

    private String redirectToList(int formId) {
        return "redirect:/webform/" + formId + "/trackablelink/list.action";
    }

	@PostMapping("/{formId:\\d+}/trackablelink/saveCommonExtensionText.action")
	@RequiredPermission("mailing.extend_trackable_links")
	public String saveCommonExtensionText(Admin admin, @PathVariable int formId, @RequestParam("linkExtension") String extensionAsText, Popups popups) {
		try {
			List<LinkProperty> extensions = LinkUtils.parseLinkExtension(extensionAsText);
			trackableLinkService.bulkUpdateTrackableLinksExtensions(admin, formId, extensions);
			popups.changesSaved();

			return redirectToList(formId);
		} catch (Exception e) {
			logger.error("Could not bulk save extensions!", e);
			alertError(popups);
		}

		return MESSAGES_VIEW;
	}

	@PostMapping("/{formId:\\d+}/trackablelink/bulkSaveUsage.action")
	@RequiredPermission("forms.change")
	public String bulkSaveUsage(Admin admin, @PathVariable int formId, @RequestParam("trackable") int trackableValue, Popups popups) {
		try {
			trackableLinkService.bulkUpdateTrackableLinksUsage(admin, formId, trackableValue);
			popups.changesSaved();

			return redirectToList(formId);
		} catch (Exception e) {
			logger.error("Could not bulk save extensions!", e);
			alertError(popups);
		}

		return MESSAGES_VIEW;
	}

	@GetMapping("/{formId:\\d+}/trackablelink/{linkId:\\d+}/view.action")
	@RequiredPermission("forms.change")
	public String view(Admin admin, @PathVariable int formId, @PathVariable int linkId, Model model, Popups popups) {
		FormTrackableLinkDto link = trackableLinkService.getFormTrackableLink(admin, formId, linkId);
		if (link == null) {
			logger.error("could not load form/link: {}/{}", formId, linkId);
			popups.defaultError();
			return MESSAGES_VIEW;
		}

		model.addAttribute("form", conversionService.convert(link, FormTrackableLinkForm.class));
		loadUserFormData(admin.getCompanyID(), formId, model);
        addDefaultExtensionsModelAttr(model, admin.getCompanyID());

		return "userform_trackablelink_view";
	}

	@PostMapping("/{formId:\\d+}/trackablelink/save.action")
	@RequiredPermission("forms.change")
	public String save(Admin admin, @PathVariable int formId, @ModelAttribute("form") FormTrackableLinkForm form, Popups popups) {
		FormTrackableLinkDto trackableLinkDto = conversionService.convert(form, FormTrackableLinkDto.class);
		try {
			boolean success = trackableLinkService.updateTrackableLink(admin, formId, trackableLinkDto);

			if (success) {
				popups.changesSaved();
				return redirectToList(formId);
			} else {
				popups.defaultError();
			}
		} catch (Exception e) {
			logger.error("Could not save link formID/linkID: {}/{}", formId, trackableLinkDto.getId());
			alertError(popups);
		}

		return MESSAGES_VIEW;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder, Admin admin) {
		binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
	}

	@GetMapping("/stats.action")
	@RequiredPermission("forms.change")
	public String stats(UserformStatsForm form, Admin admin, Model model, Popups popups) {
		if (form.isAllowedToChoseForm()) { // all forms stat page
			List<UserForm> webForms = userformService.getUserForms(admin.getCompanyID());
			if (CollectionUtils.isEmpty(webForms)) {
				popups.alert("GWUA.dashboard.records.notAvailable");
				return MESSAGES_VIEW;
			}
			if (form.getFormId() == 0) {
				form.setFormId(webForms.getFirst().getId());
			}
			model.addAttribute("userForms", webForms);
		}
		model
			.addAttribute("rows", trackableLinkService.getStats(form, admin))
			.addAttribute("years", AgnUtils.getYearList(AgnUtils.getStatStartYearForCompany(admin)))
			.addAttribute("formName", userformService.getUserFormName(form.getFormId(), admin.getCompanyID()));
		userActivityLogService.writeUserActivityLog(admin, "form statistics", "active submenu - Pages & Forms");
		return "userform_stats";
	}

	@RequestMapping(value = "/statsCsv.action", method = {RequestMethod.POST, RequestMethod.GET})
	@RequiredPermission("forms.change")
	public ResponseEntity<InputStreamResource> export(UserformStatsForm form, Admin admin) throws Exception {
		userActivityLogService.writeUserActivityLog(admin, "form statistics csv", "id = " + form.getFormId());
		return MvcUtils.csvFileResponse(
			trackableLinkService.getCsv(form, admin),
			"form_click_statistics.csv"
		);
	}
}
