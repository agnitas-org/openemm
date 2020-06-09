/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import org.agnitas.beans.RowsCountAndSelectedFieldsWebStorageEntry;
import org.agnitas.beans.RowsCountWebStorageEntry;
import org.agnitas.service.WebStorage;
import org.agnitas.service.WebStorageBundle;

import com.agnitas.emm.core.mailing.dto.ComMailingOverviewWebStorageEntry;
import com.agnitas.emm.core.target.dto.TargetOverviewWebStorageEntry;

public interface ComWebStorage extends WebStorage {
    // Define required web-storage bundles here (proprietary EMM only).
    WebStorageBundle<ComMailingOverviewWebStorageEntry> MAILING_OVERVIEW = WebStorageBundle.define("mailing-overview", ComMailingOverviewWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> MAILING_PARAMETER_OVERVIEW = WebStorageBundle.define("mailing-parameter-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> WORKFLOW_OVERVIEW = WebStorageBundle.define("workflow-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> PROFILE_FIELD_OVERVIEW = WebStorageBundle.define("profile-field-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> BLACKLIST_OVERVIEW = WebStorageBundle.define("blacklist-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> AUTO_IMPORT_OVERVIEW = WebStorageBundle.define("auto-import-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> AUTO_EXPORT_OVERVIEW = WebStorageBundle.define("auto-export-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> IMPORT_EXPORT_LOG_OVERVIEW = WebStorageBundle.define("import-export-log-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountAndSelectedFieldsWebStorageEntry> MAILING_SEPARATE_STATS_OVERVIEW = WebStorageBundle.define("mailing-separate-stats-overview", RowsCountAndSelectedFieldsWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> BIRT_REPORT_OVERVIEW = WebStorageBundle.define("birt-report-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<TargetOverviewWebStorageEntry> TARGET_OVERVIEW = WebStorageBundle.define("target-overview", TargetOverviewWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> MAILINGLIST_OVERVIEW = WebStorageBundle.define("mailinglist-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> TRACKING_POINT_OVERVIEW = WebStorageBundle.define("tracking-point-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> CONTENT_SOURCE_OVERVIEW = WebStorageBundle.define("content-source-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> PLUGIN_MANAGER_OVERVIEW = WebStorageBundle.define("plugin-manager-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> MANAGE_TABLES_CONTENT_OVERVIEW = WebStorageBundle.define("manage-tables-content-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> LOGIN_TRACK_MANAGER_OVERVIEW = WebStorageBundle.define("login-track-manager-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> WS_MANAGER_OVERVIEW = WebStorageBundle.define("ws-manager-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> POPUP_NEWS_OVERVIEW = WebStorageBundle.define("popup-news-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> BOUNCE_FILTER_OVERVIEW = WebStorageBundle.define("bounce-filter-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> BUILDING_BLOCK_OVERVIEW = WebStorageBundle.define("building-block-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> MEDIAPOOL_IMAGE_OVERVIEW = WebStorageBundle.define("mediapool-image-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> MEDIAPOOL_FONT_OVERVIEW = WebStorageBundle.define("mediapool-font-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> MEDIAPOOL_PDF_OVERVIEW = WebStorageBundle.define("mediapool-pdf-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> MEDIAPOOL_VIDEO_OVERVIEW = WebStorageBundle.define("mediapool-video-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> MEDIAPOOL_CATEGORY_OVERVIEW = WebStorageBundle.define("mediapool-category-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> ADMIN_LOGIN_LOG_OVERVIEW = WebStorageBundle.define("admin-login-log-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> MESSENGER_OVERVIEW = WebStorageBundle.define("messenger-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> PUSH_NOTIFICATION_OVERVIEW = WebStorageBundle.define("push-notification-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountAndSelectedFieldsWebStorageEntry> MAILING_RECIPIENT_OVERVIEW = WebStorageBundle.define("mailing-recipient-overview", RowsCountAndSelectedFieldsWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> DATASOURCE_OVERVIEW = WebStorageBundle.define("datasource-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> COMPANY_OVERVIEW = WebStorageBundle.define("company-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> RECIPIENT_RETARGETING_HISTORY_OVERVIEW = WebStorageBundle.define("recipient-retargeting-history-overview", RowsCountWebStorageEntry.class);
    WebStorageBundle<RowsCountWebStorageEntry> RECIPIENT_DEVICE_HISTORY_OVERVIEW = WebStorageBundle.define("recipient-device-history-overview", RowsCountWebStorageEntry.class);
}
