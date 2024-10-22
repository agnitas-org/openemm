/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.web.view;

import com.agnitas.emm.core.Permission;
import jakarta.servlet.http.HttpServletRequest;
import org.agnitas.util.AgnUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class AgnUrlBasedViewResolver extends UrlBasedViewResolver {

    private static final String NAME_SUFFIX_OF_REDESIGNED_TILES = "_redesigned";
    private static final Map<String, Permission> viewsPermissions = new HashMap<>();
    private static final Map<String, String> customRedesignedViewMappings = new HashMap<>();
    private static final List<String> classicMediapoolViewNames = List.of("grid_mediapool_content_image_list", "grid_mediapool_pdf_list",
            "grid_mediapool_font_list", "grid_mediapool_video_list", "grid_mediapool_audio_list");

    static {
        // ----- CUSTOM REDISGNED VIEW MAPPINGS -----
        classicMediapoolViewNames.forEach(v -> customRedesignedViewMappings.put(v, "mediapool-view_redesigned"));
        customRedesignedViewMappings.put("settings_admin_view", "user_view");
        customRedesignedViewMappings.put("settings_restfuluser_view", "user_view");
        customRedesignedViewMappings.put("settings_admin_rights", "user_permissions");
        customRedesignedViewMappings.put("settings_restfuluser_permissions", "user_permissions");
        customRedesignedViewMappings.put("export_result", "evaluation_finished");
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        View redesignedView = resolveRedesignedView(viewName, locale);
        if (redesignedView != null && isRedesignedViewUsageAllowed(viewName)) {
            return redesignedView;
        }

        if (redesignedView != null && canSwitchDesign(viewName)) {
            getRequest().ifPresent(r -> r.setAttribute("canSwitchDesign", true));
        }

        return super.resolveViewName(viewName, locale);
    }

    private boolean isRedesignedViewUsageAllowed(String viewName) {
        Optional<HttpServletRequest> request = getRequest();
        if (request.isEmpty() || !AgnUtils.isRedesignedUiUsed(request.get())) {
            return false;
        }

        return hasPermissionForRedesignedView(viewName, request.get());
    }

    private boolean canSwitchDesign(String viewName) {
        Optional<HttpServletRequest> request = getRequest();
        return request.filter(req -> hasPermissionForRedesignedView(viewName, req)).isPresent();
    }

    private boolean hasPermissionForRedesignedView(String viewName, HttpServletRequest req) {
        return !viewsPermissions.containsKey(viewName) || AgnUtils.allowed(req, viewsPermissions.get(viewName));
    }

    private Optional<HttpServletRequest> getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(requestAttributes.getRequest());
    }

    private View resolveRedesignedView(String viewName, Locale locale) throws Exception {
        String redesignedViewName = customRedesignedViewMappings.getOrDefault(
                viewName,
                viewName + NAME_SUFFIX_OF_REDESIGNED_TILES
        );

        View view = super.resolveViewName(redesignedViewName, locale);

        if (view instanceof TilesView) {
            return view;
        }

        return null;
    }
}
