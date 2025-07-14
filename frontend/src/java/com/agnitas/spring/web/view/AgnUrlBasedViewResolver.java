/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.web.view;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.agnitas.beans.Admin;
import com.agnitas.spring.web.view.tiles3.ApacheTilesView;
import jakarta.servlet.http.HttpServletRequest;
import com.agnitas.util.AgnUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

// TODO: EMMGUI-714: Remove when removing old design
public class AgnUrlBasedViewResolver extends UrlBasedViewResolver {

    private static final Map<String, String> customRedesignedViewMappings = new HashMap<>();
    private static final List<String> classicMediapoolViewNames = List.of("grid_mediapool_content_image_list", "grid_mediapool_pdf_list",
            "grid_mediapool_font_list", "grid_mediapool_video_list", "grid_mediapool_audio_list");

    static {
        // ----- CUSTOM REDESIGNED VIEW MAPPINGS -----
        classicMediapoolViewNames.forEach(v -> customRedesignedViewMappings.put(v, "mediapool-view_redesigned"));
        customRedesignedViewMappings.put("settings_admin_view", "user_view");
        customRedesignedViewMappings.put("settings_restfuluser_view", "user_view");
        customRedesignedViewMappings.put("settings_admin_rights", "user_permissions");
        customRedesignedViewMappings.put("settings_restfuluser_permissions", "user_permissions");
        customRedesignedViewMappings.put("export_result", "evaluation_finished");
        customRedesignedViewMappings.put("mailing_create_start", "mailing_creation_modal");
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        View redesignedUxView = resolveRedesignedUxView(viewName, locale);
        if (redesignedUxView != null && isRedesignedUxViewUsageAllowed()) {
            return redesignedUxView;
        }

        View redesignedView = resolveRedesignedView(viewName, locale);
        if (redesignedView != null && isRedesignedViewUsageAllowed()) {
            return redesignedView;
        }

        if (redesignedView != null) {
            getRequest().ifPresent(r -> r.setAttribute("canSwitchDesign", true));
        }

        return super.resolveViewName(viewName, locale);
    }

    private boolean isRedesignedUxViewUsageAllowed() {
        return getAdmin().map(a -> a.isUpdatedUxUsed(getRequest().orElse(null)))
                .orElse(false);
    }

    private boolean isRedesignedViewUsageAllowed() {
        return getAdmin().map(Admin::isRedesignedUiUsed)
                .orElse(false);
    }

    private Optional<Admin> getAdmin() {
        return getRequest().map(AgnUtils::getAdmin);
    }

    private Optional<HttpServletRequest> getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(requestAttributes.getRequest());
    }

    private View resolveRedesignedView(String viewName, Locale locale) throws Exception {
        return findView(buildRedesignedViewName(viewName), locale);
    }

    private View resolveRedesignedUxView(String viewName, Locale locale) throws Exception {
        viewName = buildRedesignedViewName(viewName) + "_ux";
        return findView(viewName, locale);
    }

    private String buildRedesignedViewName(String viewName) {
        return customRedesignedViewMappings.getOrDefault(
                viewName,
                viewName + "_redesigned"
        );
    }

    private View findView(String viewName, Locale locale) throws Exception {
        View view = super.resolveViewName(viewName, locale);

        if (view instanceof ApacheTilesView) {
            return view;
        }

        return null;
    }
}
