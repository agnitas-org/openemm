/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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

public class Permission implements Comparable<Permission> {
	public static final String[] CATEGORY_DISPLAY_ORDER = new String[] { "General", "Mailing", "Template", "Campaigns", "Subscriber-Editor", "ImportExport", "Target-Groups", "Statistics", "Forms", "Actions", "Administration", "NegativePermissions", "System", "Premium", "PushNotifications", "Messenger" };

	public static final String CATEGORY_KEY_SYSTEM = "System";
	public static final String USERRIGHT_MESSAGEKEY_PREFIX = "UserRight.";

	private static final Map<String, Permission> ALL_PERMISSIONS = new HashMap<>();

	public static final Permission ACTIONS_CHANGE = new Permission("actions.change", true, false);
	public static final Permission ACTIONS_DELETE = new Permission("actions.delete", true, false);
	public static final Permission ACTIONS_SHOW = new Permission("actions.show", true, false);

	public static final Permission ADMIN_CHANGE = new Permission("admin.change", true, false);
	public static final Permission ADMIN_DELETE = new Permission("admin.delete", true, false);
	public static final Permission ADMIN_NEW = new Permission("admin.new", true, false);
	public static final Permission ADMIN_SETGROUP = new Permission("admin.setgroup", true, false);
	public static final Permission ADMIN_SETPERMISSION = new Permission("admin.setpermission", true, false);
	public static final Permission ADMIN_SHOW = new Permission("admin.show", true, false);
	public static final Permission ADMINLOG_SHOW = new Permission("adminlog.show", false, false);

	public static final Permission ALWAYS_ALLOWED = new Permission("always.allowed", false, false);
	public static final Permission ALWAYS_DISALLOWED = new Permission("always.disallowed", false, false);

	public static final Permission BLACKLIST = new Permission("blacklist", true, false);

	public static final Permission CALENDAR_SHOW = new Permission("calendar.show", true, false);

	public static final Permission CAMPAIGN_CHANGE = new Permission("campaign.change", true, false);
	public static final Permission CAMPAIGN_DELETE = new Permission("campaign.delete", true, false);
	public static final Permission CAMPAIGN_SHOW = new Permission("campaign.show", true, false);
	public static final Permission CAMPAIGN_AUTOOPT = new Permission("campaign.autoopt", true, false);

	public static final Permission CHARSET_USE_ISO_8859_15 = new Permission("charset.use.iso_8859_15", true, false);
	public static final Permission CHARSET_USE_UTF_8 = new Permission("charset.use.utf_8", true, false);

	public static final Permission CKEDITOR_TRIMMED = new Permission("mailing.editor.trimmed", false, false);

	public static final Permission FORMS_CHANGE = new Permission("forms.change", true, false);
	public static final Permission FORMS_DELETE = new Permission("forms.delete", true, false);
	public static final Permission FORMS_IMPORT = new Permission("forms.import", true, false);
	public static final Permission FORMS_SHOW = new Permission("forms.show", true, false);

	public static final Permission IMPORT_MEDIATYPE = new Permission("import.mediatype", false, true);
	public static final Permission IMPORT_MODE_ADD = new Permission("import.mode.add", true, false);
	public static final Permission IMPORT_MODE_ADD_UPDATE = new Permission("import.mode.add_update", true, false);
	public static final Permission IMPORT_MODE_BLACKLIST = new Permission("import.mode.blacklist", true, false);
	public static final Permission IMPORT_MODE_BLACKLIST_EXCLUSIVE = new Permission("import.mode.blacklist_exclusive", true, false);
	public static final Permission IMPORT_MODE_BOUNCE = new Permission("import.mode.bounce", true, false);
	public static final Permission IMPORT_MODE_BOUNCEREACTIVATE = new Permission("import.mode.bouncereactivate", false, true);
	public static final Permission IMPORT_MODE_DOUBLECHECKING = new Permission("import.mode.doublechecking", true, false);
	public static final Permission IMPORT_MODE_ONLY_UPDATE = new Permission("import.mode.only_update", true, false);

	/** Show classic import the GUI toggle and set default to "update only the first duplicate", otherwise default is "update all dupliactes" **/
	public static final Permission IMPORT_MODE_UNSUBSCRIBE = new Permission("import.mode.unsubscribe", true, false);
	public static final Permission IMPORT_MODE_DUPLICATES = new Permission("import.mode.duplicates", true, false);
	/** Import customer data without subscribing it to a mailinglist **/
	public static final Permission IMPORT_WITHOUT_MAILINGLIST = new Permission("import.mailinglist.without", false, true);
	
	public static final Permission MAILING_ATTACHMENTS_SHOW = new Permission("mailing.attachments.show", true, false);
	public static final Permission MAILING_CAN_SEND_ALWAYS = new Permission("mailing.can_send_always", true, false);
	public static final Permission MAILING_CHANGE = new Permission("mailing.change", true, false);
	public static final Permission MAILING_COMPONENTS_CHANGE = new Permission("mailing.components.change", true, false);
	public static final Permission MAILING_COMPONENTS_SHOW = new Permission("mailing.components.show", true, false);
	public static final Permission MAILING_CONTENT_CHANGE_ALWAYS = new Permission("mailing.content.change.always", false, true);
	public static final Permission MAILING_CONTENT_SHOW_EXCLUDED_TARGETGROUPS = new Permission("mailing.content.showExcludedTargetgroups", true, false);
	/** Negative right **/
	public static final Permission MAILING_CONTENT_SHOW = new Permission("mailing.content.show", true, false);

	/** Allow link extension change **/
	public static final Permission MAILING_EXTEND_TRACKABLE_LINKS = new Permission("mailing.extend_trackable_links", true, false);
	public static final Permission MAILING_IMPORT = new Permission("mailing.import", true, false);
	public static final Permission MAILING_SEND_ADMIN_OPTIONS = new Permission("mailing.send.admin.options", true, false);
	public static final Permission MAILING_SEND_ADMIN_TARGET = new Permission("mailing.send.admin.target", true, false);
	public static final Permission MAILING_SEND_SHOW = new Permission("mailing.send.show", true, false);
	public static final Permission MAILING_SEND_WORLD = new Permission("mailing.send.world", true, false);
	public static final Permission MAILING_SETMAXRECIPIENTS = new Permission("mailing.setmaxrecipients", true, false);
	public static final Permission MAILING_SHOW = new Permission("mailing.show", true, false);
	public static final Permission MAILING_SHOW_TYPES = new Permission("mailing.show.types", true, false);


	/** Edit link targets in sent mailings **/
	public static final Permission MAILINGLIST_CHANGE = new Permission("mailinglist.change", true, false);
	public static final Permission MAILINGLIST_DELETE = new Permission("mailinglist.delete", true, false);
	public static final Permission MAILINGLIST_RECIPIENTS_DELETE = new Permission("mailinglist.recipients.delete", true, false);
	public static final Permission MAILINGLIST_SHOW = new Permission("mailinglist.show", true, false);

	public static final Permission MAILLOOP_CHANGE = new Permission("mailloop.change", true, false);
	public static final Permission MAILLOOP_DELETE = new Permission("mailloop.delete", true, false);
	public static final Permission MAILLOOP_SHOW = new Permission("mailloop.show", true, false);


	public static final Permission MEDIATYPE_EMAIL = new Permission("mediatype.email", true, false);

	public static final Permission PROFILEFIELD_SHOW = new Permission("profileField.show", true, false);
	public static final Permission PROFILEFIELD_VISIBLE = new Permission("profileField.visible", true, false);

	public static final Permission RECIPIENT_CHANGE = new Permission("recipient.change", true, false);
	public static final Permission RECIPIENT_CHANGEBULK = new Permission("recipient.change.bulk", true, false);
	public static final Permission RECIPIENT_CREATE = new Permission("recipient.create", true, false);
	public static final Permission RECIPIENT_DELETE = new Permission("recipient.delete", true, false);
	public static final Permission RECIPIENT_GENDER_EXTENDED = new Permission("recipient.gender.extended", true, false);
	public static final Permission RECIPIENT_HISTORY = new Permission("recipient.history", true, false);

	public static final Permission RECIPIENT_PROFILEFIELD_HTML_ALLOWED = new Permission("recipient.profileField.html.allowed", true, true);
	public static final Permission RECIPIENT_SHOW = new Permission("recipient.show", true, false);
	public static final Permission RECIPIENT_TRACKING_VETO = new Permission("recipient.tracking.veto", true, false);
	public static final Permission RECIPIENT_HISTORY_MAILING_DELIVERY = new Permission("recipient.history.mailing.delivery", false, true);

	public static final Permission ROLE_CHANGE = new Permission("role.change", true, false);
	public static final Permission ROLE_DELETE = new Permission("role.delete", true, false);
	public static final Permission ROLE_SHOW = new Permission("role.show", true, false);

	public static final Permission SALUTATION_CHANGE = new Permission("salutation.change", true, false);
	public static final Permission SALUTATION_DELETE = new Permission("salutation.delete", true, false);
	public static final Permission SALUTATION_SHOW = new Permission("salutation.show", true, false);

	public static final Permission SERVER_STATUS = new Permission("server.status", true, true);

	public static final Permission STATISTIC_LOAD_SPECIFIC = new Permission("statistic.load.specific", true, false);
	public static final Permission STATISTIC_SOFTBOUNCES_SHOW = new Permission("statistic.softbounces.show", true, false);

	public static final Permission STATS_DOMAINS = new Permission("stats.domains", true, false);
	public static final Permission STATS_ECS = new Permission("stats.ecs", true, false);
	public static final Permission STATS_MAILING = new Permission("stats.mailing", true, false);
	public static final Permission STATS_MONTH = new Permission("stats.month", true, false);
	public static final Permission STATS_SHOW = new Permission("stats.show", true, false);
	public static final Permission STATS_USERFORM = new Permission("stats.userform", true, false);
	public static final Permission STATS_REVENUE = new Permission("stats.revenue", false, false);

	public static final Permission TARGETS_CHANGE = new Permission("targets.change", true, false);
	public static final Permission TARGETS_CREATEML = new Permission("targets.createml", true, false);
	public static final Permission TARGETS_DELETE = new Permission("targets.delete", true, false);
	public static final Permission TARGETS_LOCK = new Permission("targets.lock", true, false);
	public static final Permission TARGETS_SHOW = new Permission("targets.show", true, false);

	public static final Permission TEMPLATE_CHANGE = new Permission("template.change", true, false);
	public static final Permission TEMPLATE_DELETE = new Permission("template.delete", true, false);
	public static final Permission TEMPLATE_SHOW = new Permission("template.show", true, false);

	public static final Permission WEBSERVICE_USER_CHANGE = new Permission("webservice.user.change", true, true);
	public static final Permission WEBSERVICE_USER_CREATE = new Permission("webservice.user.create", true, true);
	public static final Permission WEBSERVICE_USER_SHOW = new Permission("webservice.user.show", true, true);

	public static final Permission WIZARD_EXPORT = new Permission("wizard.export", true, false);
	public static final Permission WIZARD_IMPORT = new Permission("wizard.import", true, false);
	public static final Permission WIZARD_IMPORTCLASSIC = new Permission("wizard.importclassic", true, false);

	public static final Permission WORKFLOW_ACTIVATE = new Permission("workflow.activate", true, false);
	public static final Permission WORKFLOW_CHANGE = new Permission("workflow.change", true, false);
	public static final Permission WORKFLOW_DELETE = new Permission("workflow.delete", true, false);
	public static final Permission WORKFLOW_SHOW = new Permission("workflow.show", true, false);
	
	public static final Permission FORMS_EXPORT = new Permission("forms.export", true, false);
	public static final Permission MAILING_EXPORT = new Permission("mailing.export", true, false);

	// Permissions Extended

	public static final Permission COMPANY_AUTHENTICATION = new Permission("company.authentication", false, false);
	public static final Permission COMPANY_FORCE_SENDING = new Permission("company.force.sending", false, true);
	public static final Permission DEEPTRACKING = new Permission("deeptracking", false, false);
	public static final Permission IMPORT_CUSTOMERID = new Permission("import.customerid", false, true);
	public static final Permission IMPORT_MAPPING_AUTO = new Permission("import.mapping.auto", false, true);
	public static final Permission IMPORT_MODE_REACTIVATE_SUSPENDED = new Permission("import.mode.reactivateSuspended", false, true);
	public static final Permission IMPORT_MODE_ADD_UPDATE_FORCED = new Permission("import.mode.add_update_forced", false, true);
	public static final Permission IMPORT_MODE_REMOVE_STATUS = new Permission("import.mode.remove_status", false, true);
	/** Import customer data without subscribing it to a mailinglist **/

	public static final Permission IMPORT_PREPROCESSING = new Permission("import.preprocessing", false, true);
	/** Import customer data without subscribing it to a mailinglist **/
	public static final Permission MAILING_CAN_ALLOW = new Permission("mailing.can_allow", false, false);
	public static final Permission MAILING_COMPONENTS_SFTP = new Permission("mailing.components.sftp", false, true);
	public static final Permission MAILING_CONTENT_DISABLE_LINKEXTENSION = new Permission("mailing.content.disableLinkExtension", false, true);
	public static final Permission MAILING_CONTENTSOURCE_DATE_LIMIT = new Permission("mailing.contentsource.date.limit", false, false);
	public static final Permission MAILING_DELETE = new Permission("mailing.delete", false, false);
	public static final Permission MAILING_ENVELOPE_ADDRESS = new Permission("mailing.envelope_address", false, false);
	public static final Permission MAILING_EXPIRE = new Permission("mailing.expire", false, true);

	/** Allow link extension change **/
	public static final Permission MAILING_PARAMETER_CHANGE = new Permission("mailing.parameter.change", false, true);
	public static final Permission MAILING_PARAMETER_SHOW = new Permission("mailing.parameter.show", false, true);

	/** Show the tab "recipients" within the GUI mailing view **/
	public static final Permission MAILING_SETTINGS_HIDE = new Permission("mailing.settings.hide", false, false);
	public static final Permission MAILING_TRACKABLELINKS_NOCLEANUP = new Permission("mailing.trackablelinks.nocleanup", false, true);
	public static final Permission MAILING_TRACKABLELINKS_STATIC = new Permission("mailing.trackablelinks.static", false, true);

	public static final Permission MASTER_COMPANIES_SHOW = new Permission("master.companies.show", false, true);
	/** Edit link targets in sent mailings **/
	public static final Permission MAILING_TRACKABLELINKS_URL_CHANGE = new Permission("mailing.trackablelinks.url.change", false, true);
	public static final Permission GRID_CHANGE = new Permission("grid.change", false, true);

	public static final Permission RECIPIENT_HISTORY_MAILING = new Permission("recipient.history.mailing", false, true);
	public static final Permission RECIPIENT_IMPORT_ENCRYPTED = new Permission("recipient.import.encrypted", false, true);
	/**	user activity log permissions **/
	public static final Permission MASTERLOG_SHOW = new Permission("masterlog.show", false, true);
	public static final Permission TEMP_ALPHA = new Permission("temp.alpha", false, true);
	public static final Permission TEMP_BETA = new Permission("temp.beta", false, true);
	public static final Permission TEMP_GAMMA = new Permission("temp.gamma", false, true);
	public static final Permission MASTER_SHOW = new Permission("master.show", false, true);
	public static final Permission MEDIATYPE_FAX = new Permission("mediatype.fax", false, true);
	public static final Permission MEDIATYPE_MMS = new Permission("mediatype.mms", false, true);
	public static final Permission MEDIATYPE_POST = new Permission("mediatype.post", false, true);
	public static final Permission MEDIATYPE_SMS = new Permission("mediatype.sms", false, true);
	public static final Permission MEDIATYPE_WHATSAPP = new Permission("mediatype.whatsapp", false, true);

	public static final Permission ACTIONS_MIGRATION = new Permission("actions.migration", false, false);

	public static final Permission MAILING_RESUME_WORLD = new Permission("mailing.resume.world", false, false);
	
	public static final Permission RESTFUL_ALLOWED = new Permission("restful.allowed", false, false);

	private String category = null;
	private String subCategory = null;
	private String featurePackage;
	private final String tokenString;
	private final boolean visible;
	private final boolean premium;
	private int sortOrder;
	
	protected Permission(String tokenString, final boolean visible, final boolean premium) throws RuntimeException {
		this.tokenString = tokenString;
		this.visible = visible;
		this.premium = premium;

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

	/**
	 * "setCategory" method may only be used during EMM initialization process
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	/**
	 * "setSubCategory" method may only be used during EMM initialization process
	 */
	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public String getTokenString() {
		return tokenString;
	}

	public final boolean isVisible() {
		return this.visible;
	}

	public final boolean isPremium() {
		return premium;
	}
	
	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	public String getFeaturePackage() {
		return featurePackage;
	}

	public void setFeaturePackage(String featurePackage) {
		this.featurePackage = featurePackage;
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
				if (!permission.isPremium() || (allowedPremiumPermissions != null && allowedPremiumPermissions.contains(permission))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int compareTo(Permission otherPermission) {
		if (otherPermission == null) {
			return 1;
		} else if (getCategory() == null && otherPermission.getCategory() != null) {
			return 1;
		} else if (getCategory() != null && otherPermission.getCategory() == null) {
			return -1;
		} else if (getCategory() == null && otherPermission.getCategory() == null) {
			return Integer.compare(getSortOrder(), otherPermission.getSortOrder());
		} else {
			int categoryIndex = Integer.MAX_VALUE;
			for (int index = 0; index < Permission.CATEGORY_DISPLAY_ORDER.length; index++) {
				if (Permission.CATEGORY_DISPLAY_ORDER[index].equals(getCategory())) {
					categoryIndex = index;
					break;
				}
			}
			int otherCategoryIndex = Integer.MAX_VALUE;
			for (int index = 0; index < Permission.CATEGORY_DISPLAY_ORDER.length; index++) {
				if (Permission.CATEGORY_DISPLAY_ORDER[index].equals(otherPermission.getCategory())) {
					otherCategoryIndex = index;
					break;
				}
			}
			if (categoryIndex != otherCategoryIndex) {
				return Integer.compare(categoryIndex, otherCategoryIndex);
			} else if (getSubCategory() == null && otherPermission.getSubCategory() != null) {
				return 1;
			} else if (getSubCategory() != null && otherPermission.getSubCategory() == null) {
				return -1;
			} else if (getSubCategory() == null && otherPermission.getSubCategory() == null) {
				return Integer.compare(getSortOrder(), otherPermission.getSortOrder());
			} else if (!getSubCategory().equals(otherPermission.getSubCategory())) {
				return getSubCategory().compareTo(otherPermission.getSubCategory());
			} else {
				return Integer.compare(getSortOrder(), otherPermission.getSortOrder());
			}
		}
	}
}
