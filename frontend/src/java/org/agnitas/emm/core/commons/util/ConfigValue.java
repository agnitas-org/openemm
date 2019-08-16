/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.util;

import java.util.ArrayList;
import java.util.List;

import org.agnitas.util.AgnUtils;

public class ConfigValue {
	public static final List<ConfigValue> LIST_OF_ALL_CONFIGVALUES = new ArrayList<>();

	public static final ConfigValue SystemUrl = new ConfigValue("system.url");
	public static final ConfigValue DB_Vendor = new ConfigValue("DB_Vendor");
	public static final ConfigValue ConfigurationExpirationMinutes = new ConfigValue("configuration.expiration.minutes", "5");
	public static final ConfigValue Test = new ConfigValue("Test.Test");
	public static final ConfigValue Pickup_Rdir = new ConfigValue("pickup.rdir");

	public static final ConfigValue ApplicationVersion = new ConfigValue("ApplicationVersion");
	public static final ConfigValue IsLegacyInstance = new ConfigValue("legacy.version");
	public static final ConfigValue IsBetaInstance = new ConfigValue("beta.version");
	public static final ConfigValue IsLiveInstance = new ConfigValue("live.version");

	public static final ConfigValue SystemSaltFile = new ConfigValue("system.salt.file");

	public static final ConfigValue EmmDbJndiName = new ConfigValue("jdbc.emmDB.jndiName", "emm_db");
	public static final ConfigValue TempDbJndiName = new ConfigValue("jdbc.tmpDB.jndiName", "embedded");

	public static final ConfigValue BirtPrivateKeyFile = new ConfigValue("birt.privatekeyfile");
	public static final ConfigValue BirtPublicKeyFile = new ConfigValue("birt.publickeyfile");
	public static final ConfigValue BirtHostUser = new ConfigValue("birt.host.user");
	public static final ConfigValue BirtPluginDirectory = new ConfigValue("birt.plugin.directory");
	public static final ConfigValue BirtDrilldownUrl = new ConfigValue("birt.drilldownurl");

	public static final ConfigValue IgnoreDeletedI18NMessages = new ConfigValue("ignoreDeletedMessages");

	public static final ConfigValue PreviewUrl = new ConfigValue("preview.url");

	public static final ConfigValue MailingUndoLimit = new ConfigValue("undo.limit", "10");

	/** The mailing size (in KB) that should trigger a warning message **/
	public static final ConfigValue MailingSizeWarningThreshold = new ConfigValue("company.mailingSizeWarningThreshold", "2048");
	/** The mailing size (in KB) that should trigger an error message **/
	public static final ConfigValue MailingSizeErrorThreshold = new ConfigValue("company.mailingSizeErrorThreshold", "10240");
	
	public static final ConfigValue GridTemplateImportAllowed = new ConfigValue("import.gridTemplateAllowed", "false");

	public static final ConfigValue UseNewCssLibForStylesEmbedding = new ConfigValue("company.useNewCssLibForStylesEmbedding", "false");

	public static final ConfigValue WkhtmlToPdfToolPath = new ConfigValue("wkhtmltopdf");
	public static final ConfigValue WkhtmlToImageToolPath = new ConfigValue("wkhtmltoimage");
	public static final ConfigValue UseLatestCkEditor = new ConfigValue("useLatestCkEditor", "true");

	public static final ConfigValue EmmPluginsHome = new ConfigValue("plugins.home");

	public static final ConfigValue System_Licence = new ConfigValue("system.licence"); // LicenseID
	/** License types: SaaS, Inhouse **/
	public static final ConfigValue System_License_Type = new ConfigValue("licenseType");
	public static final ConfigValue System_License_ExpirationDate = new ConfigValue("expirationDate");
	public static final ConfigValue System_License_MaximumNumberOfCompanies = new ConfigValue("maximumNumberOfCompanies");
	public static final ConfigValue System_License_MaximumNumberOfAdmins = new ConfigValue("maximumNumberOfAdmins");
	public static final ConfigValue System_License_MaximumNumberOfWebserviceUsers = new ConfigValue("maximumNumberOfWebserviceUsers");
	public static final ConfigValue System_License_MaximumNumberOfSupervisors = new ConfigValue("maximumNumberOfSupervisors");
	public static final ConfigValue System_License_MaximumNumberOfCustomers = new ConfigValue("maximumNumberOfCustomers");
	public static final ConfigValue System_License_MaximumNumberOfProfileFields = new ConfigValue("maximumNumberOfProfileFields");
	public static final ConfigValue System_License_MaximumNumberOfTestAccounts = new ConfigValue("maximumNumberOfTestAccounts", "100");
	public static final ConfigValue System_License_MaximumLifetimeOfTestAccounts = new ConfigValue("maximumLifetimeOfTestAccounts", "30");
	public static final ConfigValue System_License_AllowedPremiumFeatures = new ConfigValue("allowedPremiumFeatures");
	public static final ConfigValue System_License_MaximumNumberOfReferenceTables = new ConfigValue("maximumNumberOfReferenceTables");
	public static final ConfigValue System_License_AllowMailingSendForMasterCompany = new ConfigValue("allowMailingSendForMasterCompany");

	public static final ConfigValue Linkchecker_Linktimeout = new ConfigValue("linkchecker.linktimeout", "30000");
	public static final ConfigValue Linkchecker_Threadcount = new ConfigValue("linkchecker.threadcount", "25");
	public static final ConfigValue Linkchecker_LocalUrlDetection = new ConfigValue("linkchecker.detectLocalUrls", "false");

	public static final ConfigValue Predelivery_Litmusapikey = new ConfigValue("predelivery.litmusapikey");
	public static final ConfigValue Predelivery_Litmusapiurl = new ConfigValue("predelivery.litmusapiurl");
	public static final ConfigValue Predelivery_Litmuspassword = new ConfigValue("predelivery.litmuspassword");
	public static final ConfigValue Predelivery_LitmusGuidPrefix = new ConfigValue("litmus.GuidPrefix");
	public static final ConfigValue Predelivery_LitmusStatusUrl = new ConfigValue("predelivery.litmusstatusurl", "https://status.litmus.com/");
	public static final ConfigValue Predelivery_RetentionDays = new ConfigValue("predelivery.retentationDays", "90");

	public static final ConfigValue RdirUndecodableLinkUrl = new ConfigValue("rdir.undecodableLink.url", "assets/rdir/404/404.html");
	
	public static final ConfigValue Thumbnail_Generate = new ConfigValue("thumbnail.generate");
	public static final ConfigValue Thumbnail_Scalex = new ConfigValue("thumbnail.scalex");
	public static final ConfigValue Thumbnail_Scaley = new ConfigValue("thumbnail.scaley");
	public static final ConfigValue Thumbnail_Sizex = new ConfigValue("thumbnail.sizex", "119");
	public static final ConfigValue Thumbnail_Sizey = new ConfigValue("thumbnail.sizey", "84");
	public static final ConfigValue Thumbnail_Treshold = new ConfigValue("thumbnail.treshold");

	public static final ConfigValue VelocityRuntimeCheck = new ConfigValue("velocity.runtimecheck");
	public static final ConfigValue VelocityScriptAbort = new ConfigValue("velocity.abortscripts");
	public static final ConfigValue VelocityLogDir = new ConfigValue("velocity.logdir");

	public static final ConfigValue RdirLandingpage = new ConfigValue("system.RdirLandingpage");

	public static final ConfigValue SupportEmergencyUrl = new ConfigValue("system.support_emergency_url");

	/** Time (in minutes) for mailing generation before delivery */
	public static final ConfigValue MailGenerationTimeMinutes = new ConfigValue("mailing.generation.minutes", "60");

	/** Config value for configuration of host authentication. */
	public static final ConfigValue HostAuthentication = new ConfigValue("host_authentication.authentication", "true");
	public static final ConfigValue HostAuthenticationHostIdCookieName = new ConfigValue("host_authentication.hostIdCookie.name", "com.agnitas.emm.host_id");

	/** Maximum age of pending host authentications in minutes. */
	public static final ConfigValue PendingHostAuthenticationMaxAgeMinutes = new ConfigValue("host_authentication.max_pending_age_minutes", "1440");

	/** Config value for embedding links in other measure systems like metalyzer */
	public static final ConfigValue ExternalMeasureSystemBaseLinkMailing = new ConfigValue("externalmeasuresystem.baselinkMailing");

	public static final ConfigValue UserActivityLog_Expire = new ConfigValue("system.UserActivityLog.Expire", "180");
	public static final ConfigValue WebserviceUsageLog_Expire = new ConfigValue("system.WebserviceUsageLog.Expire", "180");

	public static final ConfigValue SupervisorBinding_Expire = new ConfigValue("system.SupervisorBinding.Expire", "180");
	public static final ConfigValue SupervisorGrant_Expire = new ConfigValue("system.SupervisorGrant.Expire", "180");

	public static final ConfigValue DontWriteLatestDatasourceId = new ConfigValue("system.DontWriteLatestDatasourceId");

	public static final ConfigValue WebserviceDatasourceGroupId = new ConfigValue("webservice.DatasourceGroupId", "6");
	public static final ConfigValue WebserviceConvertPasswordsMD5toSHA512OnStartup = new ConfigValue("webservice.password.convertMD5toSHA512onStartup", "false");
	
	/* TODO (EMM-6234) Remove after complete migration. */
	public static final ConfigValue MigrateTargetGroupsOnStartup = new ConfigValue("targetgroups.migrateOnStartup", "false");


	/** Default SFTP Server and credentials (encrypted) */
	public static final ConfigValue DefaultSftpServerAndCredentials = new ConfigValue("company.default_sftp_server_and_credentials");

	/** Default SFTP PrivateKey */
	public static final ConfigValue DefaultSftpPrivateKey = new ConfigValue("company.default_sftp_privatekey");

	/** Only store images in rdir cache if they are not too big. Default 2 MB */
	public static final ConfigValue MaximumCachedImageSize = new ConfigValue("system.MaximumCachedImageSize", Integer.toString(2 * 1024 * 1024));
	/** Show warning when uploading an image with size of more than x bytes. Default 1 MB */
	public static final ConfigValue MaximumWarningImageSize = new ConfigValue("system.MaximumWarningImageSize", Integer.toString(1 * 1024 * 1024));
	/** Prevent uploading an image with size of more than x bytes. Default 5 MB */
	public static final ConfigValue MaximumUploadImageSize = new ConfigValue("system.MaximumUploadImageSize", Integer.toString(5 * 1024 * 1024));

	/** Show warning when uploading an attachment with size of more than x bytes. Default 1 MB */
	public static final ConfigValue MaximumWarningAttachmentSize = new ConfigValue("system.MaximumWarningAttachmentSize", Integer.toString(1 * 1024 * 1024));
	/** Prevent uploading an attachment with size of more than x bytes. Default 10 MB */
	public static final ConfigValue MaximumUploadAttachmentSize = new ConfigValue("system.MaximumUploadAttachmentSize", Integer.toString(10 * 1024 * 1024));

	/** Allow company to use webservice "SendServiceMailing". */
	public static final ConfigValue WebserviceEnableSendServiceMailing = new ConfigValue("webservice.SendServiceMailing");

	/** Bulk size limit for webservices. */
	public static final ConfigValue WebserviceBulkSizeLimit = new ConfigValue("webservice.bulk_size_limit");

	/** Enable/disable actionbased mailing for mailloop autoresponder. */
	public static final ConfigValue MailloopActionbasedAutoreponderUI = new ConfigValue("mailloop.actionbased_autoresponder_ui"); // TODO: Remove after transition phase (EMM-3645)

	/** Enable / disable historization of profile fields. */
	public static final ConfigValue RecipientProfileFieldHistory = new ConfigValue("recipient.profile_history");

	/** Temporary flag to show if the trigger has to be rebuild after EMM Update **/
	public static final ConfigValue RecipientProfileFieldHistoryRebuildOnStartup = new ConfigValue("recipient.profile_history.rebuild_trigger_on_startup");

	/** Temporary flag to show if the binding trigger has to be rebuild after EMM Update **/
	public static final ConfigValue RecipientBindingFieldHistoryRebuildOnStartup = new ConfigValue("recipient.binding_history.rebuild_trigger_on_startup");

	public static final ConfigValue CustomerInsightsCalculate = new ConfigValue("cust.insights_calculate", "true");
	public static final ConfigValue CustomerInsightsTimespan = new ConfigValue("cust.insights_timespan", "90");
	public static final ConfigValue CustomerReactorHigh = new ConfigValue("cust.reactor_high_performance", "0.25");
	public static final ConfigValue CustomerRebuyPeriod = new ConfigValue("cust.rebuy_period", "365");
	public static final ConfigValue CustomerRevenueAverage = new ConfigValue("cust.revenue_average", "150");
	public static final ConfigValue CustomerRevenueHigh = new ConfigValue("cust.revenue_high", "500");

	/** Maximum allowed number of user-selected profile fields for history. */
	public static final ConfigValue MaximumNumberOfUserSelectedProfileFieldsInHistory = new ConfigValue("recipient.profile_history.max_userdefined_fields");

	/** Use unsharp recipient query to increase performance. */
	public static final ConfigValue UseUnsharpRecipientQuery = new ConfigValue("performance.recipient_unsharp_query"); // TODO: Quick hack for CONRAD-371 */

	public static final ConfigValue HeatmapProxy = new ConfigValue("system.heatmap.proxy");

	public static final ConfigValue AttachmentMaxSize = new ConfigValue("attachment.maxSize", "2097152");

	/** Maximum number of rows included in an import file for classic import **/
	public static final ConfigValue ClassicImportMaxRows = new ConfigValue("import.classic.maxRows", "200000");
	/** Maximum filesize of an import file for classic import in byte **/
	public static final ConfigValue ClassicImportMaxFileSize = new ConfigValue("import.classic.maxSize", "25000000");

	/** Maximum number of rows included in an import file for profile recipient import **/
	public static final ConfigValue ProfileRecipientImportMaxRows = new ConfigValue("import.recipient.maxRows", "500000");

	/** Maximum number of rows included in an export file for profile recipient export **/
	public static final ConfigValue ProfileRecipientExportMaxRows = new ConfigValue("export.recipient.maxRows", "-1");

	/** Maximum number of rows included in an import file for reference table import **/
	public static final ConfigValue ReferenceTableImportMaxRows = new ConfigValue("import.reference.maxRows", "200000");

	/** Days before user password expiration for warning **/
	public static final ConfigValue UserPasswordExpireNotificationDays = new ConfigValue("password.expire.notification.days", "14");

	/** Days until user password is expired after last change (-1 = passwords never expire) **/
	public static final ConfigValue UserPasswordExpireDays = new ConfigValue("password.expire.days", "90");

	/** Days after user password expiration when user cannot change password anymore (-1 = passwords never finally expires) **/
	public static final ConfigValue UserPasswordFinalExpirationDays = new ConfigValue("password.expire.final.days", "90");

	/** Days until supervisor password is expired after last change (-1 = passwords never expire) **/
	public static final ConfigValue SupervisorPasswordExpireDays = new ConfigValue("supervisor.password.expire.max.days", "30");

	/** Days before supervisor password expiration for warning **/
	public static final ConfigValue SupervisorPasswordExpireNotificationDays = new ConfigValue("supervisor.password.expire.warning.days", "5");

	/** Days after user password expiration when user cannot change his password anymore (-1 = passwords never finally expires) **/
	public static final ConfigValue SupervisorPasswordFinalExpirationDays = new ConfigValue("supervisor.password.expire.final.days", "30");

	/** Use login permission granted by user. */
	public static final ConfigValue SupervisorRequiresLoginPermission = new ConfigValue("supervisor.requiresLoginPermission", "false");
	
	/** Default expire date (currently 3 years) */
	public static final ConfigValue DefaultExpireDays = new ConfigValue("expire.default", "1100");

	/** Maximum value for age of entries in statistic tables **/
	public static final ConfigValue ExpireStatisticsMax = new ConfigValue("expire.statistics.max", "1100");

	/** Default value for age of entries in statistic tables **/
	public static final ConfigValue ExpireStatisticsDefault = new ConfigValue("expire.statistics.default", "1100");

	/** Maximum value for age of entries in onepixellog tables **/
	public static final ConfigValue ExpireOnePixelMax = new ConfigValue("expire.OnePixelMax", "1100");

	/** Default value for age of entries in onepixellog tables **/
	public static final ConfigValue ExpireOnePixelDefault = new ConfigValue("expire.OnePixelDef", "1100");

	/** Maximum value for age of entries in success tables **/
	public static final ConfigValue ExpireSuccessMax = new ConfigValue("expire.SuccessMax", "1100");

	/** Default value for age of entries in success tables **/
	public static final ConfigValue ExpireSuccessDefault = new ConfigValue("expire.SuccessDef", "180");

	/** Default value for age of entries in recipients_report_tbl **/
	public static final ConfigValue ExpireRecipientsReport = new ConfigValue("expire-recipients-report", "90");

	/** Maximum time in seconds for statictic values cache (restart summary if requested after that time) **/
	public static final ConfigValue ExpireStatisticSummary = new ConfigValue("statistic.summary.expiredAfterSeconds", "900");

	/** Maximum time in seconds for statictic values cache (cleanup old data from memory without request) */
	public static final ConfigValue ExpireStatisticSummaryCleanup = new ConfigValue("statistic.summary.expiredAfterSeconds.cleanup", "-1");

	public static final ConfigValue Company_MaxReferenceTables = new ConfigValue("maxReferenceTables", "5");

	public static final ConfigValue SmtpMailRelayHostname = new ConfigValue("system.mail.host");

	public static final ConfigValue Mailaddress_Error = new ConfigValue("mailaddress.error");
	public static final ConfigValue Mailaddress_Support = new ConfigValue("mailaddress.support");
	public static final ConfigValue Mailaddress_Velocity = new ConfigValue("mailaddress.velocity");
	public static final ConfigValue Mailaddress_Frontend = new ConfigValue("mailaddress.frontend");
	public static final ConfigValue Mailaddress_ReportArchive = new ConfigValue("mailaddress.report_archive");
	public static final ConfigValue MailAddress_UploadSupport = new ConfigValue("mailaddress.upload.support");
	public static final ConfigValue MailAddress_UploadDatabase = new ConfigValue("mailaddress.upload.database");

	public static final ConfigValue Mailaddress_Sender = new ConfigValue("mailaddress.sender");
	public static final ConfigValue Mailaddress_SenderName = new ConfigValue("mailaddress.sender.name");
	public static final ConfigValue Mailaddress_ReplyTo = new ConfigValue("mailaddress.replyto");
	public static final ConfigValue Mailaddress_ReplyToName = new ConfigValue("mailaddress.replyto.name");
	public static final ConfigValue Mailaddress_Bounce = new ConfigValue("mailaddress.bounce");

	public static final ConfigValue TablespacenameCustomerIndex = new ConfigValue("tablespace.cust.index");

	/** Default value for maximum number of fields in a customer table */
	public static final ConfigValue MaxFields = new ConfigValue("maxFields", "50");

	/** Default value for maximum number of recipients for admin test mailing */
	public static final ConfigValue MaxAdminMails = new ConfigValue("maxAdminMails", "100");

	public static final ConfigValue SystemAlertMail = new ConfigValue("system.alert.mail");

	/** Maximum cachesize of measurepoints */
	public static final ConfigValue OnepixelBatchMaxCache = new ConfigValue("onepixel.batch.maxCache", "500");

	/** Maximum cachetime of measurepoints */
	public static final ConfigValue OnepixelBatchMaxCacheTimeMillis = new ConfigValue("onepixel.batch.maxCacheTimeMillis", "300000");

	/** Maximum cachesize of companies for example in agnuid checks */
	public static final ConfigValue CompanyMaxCache = new ConfigValue("company.maxCache", "500");

	/** Maximum cachetime of companies for example in agnuid checks */
	public static final ConfigValue CompanyMaxCacheTimeMillis = new ConfigValue("company.maxCacheTimeMillis", "300000");

	/** Fairness period for mailing send time */
	public static final ConfigValue SendFairnessMinutes = new ConfigValue("send.fairness.minutes", "5");

	/** Default setting how long cookie is living (in seconds, needed for rdir) */
	public static final ConfigValue CookieExpire = new ConfigValue("cookie.expire", "-1");

	/** HTTP-status used for rdir link redirections */
	public static final ConfigValue RedirectionStatus = new ConfigValue("redirection.status", "302");

	/** Maximum cachesize of miscellaneous keys in the rdir application */
	public static final ConfigValue RedirectKeysMaxCache = new ConfigValue("rdir.keys.maxCache", "500");

	/** Maximum cachetime of miscellaneous keys in the rdir application */
	public static final ConfigValue RedirectKeysMaxCacheTimeMillis = new ConfigValue("rdir.keys.maxCacheTimeMillis", "300000");

	/** Global CDN activation (if value = "activated") */
	/* Replaces by "component-storage" entry in company_info_tbl
	// public static final ConfigValue SystemCdn = new ConfigValue("system.cdn");

	/** Value for Backend CDN synchronisation */
	// TODO REMOVE: public static final ConfigValue CdnDestination = new ConfigValue("cdn.destination");
	/** Value for Backend CDN synchronisation */
	// TODO REMOVE: public static final ConfigValue CdnMaster = new ConfigValue("cdnmaster.cdnctrl");

	public static final ConfigValue BirtUrlIntern = new ConfigValue("birt.url.intern");
	public static final ConfigValue BirtUrl = new ConfigValue("birt.url");

	public static final ConfigValue HostauthenticationCookiesHttpsOnly = new ConfigValue("hostauthentication.cookies.https.only", "true");

	public static final ConfigValue ManualInstallPath = new ConfigValue("manual_install_path");

	public static final ConfigValue DBCleaner_Send_Statistics_Mail = new ConfigValue("dbcleaner.send_statistics_mail", "true");

	public static final ConfigValue PushNotificationsEnabled = new ConfigValue("webpush.push_notification");
	public static final ConfigValue PushNotificationProviderCredentials = new ConfigValue("webpush.provider_credentials");
	public static final ConfigValue PushNotificationFileSinkBaseDirectory = new ConfigValue("webpush.filesink_basedir");
	public static final ConfigValue PushNotificationResultBaseDirectory = new ConfigValue("webpush.result_basedir");
	public static final ConfigValue PushNotificationSftpHost = new ConfigValue("webpush.sftp.host");
	public static final ConfigValue PushNotificationSftpUser = new ConfigValue("webpush.sftp.user");
	public static final ConfigValue PushNotificationSftpBasePath = new ConfigValue("webpush.sftp.basepath");
	public static final ConfigValue PushNotificationSftpSshKeyFile = new ConfigValue("webpush.sftp.sshkey.file");
	public static final ConfigValue PushNotificationSftpEncryptedSshKeyPassphrase = new ConfigValue("webpush.sftp.sshkey.passphrase_encrypted");
	public static final ConfigValue PushNotificationClickTrackingUrl = new ConfigValue("webpush.click_tracking_url");								// Must be complete URL containing "[push-uid]" as placeholder for Push UID
	public static final ConfigValue PushNotificationOpenTrackingUrl = new ConfigValue("webpush.open_tracking_url");									// Must be complete URL containing "[push-uid]" as placeholder for Push UID
	public static final ConfigValue PushNotificationRecipientTrackingUrlPattern = new ConfigValue("webpush.recipient_tracking.url");						// Must be complete URL containing "[tracking-id]" as placeholder
	public static final ConfigValue PushNotificationMaxRedirectTokenGenerationAttempts = new ConfigValue("webpush.recipient_tracking.max_redirect_token_generation_attempts", "5");
	public static final ConfigValue PushNotificationMaxRedirectTokenAge = new ConfigValue("webpush.recipient_tracking.max_redirect_token_age", "300");
	public static final ConfigValue PushNotificationMaxTrackingIdGenerationAttempts = new ConfigValue("webpush.recipient_tracking.max_tracking_id_generation_attempts", "5");
	public static final ConfigValue PushNotificationVapidPublicKey =  new ConfigValue("webpush.vapid.key.public");
	public static final ConfigValue PushNotificationVapidPrivateKey =  new ConfigValue("webpush.vapid.key.private");
	public static final ConfigValue PushNotificationVapidSubject =  new ConfigValue("webpush.vapid.subject");

	public static final ConfigValue LogonIframeUrlEnglish = new ConfigValue("logon.iframe.url.en");
	public static final ConfigValue LogonIframeUrlGerman = new ConfigValue("logon.iframe.url.de");

	public static final ConfigValue CleanupBindingsOfDeletedMailinglists = new ConfigValue("cleanup.bindings_of_deleted_mailinglists");

	/** Fill the fields "lastopen_date" and "lastclick_date" in customer table. Watchout: These fields may not exist **/
	public static final ConfigValue WriteCustomerOpenOrClickField = new ConfigValue("measure.writecustomeropenorclickfield");

	/** Expiration in days for executed AutoImports without intervalpattern **/
	public static final ConfigValue AutoImport_Expire = new ConfigValue("import.AutoImport.Expire", "30");
	/** Expiration in days for executed AutoExports without intervalpattern **/
	public static final ConfigValue AutoExport_Expire = new ConfigValue("export.AutoExport.Expire", "30");

	/** Delete import files after successful (auto-)import **/
	public static final ConfigValue DELETE_SUCCESSFULLY_IMPORTED_FILES = new ConfigValue("import.importfiles.delete");

	public static final ConfigValue ExpireRecv = new ConfigValue("expire-recv", "0");
	public static final ConfigValue DefaultLinkExtension = new ConfigValue("DefaultLinkExtension");
	public static final ConfigValue PrefillCheckboxSendDuplicateCheck = new ConfigValue("prefillCheckboxSendDuplicateCheck", "false");
	public static final ConfigValue RespectHideDataSign = new ConfigValue("RespectHideDataSign", "false");
	public static final ConfigValue ExportFilename = new ConfigValue("export_filename");

	/** Always inform this email when an import was executed **/
	public static final ConfigValue ImportAlwaysInformEmail = new ConfigValue("importalwaysinformemail");

	/** Maximum number of parallel AutoImports. All AutoImports of a single client/company are executed serially **/
	public static final ConfigValue MaximumParallelAutoImports = new ConfigValue("import.MaximumParallelAutoImports", "3");

	/** Maximum number of parallel AutoExports. All AutoExports of a single client/company are executed serially **/
	public static final ConfigValue MaximumParallelAutoExports = new ConfigValue("import.MaximumParallelAutoExports", "3");

	/** Maximum number of parallel JobQueue Jobs **/
	public static final ConfigValue MaximumParallelJobQueueJobs = new ConfigValue("import.MaximumParallelJobQueueJobs", "3");

	/** Trackable Links, which contain these domains are preselected as not tracked. Wildcards (*, ?) allowed **/
	public static final ConfigValue NoLinkTrackingByDomainPreselection = new ConfigValue("links.nolinktrackingbydomainpreselection");

	public static final ConfigValue ImageLinkTemplate = new ConfigValue("imagelink-template");
	public static final ConfigValue ImageLinkTemplate_NoCache = new ConfigValue("imagelink-template-no-cache");
	public static final ConfigValue LimitBlockOperations = new ConfigValue("limit-block-operations");
	public static final ConfigValue LimitBlockOperations_Maximum = new ConfigValue("limit-block-operations-max");

	public static final ConfigValue BounceCleanupAgeInDays = new ConfigValue("cleanup.bounce-table", "90");

	public static final ConfigValue ForceSending = new ConfigValue("force-sending");
	public static final ConfigValue LocaleLanguage = new ConfigValue("locale-language");
	public static final ConfigValue LocaleCountry = new ConfigValue("locale-country");
	public static final ConfigValue LocaleTimezone = new ConfigValue("locale-timezone");

	public static final ConfigValue WhatsBroadcastApiKey = new ConfigValue("whatsbroadcast.apiKey");
	public static final ConfigValue WhatsBroadcastAttachmentHost = new ConfigValue("whatsbroadcast.attachmentHost");	// If not set, RDIR domain of company should be used

	public static final ConfigValue MessengerDeliveryMessageNSecondsBeforeSendTime = new ConfigValue("messenger.deliverMessageNSecondsBeforeSendTime", "300");

	public static final ConfigValue EnabledUIDVersion = new ConfigValue("company.enabledUIDVersion", "3");

	public static final ConfigValue EnableTrackingVeto = new ConfigValue("recipients.enableTrackingVeto", "false");
	public static final ConfigValue AnonymizeTrackingVetoRecipients = new ConfigValue("anonymizeTrackingVetoRecipients", "false");
	public static final ConfigValue TrackingVetoAllowTransactionTracking = new ConfigValue("recipient.trackingVeto.allowTransactionTracking", "false");
	public static final ConfigValue AnonymizeAllRecipients = new ConfigValue("recipient.trackingVeto.anonymizeAllRecipients", "false");

	public static final ConfigValue ExportAlwaysInformEmail = new ConfigValue("exportalwaysinformemail");

	/** Name of fullview form. Default is <i>fullview</i>. */
	public static final ConfigValue FullviewFormName = new ConfigValue("formname.fullview", "fullview");

	public static final ConfigValue MaxRestfulRequestDataSize = new ConfigValue("restful.maxRestfulRequestDataSize", Integer.toString(1024 * 1024)); // 1 MB

	public static final ConfigValue CleanRecipientsWithoutBinding = new ConfigValue("cleanup.deleteRecipientsWithoutBinding", "false");
	public static final ConfigValue CleanRecipientsData = new ConfigValue("cleanup.deleteRecipientsData", "false");
	public static final ConfigValue CleanMailingData = new ConfigValue("cleanup.deleteMailingData", "true");
	
	public static final ConfigValue MigrateLinkExtensions = new ConfigValue("MigrateLinkExtensions", "true");
	public static final ConfigValue CleanWorkflowTargets = new ConfigValue("system.CleanWorkflowTargets", "true");
	public static final ConfigValue MigrateWorkflowTargetRepresentation = new ConfigValue("system.MigrateWorkflowTargetRepresentation", "true");

	/**
	 * Values allowed: none, warning, error
	 */
	public static final ConfigValue SendMailingWithoutDkimCheck = new ConfigValue("SendMailingWithoutDkimCheck", "none");

	public static final ConfigValue RecipientEmailInUseWarning = new ConfigValue("recipient.emailInUseWarning", "true");

	public static final ConfigValue AllowEmailWithWhitespace = new ConfigValue("AllowEmailWithWhitespace", "false");
	
	public static final ConfigValue ForceSteppingBlocksize = new ConfigValue("force.steppingBlocksize", "false");
	public static final ConfigValue DefaultBlocksizeValue = new ConfigValue("default.blocksize.value", "0");

	public static final ConfigValue UseBindingHistoryForRecipientStatistics = new ConfigValue("UseBindingHistoryForRecipientStatistics", "false");

	private final String name;
	private final String defaultValue;

	ConfigValue(String name) throws RuntimeException {
		this.name = name;
		this.defaultValue = null;

		for (ConfigValue existingValue : LIST_OF_ALL_CONFIGVALUES) {
			if (existingValue.name.equals(name)) {
				throw new RuntimeException("Duplicate creation of configuration value: " + name);
			}
		}

		LIST_OF_ALL_CONFIGVALUES.add(this);
	}

	ConfigValue(String name, String defaultValue) {
		this.name = name;
		this.defaultValue = AgnUtils.replaceHomeVariables(defaultValue);

		for (ConfigValue existingValue : LIST_OF_ALL_CONFIGVALUES) {
			if (existingValue.name.equals(name)) {
				throw new RuntimeException("Duplicate creation of configuration value: " + name);
			}
		}

		LIST_OF_ALL_CONFIGVALUES.add(this);
	}

	@Override
	public String toString() {
		return name;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public static ConfigValue getConfigValueByName(String name) {
		for (ConfigValue existingValue : LIST_OF_ALL_CONFIGVALUES) {
			if (existingValue.name.equals(name)) {
				return existingValue;
			}
		}
		throw new RuntimeException("Unknown config value name: " + name);
	}
}
