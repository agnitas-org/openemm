/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.util.AgnUtils;

public class Permission {
	public static String[] CATEGORY_DISPLAY_ORDER = new String[0];
	public static final Map<String, String[]> SUBCATEGORY_DISPLAY_ORDER = new HashMap<>();

	public static final String CATEGORY_KEY_SYSTEM = "System";
	public static final String USERRIGHT_MESSAGEKEY_PREFIX = "UserRight.";

	private static final Map<String, Permission> ALL_PERMISSIONS = new HashMap<>();

	public static final Permission ACTIONS_CHANGE = new Permission("actions.change", true, PermissionType.Standard);
	public static final Permission ACTIONS_DELETE = new Permission("actions.delete", true, PermissionType.Standard);
	public static final Permission ACTIONS_SHOW = new Permission("actions.show", true, PermissionType.Standard);

	public static final Permission ADMIN_CHANGE = new Permission("admin.change", true, PermissionType.Standard);
	public static final Permission ADMIN_DELETE = new Permission("admin.delete", true, PermissionType.Standard);
	public static final Permission ADMIN_NEW = new Permission("admin.new", true, PermissionType.Standard);
	public static final Permission ADMIN_SETGROUP = new Permission("admin.setgroup", true, PermissionType.Standard);
	public static final Permission ADMIN_SETPERMISSION = new Permission("admin.setpermission", true, PermissionType.Standard);
	public static final Permission ADMIN_SHOW = new Permission("admin.show", true, PermissionType.Standard);
	public static final Permission ADMINLOG_SHOW = new Permission("adminlog.show", true, PermissionType.Standard);
	public static final Permission ADMIN_SEND_WELCOME = new Permission("admin.sendWelcome", true, PermissionType.Standard);

	public static final Permission RESTFULUSER_CHANGE = new Permission("restfulUser.change", true, PermissionType.System);
	public static final Permission RESTFULUSER_DELETE = new Permission("restfulUser.delete", true, PermissionType.System);
	public static final Permission RESTFULUSER_NEW = new Permission("restfulUser.new", true, PermissionType.System);
	public static final Permission RESTFULUSER_SHOW = new Permission("restfulUser.show", true, PermissionType.System);

	public static final Permission ALWAYS_ALLOWED = new Permission("always.allowed", false, PermissionType.Standard);
	public static final Permission ALWAYS_DISALLOWED = new Permission("always.disallowed", false, PermissionType.Standard);

	public static final Permission BLACKLIST = new Permission("blacklist", true, PermissionType.Standard);

	public static final Permission CALENDAR_SHOW = new Permission("calendar.show", true, PermissionType.Standard);

	public static final Permission CAMPAIGN_CHANGE = new Permission("campaign.change", true, PermissionType.Standard);
	public static final Permission CAMPAIGN_DELETE = new Permission("campaign.delete", true, PermissionType.Standard);
	public static final Permission CAMPAIGN_SHOW = new Permission("campaign.show", true, PermissionType.Standard);

	public static final Permission CHARSET_USE_ISO_8859_15 = new Permission("charset.use.iso_8859_15", true, PermissionType.Standard);
	public static final Permission CHARSET_USE_UTF_8 = new Permission("charset.use.utf_8", true, PermissionType.Standard);

	public static final Permission CKEDITOR_TRIMMED = new Permission("mailing.editor.trimmed", false, PermissionType.Standard);
	public static final Permission WYSIWYG_EDITOR_HIDE = new Permission("mailing.editor.hide", false, PermissionType.Standard);
	public static final Permission HTML_EDITOR_HIDE = new Permission("mailing.editor.hide.html", false, PermissionType.Standard);
	public static final Permission CKEDITOR_EXTENDED = new Permission("mailing.editor.extended", false, PermissionType.Standard);

	public static final Permission DATASOURCE_SHOW = new Permission("datasource.show", true, PermissionType.Standard);
	
	public static final Permission EXPORT_CHANGE = new Permission("export.change", true, PermissionType.Standard);
	public static final Permission EXPORT_DELETE = new Permission("export.delete", true, PermissionType.Standard);

	public static final Permission FORMS_CHANGE = new Permission("forms.change", true, PermissionType.Standard);
	public static final Permission FORMS_DELETE = new Permission("forms.delete", true, PermissionType.Standard);
	public static final Permission FORMS_IMPORT = new Permission("forms.import", true, PermissionType.Standard);
	public static final Permission FORMS_SHOW = new Permission("forms.show", true, PermissionType.Standard);

	public static final Permission IMPORT_CHANGE = new Permission("import.change", true, PermissionType.Standard);
	public static final Permission IMPORT_DELETE = new Permission("import.delete", true, PermissionType.Standard);
	public static final Permission IMPORT_MEDIATYPE = new Permission("import.mediatype", false, PermissionType.Premium);
	public static final Permission IMPORT_MODE_ADD = new Permission("import.mode.add", true, PermissionType.Standard);
	public static final Permission IMPORT_MODE_ADD_UPDATE = new Permission("import.mode.add_update", true, PermissionType.Standard);
	public static final Permission IMPORT_MODE_BLACKLIST = new Permission("import.mode.blacklist", true, PermissionType.Standard);
	public static final Permission IMPORT_MODE_BLACKLIST_EXCLUSIVE = new Permission("import.mode.blacklist_exclusive", false, PermissionType.Premium);
	public static final Permission IMPORT_MODE_BOUNCE = new Permission("import.mode.bounce", true, PermissionType.Standard);
	public static final Permission IMPORT_MODE_BOUNCEREACTIVATE = new Permission("import.mode.bouncereactivate", false, PermissionType.Premium);
	public static final Permission IMPORT_MODE_DOUBLECHECKING = new Permission("import.mode.doublechecking", true, PermissionType.Standard);
	public static final Permission IMPORT_MODE_ONLY_UPDATE = new Permission("import.mode.only_update", true, PermissionType.Standard);
	public static final Permission IMPORT_MAILINGLISTS_ALL = new Permission("import.mailinglists.all", true, PermissionType.Standard);

	/** Show classic import the GUI toggle and set default to "update only the first duplicate", otherwise default is "update all dupliactes" **/
	public static final Permission IMPORT_MODE_UNSUBSCRIBE = new Permission("import.mode.unsubscribe", true, PermissionType.Standard);
	public static final Permission IMPORT_MODE_DUPLICATES = new Permission("import.mode.duplicates", true, PermissionType.Standard);
	/** Import customer data without subscribing it to a mailinglist **/
	public static final Permission IMPORT_WITHOUT_MAILINGLIST = new Permission("import.mailinglist.without", false, PermissionType.Standard);

	public static final Permission MAILING_ATTACHMENTS_SHOW = new Permission("mailing.attachments.show", true, PermissionType.Standard);
	public static final Permission MAILING_CAN_SEND_ALWAYS = new Permission("mailing.can_send_always", true, PermissionType.Standard);
	public static final Permission MAILING_CHANGE = new Permission("mailing.change", true, PermissionType.Standard);
	public static final Permission MAILING_CLASSIC = new Permission("mailing.classic", true, PermissionType.Standard);
	public static final Permission MAILING_COMPONENTS_CHANGE = new Permission("mailing.components.change", true, PermissionType.Standard);
	public static final Permission MAILING_COMPONENTS_SHOW = new Permission("mailing.components.show", true, PermissionType.Standard);
	public static final Permission MAILING_CONTENT_CHANGE_ALWAYS = new Permission("mailing.content.change.always", false, PermissionType.System);
	public static final Permission MAILING_CONTENT_SHOW_EXCLUDED_TARGETGROUPS = new Permission("mailing.content.showExcludedTargetgroups", false, PermissionType.Standard);
	/** Negative permission **/
	public static final Permission MAILING_CONTENT_SHOW = new Permission("mailing.content.show", true, PermissionType.Standard);
	public static final Permission MAILING_DELETE = new Permission("mailing.delete", true, PermissionType.Standard);
	public static final Permission MAILING_EMC = new Permission("mailing.emc", false, PermissionType.Premium);
	/** Allow link extension change **/
	public static final Permission MAILING_EXTEND_TRACKABLE_LINKS = new Permission("mailing.extend_trackable_links", true, PermissionType.Standard);
	public static final Permission MAILING_IMPORT = new Permission("mailing.import", true, PermissionType.Standard);
	public static final Permission MAILING_SEND_ADMIN_OPTIONS = new Permission("mailing.send.admin.options", true, PermissionType.Standard);
	public static final Permission MAILING_SEND_ADMIN_TARGET = new Permission("mailing.send.admin.target", true, PermissionType.Standard);
	public static final Permission MAILING_SEND_SHOW = new Permission("mailing.send.show", true, PermissionType.Standard);
	public static final Permission MAILING_SEND_WORLD = new Permission("mailing.send.world", true, PermissionType.Standard);
	public static final Permission MAILING_SETMAXRECIPIENTS = new Permission("mailing.setmaxrecipients", true, PermissionType.Standard);
	public static final Permission MAILING_SHOW = new Permission("mailing.show", true, PermissionType.Standard);
	public static final Permission MAILING_SHOW_TYPES = new Permission("mailing.show.types", true, PermissionType.Standard);
	
	/** Negative permission **/
	public static final Permission MAILING_CONTENT_READONLY = new Permission("mailing.content.readonly", true, PermissionType.Standard);

	public static final Permission MAILINGLIST_CHANGE = new Permission("mailinglist.change", true, PermissionType.Standard);
	public static final Permission MAILINGLIST_DELETE = new Permission("mailinglist.delete", true, PermissionType.Standard);
	public static final Permission MAILINGLIST_RECIPIENTS_DELETE = new Permission("mailinglist.recipients.delete", true, PermissionType.Standard);
	public static final Permission MAILINGLIST_SHOW = new Permission("mailinglist.show", true, PermissionType.Standard);

	public static final Permission MAILLOOP_CHANGE = new Permission("mailloop.change", true, PermissionType.Standard);
	public static final Permission MAILLOOP_DELETE = new Permission("mailloop.delete", true, PermissionType.Standard);
	public static final Permission MAILLOOP_SHOW = new Permission("mailloop.show", true, PermissionType.Standard);

	public static final Permission MEDIATYPE_EMAIL = new Permission("mediatype.email", true, PermissionType.Standard);

	public static final Permission PROFILEFIELD_SHOW = new Permission("profileField.show", true, PermissionType.Standard);
	public static final Permission PROFILEFIELD_VISIBLE = new Permission("profileField.visible", true, PermissionType.Standard);

	public static final Permission RECIPIENT_CHANGE = new Permission("recipient.change", true, PermissionType.Standard);
	public static final Permission RECIPIENT_CHANGEBULK = new Permission("recipient.change.bulk", true, PermissionType.Standard);
	public static final Permission RECIPIENT_CREATE = new Permission("recipient.create", true, PermissionType.Standard);
	public static final Permission RECIPIENT_DELETE = new Permission("recipient.delete", true, PermissionType.Standard);
	public static final Permission RECIPIENT_GENDER_EXTENDED = new Permission("recipient.gender.extended", true, PermissionType.Standard);
	public static final Permission RECIPIENT_HISTORY = new Permission("recipient.history", true, PermissionType.Standard);
	public static final Permission RECIPIENT_SHOW = new Permission("recipient.show", true, PermissionType.Standard);
	public static final Permission RECIPIENT_HISTORY_MAILING_DELIVERY = new Permission("recipient.history.mailing.delivery", false, PermissionType.Premium);
	
	public static final Permission REPORT_BIRT_DELETE = new Permission("report.birt.delete", true, PermissionType.Standard);
	public static final Permission REPORT_BIRT_CHANGE = new Permission("report.birt.change", true, PermissionType.Standard);
	public static final Permission REPORT_BIRT_SHOW = new Permission("report.birt.show", true, PermissionType.Standard);
	
	public static final Permission ROLE_CHANGE = new Permission("role.change", true, PermissionType.Standard);
	public static final Permission ROLE_DELETE = new Permission("role.delete", true, PermissionType.Standard);
	public static final Permission ROLE_SHOW = new Permission("role.show", true, PermissionType.Standard);

	public static final Permission SALUTATION_CHANGE = new Permission("salutation.change", true, PermissionType.Standard);
	public static final Permission SALUTATION_DELETE = new Permission("salutation.delete", true, PermissionType.Standard);
	public static final Permission SALUTATION_SHOW = new Permission("salutation.show", true, PermissionType.Standard);
	public static final Permission SHOW_MIGRATION_PERMISSIONS = new Permission("master.permission.migration.show", false, PermissionType.System);
	public static final Permission SERVER_STATUS = new Permission("server.status", true, PermissionType.System);

	public static final Permission SETTINGS_EXTENDED = new Permission("settings.extended", true, PermissionType.Standard);
	
	public static final Permission STATISTIC_SOFTBOUNCES_SHOW = new Permission("statistic.softbounces.show", true, PermissionType.Standard);

	public static final Permission STATS_DOMAINS = new Permission("stats.domains", true, PermissionType.Standard);
	public static final Permission STATS_ECS = new Permission("stats.ecs", true, PermissionType.Standard);
	public static final Permission STATS_MAILING = new Permission("stats.mailing", true, PermissionType.Standard);
	public static final Permission STATS_MONTH = new Permission("stats.month", true, PermissionType.Standard);
	public static final Permission STATS_SHOW = new Permission("stats.show", true, PermissionType.Standard);
	public static final Permission STATS_USERFORM = new Permission("stats.userform", true, PermissionType.Standard);
	public static final Permission STATS_REVENUE = new Permission("stats.revenue", false, PermissionType.Standard);

	public static final Permission TARGETS_CHANGE = new Permission("targets.change", true, PermissionType.Standard);
	public static final Permission TARGETS_DELETE = new Permission("targets.delete", true, PermissionType.Standard);
	public static final Permission TARGETS_LOCK = new Permission("targets.lock", true, PermissionType.Standard);
	public static final Permission TARGETS_SHOW = new Permission("targets.show", true, PermissionType.Standard);

	public static final Permission TEMPLATE_CHANGE = new Permission("template.change", true, PermissionType.Standard);
	public static final Permission TEMPLATE_DELETE = new Permission("template.delete", true, PermissionType.Standard);
	public static final Permission TEMPLATE_SHOW = new Permission("template.show", true, PermissionType.Standard);

	public static final Permission WEBSERVICE_USER_CHANGE = new Permission("webservice.user.change", true, PermissionType.System);
	public static final Permission WEBSERVICE_USER_CREATE = new Permission("webservice.user.create", true, PermissionType.System);
	public static final Permission WEBSERVICE_USER_SHOW = new Permission("webservice.user.show", true, PermissionType.System);

	public static final Permission WIZARD_EXPORT = new Permission("wizard.export", true, PermissionType.Standard);
	public static final Permission WIZARD_IMPORT = new Permission("wizard.import", true, PermissionType.Standard);
	public static final Permission WIZARD_IMPORTCLASSIC = new Permission("wizard.importclassic", true, PermissionType.Standard);

	public static final Permission WORKFLOW_ACTIVATE = new Permission("workflow.activate", true, PermissionType.Standard);
	public static final Permission WORKFLOW_CHANGE = new Permission("workflow.change", true, PermissionType.Standard);
	public static final Permission WORKFLOW_DELETE = new Permission("workflow.delete", true, PermissionType.Standard);
	public static final Permission WORKFLOW_SHOW = new Permission("workflow.show", true, PermissionType.Standard);

	public static final Permission FORMS_EXPORT = new Permission("forms.export", true, PermissionType.Standard);
	public static final Permission MAILING_EXPORT = new Permission("mailing.export", true, PermissionType.Standard);

	// Permissions Extended
	public static final Permission AI_IMAGES_GENERATION = new Permission("ai.images.generation", false, PermissionType.Premium);
	public static final Permission AUTO_IMPORT_CONTENT_SOURCE = new Permission("auto.import.content.source", false, PermissionType.Premium);
	public static final Permission CLEANUP_RECIPIENT_TRACKING = new Permission("cleanup.recipient.tracking", false, PermissionType.Premium);
	public static final Permission CLEANUP_RECIPIENT_DATA = new Permission("cleanup.recipient.data", false, PermissionType.Premium);
	public static final Permission COMPANY_AUTHENTICATION = new Permission("company.authentication", false, PermissionType.System);
	public static final Permission COMPANY_DEFAULT_STEPPING = new Permission("company.default.stepping", false, PermissionType.System);
	public static final Permission COMPANY_SETTINGS_INTERN = new Permission("company.settings.intern", false, PermissionType.System);
	public static final Permission COMPANY_SETTINGS_DEEPTRACKING = new Permission("company.settings.deeptracking", false, PermissionType.Premium);
	
	public static final Permission DEEPTRACKING = new Permission("deeptracking", false, PermissionType.Standard);
	public static final Permission EXPORT_OWN_COLUMNS = new Permission("export.ownColumns", false, PermissionType.Standard);
	public static final Permission IMPORT_CUSTOMERID = new Permission("import.customerid", false, PermissionType.Premium);
	public static final Permission IMPORT_DATATYPE_JSON = new Permission("import.datatype.json", false, PermissionType.Premium);
	public static final Permission IMPORT_MAPPING_AUTO = new Permission("import.mapping.auto", false, PermissionType.Premium);
	public static final Permission IMPORT_MODE_REACTIVATE_SUSPENDED = new Permission("import.mode.reactivateSuspended", false, PermissionType.Premium);
	public static final Permission IMPORT_MODE_ADD_UPDATE_FORCED = new Permission("import.mode.add_update_forced", false, PermissionType.Premium);
	public static final Permission IMPORT_MODE_REMOVE_STATUS = new Permission("import.mode.remove_status", false, PermissionType.Premium);
	/** Allow configuration of SQL action scripts from import_action_tbl to execute before import is executed **/
	public static final Permission IMPORT_PREPROCESSING = new Permission("import.preprocessing", false, PermissionType.Premium);
	public static final Permission MAILING_AI_CONTENT = new Permission("mailing.ai.content", false, PermissionType.Premium);
	/** Import customer data without subscribing it to a mailinglist **/
	public static final Permission MAILING_CAN_ALLOW = new Permission("mailing.can_allow", false, PermissionType.Standard);
	public static final Permission MAILING_COMPONENTS_SFTP = new Permission("mailing.components.sftp", false, PermissionType.Premium);
	public static final Permission MAILING_CONTENT_DISABLE_LINKEXTENSION = new Permission("mailing.content.disableLinkExtension", false, PermissionType.Premium);
	public static final Permission MAILING_CONTENTSOURCE_DATE_LIMIT = new Permission("mailing.contentsource.date.limit", false, PermissionType.Standard);
	public static final Permission MAILING_ENCRYPTED_SEND = new Permission("mailing.encrypted.send", false, PermissionType.System);
	public static final Permission MAILING_ENVELOPE_ADDRESS = new Permission("mailing.envelope_address", false, PermissionType.Standard);
	public static final Permission MAILING_EXPIRE = new Permission("mailing.expire", false, PermissionType.System);
	/** Allow link extension change **/
	public static final Permission MAILING_PARAMETER_CHANGE = new Permission("mailing.parameter.change", false, PermissionType.Premium);
	public static final Permission MAILING_PARAMETER_SHOW = new Permission("mailing.parameter.show", false, PermissionType.Premium);
	public static final Permission MAILING_RECIPIENTS_SHOW = new Permission("mailing.recipients.show", true, PermissionType.Standard);
	public static final Permission MAILING_RESUME_WORLD = new Permission("mailing.resume.world", false, PermissionType.Standard);
	/** Show the tab "recipients" within the GUI mailing view **/
	public static final Permission MAILING_SETTINGS_HIDE = new Permission("mailing.settings.hide", false, PermissionType.Standard);
	public static final Permission MAILING_TRACKABLELINKS_NOCLEANUP = new Permission("mailing.trackablelinks.nocleanup", false, PermissionType.Premium);
	public static final Permission MAILING_TRACKABLELINKS_STATIC = new Permission("mailing.trackablelinks.static", false, PermissionType.Premium);
	/** Edit link targets in sent mailings **/
	public static final Permission MAILING_TRACKABLELINKS_URL_CHANGE = new Permission("mailing.trackablelinks.url.change", false, PermissionType.Premium);
	public static final Permission MASTER_COMPANIES_SHOW = new Permission("master.companies.show", false, PermissionType.System);
	/**	user activity log permissions **/
	public static final Permission MASTERLOG_SHOW = new Permission("masterlog.show", false, PermissionType.System);
	public static final Permission MASTER_SHOW = new Permission("master.show", false, PermissionType.System);
	public static final Permission MEDIATYPE_FAX = new Permission("mediatype.fax", false, PermissionType.Premium);
	public static final Permission MEDIATYPE_POST = new Permission("mediatype.post", false, PermissionType.Premium);
	public static final Permission MEDIATYPE_SMS = new Permission("mediatype.sms", false, PermissionType.Premium);
	public static final Permission RECIPIENT_HISTORY_MAILING = new Permission("recipient.history.mailing", false, PermissionType.Premium);
	public static final Permission RECIPIENT_IMPORT_ENCRYPTED = new Permission("recipient.import.encrypted", false, PermissionType.Premium);
	public static final Permission USER_ACTIVITY_ACTIONS_EXTENDED = new Permission("user.activity.actions.extended", false, PermissionType.System);
	public static final Permission ADMIN_MANAGEMENT_SHOW = new Permission("admin.management.show", true, PermissionType.Standard);

	// Special migration and rollback permissions
	public static final Permission AI_SUPPORT_CHAT = new Permission("ai.support.chat", false, PermissionType.Migration);
    public static final Permission RECIPIENT_DISTRIBUTION_STAT = new Permission("recipient.stat.distribution", false, PermissionType.Migration);

	// ---------- REDESIGN ----------
	public static final Permission USE_OLD_UI = new Permission("use.old.ui", false, PermissionType.Migration);
	public static final Permission DASHBOARD_ADD_ONS_TILE = new Permission("dashboard.add-ons.tile", false, PermissionType.Migration);
	// ------------------------------

	private final String tokenString;
	private final boolean visible;
	private final PermissionType permissionType;
	
	protected Permission(String tokenString, final boolean visible, final PermissionType permissionType) throws RuntimeException {
		this.tokenString = tokenString;
		this.visible = visible;
		this.permissionType = permissionType;

		Permission existing = ALL_PERMISSIONS.get(tokenString);
		if (existing != null) {
			throw new RuntimeException("Duplicate creation of permission: " + tokenString);
		}

		ALL_PERMISSIONS.put(tokenString, this);
	}

	@Override
	public String toString() {
		return tokenString;
	}

	public String getTokenString() {
		return tokenString;
	}

	public final boolean isVisible() {
		return this.visible;
	}

	public final PermissionType getPermissionType() {
		return permissionType;
	}
	
	/**
	 * Get a list of all known permissions. The list of permissions cannot be
	 * changed this way.
	 */
	public static List<Permission> getAllSystemPermissions() {
		return new ArrayList<>(ALL_PERMISSIONS.values());
	}

	public static Permission getPermissionByToken(String token) {
		return ALL_PERMISSIONS.get(token);
	}

	public static Set<Permission> fromTokens(Collection<String> tokens) {
		HashSet<Permission> permissions = new HashSet<>();

		for (String token : tokens) {
			Permission permission = getPermissionByToken(token);
			if (permission != null) {
				permissions.add(permission);
			}
		}

		return permissions;
	}

	public static Permission[] getPermissionsByToken(String token) throws Exception {
		List<Permission> permissionList = new ArrayList<>();
		for (String tokenString : AgnUtils.splitAndTrimStringlist(token)) {
			Permission permission = getPermissionByToken(tokenString);
			if (permission == null) {
				throw new Exception("Invalid security token found: " + token);
			} else {
				permissionList.add(permission);
			}
		}
		return permissionList.toArray(new Permission[permissionList.size()]);
	}

	/**
	 * @param allowedPremiumPermissions
	 * @return Returns true if grantedPermissions contains any of
	 *         checkedPermissions.
	 */
	public static boolean permissionAllowed(Collection<Permission> grantedPermissions, Collection<Permission> allowedPremiumPermissions, Permission... checkedPermissions) {
		for (Permission permission : checkedPermissions) {
			if (permission == null || permission == Permission.ALWAYS_DISALLOWED) {
				return false;
			} else if (permission == Permission.ALWAYS_ALLOWED) {
				return true;
			} else if (grantedPermissions.contains(permission)) {
				if (permission.getPermissionType() == PermissionType.Standard
						|| permission.getPermissionType() == PermissionType.Migration
						|| (allowedPremiumPermissions != null && allowedPremiumPermissions.contains(permission))) {
					return true;
				}
			}
		}
		return false;
	}
}
