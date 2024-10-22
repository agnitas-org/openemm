/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upselling.web;

import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.upselling.form.UpsellingForm;
import com.agnitas.web.perm.annotations.AlwaysAllowed;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AlwaysAllowed
public class UpsellingController implements XssCheckAware {

    private static final String[] CUSTOM_VIEWS = new String[]{
            "grid_template_upselling",
            "mailing_predelivery_upselling",
            "auto_export_upselling",
            "auto_import_upselling",
            "manage_tables_upselling",
            "mediapool_upselling",
            "clients_upselling"
    };

    private final ConfigService configService;

    public UpsellingController(final ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/upselling.action")
    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    public String view(final Admin admin, final UpsellingForm form, final Model model) {
        String featureName = form.getFeatureNameKey();
        String activeSideMenu = StringUtils.defaultIfEmpty(form.getSidemenuActive(), featureName);

        model.addAttribute("featureNameKey", featureName);
        model.addAttribute("sidemenuActive", activeSideMenu);
        model.addAttribute("sidemenuSubActive", StringUtils.trimToEmpty(form.getSidemenuSubActive()));
        model.addAttribute("navigationKey", StringUtils.trimToEmpty(form.getNavigationKey()));
        model.addAttribute("upsellingInfoUrl", getUpsellingInfoPageUrl(admin));

        String messageKey = featureNameToMessageKey(StringUtils.defaultString(featureName));
        if (StringUtils.isNotBlank(messageKey)) {
            model.addAttribute("headlineKey", String.format("%s.teaser.headline", messageKey));
            model.addAttribute("descriptionKey", String.format("%s.teaser.description", messageKey));
            model.addAttribute("upgradeInfoKey", String.format("%s.teaser.upgradeInfo", messageKey));
        }

        return getView(form.getPage());
    }

    @GetMapping("/upsellingRedesigned.action")
    public String viewRedesigned(@RequestParam String page, Admin admin, Model model) {
        model.addAttribute("adminLocale", admin.getLocale());
        return getView(page);
    }
    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    private String featureNameToMessageKey(String featureName) {
        switch (featureName) {
            case "CompanyAdmin":
                return "clients";
            case "Reports":
                return "reports";
            default:
                return "";
        }
    }

    private String getView(String page) {
        if (ArrayUtils.contains(CUSTOM_VIEWS, page)) {
            return page;
        }

        return "common_upselling";
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    private String getUpsellingInfoPageUrl(final Admin admin) {
        if ("en".equalsIgnoreCase(admin.getAdminLang())) {
            return configService.getValue(ConfigValue.UpsellingInfoUrlEnglish);
        }
        return configService.getValue(ConfigValue.UpsellingInfoUrlGerman);
    }
}
