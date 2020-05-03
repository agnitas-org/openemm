/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;

import com.agnitas.messages.DBMessagesResource;
import com.agnitas.messages.I18nString;

public class Permission {
	public static final String[] ORDERED_STANDARD_RIGHT_CATEGORIES = new String[] { "General", "Mailing", "Template", "Campaigns", "Subscriber-Editor", "ImportExport", "Statistics", "Target-Groups", "Mailinglist", "Forms", "Actions", "Administration", "NegativePermissions" };
	public static final String[] ORDERED_PREMIUM_RIGHT_CATEGORIES = new String[] { "Premium", "Account", "PushNotifications", "Messenger" };
	public static final List<String> PREMIUM_RIGHT_CATEGORIES_LIST = Arrays.asList(ORDERED_PREMIUM_RIGHT_CATEGORIES);
	public static final String CATEGORY_KEY_SYSTEM = "System";
	public static final String CATEGORY_KEY_OTHERS = "others";
	public static final String USERRIGHT_MESSAGEKEY_PREFIX = "UserRight.";

	public static final String FEATURENAME_AUTOMATIONPACKAGE = "Automation Package";
	public static final String FEATURENAME_RETARGETINGPACKAGE = "Retargeting Package";
	public static final String FEATURENAME_WEBPUSHPACKAGE = "Webpush Package";
	public static final String FEATURENAME_LAYOUTPACKAGE = "Layout Package";
	public static final String FEATURENAME_ANALYTICSPACKAGE = "Analytics Package";
	public static final String FEATURENAME_DELIVERYPACKAGE = "Delivery Package";

	private static Map<Permission, String> CATEGORY_BY_SYSTEM_PERMISSIONS = null;
	private static final Map<String, Permission> SYSTEM_PERMISSIONS = new HashMap<>();

	public static final Permission ACTION_OP_ACTIVATEDOUBLEOPTIN = new Permission("action.op.ActivateDoubleOptIn", true, false);
	public static final Permission ACTION_OP_CONTENTVIEW = new Permission("action.op.ContentView", true, true);
	public static final Permission ACTION_OP_EXECUTESCRIPT = new Permission("action.op.ExecuteScript", true, false);
	public static final Permission ACTION_OP_GETARCHIVELIST = new Permission("action.op.GetArchiveList", true, false);
	public static final Permission ACTION_OP_GETARCHIVEMAILING = new Permission("action.op.GetArchiveMailing", true, false);
	public static final Permission ACTION_OP_GETCUSTOMER = new Permission("action.op.GetCustomer", true, false);
	public static final Permission ACTION_OP_IDENTIFYCUSTOMER = new Permission("action.op.IdentifyCustomer", true, false);
	public static final Permission ACTION_OP_SENDMAILING = new Permission("action.op.SendMailing", true, false);
	public static final Permission ACTION_OP_SERVICEMAIL = new Permission("action.op.ServiceMail", true, false);
	public static final Permission ACTION_OP_SUBSCRIBECUSTOMER = new Permission("action.op.SubscribeCustomer", true, false);
	public static final Permission ACTION_OP_UNSUBSCRIBECUSTOMER = new Permission("action.op.UnsubscribeCustomer", true, false);
	public static final Permission ACTION_OP_UPDATECUSTOMER = new Permission("action.op.UpdateCustomer", true, false);

	public static final Permission ACTIONS_CHANGE = new Permission("actions.change", true, false);
	public static final Permission ACTIONS_DELETE = new Permission("actions.delete", true, false);
	public static final Permission ACTIONS_SHOW = new Permission("actions.show", true, false);

	public static final Permission ADMIN_CHANGE = new Permission("admin.change", true, false);
	public static final Permission ADMIN_DELETE = new Permission("admin.delete", true, false);
	public static final Permission ADMIN_NEW = new Permission("admin.new", true, false);
	public static final Permission ADMIN_SETGROUP = new Permission("admin.setgroup", true, false);
	public static final Permission ADMIN_SETPERMISSION = new Permission("admin.setpermission", true, false);
	public static final Permission ADMIN_SHOW = new Permission("admin.show", true, false);
	public static final Permission ADMINLOG_SHOW = new Permission("adminlog.show", false, true);

	public static final Permission ALWAYS_ALLOWED = new Permission("always.allowed", false, false);
	public static final Permission ALWAYS_DISALLOWED = new Permission("always.disallowed", false, false);

	public static final Permission BLACKLIST = new Permission("blacklist", true, false);

	public static final Permission CALENDAR_SHOW = new Permission("calendar.show", true, false);

	public static final Permission CAMPAIGN_CHANGE = new Permission("campaign.change", true, false);
	public static final Permission CAMPAIGN_DELETE = new Permission("campaign.delete", true, false);
	public static final Permission CAMPAIGN_SHOW = new Permission("campaign.show", true, false);

	public static final Permission CHARSET_USE_ISO_8859_15 = new Permission("charset.use.iso_8859_15", true, false);
	public static final Permission CHARSET_USE_UTF_8 = new Permission("charset.use.utf_8", true, false);

	public static final Permission CKEDITOR_TRIMMED = new Permission("mailing.editor.trimmed", false, false);

	public static final Permission FORMS_CHANGE = new Permission("forms.change", true, false);
	public static final Permission FORMS_DELETE = new Permission("forms.delete", true, false);
	public static final Permission FORMS_IMPORT = new Permission("forms.import", true, false);
	public static final Permission FORMS_SHOW = new Permission("forms.show", true, false);

	public static final Permission IMPORT_MODE_ADD = new Permission("import.mode.add", true, false);
	public static final Permission IMPORT_MODE_ADD_UPDATE = new Permission("import.mode.add_update", true, false);
	public static final Permission IMPORT_MODE_BLACKLIST = new Permission("import.mode.blacklist", true, false);
	public static final Permission IMPORT_MODE_BOUNCE = new Permission("import.mode.bounce", true, false);
	public static final Permission IMPORT_MODE_BOUNCEREACTIVATE = new Permission("import.mode.bouncereactivate", true, true);
	public static final Permission IMPORT_MODE_DOUBLECHECKING = new Permission("import.mode.doublechecking", true, false);
	public static final Permission IMPORT_MODE_ONLY_UPDATE = new Permission("import.mode.only_update", true, false);

	/** Show classic import the GUI toggle and set default to "update only the first duplicate", otherwise default is "update all dupliactes" **/
	public static final Permission IMPORT_MODE_UNSUBSCRIBE = new Permission("import.mode.unsubscribe", true, false);
	public static final Permission IMPORT_MODE_DUPLICATES = new Permission("import.mode.duplicates", true, false);
	/** Import customer data without subscribing it to a mailinglist **/
	public static final Permission IMPORT_WITHOUT_MAILINGLIST = new Permission("import.mailinglist.without", true, true);

	public static final Permission MAILING_ATTACHMENTS_SHOW = new Permission("mailing.attachments.show", true, false);
	public static final Permission MAILING_CAN_SEND_ALWAYS = new Permission("mailing.can_send_always", true, false);
	public static final Permission MAILING_CHANGE = new Permission("mailing.change", true, false);
	public static final Permission MAILING_COMPONENTS_CHANGE = new Permission("mailing.components.change", true, false);
	public static final Permission MAILING_COMPONENTS_SHOW = new Permission("mailing.components.show", true, false);
	public static final Permission MAILING_CONTENT_CHANGE_ALWAYS = new Permission("mailing.content.change.always", true, true);
	public static final Permission MAILING_CONTENT_SHOW_EXCLUDED_TARGETGROUPS = new Permission("mailing.content.showExcludedTargetgroups", true, true);
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

	public static final Permission PLUGINMANAGER_CHANGE = new Permission("pluginmanager.change", true, false);
	public static final Permission PLUGINMANAGR_SHOW = new Permission("pluginmanager.show", true, false);

	public static final Permission PROFILEFIELD_SHOW = new Permission("profileField.show", true, false);
	public static final Permission PROFILEFIELD_VISIBLE = new Permission("profileField.visible", true, false);

	public static final Permission RECIPIENT_CHANGE = new Permission("recipient.change", true, false);
	public static final Permission RECIPIENT_CHANGEBULK = new Permission("recipient.change.bulk", true, false);
	public static final Permission RECIPIENT_CREATE = new Permission("recipient.create", true, false);
	public static final Permission RECIPIENT_DELETE = new Permission("recipient.delete", true, false);
	public static final Permission RECIPIENT_GENDER_EXTENDED = new Permission("recipient.gender.extended", true, true);
	public static final Permission RECIPIENT_HISTORY = new Permission("recipient.history", true, false);

	public static final Permission RECIPIENT_PROFILEFIELD_HTML_ALLOWED = new Permission("recipient.profileField.html.allowed", true, true);
	public static final Permission RECIPIENT_SHOW = new Permission("recipient.show", true, false);
	public static final Permission RECIPIENT_TRACKING_VETO = new Permission("recipient.tracking.veto", true, false);

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
	public static final Permission STATS_MAILING_MIGRATION = new Permission("stats.mailing.migration");
	public static final Permission STATS_MONTH = new Permission("stats.month", true, false);
	public static final Permission STATS_SHOW = new Permission("stats.show", true, false);
	public static final Permission STATS_USERFORM = new Permission("stats.userform", true, false);

	public static final Permission TARGETS_CHANGE = new Permission("targets.change", true, false);
	public static final Permission TARGETS_CREATEML = new Permission("targets.createml", true, false);
	public static final Permission TARGETS_DELETE = new Permission("targets.delete", true, false);
	public static final Permission TARGETS_LOCK = new Permission("targets.lock", true, true);
	public static final Permission TARGETS_SHOW = new Permission("targets.show", true, false);


	public static final Permission TEMPLATE_CHANGE = new Permission("template.change", true, false);
	public static final Permission TEMPLATE_DELETE = new Permission("template.delete", true, false);
	public static final Permission TEMPLATE_SHOW = new Permission("template.show", true, false);

	public static final Permission UPDATE_SHOW = new Permission("update.show", true, true);

	public static final Permission WEBSERVICE_USER_CHANGE = new Permission("webservice.user.change", true, true);
	public static final Permission WEBSERVICE_USER_CREATE = new Permission("webservice.user.create", true, true);
	public static final Permission WEBSERVICE_USER_SHOW = new Permission("webservice.user.show", true, true);

	public static final Permission WIZARD_EXPORT = new Permission("wizard.export", true, false);
	public static final Permission WIZARD_IMPORT = new Permission("wizard.import", true, false);
	public static final Permission WIZARD_IMPORTCLASSIC = new Permission("wizard.importclassic", true, false);

	public static final Permission WORKFLOW_ACTIVATE = new Permission("workflow.activate", true, false);
	public static final Permission WORKFLOW_DELETE = new Permission("workflow.delete", true, false);
	public static final Permission WORKFLOW_EDIT = new Permission("workflow.edit", true, false);
	public static final Permission WORKFLOW_SHOW = new Permission("workflow.show", true, false);

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
	public static final Permission MAILING_CONTENTSOURCE_DATE_LIMIT = new Permission("mailing.contentsource.date.limit", false, true);
	public static final Permission MAILING_DELETE = new Permission("mailing.delete", false, false);
	public static final Permission MAILING_ENVELOPE_ADDRESS = new Permission("mailing.envelope_address", false, true);
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
	public static final Permission MASTERLOG_SHOW = new Permission("user.activity.log.rollback", false, true);
	public static final Permission TEMP_ALPHA = new Permission("temp.alpha", false, true);
	public static final Permission TEMP_BETA = new Permission("temp.beta", false, true);
	public static final Permission TEMP_GAMMA = new Permission("temp.gamma", false, true);
	public static final Permission MASTER_SHOW = new Permission("master.show", false, true);
	public static final Permission MEDIATYPE_FAX = new Permission("mediatype.fax", false, true);
	public static final Permission MEDIATYPE_MMS = new Permission("mediatype.mms", false, true);
	public static final Permission MEDIATYPE_PRINT = new Permission("mediatype.print", false, true);
	public static final Permission MEDIATYPE_SMS = new Permission("mediatype.sms", false, true);
	public static final Permission MEDIATYPE_WHATSAPP = new Permission("mediatype.whatsapp", false, true);

	//temporary content tab migration permission (GWUA-3758)
	public static final Permission CONTENT_TAB_MIGRATION = new Permission("content_tab_migration", true, true);

	// temporary MVC migration permissions
	public static final Permission RECIPIENTS_REPORT_MIGRATION = new Permission("recipientsreport.migration", false, true);
	public static final Permission STATS_RECIPIENT_MIGRATION = new Permission("stats.recipient.migration",  false, true);


	private String category = null;
	private String subCategory = null;
	private final String tokenString;
	private final boolean visible;
	private final boolean premium;

	Permission(String token) throws RuntimeException {
		this(token, true, true);
	}

	Permission(String tokenString, final boolean visible) throws RuntimeException {
		this(tokenString, true, true);
	}
	Permission(String tokenString, final boolean visible, final boolean premium) throws RuntimeException {
		this.tokenString = tokenString;
		this.visible = visible;
		this.premium = premium;

		Permission existing = SYSTEM_PERMISSIONS.get(tokenString);
		if (existing != null) {
			throw new RuntimeException("Duplicate creation of permission: " + tokenString);
		}

		SYSTEM_PERMISSIONS.put(tokenString, this);
	}

	@Override
	public String toString() {
		return tokenString;
	}

	/**
	 * "setCategory" method may only be used during EMM initialization process
	 */
	protected void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	/**
	 * "setSubCategory" method may only be used during EMM initialization process
	 */
	protected void setSubCategory(String subCategory) {
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

	/**
	 * Get a list of all known permissions. The list of permissions cannot be
	 * changed this way.
	 */
	public static List<Permission> getAllSystemPermissions() {
		return new ArrayList<>(SYSTEM_PERMISSIONS.values());
	}

	public static Permission getPermissionByToken(String token) {
		return SYSTEM_PERMISSIONS.get(token);
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
	 * Read all permissions available on the current system and their category from
	 * the messages
	 */
	public static Map<Permission, String> getAllPermissionsAndCategories() {
		// Read all permissions available on the current system
		if (CATEGORY_BY_SYSTEM_PERMISSIONS == null) {
			Map<Permission, String> categoryBySystemPermissions = new HashMap<>();
			Map<Permission, String> subCategoryBySystemPermissions = new HashMap<>();

			List<String> standardCategories = Arrays.asList(ORDERED_STANDARD_RIGHT_CATEGORIES);

			if (I18nString.MESSAGE_RESOURCES == null) {
				// On ConfigService startup check for i18n data
				// Might be used by ConfigurationValidityCheckListener
				new DBMessagesResource().init();
			}

			// Collect all rights by their message key
			if (I18nString.MESSAGE_RESOURCES != null && I18nString.MESSAGE_RESOURCES.getAvailableKeys() != null) {
				for (String messageKey : I18nString.MESSAGE_RESOURCES.getAvailableKeys()) {
					if (messageKey.startsWith(USERRIGHT_MESSAGEKEY_PREFIX)) {
						String category = messageKey.substring(USERRIGHT_MESSAGEKEY_PREFIX.length(),
								messageKey.indexOf(".", USERRIGHT_MESSAGEKEY_PREFIX.length()));
						String subCategory = null;
						if (category.contains("#")) {
							subCategory = category.substring(category.indexOf("#") + 1);
							category = category.substring(0, category.indexOf("#"));
						}
						if (!PREMIUM_RIGHT_CATEGORIES_LIST.contains(category) && !standardCategories.contains(category)
								&& !CATEGORY_KEY_SYSTEM.equals(category)) {
							// Unknown categories are used as "others"
							category = CATEGORY_KEY_OTHERS;
							subCategory = null;
						}

						String right = messageKey.substring(USERRIGHT_MESSAGEKEY_PREFIX.length() + category.length()
								+ (subCategory != null && StringUtils.isNotEmpty(subCategory) ? subCategory.length() + 1
										: 0)
								+ 1);
						Permission currentPermission = getPermissionByToken(right);
						if (currentPermission != null) {
							currentPermission.setCategory(category);
							currentPermission.setSubCategory(subCategory);
							// Only use messagekeys which have a permission item
							if (!categoryBySystemPermissions.containsKey(currentPermission)
									|| !category.equals(CATEGORY_KEY_OTHERS)) {
								// Only use duplicate entries if they don't move a right from standard category
								// into others-category
								categoryBySystemPermissions.put(currentPermission, category);
							}
							subCategoryBySystemPermissions.put(currentPermission, subCategory);
						}
					}
				}
			}

			// Add rights to category "others", which have no message key category and
			// therefore cannot be found in messages
			for (Permission permission : SYSTEM_PERMISSIONS.values()) {
				if (!categoryBySystemPermissions.containsKey(permission) &&
						Permission.ALWAYS_ALLOWED != permission && Permission.ALWAYS_DISALLOWED != permission) {
					categoryBySystemPermissions.put(permission, CATEGORY_KEY_OTHERS);
					permission.setCategory(CATEGORY_KEY_OTHERS);
				}
			}

			CATEGORY_BY_SYSTEM_PERMISSIONS = categoryBySystemPermissions;
		}
		return CATEGORY_BY_SYSTEM_PERMISSIONS;
	}

	/**
	 * @param allowedPremiumPermissions
	 * @return Returns true if grantedPermissions contains any of
	 *         checkedPermissions.
	 */
	public static boolean permissionAllowed(Collection<Permission> grantedPermissions,
			Collection<Permission> allowedPremiumPermissions, Permission... checkedPermissions) {
		for (Permission permission : checkedPermissions) {
			if (permission == null || permission == Permission.ALWAYS_DISALLOWED) {
				return false;
			} else if (permission == Permission.ALWAYS_ALLOWED) {
				return true;
			} else if (grantedPermissions.contains(permission)) {
				if (!Permission.PREMIUM_RIGHT_CATEGORIES_LIST.contains(permission.getCategory())
						|| (allowedPremiumPermissions != null && allowedPremiumPermissions.contains(permission))) {
					return true;
				}
			}
		}
		return false;
	}
}
