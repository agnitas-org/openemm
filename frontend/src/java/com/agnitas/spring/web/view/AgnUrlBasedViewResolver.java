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
    private static final List<String> classicMediapoolCategoryViewNames = List.of("media_pool_category_list", "media_pool_category_view",
            "media_pool_category_delete", "media_pool_category_bulk_delete");
    private static final List<String> classicUsersViewNames = List.of("settings_admin_view", "settings_restfuluser_view", "settings_restfuluser_permissions", "settings_admin_list", "settings_restfuluser_list",
            "settings_admin_delete_ajax", "settings_restfuluser_delete_ajax", "webserviceuser_list", "webserviceuser_view");

    private static final List<String> classicMailingSendViewNames = List.of("mailing_send", "mailing_delivery_status_box",
            "date_based_activation_confirm", "mailing_send_confirm_ajax", "mailing_deactivation_confirm", "mailing_cancel_generation_question_ajax",
            "mailing_resume_generation_by_copy_question_ajax", "mailing_resume_generation_question_ajax", "action_based_activation_confirm");

    private static final List<String> classicMailingPreviewViewNames = List.of("preview.0", "preview.1", "preview.3", "preview.4", "preview.5", "preview.6", "mailing_preview_select", "mailing_preview_errors");
    private static final List<String> classicLbViewNames = List.of("grid_template_list", "grid_template_name", "grid_template_view", "grid_template_div_containers", "grid_mailing_div_containers", "grid_template_div_containers_dialog", "grid_mediapool_image_pick");

    static {
        viewsPermissions.put("bounce_filter_list", Permission.RESPONSE_PROCESSING_UI_MIGRATION);
        viewsPermissions.put("bounce_filter_view", Permission.RESPONSE_PROCESSING_UI_MIGRATION);
        viewsPermissions.put("recipient_list", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("recipient_view", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("recipient_delete", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("recipient_duplicate_list", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("import_wizard_profile_view", Permission.IMPORT_PROFILE_UI_MIGRATION);
        viewsPermissions.put("import_wizard_profile_list", Permission.IMPORT_PROFILE_UI_MIGRATION);
        viewsPermissions.put("tracking_point_list", Permission.TRACKING_POINT_UI_MIGRATION);
        viewsPermissions.put("recipient_history", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("recipient_mailings", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("recipient_reactions_history", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("recipient_webtracking_history", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("mailing_successful_delivery_info", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("mailing_delivery_info", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("recipient_insights", Permission.RECIPIENTS_UI_MIGRATION);
        viewsPermissions.put("domains_entry_list", Permission.DOMAINS_UI_MIGRATION);
        viewsPermissions.put("domain_delete_ajax", Permission.DOMAINS_UI_MIGRATION);
        viewsPermissions.put("notification_list", Permission.PUSH_NOTIFICATION_UI_MIGRATION);
        viewsPermissions.put("mailing_list", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_undo", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("released_grid_templates", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_templates_redesigned", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("actions_list", Permission.TRIGGER_MANAGEMENT_UI_MIGRATION);
        viewsPermissions.put("actions_view_forms", Permission.TRIGGER_MANAGEMENT_UI_MIGRATION);
        viewsPermissions.put("actions_view", Permission.TRIGGER_MANAGEMENT_UI_MIGRATION);
        viewsPermissions.put("master_blacklist_global_add", Permission.GLOBAL_BLACKLIST_UI_MIGRATION);
        viewsPermissions.put("settings_company_list", Permission.CLIENTS_UI_MIGRATION);
        viewsPermissions.put("settings_company_delete", Permission.CLIENTS_UI_MIGRATION);
        viewsPermissions.put("settings_company_view", Permission.CLIENTS_UI_MIGRATION);
        viewsPermissions.put("settings_company_create", Permission.CLIENTS_UI_MIGRATION);
        viewsPermissions.put("settings_company_delete_samples", Permission.CLIENTS_UI_MIGRATION);
        viewsPermissions.put("settings_company_users", Permission.CLIENTS_UI_MIGRATION);
        viewsPermissions.put("targets_list", Permission.TARGET_GROUPS_UI_MIGRATION);
        viewsPermissions.put("target_view", Permission.TARGET_GROUPS_UI_MIGRATION);
        viewsPermissions.put("target_dependents_list", Permission.TARGET_GROUPS_UI_MIGRATION);
        viewsPermissions.put("targets_delete_recipients_confirm", Permission.TARGET_GROUPS_UI_MIGRATION);
        viewsPermissions.put("targets_delete_confirm", Permission.TARGET_GROUPS_UI_MIGRATION);
        viewsPermissions.put("targets_bulk_delete_confirm", Permission.TARGET_GROUPS_UI_MIGRATION);
        viewsPermissions.put("auto_export_list", Permission.AUTO_EXPORT_UI_MIGRATION);
        viewsPermissions.put("auto_export_delete_ajax", Permission.AUTO_EXPORT_UI_MIGRATION);
        viewsPermissions.put("auto_import_list", Permission.AUTO_IMPORT_UI_MIGRATION);
        viewsPermissions.put("auto_import_delete_ajax", Permission.AUTO_IMPORT_UI_MIGRATION);
        viewsPermissions.put("workflow_list", Permission.CAMPAIGNS_UI_MIGRATION);
        viewsPermissions.put("workflow_view", Permission.CAMPAIGNS_UI_MIGRATION);
        viewsPermissions.put("mailing_predelivery_list", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_settings_view", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_grid_base", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_images_list", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_attachments", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_trackablelink_list", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_trackablelink_view", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_trackablelink_bulk_actions", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_trackablelink_bulk_clear_extensions", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_priorities", Permission.MAILING_PRIORITY_UI_MIGRATION);
        viewsPermissions.put("archive_list", Permission.ARCHIVE_UI_MIGRATION);
        viewsPermissions.put("archive_view", Permission.ARCHIVE_UI_MIGRATION);
        viewsPermissions.put("import_modal", Permission.IMPORT_UI_MIGRATION);
        viewsPermissions.put("import_view", Permission.IMPORT_UI_MIGRATION);
        viewsPermissions.put("settings_manage_tables_listtables", Permission.REFERENCE_TABLES_UI_MIGRATION);
        viewsPermissions.put("recipient_reports", Permission.RECIPIENTS_REPORT_UI_MIGRATION);
        viewsPermissions.put("recipient_report_view", Permission.RECIPIENTS_REPORT_UI_MIGRATION);
        viewsPermissions.put("webhooks_list", Permission.WEBHOOKS_UI_MIGRATION);
        viewsPermissions.put("webhook_view", Permission.WEBHOOKS_UI_MIGRATION);
        viewsPermissions.put("birtreport_list", Permission.STATISTICS_REPORTS_UI_MIGRATION);
        viewsPermissions.put("birtreport_view", Permission.STATISTICS_REPORTS_UI_MIGRATION);
        viewsPermissions.put("mailing_recipients_list", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("ecs_preview", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("ecs_view", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("stats_mailing_view", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("mailing_content_list", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("generate_text_question_ajax", Permission.MAILING_UI_MIGRATION);
        viewsPermissions.put("company_insights", Permission.CUSTOMER_INSIGHTS_UI_MIGRATION);
        viewsPermissions.put("grid_template_text_modules", Permission.LAYOUT_BUILDER_UI_MIGRATION);
        viewsPermissions.put("grid_template_preview", Permission.LAYOUT_BUILDER_UI_MIGRATION);
        classicMailingSendViewNames.forEach(v -> viewsPermissions.put(v, Permission.MAILING_UI_MIGRATION));
        classicMailingPreviewViewNames.forEach(v -> viewsPermissions.put(v, Permission.MAILING_UI_MIGRATION));
        classicUsersViewNames.forEach(v -> viewsPermissions.put(v, Permission.USERS_UI_MIGRATION));
        classicLbViewNames.forEach(v -> viewsPermissions.put(v, Permission.LAYOUT_BUILDER_UI_MIGRATION));

        // ----- CUSTOM REDISGNED VIEW MAPPINGS -----
        classicMediapoolViewNames.forEach(v -> customRedesignedViewMappings.put(v, "mediapool-view_redesigned"));
        customRedesignedViewMappings.put("settings_admin_view", "user_view");
        customRedesignedViewMappings.put("settings_restfuluser_view", "user_view");
        customRedesignedViewMappings.put("settings_admin_rights", "user_permissions");
        customRedesignedViewMappings.put("settings_restfuluser_permissions", "user_permissions");
        customRedesignedViewMappings.put("birtreport_download", "evaluation_finished");
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
        if (request.isEmpty() || !AgnUtils.allowed(request.get(), Permission.UI_DESIGN_MIGRATION)) {
            return false;
        }

        return hasPermissionForRedesignedView(viewName, request.get());
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
