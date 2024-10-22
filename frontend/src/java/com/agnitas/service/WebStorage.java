/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import com.agnitas.emm.core.mailing.dto.MailingOverviewWebStorageEntry;
import com.agnitas.emm.core.target.dto.FilterTypesAndRowsCountWebStorageEntry;
import com.agnitas.emm.core.target.dto.TargetOverviewWebStorageEntry;
import com.agnitas.emm.core.trackablelinks.dto.TrackableLinksOverviewWebStorageEntry;
import org.agnitas.beans.BooleanWebStorageEntry;
import org.agnitas.beans.RowsCountAndSelectedFieldsWebStorageEntry;
import org.agnitas.beans.SortingWebStorageEntry;
import org.agnitas.beans.WebStorageEntry;
import org.agnitas.emm.core.recipient.dto.RecipientOverviewWebStorageEntry;
import org.agnitas.service.WebStorageBundle;

import java.util.function.Consumer;

public interface WebStorage {
    // Define required web-storage bundles here (available to OpenEMM).
    WebStorageBundle<MailingOverviewWebStorageEntry> MAILING_OVERVIEW = WebStorageBundle.define("mailing-overview", MailingOverviewWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> MAILING_COMPARISON_OVERVIEW = WebStorageBundle.define("mailing-comparison-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> ARCHIVE_OVERVIEW = WebStorageBundle.define("archive-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> ARCHIVE_MAILINGS_OVERVIEW = WebStorageBundle.define("archive-mailings-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> SALUTATION_OVERVIEW = WebStorageBundle.define("salutation-overview", SortingWebStorageEntry.class);
    WebStorageBundle<RecipientOverviewWebStorageEntry> RECIPIENT_OVERVIEW = WebStorageBundle.define("recipient-overview", RecipientOverviewWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> BIRT_REPORT_OVERVIEW = WebStorageBundle.define("birt-report-overview", SortingWebStorageEntry.class);
    WebStorageBundle<RecipientOverviewWebStorageEntry> RECIPIENT_DUPLICATE_OVERVIEW = WebStorageBundle.define("recipient-duplicate-overview", RecipientOverviewWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> RECIPIENT_STATUS_HISTORY_OVERVIEW = WebStorageBundle.define("recipient-status-history-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> RECIPIENT_MAILING_HISTORY_OVERVIEW = WebStorageBundle.define("recipient-mailing-history-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> IMPORT_PROFILE_OVERVIEW = WebStorageBundle.define("import-profile-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> EXPORT_PROFILE_OVERVIEW = WebStorageBundle.define("export-profile-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> USERFORM_OVERVIEW = WebStorageBundle.define("userform-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> USERFORM_IMAGES_OVERVIEW = WebStorageBundle.define("userform-images-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> MAILING_IMAGES_OVERVIEW = WebStorageBundle.define("mailing-images-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> MAILING_ATTACHMENTS_OVERVIEW = WebStorageBundle.define("mailing-attachments-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> ACTION_OVERVIEW = WebStorageBundle.define("action-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> USERLOG_OVERVIEW = WebStorageBundle.define("useractivitylog-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> RESTFUL_USERLOG_OVERVIEW = WebStorageBundle.define("restful-useractivitylog-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> SOAP_USERLOG_OVERVIEW = WebStorageBundle.define("soap-useractivitylog-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> ADMIN_OVERVIEW = WebStorageBundle.define("admin-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> USER_GROUP_OVERVIEW = WebStorageBundle.define("user-group-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> IMPORT_WIZARD_ERRORS_OVERVIEW = WebStorageBundle.define("import-wizard-errors-overview", SortingWebStorageEntry.class);
    // TODO: EMMGUI-714: replace with SortingWebStorageEntry when old design will be removed
    WebStorageBundle<FilterTypesAndRowsCountWebStorageEntry> TARGET_DEPENDENTS_OVERVIEW = WebStorageBundle.define("target-dependents-overview", FilterTypesAndRowsCountWebStorageEntry.class);
    WebStorageBundle<FilterTypesAndRowsCountWebStorageEntry> MAILING_SEND_DEPENDENTS_OVERVIEW = WebStorageBundle.define("mailing-send-dependents-overview", FilterTypesAndRowsCountWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> PROFILE_FIELD_DEPENDENTS_OVERVIEW = WebStorageBundle.define("profile-fields-dependents-overview", SortingWebStorageEntry.class);
    WebStorageBundle<BooleanWebStorageEntry> IS_WIDE_SIDEBAR = WebStorageBundle.define("is-wide-sidebar", BooleanWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> MAILINGLIST_OVERVIEW = WebStorageBundle.define("mailinglist-overview", SortingWebStorageEntry.class);
    WebStorageBundle<TargetOverviewWebStorageEntry> TARGET_OVERVIEW = WebStorageBundle.define("target-overview", TargetOverviewWebStorageEntry.class);
    WebStorageBundle<RowsCountAndSelectedFieldsWebStorageEntry> MAILING_SEPARATE_STATS_OVERVIEW = WebStorageBundle.define("mailing-separate-stats-overview", RowsCountAndSelectedFieldsWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> IMPORT_EXPORT_LOG_OVERVIEW = WebStorageBundle.define("import-export-log-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> PROFILE_FIELD_OVERVIEW = WebStorageBundle.define("profile-field-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> WORKFLOW_OVERVIEW = WebStorageBundle.define("workflow-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> BLACKLIST_OVERVIEW = WebStorageBundle.define("blacklist-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> WS_MANAGER_OVERVIEW = WebStorageBundle.define("ws-manager-overview", SortingWebStorageEntry.class);
    WebStorageBundle<TrackableLinksOverviewWebStorageEntry> TRACKABLE_LINKS = WebStorageBundle.define("trackable-links-overview", TrackableLinksOverviewWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> ADMIN_LOGIN_LOG_OVERVIEW = WebStorageBundle.define("admin-login-log-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> DATASOURCE_OVERVIEW = WebStorageBundle.define("datasource-overview", SortingWebStorageEntry.class);
    WebStorageBundle<SortingWebStorageEntry> BOUNCE_FILTER_OVERVIEW = WebStorageBundle.define("bounce-filter-overview", SortingWebStorageEntry.class);
    WebStorageBundle<RowsCountAndSelectedFieldsWebStorageEntry> MAILING_RECIPIENT_OVERVIEW = WebStorageBundle.define("mailing-recipient-overview", RowsCountAndSelectedFieldsWebStorageEntry.class);

    /**
     * Clear storage, then parse {@code dataAsJson} and store all the recognized bundles in the storage. The {@code dataAsJson}
     * must contain a single root object, each property represents a separate bundle (property name should match the bundle name
     * so the property value will be parsed as a type returned by {@link WebStorageBundle#getType()}).
     * All the bundles missing from {@link WebStorageBundle#definitions()} will be ignored.
     *
     * @param dataAsJson a JSON-string containing web-storage bundles to store.
     */
    void setup(String dataAsJson);

    /**
     * Clear storage, remove all the stored data from memory.
     */
    void reset();

    /**
     * Get a clone of a data bundle identified by given {@code bundle} descriptor. Each time this method is called it creates
     * and returns a fresh copy of a stored data bundle to prevent concurrency issues caused by simultaneous read/write
     * operations on the same bundles.
     *
     * If the requested data bundle is missing it will be instantiated immediately using default constructor.
     *
     * @param key a descriptor (key) of a data bundle to be accessed.
     * @return a fresh copy of requested data bundle.
     */
    <T extends WebStorageEntry> T get(WebStorageBundle<T> key);

    /**
     * Have a synchronized access (for both read and write operations) via {@code consumer} functional interface to a data
     * bundle identified by given {@code bundle} descriptor. The {@code consumer} gets an original object, so all the changes
     * made to a data bundle within that function will be preserved.
     *
     * If the requested data bundle is missing it will be instantiated immediately using default constructor.
     *
     * @param key a descriptor (key) of a data bundle to be accessed.
     * @param consumer a function that the referenced data bundle will be passed to.
     */
    <T extends WebStorageEntry> void access(WebStorageBundle<T> key, Consumer<T> consumer);


    /**
     * Have a synchronized access via {@code key}.
     *
     * @param key a descriptor (key) of a data bundle to be accessed.
     * @return true if entry for the key has already been initialized.
     */
    <T extends WebStorageEntry> boolean isPresented(WebStorageBundle<T> key);
}
