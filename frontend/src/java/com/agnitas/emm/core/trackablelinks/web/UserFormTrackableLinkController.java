/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.web;

import java.util.List;

import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;

import com.agnitas.beans.Admin;
import com.agnitas.beans.LinkProperty;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty;
import com.agnitas.emm.core.trackablelinks.dto.FormTrackableLinkDto;
import com.agnitas.emm.core.trackablelinks.form.FormTrackableLinkForm;
import com.agnitas.emm.core.trackablelinks.form.FormTrackableLinksForm;
import com.agnitas.emm.core.trackablelinks.service.FormTrackableLinkService;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.emm.core.userform.service.ComUserformService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.util.LinkUtils;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/webform")
@PermissionMapping("userform.trackablelink")
public class UserFormTrackableLinkController implements XssCheckAware {

	private static final Logger logger = LogManager.getLogger(UserFormTrackableLinkController.class);

	private final LinkService linkService;
	private final FormTrackableLinkService trackableLinkService;
	private final ComUserformService userformService;
	private final BirtStatisticsService birtStatisticsService;
	private final ConfigService configService;
	private final ExtendedConversionService conversionService;

	public UserFormTrackableLinkController(LinkService linkService, FormTrackableLinkService trackableLinkService, ComUserformService userformService, BirtStatisticsService birtStatisticsService, ConfigService configService, ExtendedConversionService conversionService) {
		this.linkService = linkService;
		this.trackableLinkService = trackableLinkService;
		this.userformService = userformService;
		this.birtStatisticsService = birtStatisticsService;
		this.configService = configService;
		this.conversionService = conversionService;
	}

	@GetMapping("/{formId:\\d+}/trackablelink/list.action")
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
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		return "messages";
	}

    private void addDefaultExtensionsModelAttr(Model model, int companyId) {
        List<LinkProperty> defaultExtensions = linkService.getDefaultExtensions(companyId);
        model.addAttribute("defaultExtensions", conversionService.convert(defaultExtensions, LinkProperty.class, ExtensionProperty.class));
    }

    private void loadUserFormData(int companyId, @PathVariable int formId, Model model) {
		model.addAttribute("userFormId", formId);
		model.addAttribute("userFormName", userformService.getUserFormName(formId, companyId));
	}

	@PostMapping("/{formId:\\d+}/trackablelink/bulkSave.action")
	@PermissionMapping("save")
	public String bulkSave(Admin admin, @PathVariable int formId, @ModelAttribute("form") FormTrackableLinksForm form, Popups popups) {
		try {
			List<LinkProperty> commonExtensions = conversionService.convert(form.getCommonExtensions(), ExtensionProperty.class, LinkProperty.class);
			trackableLinkService.bulkUpdateTrackableLinks(admin, formId, form.getLinks(), form.isTrackable(), commonExtensions);
			popups.success("default.changes_saved");

			return redirectToList(formId);

		} catch (Exception e) {
			logger.error("Could not bulk save links!", e);
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		return "messages";
	}

    private String redirectToList(@PathVariable int formId) {
        return "redirect:/webform/" + formId + "/trackablelink/list.action";
    }

	@PostMapping("/{formId:\\d+}/trackablelink/saveCommonExtensionText.action")
	@PermissionMapping("save.extension")
	public String saveCommonExtensionText(Admin admin, @PathVariable int formId, @RequestParam("linkExtension") String extensionAsText, Popups popups) {
		try {
			List<LinkProperty> extensions = LinkUtils.parseLinkExtension(extensionAsText);
			trackableLinkService.bulkUpdateTrackableLinksExtensions(admin, formId, extensions);
			popups.success("default.changes_saved");

			return redirectToList(formId);
		} catch (Exception e) {
			logger.error("Could not bulk save extensions!", e);
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		return "messages";
	}

	@PostMapping("/{formId:\\d+}/trackablelink/bulkSaveUsage.action")
	@PermissionMapping("save")
	public String bulkSaveUsage(Admin admin, @PathVariable int formId, @RequestParam("trackable") int trackableValue, Popups popups) {
		try {
			trackableLinkService.bulkUpdateTrackableLinksUsage(admin, formId, trackableValue);
			popups.success("default.changes_saved");

			return redirectToList(formId);
		} catch (Exception e) {
			logger.error("Could not bulk save extensions!", e);
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		return "messages";
	}

	@GetMapping("/{formId:\\d+}/trackablelink/{linkId:\\d+}/view.action")
	public String view(Admin admin, @PathVariable int formId, @PathVariable int linkId, Model model, Popups popups) {
		FormTrackableLinkDto link = trackableLinkService.getFormTrackableLink(admin, formId, linkId);
		if (link == null) {
			logger.error("could not load form/link: {}/{}", formId, linkId);
			popups.alert("Error");
			return "messages";
		}

		model.addAttribute("form", conversionService.convert(link, FormTrackableLinkForm.class));
		loadUserFormData(admin.getCompanyID(), formId, model);
        addDefaultExtensionsModelAttr(model, admin.getCompanyID());

		return "userform_trackablelink_view";
	}

	@PostMapping("/{formId:\\d+}/trackablelink/save.action")
	public String save(Admin admin, @PathVariable int formId, @ModelAttribute("form") FormTrackableLinkForm form, Popups popups) {
		FormTrackableLinkDto trackableLinkDto = conversionService.convert(form, FormTrackableLinkDto.class);
		try {
			boolean success = trackableLinkService.updateTrackableLink(admin, formId, trackableLinkDto);

			if (success) {
				popups.success("default.changes_saved");
				return redirectToList(formId);
			} else {
				popups.alert("Error");
			}
		} catch (Exception e) {
			logger.error("Could not save link formID/linkID: {}/{}", formId, trackableLinkDto.getId());
			popups.alert("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl));
		}

		return "messages";
	}

	@GetMapping("/{formId:\\d+}/trackablelink/statistic.action")
	public String statistic(Admin admin, @PathVariable int formId, Model model, Popups popups) {
		try {
			UserFormDto userForm = userformService.getUserForm(admin.getCompanyID(), formId);
			if (userForm != null) {
				String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
				String urlWithoutFormat = birtStatisticsService.getUserFormTrackableLinkStatisticUrl(admin, sessionId, formId);
				model.addAttribute("birtStatisticUrlWithoutFormat", urlWithoutFormat);

				model.addAttribute("userFormName", userForm.getName());
				model.addAttribute("userFormId", userForm.getId());
			} else {
				popups.alert("Error");
			}
		} catch (Exception e) {
			logger.error("Could not obtain user form links statistic!", e);
			popups.alert("Error");
		}
		return "userform_userform_stats";
	}
}
