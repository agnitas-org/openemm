/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import static com.agnitas.emm.core.maildrop.MaildropStatus.WORLD;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Stream;

import com.agnitas.backend.dao.ConfigDAO;
import com.agnitas.backend.dao.DkimDAO;
import com.agnitas.backend.dao.RecipientDAO;
import com.agnitas.backend.dao.TagDAO;
import com.agnitas.backend.dao.TitleDAO;
import com.agnitas.backend.dao.UrlDAO;
import com.agnitas.backend.exceptions.CancelException;
import com.agnitas.emm.common.FollowUpType;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.preview.Page;
import com.agnitas.util.Bit;
import com.agnitas.util.DBConfig;
import com.agnitas.util.Log;
import com.agnitas.util.ParameterParser;
import com.agnitas.util.Str;
import com.agnitas.util.Substitute;
import com.agnitas.util.Systemconfig;
import com.agnitas.util.Title;
import com.agnitas.util.importvalues.MailType;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Class holding most of central configuration and global database
 * information
 */
public class Data {
	/**
	 * unspecified licence ID
	 */
	private final static int LICENCE_UNSPEC = 0;
	/**
	 * Constant for onepixellog: no automatic insertion
	 */
	final public static int OPL_NONE = 0;
	/**
	 * Constant for onepixellog: insertion on top
	 */
	final public static int OPL_TOP = 1;
	/**
	 * Constant for onepixellog: insertion at bottom
	 */
	final public static int OPL_BOTTOM = 2;
	/**
	 * The global system configuration
	 */
	public static Systemconfig syscfg;
	public static Systemconfig.Selection selection;
	public static DBConfig dbcfg;
	public static boolean emm;
	/**
	 * fqdn, hostname, user, etc.
	 */
	public static String defaultDBID;
	/**
	 * incarnation of invokation to create unique filenames
	 */
	static long incarnation = 0;
	/**
	 * incarnation of invocation to get unique log entries
	 */
	static long logIncarnation = 0;
	/**
	 * all informations about the company of this mailing
	 */
	public Company company;
	/**
	 * all mailinglist related stuff of the mailinglist of this mailing
	 */
	public Mailinglist mailinglist;
	/**
	 * all about the maildrop_status for this mail generation
	 */
	public MaildropStatus maildropStatus;
	/**
	 * all about the mailing core information
	 */
	public Mailing mailing;
	/**
	 * all about the mailings target expression
	 */
	public TargetExpression targetExpression;
	/**
	 * Configuration from properties file
	 */
	protected Config cfg = null;
	/**
	 * Loglevel
	 */
	protected int logLevel = Log.ERROR;
	protected int licenceID = LICENCE_UNSPEC;
	/**
	 * for database access of large chunks, limit single statement
	 */
	protected long limitBlockOperations = 0;
	protected long limitBlockOperationsMax = 0;
	/**
	 * if set, test VIP are still addressed, even if there are explicit test recipients selected
	 */
	protected boolean enforceTestVIP = false;
	/*
	 * optional set recipient to bounce if a bounce is
	 * recorded on another mailinglist
	 */
	protected boolean bounceBleed = true;
	protected int bounceBleedDuration = 180;
	/*
	 * if generation is aborted when voucher codes are insufficent
	 */
	protected boolean vouchersMandatory = false;
	/**
	 * database driver name
	 */
	protected String dbDriver = null;
	/**
	 * database login
	 */
	protected String dbLogin = null;
	/**
	 * database password
	 */
	protected String dbPassword = null;
	/**
	 * database connect expression
	 */
	protected String dbConnect = null;
	/**
	 * name of program to execute meta files
	 */
	protected String xmlBack = "xmlback";
	/**
	 * validate each generated block
	 */
	protected boolean xmlValidate = false;
	/**
	 * Send samples of worldmailing to dedicated address(es)
	 */
	protected String sampleEmails = null;
	protected String deliveryCheckEmails = null;
	protected String preDeliveryCheckEmails = null;

	/**
	 * the user_status for this query
	 */
	public long defaultUserStatus = UserStatus.Active.getStatusCode();
	/**
	 * in case of campaing mailing, send mail only to this customer
	 */
	public long campaignCustomerID = 0;
	/**
	 * for campaign mailings, use this user status in the binding table
	 */
	public long[] campaignUserStatus = null;
	/**
	 * for campaign mailings, enforce sending ignoring mailinglist and user status
	 */
	private boolean defaultCampaignForceSending = false;
	public boolean campaignForceSending = false;
	/**
	 * for campaign mailings, enable target groups
	 */
	private boolean defaultCampaignEnableTargetGroups = false;
	public boolean campaignEnableTargetGroups = false;
	/**
	 * for provider preview sending
	 */
	public String providerEmail = null;
	/**
	 * for preview mailings use this for matching the customer ID
	 */
	public long previewCustomerID = 0;
	/**
	 * for preview mailings store output
	 */
	public Page previewOutput = null;
	/**
	 * for preview of external text input
	 */
	public String previewInput = null;
	/**
	 * for preview mailings if preview should be anon
	 */
	public boolean previewAnon = false;
	/**
	 * on anon preview, preserver plain links, if this is set
	 */
	public boolean previewAnonPreserveLinks = false;
	/**
	 * for preview mailings if only partial preview requested
	 */
	public String previewSelector = null;
	/**
	 * for preview mailings if preview is cachable
	 */
	public boolean previewCachable = true;
	/**
	 * for enforcment of active target ids
	 */
	public long[] previewTargetIDs = null;
	/**
	 * for preview mailings to convert to entities
	 */
	public boolean previewConvertEntities = false;
	/**
	 * for preview mailings for ECS
	 */
	public boolean previewEcsUIDs = false;
	/**
	 * for preview mailings to create all possible parts
	 */
	public boolean previewCreateAll = false;
	/**
	 * for preview mailings to create cachable image lings
	 */
	public boolean previewCacheImages = true;
	/**
	 * clear read data to anonymize input
	 */
	public boolean previewClearData = false;
	/**
	 * optional list of addresses to send bcc copy of mailing
	 */
	private List<String> bcc = null;
	private int bccBaseIndex = 0;
	private boolean bccBaseIsBlocklistChecked = false;
	/*
	 * overwrite company_info_tbl entries
	 */
	private Map<String, String> companyInfoMap = null;
	/**
	 * overwtite existing database fields
	 */
	public Map<String, String> overwriteMap = null;
	/**
	 * virtual database fields
	 */
	private Map<String, String> virtualMap = null;
	/**
	 * static values
	 */
	public Map<String, Object> staticMap = null;
	/**
	 * optional infos for this mailing
	 */
	public Map<String, String> mailingInfo = null;
	/**
	 * keeps track of all config values from config tbl
	 */
	protected ConfigDAO configDao = null;
	/**
	 * keeps track of already read EMMTags from database
	 */
	private TagDAO tagDao = null;
	/**
	 * instance to write logs to
	 */
	protected Log log = null;
	/**
	 * the ID to write as marker in the logfile
	 */
	protected String lid = null;
	/**
	 * the connection to the database
	 */
	public DBase dbase = null;
	/**
	 * current send date, calculated from sendtimstamp and stepping
	 */
	public Date currentSendDate = null;
	/**
	 * the currentSendDate in epoch
	 */
	public long sendSeconds = 0;
	/**
	 * display send date
	 */
	public Date genericSendDate = null;
	/**
	 * force start of new block
	 */
	public boolean forceNewBlock = false;
	/**
	 * the base for the auto URL
	 */
	public String autoURL = null;
	public String autoTag = "/r.html?";
	/**
	 * the base for the onepixellog URL
	 */
	public String onePixelURL = null;
	public String onePixelTag = "/g.html?";
	/**
	 * the base for honeypot URL
	 */
	public String honeyPotURL = null;
	public String honeyPotTag = "/hplt.html?";
	/**
	 * the largest mailtype to generate
	 */
	public int masterMailtype = MailType.HTML_OFFLINE.getIntValue();
	/**
	 * default line length in text part
	 */
	public int lineLength = 72;
	/**
	 * where to automatically place the onepixellog
	 */
	public int onepixlog = OPL_NONE;
	/**
	 * if clearance fragment should be added to test mailing
	 */
	public boolean requiresClearance = false;
	/**
	 * the base domain to build the base URLs
	 */
	public String rdirDomain = null;
	public String rdirDomainForImages = null;
	/**
	 * the mailloop domain
	 */
	public String mailloopDomain = null;
	/**
	 * Collection of media information
	 */
	private List<Media> media = null;
	public Media mediaEMail = null;
	/**
	 * Bitfield of available media types in mailing
	 */
	public long availableMedias = 0;
	/**
	 * colect blocklisted recipients
	 */
	public Set<Long> blocklisted = null;
	/**
	 * number of all subscriber of a mailing
	 */
	public long totalSubscribers = -1;
	public long totalReceivers = -1;
	/**
	 * helper class for string substitution
	 */
	protected Substitute substitute = null;
	/**
	 * number of all subscriber of a mailing
	 */
	private BC bigClause = null;
	/**
	 * all URLs from rdir url table
	 */
	public List<URL> URLlist = null;
	private Map<String, URL> URLTable = null;
	/**
	 * information about URL extensions
	 */
	public URLExtension extendURL = null;
	/**
	 * all title tags
	 */
	private Map<Long, Title> titles = null;
	/**
	 * all content blocks of the mailing
	 */
	protected BlockCollection blocks = null;
	/**
	 * all content blocks in a map for access by name
	 */
	protected Map<String, BlockData> blockTable = null;
	/**
	 * all code fragments for scripted tags
	 */
	private Map<String, Code> codes = new HashMap<>();
	/**
	 * all informations about available images
	 */
	private Imagepool imagepool = null;
	/**
	 * the share with your network configuration
	 */
	private SWYN swyn = null;

	/**
	 * layout of the customer table
	 */
	public List<Column> layout = null;
	/**
	 * number of entries in layout
	 */
	public int lcount = 0;
	/**
	 * number of entries in layout used
	 */
	public int lusecount = 0;
	/**
	 * for housekeeping of created files
	 */
	protected List<String> toRemove = null;
	/**
	 * direct path configuration
	 */
	public boolean directPath = false;
	/**
	 * for building anonymized URLs
	 */
	public String anonURL = null;
	public String anonTag = "/v.html?";
	public int mailingType = MailingType.NORMAL.getCode();
	public String workStatus = null;
	/**
	 * mailing priority related
	 */
	public boolean isPriorityMailing = false;
	public int mailingPriority = 0;
	public String priorityTable = null;
	public int priorityTimeID = 0;
	/**
	 * worklflow manager related
	 */
	public boolean isWorkflowMailing = false;
	/**
	 * reference to other tables
	 */
	public Map<String, Reference> references = null;
	/**
	 * image link template
	 */
	protected String imageTemplate = null;
	protected String imageTemplateNoCache = null;
	protected String mediapoolTemplate = null;
	protected String mediapoolTemplateNoCache = null;
	/**
	 * media SQL restriction
	 */
	private String mediaRestrictSQL = null;
	/**
	 * optional referencing followup for non clickers
	 */
	protected long followupReference = 0;
	protected String followupMethod = null;
	private String followupRefSQL = null;
	/**
	 * optional remove duplicate addresses from output
	 */
	protected boolean removeDuplicateEMails = false;
	/**
	 * optional force using encrypted transport
	 */
	protected boolean sendEncrypted = false;
	/**
	 * optional external tracker information
	 */
	public List<Tracker> tracker = null;
	/**
	 * optional dkim in action
	 */
	protected boolean dkimActive = false;
	protected List <DkimDAO.DKIM> dkimAvailable = null;
	/**
	 * database id
	 */
	protected String dbID = null;
	public String dbMS = null;
	/**
	 * temp. tablespace
	 */
	private String dbTempTablespace = null;
	private boolean dbHasTablespaces = true;
	/**
	 * my incarnation
	 */
	private long inc = 0;

	private boolean isMobilePreview;

	static {
		syscfg = Systemconfig.create ();
		selection = syscfg.selection ();
		dbcfg = new DBConfig ();
		emm = syscfg.get ("licence", LICENCE_UNSPEC) > 0;
		String[]	dbids = dbcfg.ids ();
		defaultDBID = (dbids != null) && (dbids.length == 1) ? dbids[0] : (emm ? "emm" : "openemm");
	}

	/**
	 * Constructor for the class
	 *
	 * @param program   the name of the program (for logging setup)
	 * @param option    output option for logging
	 */
	public Data (String program, String option) {
		setupLogging(program);
	}
	public Data (String program) {
		this (program, null);
	}
	
	/**
	 * Setup data
	 *
	 * @param generation_spec the status_id to read the mailing information from
	 */
	public void setup (String generation_spec, Map <String, Object> opts) throws Exception {
		configuration();
		logging(Log.DEBUG, "init", "Data read" + (generation_spec != null ? " for " + generation_spec : ""));
		options (opts, 0);
		setupDatabase();
		logging(Log.DEBUG, "init", "Initial database connection established");
		if (generation_spec != null) {
			try {
				getMailingInformations(generation_spec);
			} catch (CancelException e) {
				throw e;
			} catch (Exception e) {
				throw new Exception("Database failure: " + e, e);
			}
			logging(Log.DEBUG, "init", "Initial data read from database");
			checkMailingData();
			lid = "(" + company.id() + "/" + mailinglist.id() + "/" + mailing.id() + "/" + maildropStatus.id() + ")";
			if (islog(Log.DEBUG)) {
				logSettings();
			}
			mailing.setWorkStatus(MailingStatus.IN_GENERATION, null);
		}
	}

	/**
	 * Cleanup all open resources and write mailing status before
	 */
	public void done() throws Exception {
		int cnt;
		String msg;

		cnt = 0;
		msg = "";
		if (bigClause != null) {
			bigClause = bigClause.done();
		}
		if (dbase != null) {
			logging(Log.DEBUG, "deinit", "Shuting down database connection");
			closeDatabase();
		}
		if (toRemove != null) {
			int fcnt = toRemove.size();

			if (fcnt > 0) {
				logging(Log.DEBUG, "deinit", "Remove " + fcnt + " file" + Log.exts(fcnt) + " if existing");
				while (fcnt-- > 0) {
					String fname = toRemove.remove(0);
					File file = new File(fname);

					if (file.exists()) {
						if (!file.delete()) {
							msg += "\trm " + fname + "\n";
						}
					}
					file = null;
				}
			}
			toRemove = null;
		}
		if (company != null) {
			company = company.done();
		}
		if (mailinglist != null) {
			mailinglist = mailinglist.done();
		}
		if (maildropStatus != null) {
			maildropStatus = maildropStatus.done();
		}
		if (mailing != null) {
			mailing = mailing.done();
		}
		if (targetExpression != null) {
			targetExpression = targetExpression.done();
		}
		if (cnt > 0) {
			throw new Exception("Unable to cleanup:\n" + msg);
		}
		logging(Log.DEBUG, "deinit", "Cleanup done: " + msg);
	}

	/**
	 * Suspend call between setup and main execution
	 *
	 */
	public void suspend() {
		if (maildropStatus != null) {
			if (maildropStatus.isCampaignMailing() || maildropStatus.isVerificationMailing() || maildropStatus.isPreviewMailing()) {
				closeDatabase();
			}
		}
	}

	/**
	 * Resume before main execution
	 *
	 */
	public void resume() throws Exception {
		if (maildropStatus.isCampaignMailing() || maildropStatus.isVerificationMailing() || maildropStatus.isPreviewMailing()) {
			if (dbase == null) {
				setupDatabase();
			}
		}
	}

	/**
	 * Setup logging interface
	 *
	 * @param program    to create the logging path
	 */
	protected void setupLogging(String program) {
		log = new Log(program, logLevel, logIncarnation ());
	}

	/**
	 * Write all settings to logfile
	 */
	protected void logSettings() {
		logging(Log.DEBUG, "init", "Initial data valid");
		logging(Log.DEBUG, "init", "All set variables:");
		logging(Log.DEBUG, "init", "\tlogLevel = " + log.levelDescription() + " (" + log.level() + ")");
		logging(Log.DEBUG, "init", "\tdbDriver = " + dbDriver);
		logging(Log.DEBUG, "init", "\tdbLogin = " + dbLogin);
		logging(Log.DEBUG, "init", "\tdbPassword = ******");
		logging(Log.DEBUG, "init", "\tdbConnect = " + dbConnect);
		logging(Log.DEBUG, "init", "\txmlBack = " + xmlBack);
		logging(Log.DEBUG, "init", "\txmlValidate = " + xmlValidate);
		logging(Log.DEBUG, "init", "\tsampleEmails = " + sampleEmails);
		logging(Log.DEBUG, "init", "\tdeliveryCheckEmails = " + deliveryCheckEmails);
		logging(Log.DEBUG, "init", "\tdefaultUserStatus = " + defaultUserStatus);
		logging(Log.DEBUG, "init", "\tdefaultCampaignForceSending = " + defaultCampaignForceSending);
		logging(Log.DEBUG, "init", "\tdefaultCampaignEnableTargetGroups = " + defaultCampaignEnableTargetGroups);
		logging(Log.DEBUG, "init", "\tdbase = " + dbase);
		logging(Log.DEBUG, "init", "\tsendSeconds = " + sendSeconds);
		logging(Log.DEBUG, "init", "\tautoURL = " + autoURL);
		logging(Log.DEBUG, "init", "\tonePixelURL = " + onePixelURL);
		logging(Log.DEBUG, "init", "\thoneyPotURL = " + honeyPotURL);
		logging(Log.DEBUG, "init", "\tmasterMailtype = " + masterMailtype);
		logging(Log.DEBUG, "init", "\tlineLength = " + lineLength);
		logging(Log.DEBUG, "init", "\tonepixlog = " + onepixlog);
		logging(Log.DEBUG, "init", "\trequiresClearance = " + requiresClearance);
		logging(Log.DEBUG, "init", "\trdirDomain = " + rdirDomain);
		if (rdirDomainForImages != null) {
			logging(Log.DEBUG, "init", "\trdirDomainForImages = " + rdirDomainForImages);
		}
		logging(Log.DEBUG, "init", "\tmailloopDomain = " + mailloopDomain);
		logging(Log.DEBUG, "init", "\tdbID = " + (dbID == null ? "*unset*" : dbID));
		logging(Log.DEBUG, "init", "\tdbMS = " + (dbMS == null ? "*unset*" : dbMS));
		logging(Log.DEBUG, "init", "\tdbTempTablespace = " + (dbTempTablespace == null ? "*unset*" : dbTempTablespace));
		logging(Log.DEBUG, "init", "\tdbHasTablespaces = " + dbHasTablespaces);
		logging(Log.DEBUG, "init", "\tdirectPath = " + directPath);
		logging(Log.DEBUG, "init", "\tanonURL = " + anonURL);
		logging(Log.DEBUG, "init", "\tlicenceID = " + licenceID);
		logging(Log.DEBUG, "init", "\tlimitBlockOperations = " + limitBlockOperations);
		logging(Log.DEBUG, "init", "\tlimitBlockOperationsMax = " + limitBlockOperationsMax);
		logging(Log.DEBUG, "init", "\tenforceTestVIP = " + enforceTestVIP);
		logging(Log.DEBUG, "init", "\tbounceBleed = " + bounceBleed);
		logging(Log.DEBUG, "init", "\tbounceBleedDuration = " + bounceBleedDuration);
		logging(Log.DEBUG, "init", "\tvouchersMandatory = " + vouchersMandatory);
		logging(Log.DEBUG, "init", "\tisPriorityMailing = " + isPriorityMailing);
		logging(Log.DEBUG, "init", "\tmailingPriority = " + mailingPriority);
		if (references != null) {
			logging(Log.DEBUG, "init", "\treferences = " + references.values().stream().map((r) -> r.table() + " as " + r.name()).reduce((a, b) -> a + ", " + b).orElse("*none*"));
		}
		logging(Log.DEBUG, "init", "\tfollowupReference = " + followupReference);
		logging(Log.DEBUG, "init", "\tfollowupMethod = " + (followupMethod == null ? "*non-opener*" : followupMethod));
		logging(Log.DEBUG, "init", "\tremoveDuplicateEMails = " + removeDuplicateEMails);
		logging(Log.DEBUG, "init", "\tsendEncrypted = " + sendEncrypted);
		logging(Log.DEBUG, "init", "\tmailID = " + getFilenameMailingID());
		company.logSettings();
		mailinglist.logSettings();
		maildropStatus.logSettings();
		mailing.logSettings();
		targetExpression.logSettings();
	}

	/*
	 * Setup configuration
	 * @param checkRsc first check for resource bundle
	 */
	protected void configuration() throws Exception {
		cfg = new Config("mailout.");
		for (Map.Entry <String, String> kv : System.getenv ().entrySet ()) {
			String	name = kv.getKey ();
			
			if (name.startsWith ("MAILOUT_")) {
				String	key = name.substring (8).toLowerCase ();
				
				cfg.set (key, kv.getValue ());
				cfg.lock (key);
			}
		}
		configure();
	}

	/*
	 * the main configuration
	 */
	protected void configure() throws Exception {
		dbID = syscfg.get ("dbid", defaultDBID);
		if (dbcfg.selectRecord(dbID)) {
			dbMS = dbcfg.findInRecord("dbms", dbMS);
			dbLogin = dbcfg.findInRecord("user", dbLogin);
			dbPassword = dbcfg.findInRecord("password", dbPassword);
			dbDriver = dbcfg.findInRecord("jdbc-driver", dbDriver);
			dbConnect = dbcfg.findInRecord("jdbc-connect", dbConnect);
		} else {
			if (DBase.DATASOURCE == null) {
				logging(Log.WARNING, "cfg", "No entry for dbID " + dbID + " found.");
			}
		}
		try {
			DBase temp = new DBase(this);

			try {
				temp.setup();
				temp.initialize();
				if (configDao == null) {
					configDao = new ConfigDAO (temp);
				}
				logging(Log.DEBUG, "cfg", "Retrieving configuration from database for \"" + Systemconfig.fqdn + "\" and \"" + Systemconfig.hostname + "\" for version \"" + Systemconfig.version + "\"");
				Map<String, String> entry = configEntry("mailout");

				if (entry != null) {
					for (Map.Entry<String, String> kv : entry.entrySet()) {
						String name = kv.getKey();
						String value = kv.getValue();

						if ((name != null) && (value != null)) {
							if (name.toLowerCase().startsWith("ini.")) {
								cfg.set(name.substring(4), value);
							}
						}
					}

					Map <String, String>	updated = cfg.copy ();
					boolean			changed = false;
					
					for (Map.Entry <String, String> kv : updated.entrySet ()) {
						String	key = kv.getKey ();
						String	value = kv.getValue ();
						String	dbkey = "ini." + key;
						
						if ((! entry.containsKey (key)) && (! entry.containsKey (dbkey))) {
							configDao.add (temp, "mailout", key, value, Systemconfig.user + "@" + Systemconfig.fqdn, "set from legacy configuration file (" + Systemconfig.user + "@" + Systemconfig.fqdn +" in version " + Systemconfig.version + ")");
							changed = true;
						}
					}
					if (changed) {
						configDao.reread (temp);
					}
				}
			} finally {
				temp.done();
			}
		} catch (Exception e) {
			logging(Log.WARNING, "cfg", "Failed to (over)load data from database: " + e.toString());
		}

		String	logLevelConfig = cfg.cget ("loglevel");
		
		if (logLevelConfig != null) {
			try {
				logLevel = Log.matchLevel(logLevelConfig);
				if (log != null) {
					log.level(logLevel);
				}
			} catch (NumberFormatException e) {
				throw new Exception("Loglevel must be a known string or a numerical value, not " + logLevelConfig, e);
			}
		}
		xmlBack = cfg.cget("xmlback", Str.makePath("$home", "bin", xmlBack));
		xmlValidate = cfg.cget("xmlvalidate", xmlValidate);
		if (((sampleEmails = cfg.cget("sample_emails", sampleEmails)) != null) && ((sampleEmails.length() == 0) || sampleEmails.equals("-"))) {
			sampleEmails = null;
		}
		if (((deliveryCheckEmails = cfg.cget ("delivery_check_emails", deliveryCheckEmails)) != null) && ((deliveryCheckEmails.length () == 0) || deliveryCheckEmails.equals ("-"))) {
			deliveryCheckEmails = null;
		}
		
		licenceID = syscfg.get ("licence", licenceID);
		limitBlockOperations = cfg.cget("limit_block_operations", limitBlockOperations);
		limitBlockOperationsMax = cfg.cget("limit_block_operations_max", limitBlockOperationsMax);
		dbTempTablespace = cfg.cget("db_temp_tablespace");
		dbHasTablespaces = dbcfg.findInRecord("tablespaces", dbHasTablespaces);
		
		company = new Company(this);
		company.configure(cfg);
		mailinglist = new Mailinglist(this);
		mailinglist.configure(cfg);
		maildropStatus = new MaildropStatus(this);
		maildropStatus.configure(cfg);
		mailing = new Mailing(this);
		mailing.configure(cfg);
		targetExpression = new TargetExpression(this);
		targetExpression.configure(cfg);
	}

	/**
	 * check if database is available
	 */
	private void checkDatabase() throws Exception {
		if (dbase == null) {
			throw new Exception("Database not available");
		}
	}

	/**
	 * setup database connection and retrieve a list of all available
	 * tables
	 *
	 */
	protected void setupDatabase() throws Exception {
		try {
			dbase = new DBase(this);
			dbase.setup();
			dbase.initialize();
		} catch (Exception e) {
			throw new Exception("Database setup failed: " + e.toString(), e);
		}
	}

	/**
	 * close a database and free all assigned data
	 */
	protected void closeDatabase() {
		if (dbase != null) {
			dbase = dbase.done();
		}
	}

	/**
	 * query all basic information about this mailing
	 *
	 * @param generation_spec the reference to the mailing or company and mailinglist
	 */
	@DaoUpdateReturnValueCheck
	private void getMailingInformations(String generation_spec) throws Exception {
		try {
			String[] sdetail = generation_spec.split(":", 2);

			if (sdetail.length == 2) {
				setupMailingInformations(sdetail[0], sdetail[1]);
			} else {
				maildropStatus.id(Long.parseLong(generation_spec));
				maildropStatus.retrieveInformation();
			}
			company.retrieveInformation();
			if (companyInfoMap != null) {
				companyInfoMap
					.entrySet ()
					.stream ()
					.forEach (e -> company.infoAdd (e.getKey (), e.getValue ()));
			}
			if (company.id () > 0) {
				String	newLogLevel = company.info ("merger:loglevel");
				
				if (newLogLevel != null) {
					try {
						logging (Log.INFO, "init", "Change loglevel to " + newLogLevel);
						log.levelDescription (newLogLevel);
					} catch (Exception e) {
						logging (Log.ERROR, "init", "Failed to set loglevel to new level " + newLogLevel + ": " + e.toString ());
					}
				}
			}
			if (emm) {
				retrieveReferenceTableDefinitions(mailing.id());
			}
			if (mailing.id() > 0) {
				retrieveMailingInformation();
				retrieveMediaInformation();
				targetExpression.retrieveInformation();
				mailing.checkMailerset();
			}
			targetExpression.handleMissingTargetExpression();
			retrieveURLsForMeasurement();
			retrieveCustomerTableLayout();
			finalizeConfiguration();
		} catch (CancelException e) {
			throw e;
		} catch (Exception e) {
			logging(Log.ERROR, "init", "Error in quering initial data: " + e.toString(), e);
			throw new Exception("Database error/initial query: " + e, e);
		}
	}

	private void setupMailingInformations(String prefix, String status) throws Exception {
		switch (prefix) {
			case "provider":
				setupMailingInformationsForProviderPreview(status);
				break;
			case "preview":
				setupMailingInformationsForPreview(status);
				break;
			default:
				throw new Exception("Unknown status prefix \"" + prefix + "\" encountered");
		}
	}

	private void setupMailingInformationsForProviderPreview(String status) throws SQLException, CancelException {
		maildropStatus.id(0);
		mailing.id(Long.parseLong(status));
		company.id(mailing.companyID());
		maildropStatus.statusField("V");
		maildropStatus.sendDate(null);
	}

	private void setupMailingInformationsForPreview(String status) throws SQLException, CancelException {
		String[] opts = status.split(",");

		maildropStatus.id(0);
		if (opts.length > 0) {
			try {
				mailing.id(Long.parseLong(opts[0]));
			} catch (NumberFormatException e) {
				logging(Log.WARNING, "setup", "Unparseable input string for mailing_id: \"" + opts[0] + "\": " + e.toString());
				mailing.id(0);
			}
		} else {
			mailing.id(0);
		}
		if (mailing.id() > 0) {
			company.id(mailing.companyID());
			try {
				if (opts.length > 2) {
					mailinglist.id(Long.parseLong(opts[2]));
				}
			} catch (NumberFormatException e) {
				logging(Log.WARNING, "setup", "Unparseable input string for preview \"" + status + "\": " + e.toString());
			}
		} else {
			mailing.id(0);
			company.id(-1);
			mailinglist.id(-1);
			try {
				if (opts.length > 1) {
					company.id(Long.parseLong(opts[1]));
					if (opts.length > 2) {
						mailinglist.id(Long.parseLong(opts[2]));
					}
				}
			} catch (NumberFormatException e) {
				logging(Log.WARNING, "setup", "Unparseable input string for preview \"" + status + "\": " + e.toString());
			}
		}
		maildropStatus.statusField("P");
		maildropStatus.sendDate(null);
	}

	protected void retrieveReferenceTableDefinitions(long mailingId) throws Exception {
		String ref = company.info("references", mailingId);

		if (ref != null) {
			String[] refs = ref.split("\\?");

			for (int n = 0; n < refs.length; ++n) {
				addReference(refs[n], true);
			}
		}

		List<Map<String, Object>> rq;
		rq = dbase.query(
			"SELECT name, reftable, refsource, refcolumn, backref, joincondition, order_by, voucher, voucher_renew " +
			"FROM reference_tbl " +
			"WHERE company_id = :companyID AND (deleted IS NULL OR deleted = 0)",
				"companyID", company.id());
		for (int n = 0; n < rq.size(); ++n) {
			Map<String, Object> row = rq.get(n);

			addReference(dbase.asString(row.get("name")),
					dbase.asString(row.get("reftable")),
					dbase.asString(row.get("refsource")),
					dbase.asString(row.get("refcolumn")),
					dbase.asString(row.get("backref")),
					dbase.asString(row.get("joincondition")),
					dbase.asString(row.get("order_by")),
					dbase.asInt(row.get("voucher")) == 1,
					dbase.asInt(row.get("voucher_renew")) == 1,
					true);
		}
		resolveReferenceAliases();
	}

	private void retrieveMailingInformation() throws Exception {
		retrieveMailingStaticInformation();
		
		int	licenceIDfromDB = retrieveLicenceID ();
		
		if ((licenceIDfromDB != 0) && (licenceID != licenceIDfromDB)) {
			if (licenceID != LICENCE_UNSPEC) {
				logging (Log.ERROR, "setup", "licenceID mismatch! Configuration says " + licenceID + ", but database says " + licenceIDfromDB + ", use later one");
			}
			licenceID = licenceIDfromDB;
		}
		retrieveMailingDynamicInformation();
	}

	private void retrieveMailingStaticInformation() throws Exception {
		Date creation;

		if (mailinglist.id() <= 0) {
			mailinglist.id(mailing.mailinglistID());
		}

		creation = mailing.creationDate();
		company.secretTimestamp(creation != null ? creation.getTime() : 0);
		targetExpression.expression(mailing.targetExpression());
		targetExpression.splitID(mailing.splitID());
		targetExpression.deliveryRestrictID (mailing.deliveryRestrictID ());
		if (maildropStatus.isPreviewMailing()) {
			targetExpression.clear();
		}

		mailingType = mailing.mailingType();
		workStatus = mailing.workStatus();
		mailingPriority = mailing.priority();
		if (mailingPriority == -1) {
			mailingPriority = 0;
			if (company.priorityCount() > 0) {
				if (mailing.mailtemplateID() > 0) {
					findMailingPriorityFromTemplate();
				}
			}
		}
		mailinglist.retrieveInformation();

		if (rdirDomain == null) {
			rdirDomain = mailinglist.rdirDomain();
			if (rdirDomain == null) {
				rdirDomain = company.rdirDomain();
			}
		}
		mailloopDomain = company.mailloopDomain();
		isWorkflowMailing = mailing.isWorkflowMailing();
		if (isWorkflowMailing) {
			logging(Log.DEBUG, "dryrun", "Dryrun mailing detected, apply target expression to test mailing");
		}
	}

	private void findMailingPriorityFromTemplate() {
		mailingPriority = mailing.sourceTemplatePriority();
		if (mailingPriority > 0) {
			logging(Log.DEBUG, "prio", "Found mailing priority " + mailingPriority + " from template");
		} else {
			logging(Log.DEBUG, "prio", "No mailing priority from template found");
		}
	}

	protected int retrieveLicenceID() throws SQLException {
		String value = configValue("system", "licence");

		if (value != null) {
			return Str.atoi(value, licenceID);
		}
		return 0;
	}

	private void retrieveMailingDynamicInformation() {
		Map<String, String> minfo = mailing.info();

		if (minfo != null) {
			for (Map.Entry<String, String> kv : minfo.entrySet()) {
				addMailingInfo(kv.getKey(), kv.getValue());
			}
		}
	}

	private void retrieveMediaInformation() throws Exception {
		media = mailing.media();

		parseMediaStaticInformation();

		Dkim dkim = new Dkim(this);

		dkimActive = dkim.check(mailing.fromEmail());
		if (dkimActive) {
			dkimAvailable = dkim.dkims ();
		}
	}

	private void parseMediaStaticInformation() throws Exception {
		int usedMedia = 0;
		Map <String, String> mediaConfig = configEntry ("media");
		
		availableMedias = 0;
		for (Media tmp : media()) {
			String	config = cfg.cget ("media-" + Media.typeName (tmp.type) + "-config");
			
			if ((config != null) && (config.length () > 0)) {
				tmp.setParameter ("config", config);
			}
			switch (tmp.type) {
			case Media.TYPE_EMAIL:
				mediaEMail = tmp;
				mailing.fromEmail(new EMail(findMediadata(tmp, "from")));
				mailing.replyTo(new EMail(findMediadata(tmp, "reply")));
				String env = findMediadata(tmp, "envelope");
				if (env != null) {
					mailing.envelopeFrom(new EMail(env));
				}
				mailing.subject(findMediadata(tmp, "subject"));
				mailing.setPreHeader(findMediadata(tmp, "preHeader"));
				masterMailtype = ifindMediadata(tmp, "mailformat", masterMailtype);
				if (masterMailtype > MailType.HTML_OFFLINE.getIntValue()) {
					masterMailtype = MailType.HTML_OFFLINE.getIntValue();
				}
				mailing.encoding(findMediadata(tmp, "encoding", mailing.defaultEncoding()));
				mailing.charset(findMediadata(tmp, "charset", mailing.defaultCharset()));
				lineLength = ifindMediadata(tmp, "linefeed", lineLength);

				String opl = findMediadata(tmp, "onepixlog", "none");

				if (opl.equals("top")) {
					onepixlog = OPL_TOP;
				} else if (opl.equals("bottom")) {
					onepixlog = OPL_BOTTOM;
				} else {
					onepixlog = OPL_NONE;
				}
				requiresClearance = bfindMediadata (tmp, "clearance", requiresClearance);

				followupReference = ifindMediadata(tmp, "followup_for", 0L);
				if (!maildropStatus.isWorldMailing()) {
					followupReference = 0L;
				}
				if (followupReference > 0) {
					followupMethod = findMediadata(tmp, "followup_method", null);
				}
				removeDuplicateEMails = bfindMediadata(tmp, "remove_dups", false);
				sendEncrypted = bfindMediadata (tmp, "send_encrypted", false);
				if (bfindMediadata(tmp, "intelliad_enabled", false)) {
					String id = findMediadata(tmp, "intelliad_string", null);

					if (id != null) {
						addTracker("intelliAd", id);
					} else {
						logging(Log.WARNING, "init", "Missing intelliAd ID string even if tracking for intelliAd is enabled");
					}
				}

				String bcc_recv = findMediadata(tmp, "bcc");

				if (bcc_recv != null) {
					for (String recv : bcc_recv.split(", *")) {
						addBcc(recv);
					}
					if (bcc != null) {
						bccBaseIndex = bcc.size();
					}
				}
				break;
			default:
				break;
			}
			if (tmp.stat == Media.STAT_ACTIVE) {
				if (!Bit.isset(availableMedias, tmp.type)) {
					availableMedias = Bit.set(availableMedias, tmp.type);
					++usedMedia;
				}
				if (mediaConfig != null) {
					String	mediaName = Media.typeName (tmp.type);
				
					for (Map.Entry <String, String> es : mediaConfig.entrySet ()) {
						String[] keyParts = es.getKey ().split (":", 2);
					
						if ((keyParts.length == 2) &&
						    mediaName.equals (keyParts[0].toLowerCase ()) &&
						    (! tmp.containsKey (keyParts[1]))) {
							tmp.setParameter (keyParts[1], es.getValue ());
						}
					}
				}
			}
		}
		if (usedMedia > 0) {
			long seen;

			if (usedMedia == 1) {
				mediaRestrictSQL = "bind.mediatype = ";
			} else {
				mediaRestrictSQL = "bind.mediatype IN (";
			}
			seen = 0;
			for (Media tmp : media()) {
				if (!Bit.isset(seen, tmp.type)) {
					if (seen != 0) {
						mediaRestrictSQL += ", ";
					}
					mediaRestrictSQL += Integer.toString(tmp.type);
					seen = Bit.set(seen, tmp.type);
				}
			}
			if (usedMedia > 1) {
				mediaRestrictSQL += ")";
			}
		} else {
			mediaRestrictSQL = null;
		}
		mailing.setEnvelopeFrom();
		company.infoAdd("_envelope_from", mailing.getEnvelopeFrom());
		company.infoAdd("_envelope_forced", Str.btoa (mailing.envelopeForced ()));
		mailing.setEncoding();
		mailing.setCharset();
	}

	public List<Media> media() {
		return media;
	}

	private void retrieveURLsForMeasurement() throws Exception {
		UrlDAO	urlDao = new UrlDAO (company.id (), mailing.id ());

		extendURL = new URLExtension (this);
		URLlist = urlDao.retrieve (dbase, extendURL);
	}

	protected void retrieveCustomerTableLayout() throws Exception {
		Map<String, Column> cmap = new HashMap<>();

		getTableLayout("customer_" + company.id() + "_tbl", null);
		for (Column c : layout) {
			cmap.put(c.getName(), c);
		}

		for (Map<String, Object> row : dbase.query("SELECT col_name, shortname FROM customer_field_tbl WHERE company_id = :companyID", "companyID", company.id())) {
			String column = dbase.asString(row.get("col_name"));

			if (column != null) {
				Column c = cmap.get(column);

				if (c != null) {
					c.setAlias(dbase.asString(row.get("shortname")));
				}
			}
		}
	}

	private void finalizeConfiguration() throws Exception {
		setupSubstitution();

		if (Str.atob(company.info("opt-in-mailing", mailing.id()), false)) {
			defaultUserStatus = UserStatus.WaitForConfirm.getStatusCode();
		}
		if (Str.atob(company.info("resolve-target-groups-by-database", mailing.id()), false)) {
			targetExpression.forceResolveByDatabase (true);
		}

		imageTemplate = company.infoSubstituted("imagelink-template", mailing.id(), "%(rdir-domain)/image/%(licence-id)/%(company-id)/%(mailing-id)/[name]");
		imageTemplateNoCache = company.infoSubstituted("imagelink-template-no-cache", mailing.id(), "%(rdir-domain)/image/nc/%(licence-id)/%(company-id)/%(mailing-id)/[name]");
		mediapoolTemplate = company.infoSubstituted("imagelink-mediapool-template", mailing.id(), "%(rdir-domain)/mediapool_element/%(licence-id)/%(company-id)/%(mailing-id)/[name]");
		mediapoolTemplateNoCache = company.infoSubstituted("imagelink-mediapool-template-no-cache", mailing.id(), "%(rdir-domain)/mediapool_element/nc/%(licence-id)/%(company-id)/%(mailing-id)/[name]");
		preDeliveryCheckEmails = company.infoSubstituted ("SpamCheckAddress", mailing.id ());
		
		if ((rdirDomainForImages != null) && (! rdirDomainForImages.equals (rdirDomain))) {
			imageTemplate = imageTemplate.replace (rdirDomain, rdirDomainForImages);
			imageTemplateNoCache = imageTemplateNoCache.replace (rdirDomain, rdirDomainForImages);
			mediapoolTemplate = mediapoolTemplate.replace (rdirDomain, rdirDomainForImages);
			mediapoolTemplateNoCache = mediapoolTemplateNoCache.replace (rdirDomain, rdirDomainForImages);
		}

		if (followupReference > 0) {
			final int METHOD_NON_OPENER = 0;
			final int METHOD_OPENER = 1;
			final int METHOD_NON_CLICKER = 2;
			final int METHOD_CLICKER = 3;
			int method = METHOD_NON_OPENER;

			if (followupMethod != null) {
				if (followupMethod.equals(FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey())) {
					method = METHOD_NON_OPENER;
				} else if (followupMethod.equals(FollowUpType.TYPE_FOLLOWUP_OPENER.getKey())) {
					method = METHOD_OPENER;
				} else if (followupMethod.equals(FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey())) {
					method = METHOD_NON_CLICKER;
				} else if (followupMethod.equals(FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey())) {
					method = METHOD_CLICKER;
				}
			}
			if ((!company.mailtracking()) && ((method == METHOD_NON_OPENER) || (method == METHOD_NON_CLICKER))) {
				throw new Exception("Request followupmail to " + followupReference + " for " + (followupMethod == null ? "non-opener" : followupMethod) + " requires active mailtracking");
			}

			long statusID = maildropStatus.findLargestStatusIDForWorldMailing(followupReference);

			if (statusID > 0) {
				if ((method == METHOD_NON_OPENER) || (method == METHOD_NON_CLICKER)) {
					int cnt;

					Map<String, Object> row = dbase.querys("SELECT count(*) cnt FROM " + company.mailtrackingTable() + " WHERE maildrop_status_id = :statusID", "statusID", statusID);
					if (row == null) {
						throw new Exception("Failed to query reference mailingID " + followupReference + ", failed to count");
					}
					cnt = dbase.asInt(row.get("cnt"));
					if (cnt == 0) {
						throw new Exception("Reference MailingID " + followupReference + " has no entry in mailtracktable " + company.mailtrackingTable() + " for StatusID " + statusID);
					}
					followupRefSQL = "(EXISTS (SELECT 1 FROM " + company.mailtrackingTable() + " fup_mt WHERE maildrop_status_id = " + statusID + " AND fup_mt.customer_id = bind.customer_id)" + " AND NOT EXISTS (SELECT 1 FROM rdirlog_" + company.id() + "_tbl fup_rd WHERE mailing_id = " + followupReference + " AND fup_rd.customer_id = bind.customer_id)";
					if (method == METHOD_NON_OPENER) {
						followupRefSQL += " AND NOT EXISTS (SELECT 1 FROM onepixellog_" + company.id() + "_tbl fup_op WHERE mailing_id = " + followupReference + " AND fup_op.customer_id = bind.customer_id)";
					}
					followupRefSQL += ")";
				} else if ((method == METHOD_OPENER) || (method == METHOD_CLICKER)) {
					followupRefSQL = "(EXISTS (SELECT 1 FROM rdirlog_" + company.id() + "_tbl fup_rd WHERE mailing_id = " + followupReference + " AND fup_rd.customer_id = bind.customer_id)";
					if (method == METHOD_OPENER) {
						followupRefSQL += " OR EXISTS (SELECT 1 FROM onepixellog_" + company.id() + "_tbl fup_op WHERE mailing_id = " + followupReference + " AND fup_op.customer_id = bind.customer_id)";
					}
					followupRefSQL += ")";
				} else {
					throw new Exception("Invalid followup method " + method + " found");
				}
			} else {
				throw new Exception("Invalid reference MailingID " + followupReference + " found (no world mailing with this mailingID available)");
			}
		}

		String temp;
		if ((temp = company.infoSubstituted("message-domain")) != null) {
			mailing.domain(temp);
		}
		if ((temp = company.infoSubstituted("message-mailer")) != null) {
			mailing.mailer(temp);
		}
		if ((temp = company.info("block-size", mailing.id())) != null) {
			mailing.blockSize(Str.atoi(temp, -1));
		}
		if ((temp = company.info("block-step", mailing.id())) != null) {
			mailing.stepping(Str.atoi(temp, -1));
		}
		if ((temp = company.info ("dynblock-size-min", mailing.id ())) != null) {
			mailing.dynBlockSizeMin (Str.atoi (temp, -1));
		}
		if ((temp = company.info ("dynblock-size-max", mailing.id ())) != null) {
			mailing.dynBlockSizeMax (Str.atoi (temp, -1));
		}
		if ((temp = company.info ("dynblock-divisor", mailing.id ())) != null) {
			mailing.dynBlockDivisor (Str.atoi (temp, -1));
		}
		if ((temp = company.info("force-sending", mailing.id())) != null) {
			defaultCampaignForceSending = Str.atob(temp, defaultCampaignForceSending);
		}
		if ((temp = company.info("campaign-enable-target-groups", mailing.id())) != null) {
			defaultCampaignEnableTargetGroups = Str.atob(temp, defaultCampaignEnableTargetGroups);
		}
		if ((temp = company.info("limit-block-operations", mailing.id())) != null) {
			limitBlockOperations = Str.atoi(temp, 0);
		}
		if ((temp = company.info("limit-block-operations-max", mailing.id())) != null) {
			limitBlockOperationsMax = Str.atoi(temp, 0);
		}
		if ((temp = company.info ("enforce-test-vip", mailing.id ())) != null) {
			enforceTestVIP = Str.atob (temp, false);
		}
		if ((temp = company.info ("bounce-bleed", mailing.id ())) != null) {
			bounceBleed = Str.atob (temp, false);
		}
		if ((temp = company.info ("bounce-bleed-duration", mailing.id ())) != null) {
			bounceBleedDuration = Str.atoi (temp, bounceBleedDuration);
		}
		if ((temp = company.info ("vouchers-mandatory", mailing.id ())) != null) {
			vouchersMandatory = Str.atob (temp, false);
		}

		setupUrlAndTags(mailing.id ());

		isPriorityMailing = false;
		if ((maildropStatus.isWorldMailing() || maildropStatus.isRuleMailing()) && (company.priorityCount() > 0) && (mailingPriority > 0)) {
			logging(Log.DEBUG, "priority", "Company is enabled for priority mailings with a limit of " + company.priorityCount() + " and mailing priority of " + mailingPriority);

			String priorityConfigTable = "priority_config_tbl";

			priorityTable = "priority_" + company.id() + "_tbl";
			if (dbase.tableExists(priorityConfigTable) && dbase.tableExists(priorityTable)) {
				Calendar now = Calendar.getInstance();
				String configQuery = "SELECT value FROM " + priorityConfigTable + " WHERE company_id = :companyID AND variable = :variable";
				String timeID;

				priorityTimeID = now.get(Calendar.YEAR) * 10000 + (now.get(Calendar.MONTH) + 1) * 100 + now.get(Calendar.DAY_OF_MONTH);
				timeID = Integer.toString(priorityTimeID);

				logging(Log.DEBUG, "priorty", "Looking for existing configuration for " + timeID);

				Map<String, Object> row = dbase.querys(configQuery, "companyID", company.id(), "variable", "last-run");
				if (row != null) {
					String value = dbase.asString(row.get("value"));

					if ((value != null) && value.equals(timeID)) {
						row = dbase.querys(configQuery, "companyID", company.id(), "variable", "set-priorities");
						if (row != null) {
							value = dbase.asString(row.get("value"));
							if (value != null) {
								try {
									JsonFactory factory = new JsonFactory();
									try (JsonParser parser = factory.createParser(value)) {
										String mailingID = Long.toString(mailing.id());

										while (!parser.isClosed()) {
											JsonToken jsonToken = parser.nextToken();

											if ((jsonToken == JsonToken.FIELD_NAME) && parser.getValueAsString().equals(mailingID)) {
												isPriorityMailing = true;
												break;
											}
										}

										if (!isPriorityMailing) {
											logging(Log.DEBUG, "priority", "No entry in set priorities for mailing " + mailingID + " found");
										}
									}
								} catch (Exception e) {
									logging(Log.DEBUG, "priority", "Failed in parsing json: " + e.toString() + "\n" + value);
								}
							} else {
								logging(Log.DEBUG, "priority", "Empty value for setted priorities found");
							}
						} else {
							logging(Log.DEBUG, "priority", "No priority setting information found");
						}
					} else {
						logging(Log.DEBUG, "priority", "Priority for today (" + timeID + ") not (yet) calculated, last calculation had been " + (value != null ? value : "never"));
					}
				} else {
					logging(Log.DEBUG, "priority", "Priority had never been calculated for this mailing");
				}
				logging(Log.DEBUG, "priority", "Priotory handling is " + (isPriorityMailing ? "enabled" : "disabled"));
			} else {
				logging(Log.DEBUG, "priority", "Either " + priorityConfigTable + " or " + priorityTable + " is not existing");
			}
		} else if (company.priorityCount() > 0) {
			logging(Log.DEBUG, "priority", "No priority found for this mailing");
		}
	}

	/**
	 * Validate all set variables and make a sanity check
	 * on the database to avoid double triggering of a
	 * mailing
	 */
	@DaoUpdateReturnValueCheck
	public void checkMailingData() throws Exception {
		int cnt;
		String msg;

		cnt = 0;
		msg = "";
		if (maildropStatus.isWorldMailing()) {
			try {
				long nid;

				checkDatabase();
				nid = maildropStatus.findSmallestStatusIDForWorldMailing(mailing.id());
				if (nid == 0) {
					throw new Exception("no entry at all for mailingID " + mailing.id() + " found");
				}
				if (nid != maildropStatus.id()) {
					++cnt;
					msg += "\tlowest maildrop_status_id is not mine (" + maildropStatus.id() + ") but " + nid + "\n";
					maildropStatus.removeEntry();
				}
			} catch (Exception e) {
				++cnt;
				msg += "\tunable to requery my status_id: " + e.toString() + "\n";
			}
		}
		if ((!maildropStatus.isPreviewMailing()) && (maildropStatus.id() < 0)) {
			++cnt;
			msg += "\tmaildrop_status_id is less than 1 (" + maildropStatus.id() + ")\n";
		}
		if (company.id() <= 0) {
			++cnt;
			msg += "\tcompany_id is less than 1 (" + company.id() + ")\n";
		}
		if (mailinglist.id() <= 0) {
			++cnt;
			msg += "\tmailinglist_id is less than 1 (" + mailinglist.id() + ")\n";
		}
		if (mailing.id() < 0) {
			++cnt;
			msg += "\tmailing_id is less than 0 (" + mailing.id() + ")\n";
		}
		if ((!maildropStatus.isAdminMailing()) && (!maildropStatus.isTestMailing()) && (!maildropStatus.isCampaignMailing()) && (!maildropStatus.isVerificationMailing()) && (!maildropStatus.isRuleMailing()) && (!maildropStatus.isOnDemandMailing()) && (!maildropStatus.isWorldMailing()) && (!maildropStatus.isPreviewMailing())) {
			++cnt;
			msg += "\tstatus_field must be one of A, V, T, E, R, D, W or P (" + maildropStatus.statusField() + ")\n";
		}

		currentSendDate = currentSendDate (maildropStatus.sendDate());
		sendSeconds = currentSendDate.getTime () / 1000;
		
		if ((autoURL == null) || (autoURL.length() == 0)) {
			++cnt;
			msg += "\tmissing or empty auto_url\n";
		}
		if ((onePixelURL == null) || (onePixelURL.length() == 0)) {
			//			++cnt;
			onePixelURL = "file://localhost/";
			msg += "\tmissing or empty onepixel_url\n";
		}
		if (lineLength < 0) {
			++cnt;
			msg += "\tlinelength is less than zero\n";
		}
		if (cnt > 0) {
			logging(Log.ERROR, "init", "Error configuration report:\n" + msg);
			throw new Exception(msg);
		}
		if (msg.length() > 0) {
			logging(Log.INFO, "init", "Configuration report:\n" + msg);
		}
	}

	private void addTracker(String name, String code) {
		if (tracker == null) {
			tracker = new ArrayList<>();
		}
		tracker.add(new Tracker(name, code));
	}

	private synchronized long getIncarnation() {
		if (++incarnation < 0) {
			incarnation = 1;
		}
		return incarnation;
	}

	private synchronized long logIncarnation() {
		if (++logIncarnation < 0) {
			logIncarnation = 1;
		}
		return logIncarnation;
	}

	/**
	 * find an entry from the media record for this mailing
	 *
	 * @param m    instance of media record
	 * @param id   the ID to look for
	 * @param dflt a default value if no entry is found
	 * @return the found entry or the default
	 */
	public String findMediadata(Media m, String id, String dflt) {
		return m.findParameterValue(id, dflt);
	}

	public String findMediadata(Media m, String id) {
		return findMediadata(m, id, null);
	}

	/**
	 * find a numeric entry from the media record for this mailing
	 *
	 * @param m    instance of media record
	 * @param id   the ID to look for
	 * @param dflt a default value if no entry is found
	 * @return the found entry or the default
	 */
	public long ifindMediadata(Media m, String id, long dflt) {
		String tmp = findMediadata(m, id, null);

		if (tmp != null) {
			try {
				return Integer.parseInt(tmp);
			} catch (Exception e) {
				// do nothing
			}
		}
		return dflt;
	}

	public int ifindMediadata(Media m, String id, int dflt) {
		return (int) ifindMediadata(m, id, (long) dflt);
	}

	/**
	 * find a boolean entry from the media record for this mailing
	 *
	 * @param m    instance of media record
	 * @param id   the ID to look for
	 * @param dflt a default value if no entry is found
	 * @return the found entry or the default
	 */
	public boolean bfindMediadata(Media m, String id, boolean dflt) {
		String tmp = findMediadata(m, id, null);
		boolean rc = dflt;

		if (tmp != null) {
			if (tmp.length() == 0) {
				rc = true;
			} else {
				String tok = tmp.substring(0, 1).toLowerCase();

				if (tok.equals("t") || tok.equals("y") || tok.equals("+") || tok.equals("1")) {
					rc = true;
				}
			}
		} else {
			rc = false;
		}
		return rc;
	}

	protected void setupUrlAndTags(long MailingID) {
		String url, tag;
		if (rdirDomain == null) {
			rdirDomain = company.infoSubstituted("url-default");
		}
		if ((url = company.infoSubstituted("url-auto")) != null) {
			autoURL = url;
		}
		if ((url = company.infoSubstituted("url-onepixel")) != null) {
			onePixelURL = url;
		}
		if ((url = company.infoSubstituted("url-honeypot")) != null) {
			honeyPotURL = url;
		}
		if ((url = company.infoSubstituted("url-anon")) != null) {
			anonURL = url;
		}
		if ((tag = company.infoSubstituted("url-auto-tag")) != null) {
			autoTag = tag;
		}
		if ((tag = company.infoSubstituted("url-onepixel-tag")) != null) {
			onePixelTag = tag;
		}
		if ((tag = company.infoSubstituted("url-honeypot-tag")) != null) {
			honeyPotTag = tag;
		}
		if ((tag = company.infoSubstituted("url-anon-tag")) != null) {
			anonTag = tag;
		}

		if (rdirDomain != null) {
			company.infoAdd("_rdir_domain", rdirDomain);
		}
		if (mailloopDomain != null) {
			company.infoAdd("_mailloop_domain", mailloopDomain);
		}
		directPath = Str.atob(company.info("direct-path", MailingID), false);
		if (rdirDomain != null) {
			if (autoURL == null) {
				autoURL = rdirDomain + autoTag;
			}
			if (onePixelURL == null) {
				onePixelURL = (rdirDomainForImages != null ? rdirDomainForImages : rdirDomain) + onePixelTag;
			}
			if (honeyPotURL == null) {
				honeyPotURL = rdirDomain + honeyPotTag;
			}
			if (anonURL == null) {
				anonURL = rdirDomain + anonTag;
			}
		}
	}

	protected void setupSubstitution() {
		substitute = new Substitute("status-id", maildropStatus.id(),
					    "licence-id", licenceID,
					    "company-id", company.id(),
					    "company-token", company.token(),
					    "base-company-id", company.baseID(),
					    "mailinglist-id", mailinglist.id(),
					    "mailtemplate-id", mailing.mailtemplateID(),
					    "mailing-id", mailing.id(),
					    "mail-id", getFilenameMailingID(),
					    "status-field", maildropStatus.statusField(),
					    "mailing-name", mailing.name(),
					    "company-name", company.name(),
					    "rdir-domain", rdirDomain,
					    "rdir-domain-for-images", rdirDomainForImages != null ? rdirDomainForImages : rdirDomain,
					    "mailloop-domain", mailloopDomain,
					    "total-subscribers", totalSubscribers,
					    "total-receivers", totalReceivers);
		if (mailingInfo != null) {
			for (Map.Entry<String, String> kv : mailingInfo.entrySet()) {
				substitute.put("mailing." + kv.getKey(), kv.getValue());
			}
		}
	}

	public String substituteString(String str, Map<String, String> extra, List<String> missing, String defaultValue) {
		return substitute.replace(str != null ? str : defaultValue, extra, missing);
	}

	public String substituteString(String str, Map<String, String> extra, List<String> missing) {
		return substituteString(str, extra, missing, null);
	}

	public String substituteString(String str, Map<String, String> extra, String defaultValue) {
		return substituteString(str, extra, null, defaultValue);
	}

	public String substituteString(String str, Map<String, String> extra) {
		return substituteString(str, extra, null, null);
	}

	public String substituteString(String str, String defaultValue) {
		return substituteString(str, null, null, defaultValue);
	}

	public String substituteString(String str) {
		return substituteString(str, null, null, null);
	}

	public void addMailingInfo(String name, String value) {
		if (mailingInfo == null) {
			mailingInfo = new HashMap<>();
		}
		mailingInfo.put(name, value == null ? "" : value);
	}

	protected boolean doAddReference(Reference r, boolean overwrite) {
		if (r.valid()) {
			if (references == null) {
				references = new HashMap<>();
			}
			if (overwrite || (!references.containsKey(r.name()))) {
				references.put(r.name(), r);
			}
			return true;
		}
		return false;
	}

	public boolean addReference(String name, String table, String customerExpression, String keyColumn, String backRef, String joinCondition, String orderBy, boolean isVoucher, boolean voucherRenew, boolean overwrite) {
		return doAddReference(new Reference(this, name, table, customerExpression, keyColumn, backRef, joinCondition, orderBy, isVoucher, voucherRenew), overwrite);
	}

	public boolean addReference(String ref, boolean overwrite) {
		boolean rc = false;

		if (ref != null) {
			Reference r = new Reference(this, ref.trim());

			if (r.valid()) {
				rc = doAddReference(r, overwrite);
			} else {
				logging(Log.WARNING, "ref", "Reference expression \"" + ref + "\" is invalid");
			}
		}
		return rc;
	}

	private void resolveReferenceAliases() {
		String enableReferenceAliases;
		String refAliases;

		if (((enableReferenceAliases = company.info("ref:enable-reference-aliases")) != null) &&
		    Str.atob(enableReferenceAliases, false) &&
		    (references != null) &&
		    (mailingInfo != null) &&
		    ((refAliases = mailingInfo.get("ref:aliases")) != null)) {
			Map<String, String> aliases = new HashMap<>();

			for (String elem : refAliases.split(", *")) {
				String[] pair = elem.split(" *= *", 2);

				if (pair.length == 2) {
					aliases.put(pair[1], pair[0]);
				}
			}
			for (Reference r : references.values()) {
				String alias = aliases.get(r.name());

				if (alias != null) {
					if (r.validName(alias)) {
						logging(Log.DEBUG, "resolve-ref", "Rename reference table " + r.name() + " to " + alias);
						r.name(alias);
						references.put(r.name(), r);
					} else {
						logging(Log.DEBUG, "resolve-ref", "Failed to rename reference table " + r.name() + " to " + alias + " due to invalid alias name");
					}
				}
			}
		}
	}

	public boolean fullfillReference(Reference r) {
		boolean rc;

		if (!r.isFullfilled()) {
			r.fullfill();
			try {
				getTableLayout(r.table(), r.name());
				rc = true;
			} catch (Exception e) {
				logging(Log.ERROR, "layout", "Failed to get layout for reference table \"" + r.table() + "\"", e);
				rc = false;
			}
			if (r.backReference() != null) {
				Reference br = references.get(r.backReference());

				if (br != null) {
					if (!fullfillReference(br)) {
						rc = false;
					}
				} else {
					logging(Log.ERROR, "layout", "Failed to find back reference \"" + r.backReference() + "\" for \"" + r.name() + "\"");
					rc = false;
				}
			}
		} else {
			rc = true;
		}
		return rc;
	}

	public void markBlocklisted(Long cid) {
		if (blocklisted == null) {
			blocklisted = new HashSet<>();
		}
		if (!blocklisted.contains(cid)) {
			blocklisted.add(cid);
			totalReceivers--;
		}
	}

	public void correctReceiver() {
		if (blocklisted != null) {
			for (Long cid : blocklisted) {
				bigClause.removeReceiver(cid);
			}
		}
	}

	public int getNoOfBlocklisted() {
		return blocklisted == null ? 0 : blocklisted.size();
	}

	public String boundary() {
		return mailing.boundary();
	}

	public static class Layout implements ResultSetExtractor<Object> {
		private Data data;
		private String table;
		private List<Column> layout;
		private String ref;

		public Layout(Data nData, String nTable, List<Column> nLayout, String nRef) {
			data = nData;
			table = nTable;
			layout = nLayout;
			ref = nRef;
		}

		public List<Column> getLayout() {
			return layout;
		}

		@Override
		public Object extractData(ResultSet rset) throws SQLException, org.springframework.dao.DataAccessException {
			ResultSetMetaData meta = rset.getMetaData();
			int ccnt = meta.getColumnCount();

			for (int n = 0; n < ccnt; ++n) {
				String cname = meta.getColumnName(n + 1).toLowerCase ();
				int ctype = meta.getColumnType(n + 1);
				String tname = meta.getColumnTypeName(n + 1);

				if (tname != null) {
					tname = tname.toLowerCase();
				}
				if ((ctype == -1) && (tname != null) && (tname.equals("varchar") || tname.equals ("longtext"))) {
					ctype = Types.VARCHAR;
				}
				if (Column.typeStr(ctype) != null) {
					Column c = new Column(cname, ctype);

					c.setRef(ref);
					if (layout == null) {
						layout = new ArrayList<>();
					}
					layout.add(c);
				} else if (data != null) {
					data.logging (Log.WARNING, "layout", "Column \"" + table + "." + cname + "\" has unsupported type id " + ctype + " and name is \"" + (tname != null ? tname : "unknown") + "\"");
				}
			}
			return this;
		}
	}

	protected void getTableLayout(String table, String ref) throws Exception {
		Data data = this;
		DBase.Retry<List<Column>> r = dbase.new Retry<>("layout", dbase, dbase.cursor()) {
			@Override
			public void execute() throws SQLException {
				String query = "SELECT * FROM " + table + " WHERE 1 = 0";
				Layout temp = new Layout(data, table, layout, ref);

				cursor.query(query, null, temp);
				priv = temp.getLayout();
			}
		};
		if (dbase.retry(r)) {
			layout = r.priv;
			if (layout != null) {
				lcount = layout.size();
				lusecount = lcount;
			}
			return;
		}
		throw r.error;
	}

	public Locale getLocale(String language, String country) {
		if (language == null) {
			language = company.info("locale-language", mailing.id());
		}
		if (country == null) {
			country = company.info("locale-country", mailing.id());
		}
		if (language == null) {
			return null;
		}
		if (country == null) {
			return new Locale(language);
		}
		return new Locale(language, country);
	}

	public TimeZone getTimeZone(String timezone) {
		if (timezone == null) {
			timezone = company.info("locale-timezone", mailing.id());
		}
		if (timezone == null) {
			return null;
		}
		return TimeZone.getTimeZone(timezone);
	}

	public Map<String, String> configEntry(String configClass) throws SQLException {
		if (configDao == null) {
			configDao = new ConfigDAO (dbase);
		}
		return configDao.getClassEntry(configClass);
	}

	public String configValue(String configClass, String configName) throws SQLException {
		Map<String, String> entry = configEntry(configClass);

		return entry != null ? entry.get(configName) : null;
	}

	public Title getTitle(Long tid) throws SQLException {
		if (titles == null) {
			titles = (new TitleDAO(dbase, company.id())).titles();
		}
		return titles.get(tid);
	}

	public TagDAO.Entry getTag(String tagName) throws SQLException {
		setupTagDao();
		return tagDao.get(tagName);
	}

	public TagDAO.Function getTagFunction(String functionName) throws SQLException {
		setupTagDao();
		return tagDao.getFunction(dbase, functionName);
	}

	private void setupTagDao() throws SQLException {
		if (tagDao == null) {
			tagDao = new TagDAO(dbase, company.id());
		}
	}

	/**
	 * Fill already sent recipient in seen hashset for
	 * recovery prupose
	 *
	 * @param seen the hashset to fill with seen customerIDs
	 */
	public void prefillRecipients(Set<Long> seen) throws IOException {
		if (maildropStatus.isWorldMailing() || maildropStatus.isRuleMailing() || maildropStatus.isOnDemandMailing()) {
			File recovery = new File(mailing.outputDirectory("meta"), "recover-" + maildropStatus.id() + ".list");

			if (recovery.exists()) {
				logging(Log.INFO, "recover", "Found recovery file " + recovery.getAbsolutePath());
				markToRemove(recovery.getAbsolutePath());
				try (FileInputStream in = new FileInputStream(recovery)) {
					byte[] content = new byte[(int) recovery.length()];
					in.read(content);
					String[] data = (new String(content, StandardCharsets.US_ASCII)).split("\n");
					for (String line : data) {
						if (line.length() > 0) {
							seen.add(Long.decode(line));
						}
					}
				}
			}
		}
	}

	/**
	 * modify sending timestamp for next block
	 *
	 * @param timestamp current calculated send timestamp in seconds
	 * @return new timestamp
	 */
	public long modifyNextBlockTimestamp(long timestamp) {
		if (omgEnabled()) {
			if (omgTimediff == null) {
				if (maildropStatus.optimizeForDay()) {
					Calendar temp = Calendar.getInstance();

					temp.setTimeInMillis(sendSeconds * 1000);
					temp.set(Calendar.HOUR_OF_DAY, 0);
					temp.set(Calendar.MINUTE, 0);
					temp.set(Calendar.SECOND, 0);
					temp.set(Calendar.MILLISECOND, 0);

					omgTimediff = (temp.getTimeInMillis() / 1000 - sendSeconds) / 60;
					if (omgTimediff > 0) {
						omgTimediff = 0L;
					}
				} else {
					omgTimediff = 0L;
				}
			}
			long ntimestamp = sendSeconds + (omgTimediff - omgOffset) * 60;

			if (ntimestamp != timestamp) {
				timestamp = ntimestamp;
			}
		}
		return timestamp;
	}

	/**
	 * Sanity check for mismatch company_id and perhaps deleted
	 * mailing
	 */
	public void sanityCheck(Blocklist blocklist) throws Exception {
		if (!maildropStatus.isPreviewMailing()) {
			if (!mailing.exists()) {
				throw new Exception("No entry for mailingID " + mailing.id() + " in mailing table found");
			}
			if (mailing.companyID() != company.id()) {
				throw new Exception("Original companyID " + company.id() + " for mailing " + mailing.id() + " does not match current company_id " + mailing.companyID());
			}
			if (mailing.deleted()) {
				try {
					maildropStatus.setGenerationStatus(0, 4);
				} catch (Exception e) {
					logging(Log.ERROR, "sanity", "Failed to set generation status: " + e.toString(), e);
				}
				throw new Exception("Mailing " + mailing.id() + " marked as deleted");
			}
			if (bcc != null) {
				int size = bcc.size();

				for (int n = bccBaseIsBlocklistChecked ? bccBaseIndex : 0; n < size; ) {
					String recv = bcc.get(n);

					if (blocklist.isBlockListed(recv) != null) {
						logging(Log.ERROR, "bcc", "Address \"" + recv + "\" is on blocklist, do not use it");
						bcc.remove(n);
						--size;
						if (n < bccBaseIndex) {
							--bccBaseIndex;
						}
					} else {
						++n;
					}
				}
				bccBaseIsBlocklistChecked = true;
			}
		} else {
			previewClearData = false;
			if (previewCustomerID == 0) {
				logging(Log.DEBUG, "sanity", "No preview customer given, try find a valid admin/test recipient");

				RecipientDAO recipientDao = new RecipientDAO();
				if (mailing.exists() && (mailing.mailinglistID() > 0)) {
					previewCustomerID = recipientDao.findRecipientForPreview(dbase, company.id(), mailing.mailinglistID(), previewTargetIDs);
				} else {
					logging(Log.WARNING, "sanity", "Mailing does not exists or has no assigned mailinglistID");
				}
				if (previewCustomerID == 0) {
					throw new Exception("No customer at all in " + company.id() + " found.");
				}
			}
		}
	}

	public Code findCode(String codeName) {
		Code rc;

		try {
			rc = codes.get(codeName);
			if (rc == null) {
				rc = new Code(this, codeName);
				if (!rc.retrieveCode()) {
					logging(Log.WARNING, "data", "Failed to retrieve code for block " + codeName);
				}
				codes.put(codeName, rc);
			}
		} catch (SQLException e) {
			rc = null;
			logging(Log.ERROR, "data", "Failed to retrieve code for " + codeName + ": " + e.toString(), e);
		}
		return rc != null && rc.isValid() ? rc : null;
	}

	/**
	 * Executed at start of mail generation
	 */
	static private final SimpleDateFormat	dateFormater = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
	public void startExecution() throws Exception {
		bigClause = new BC(this);
		if (!bigClause.prepareQueryParts()) {
			throw new Exception("Failed to setup the query parts");
		}
		if (blocklisted != null) {
			blocklisted.clear();
		}
		totalSubscribers = bigClause.subscriber();
		totalReceivers = bigClause.receiver();
		mailing.receiverCount (totalReceivers);
		logging(Log.DEBUG, "start", "\ttotalSubscribers = " + totalSubscribers);
		logging(Log.DEBUG, "start", "\ttotalReceivers = " + totalReceivers);
		if (maildropStatus.isWorldMailing() || maildropStatus.isRuleMailing() || maildropStatus.isOnDemandMailing()) {
			logging("generate",
				"company=" + company.id() +
				"\tmailinglist=" + mailinglist.id() +
				"\tmailing=" + mailing.id() +
				"\tmailid=" + getFilenameMailingID() +
				"\tcount=" + totalReceivers +
				"\tstatus_field=" + maildropStatus.statusField() +
				"\ttimestamp=" + dateFormater.format (new Date ())
			);
		}
	}

	/**
	 * Executed at end of mail generation
	 */
	public void endExecution() {
		if (bigClause != null) {
			bigClause.done();
			bigClause = null;
		}
	}

	/**
	 * Change generation state for the current mailing
	 */
	public void updateGenerationState(int newstatus) {
		if (maildropStatus.isAdminMailing() || maildropStatus.isTestMailing() || maildropStatus.isWorldMailing() || maildropStatus.isRuleMailing() || maildropStatus.isOnDemandMailing()) {
			try {
				if ((newstatus == 0) || (newstatus == 4)) {
					if (maildropStatus.isRuleMailing() || maildropStatus.isOnDemandMailing()) {
						newstatus = 1;
					} else {
						newstatus = newstatus == 4 ? newstatus : 3;
					}
				}
				maildropStatus.setGenerationStatus(0, newstatus);
			} catch (Exception e) {
				logging(Log.ERROR, "genstate", "Unable to update generation state: " + e.toString(), e);
			}
		}
	}

	public void updateGenerationState() {
		updateGenerationState(0);
	}
	
	public void updateGenerateionStateForCancel () {
		updateGenerationState(4);
	}

	/**
	 * Called when main generation starts
	 */
	public List<String> generationClauses() {
		return bigClause.createClauses();
	}

	public String getSelectExpression(boolean fullExpression) {
		return bigClause.createSelect(fullExpression);
	}

	/**
	 * Save receivers to mailtracking table
	 */
	@DaoUpdateReturnValueCheck
	public void toMailtrack() {
		if (company.mailtracking()) {
			if (company.mailtrackingExtended()) {
				logging(Log.DEBUG, "mailtrack", "Mailtrack will be written later");
			} else {
				//				long	chunks = limitBlockChunks ();		// not active due to performance issues
				long	chunks = 1;
				int	mediaType = (media != null) && (media.size () == 1) ? media.get (0).type : Media.TYPE_UNRELATED;
				String	query = bigClause.mailtrackStatement(company.mailtrackingTable(), mediaType);
				
				if (query != null) {
					for (long chunk = 0; chunk < chunks; ++chunk) {
						String cquery = chunks == 1 ? query : query + " WHERE mod(customer_id, " + chunks + ") = " + chunk;
						boolean success = false;
						int retry = 3;

						while ((!success) && (retry-- > 0)) {
							try {
								synchronized (company.mailtrackingTable()) {
									dbase.execute(cquery);
									dbase.flush();
								}
								success = true;
							} catch (Exception e) {
								logging(Log.ERROR, "mailtrack", "Failed to write mailtracking using " + cquery + ": " + e.toString(), e);
							}
						}
					}
				}
			}
		}
		if ((mailingType == MailingType.INTERVAL.getCode()) && (maildropStatus.isWorldMailing() || maildropStatus.isOnDemandMailing())) {
			String intervalTrackTable = "interval_track_" + company.id() + "_tbl";
			String query = bigClause.intervalStatement(intervalTrackTable);

			try {
				dbase.update(query, "mailingID", mailing.id(), "sendDate", currentSendDate (maildropStatus.sendDate ()));
				dbase.flush();
			} catch (Exception e) {
				logging(Log.ERROR, "mailtrack", "Failed to save interval mailing information: " + e.toString(), e);
			}
		}
	}

	private void typeerr(Object o, String what) throws Exception {
		throw new Exception(what + ": unknown data type:  " + o.getClass().toString());
	}

	private String obj2str (Object o) {
		if (o instanceof String) {
			return (String) o;
		}
		if (o != null) {
			return o.toString ();
		}
		return null;
	}
	
	/**
	 * Convert a given object to an integer
	 *
	 * @param o    the input object
	 * @param what for logging purpose
	 * @return the converted value
	 */
	private int obj2int(Object o, String what) throws Exception {
		int rc = 0;

		if (o instanceof Integer) {
			rc = ((Integer) o).intValue();
		} else if (o instanceof Long) {
			rc = ((Long) o).intValue();
		} else if (o instanceof Double) {
			rc = (int) ((Double) o).doubleValue();
		} else if (o instanceof String) {
			rc = Integer.parseInt((String) o);
		} else {
			typeerr(o, what);
		}
		return rc;
	}

	/**
	 * Convert a given object to a long
	 *
	 * @param o    the input object
	 * @param what for logging purpose
	 * @return the converted value
	 */
	private long obj2long(Object o, String what) throws Exception {
		long rc = 0;

		if (o instanceof Integer) {
			rc = ((Integer) o).longValue();
		} else if (o instanceof Long) {
			rc = ((Long) o).longValue();
		} else if (o instanceof Double) {
			rc = (long) ((Double) o).doubleValue();
		} else if (o instanceof String) {
			rc = Long.parseLong((String) o);
		} else {
			typeerr(o, what);
		}
		return rc;
	}

	/**
	 * Convert a given object to a boolean
	 *
	 * @param o    the input object
	 * @param what for logging purpose
	 * @return the converted value
	 */
	private boolean obj2bool(Object o, String what) throws Exception {
		boolean rc = false;

		if (o instanceof Boolean) {
			rc = ((Boolean) o).booleanValue();
		} else if (o instanceof Integer) {
			rc = ((Integer) o).intValue() != 0;
		} else if (o instanceof Long) {
			rc = ((Long) o).longValue() != 0L;
		} else if (o instanceof Double) {
			rc = ((Double) o).doubleValue() != 0.0;
		} else if (o instanceof String) {
			rc = Boolean.parseBoolean((String) o);
		} else {
			typeerr(o, what);
		}
		return rc;
	}

	/**
	 * Convert a given object to a date
	 *
	 * @param o    the input object
	 * @param what for logging purpose
	 * @return the converted value
	 */
	private Date obj2date(Object o, String what) throws Exception {
		Date rc = null;

		if (o instanceof Date) {
			rc = (Date) o;
		} else {
			typeerr(o, what);
		}
		return rc;
	}

	/**
	 * Convert a given object to an array
	 *
	 * @param o the input object
	 * @return the extracted list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<Object> obj2list(Object o) {
		for (Class<List> cls : (Class<List>[]) o.getClass().getInterfaces()) {
			if (cls == List.class) {
				return ((List<Object>) o);
			}
		}
		ArrayList<Object> rc = new ArrayList<>(1);

		rc.add(o);
		return rc;
	}

	private long[] obj2longarray(Object o, String what) throws Exception {
		if (o instanceof String) {
			return Stream.of (((String) o).split (", *")).mapToLong (v -> Long.parseLong (v)).toArray ();
		}
		List<Object> in = obj2list(o);
		long[] rc = new long[in.size()];
		int idx = 0;

		for (Object e : in) {
			rc[idx++] = obj2long(e, what);
		}
		return rc;
	}

	/**
	 * Parse options passed during runtime
	 *
	 * @param opts  the options to use
	 * @param state if 1, the before initialization pass, 2 on execution pass
	 */
	@SuppressWarnings("unchecked")
	public void options(Map<String, Object> opts, int state) throws Exception {
		Object tmp;

		if (opts == null) {
			return;
		}
		logging (Log.DEBUG, "options" + state, "-- OPTION DUMP START --");
		for (Map.Entry <String, Object> option : opts.entrySet ()) {
			logging (Log.DEBUG, "options" + state, "--> " + option.getKey () + " = " + option.getValue ());
		}
		logging (Log.DEBUG, "options" + state, "--  OPTION DUMP END  --");
		switch (state) {
		case 0:
			if ((tmp = opts.get ("rdir-domain")) != null) {
				rdirDomain = obj2str (tmp);
				logging (Log.DEBUG, "options0", "--> rdir-domain = " + rdirDomain);
			}
			if ((tmp = opts.get ("rdir-domain-for-images")) != null) {
				rdirDomainForImages = obj2str (tmp);
				logging (Log.DEBUG, "options0", "--> rdir-domain-for-images = " + rdirDomainForImages);
			}
			if ((companyInfoMap = mormalizeReplacementMap(opts.get("company-info"))) != null) {
				logging(Log.DEBUG, "options0", "--> company-info = " + companyInfoMap);
			}
			break;
		case 1:
			if ((previewInput = (String) opts.get("preview-input")) != null) {
				logging(Log.DEBUG, "options1", "--> preview-input = " + previewInput);
			}
			if ((tmp = opts.get("preview-create-all")) != null) {
				previewCreateAll = obj2bool(tmp, "preview-create-all");
				logging(Log.DEBUG, "options1", "--> preview-create-all = " + previewCreateAll);
			}
			if ((tmp = opts.get("preview-cache-images")) != null) {
				previewCacheImages = obj2bool(tmp, "preview-cache-images");
				logging(Log.DEBUG, "options1", "--> preview-cache-images = " + previewCacheImages);
			}
			if ((tmp = opts.get("preview-for-mobile")) != null) {
				isMobilePreview = obj2bool(tmp, "preview-for-mobile");
				logging(Log.DEBUG, "options1", "--> preview-for-mobile = " + isMobilePreview);
			}
			break;
		case 2:
			if ((tmp = opts.get("customer-id")) != null) {
				campaignCustomerID = obj2long(tmp, "customer-id");
				logging(Log.DEBUG, "options2", "--> customer-id = " + campaignCustomerID);
			} else {
				campaignCustomerID = 0;
			}
			if ((tmp = opts.get("user-status")) != null) {
				campaignUserStatus = obj2longarray(tmp, "user-status");
				logging(Log.DEBUG, "options2", "--> user-status = " + campaignUserStatus);
			} else {
				campaignUserStatus = null;
			}
			if ((tmp = opts.get("force-sending")) != null) {
				campaignForceSending = obj2bool(tmp, "force-sending");
				logging(Log.DEBUG, "options2", "--> force-sending = " + campaignForceSending);
			} else {
				campaignForceSending = defaultCampaignForceSending;
			}
			if ((tmp = opts.get("campaign-enable-target-groups")) != null) {
				campaignEnableTargetGroups = obj2bool(tmp, "campaign-enable-target-groups");
				logging(Log.DEBUG, "options2", "--> campaign-enable-target-groups = " + campaignEnableTargetGroups);
			} else {
				campaignEnableTargetGroups = defaultCampaignEnableTargetGroups;
			}
			if ((providerEmail = (String) opts.get("provider-email")) != null) {
				logging(Log.DEBUG, "options2", "--> provider-email = " + providerEmail);
			}
			if ((tmp = opts.get("preview-for")) != null) {
				previewCustomerID = obj2long(tmp, "preview-for");
				logging(Log.DEBUG, "options2", "--> preview-for = " + previewCustomerID);
			} else {
				previewCustomerID = 0;
			}
			genericSendDate = null;
			if ((tmp = opts.get ("preview-senddate")) != null) {
				long	epoch = obj2long (tmp, "preview-senddate");

				if (epoch > 0) {
					genericSendDate = new Date (epoch * 1000);
					logging(Log.DEBUG, "options2", "--> preview-senddate = " + epoch + " (" + genericSendDate.toString () + ")");
				}
			}
				
			if ((previewOutput = (Page) opts.get("preview-output")) != null) {
				logging(Log.DEBUG, "options2", "--> preview-output = " + previewOutput);
			}
			if ((tmp = opts.get("preview-anon")) != null) {
				previewAnon = obj2bool(tmp, "preview-anon");
				logging(Log.DEBUG, "options2", "--> preview-anon = " + previewAnon);
			} else {
				previewAnon = false;
			}
			if ((tmp = opts.get("preview-anon-preserve-links")) != null) {
				previewAnonPreserveLinks = obj2bool(tmp, "preview-anon-preserve-links");
				logging(Log.DEBUG, "options2", "--> preview-anon-preserve-links = " + previewAnonPreserveLinks);
			} else {
				previewAnonPreserveLinks = false;
			}
			if ((previewSelector = (String) opts.get("preview-selector")) != null) {
				logging(Log.DEBUG, "options2", "--> preview-selector = " + previewSelector);
			}
			if ((tmp = opts.get("preview-cachable")) != null) {
				previewCachable = obj2bool(tmp, "preview-cachable");
				logging(Log.DEBUG, "options2", "--> preview-cachable = " + previewCachable);
			} else {
				previewCachable = true;
			}
			if ((tmp = opts.get("preview-target-ids")) != null) {
				previewTargetIDs = (long[]) tmp;
				for (long previewTargetID : previewTargetIDs) {
					logging(Log.DEBUG, "options2", "--> preview-target-id = " + previewTargetID);
				}
			} else {
				previewTargetIDs = null;
			}
			if ((tmp = opts.get("preview-convert-entities")) != null) {
				previewConvertEntities = obj2bool(tmp, "preview-convert-entities");
				logging(Log.DEBUG, "options2", "--> preview-convert-entities = " + previewConvertEntities);
			} else {
				previewConvertEntities = false;
			}
			if ((tmp = opts.get("preview-ecs-uids")) != null) {
				previewEcsUIDs = obj2bool(tmp, "preview-ecs-uids");
				logging(Log.DEBUG, "options2", "--> preview-ecs-uids = " + previewEcsUIDs);
			} else {
				previewEcsUIDs = false;
			}

			long now = System.currentTimeMillis() / 1000;
			if ((tmp = opts.get("send-date")) != null) {
				currentSendDate = obj2date(tmp, "send-date");
				sendSeconds = currentSendDate.getTime() / 1000;
				if (sendSeconds < now) {
					sendSeconds = now;
				}
				logging(Log.DEBUG, "options2", "--> send-date = " + currentSendDate);
			} else {
				currentSendDate = currentSendDate (maildropStatus.sendDate());
			}
			if (currentSendDate != null) {
				sendSeconds = currentSendDate.getTime() / 1000;
				if (sendSeconds < now) {
					sendSeconds = now;
				}
			} else {
				sendSeconds = now;
			}
			currentSendDate = new Date(sendSeconds * 1000);

			if ((tmp = opts.get("step")) != null) {
				mailing.stepping(obj2int(tmp, "step"));
				logging(Log.DEBUG, "options2", "--> step = " + tmp);
			}
			if ((tmp = opts.get("block-size")) != null) {
				mailing.blockSize(obj2int(tmp, "block-size"));
				logging(Log.DEBUG, "options2", "--> block-size = " + tmp);
			}
			if (bcc != null) {
				if (bccBaseIndex > 0) {
					while (bcc.size() > bccBaseIndex) {
						bcc.remove(bccBaseIndex);
					}
				} else {
					bcc.clear();
				}
			}
			if ((tmp = opts.get("bcc")) != null) {
				for (String recv : (List<String>) tmp) {
					addBcc(recv);
					logging(Log.DEBUG, "options2", "--> bcc = " + recv);
				}
			}
			if ((overwriteMap = mormalizeReplacementMap(opts.get("overwrite"))) != null) {
				logging(Log.DEBUG, "options2", "--> overwrite = " + overwriteMap);
			}
			if ((virtualMap = mormalizeReplacementMap(opts.get("virtual"))) != null) {
				logging(Log.DEBUG, "options2", "--> virtual = " + virtualMap);
			}
			if ((staticMap = toMap (opts.get("static"))) != null) {
				logging(Log.DEBUG, "options2", "--> static = " + staticMap);
			}

			String targetInformation;
			if ((staticMap != null) && ((targetInformation = (String) staticMap.get("_tg")) != null)) {
				targetExpression.setEvaluatedValues(targetInformation);
			} else {
				targetExpression.clearEvaluatedValues();
			}
			break;
		}
	}

	private Map<String, String> mormalizeReplacementMap(Object source) {
		Map <String, Object>	sourceMap = toMap (source);
		
		if (sourceMap == null) {
			return null;
		}
		
		Map<String, String> result = new HashMap<>(sourceMap.size());

		for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
			String	key = entry.getKey().toLowerCase();
			Object	value = entry.getValue();
			
			if (value == null) {
				result.put (key, "");
			} else if (value instanceof String) {
				result.put (key, (String) value);
			} else {
				result.put (key, value.toString ());
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private Map <String, Object> toMap (Object source) {
		if (source == null) {
			return null;
		}
		if (source instanceof String) {
			Map <String, Object>	result = new HashMap <> ();
			JsonFactory		factory = new JsonFactory ();
			
			try (JsonParser parser = factory.createParser ((String) source)) {
				int	state = 0;
				String	field = null;
				
				while (! parser.isClosed ()) {
					JsonToken	token = parser.nextToken ();
					
					if (state == 0) {
						switch (token) {
						case START_OBJECT:
							state = 1;
							break;
						default:
							throw new IllegalStateException ();
						}
					} else if (state == 1) {
						switch (token) {
						case END_OBJECT:
							state = -1;
							break;
						case FIELD_NAME:
							state = 2;
							field = parser.getValueAsString ();
							if (field == null) {
								throw new IllegalArgumentException ();
							}
							break;
						default:
							throw new IllegalStateException ();
						}
					} else if (state == 2) {
						String	value;
						
						switch (token) {
						case VALUE_FALSE:
							value = "false";
							break;
						case VALUE_TRUE:
							value = "true";
							break;
						case VALUE_NULL:
							value = "";
							break;
						case VALUE_NUMBER_FLOAT:
						case VALUE_NUMBER_INT:
						case VALUE_STRING:
							value = parser.getValueAsString ();
							break;
						default:
							throw new IllegalArgumentException ();
						}
						result.put (field, value);
						state = 1;
					}
				}
			} catch (Exception e) {
				logging (Log.WARNING, "toMap", "failed to parse \"" + source + "\": " + e.toString ());
			}
			return result;
		}
		return (Map <String, Object>) source;
	}

	public List<String> bcc() {
		return bcc;
	}

	private void addBcc(String recv) {
		if ((recv != null) && (!maildropStatus.isAdminMailing()) && (!maildropStatus.isTestMailing()) && (!maildropStatus.isPreviewMailing()) && (!maildropStatus.isVerificationMailing())) {
			recv = recv.trim();
			if (recv.length() > 0) {
				if (bcc == null) {
					bcc = new ArrayList<>();
				}
				bcc.add(recv);
			}
		}
	}

	public boolean useMultipleRecords() {
		if (references != null) {
			for (Reference r : references.values()) {
				if (r.isMulti()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Should we use this record, according to our virtual data?
	 *
	 * @return true if we should
	 */
	public boolean useRecord(Long cid) {
		return true;
	}

	/**
	 * Optional initialization for virtual data
	 *
	 * @param column the column to initialize
	 */
	public void initializeVirtualData(String column) {
		// nothing to do
	}

	/**
	 * Do we have data available to overwrite columns?
	 *
	 * @return true in this case
	 */
	public boolean overwriteData() {
		return overwriteMap != null;
	}

	/**
	 * Find entry in map for overwrite/virtual records
	 *
	 * @param map     optional simple hash table
	 * @param colname the name of the column
	 * @return the found string or null
	 */
	private String findInMap(Map<String, String> map, String colname) {
		if ((map != null) && map.containsKey(colname)) {
			return map.get(colname);
		}
		return null;
	}

	/**
	 * Find an overwrite column
	 *
	 * @param colname the name of the column
	 * @return the found string or null
	 */
	public String overwriteData(String colname) {
		return findInMap(overwriteMap, colname);
	}

	/**
	 * Find a virtual column
	 *
	 * @param colname the name of the column
	 * @return the found string or null
	 */
	public String virtualData(String colname) {
		return findInMap(virtualMap, colname);
	}
	
	public Map <String, String> virtualMap () {
		return virtualMap;
	}

	public String getBindingTable() {
		return bigClause.getBindingTable();
	}

	public String getBindingQuery() {
		return bigClause.getBindingQuery();
	}

	public String createSimpleClause(String reduction) {
		return bigClause.createSimpleClause("cust", reduction);
	}

	public String createSimpleClause() {
		return createSimpleClause(null);
	}

	/**
	 * If we have further restrictions due to reference mailing
	 *
	 * @return extra subsulect or null
	 */
	public String getFollowupSubselect() {
		return followupRefSQL;
	}

	/**
	 * If we have further restrictions due to selected media
	 *
	 * @return extra subsulect or null
	 */
	public String getMediaSubselect() {
		return mediaRestrictSQL;
	}

	/**
	 * Returns a default image link for a generic picture
	 *
	 * @param name   the image name
	 * @param source the source to get the image from
	 * @param use    marks if this request comes for an actual usage of the image
	 * @return the created link
	 */
	public String defaultImageLink(String name, String source, boolean use) {
		boolean nocache = maildropStatus.isPreviewMailing() && (!previewCacheImages);
		return defaultImageLink(name, source, use, nocache);
	}

	protected String defaultImageLink(String name, String source, boolean use, boolean nocache) {
		String	filename;
		Image	image = null;
		String	template = null;

		if (name == null) {
			return name;
		}

		try {
			filename = URLEncoder.encode(name, "UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			filename = name;
		}
		setupImagepool();
		if (source == null) {
			source = imagepool.findSourceFor(name);
		} else {
			source = source.toLowerCase();
		}
		switch (source) {
			default:
				break;
			case Imagepool.MAILING:
				template = nocache ? imageTemplateNoCache : imageTemplate;
				image = imagepool.findImage(name, filename, isMobilePreview);
				break;
			case Imagepool.MEDIAPOOL:
			case Imagepool.MEDIAPOOL_BACKGROUND:
				template = nocache ? mediapoolTemplateNoCache : mediapoolTemplate;
				image = imagepool.findMediapoolImage(name, filename, isMobilePreview);
				break;
		}
		if ((image != null) && (template != null)) {
			String link = template.replace("[name]", image.filename());

			if (use) {
				imagepool.markInUse(name, source, image, link);
			}
			return link;
		}
		return name;
	}

	public List<BlockData> getUsedMediapoolImages() throws SQLException {
		setupImagepool();
		return imagepool.getMediapoolImagesUsed();
	}

	public Set<String> getUsedComponentImages() {
		setupImagepool();
		return imagepool.getComponentsUsed();
	}

	/**
	 * Mark a filename to be removed during cleanup phase
	 *
	 * @param fname the filename
	 */
	public void markToRemove(String fname) {
		if (toRemove == null) {
			toRemove = new ArrayList<>();
		}
		if (!toRemove.contains(fname)) {
			toRemove.add(fname);
		}
	}

	/**
	 * Mark a file to be removed during cleanup
	 *
	 * @param file a File instance for the file to be removed
	 */
	public void markToRemove(File file) {
		markToRemove(file.getAbsolutePath());
	}

	/**
	 * Unmark a filename to be removed, if we already removed
	 * it by hand
	 *
	 * @param fname the filename
	 */
	public void unmarkToRemove(String fname) {
		if ((toRemove != null)) {
			toRemove.remove(fname);
		}
	}

	/**
	 * Unmark a file to be removed
	 *
	 * @param file a File instance
	 */
	public void unmarkToRemove(File file) {
		unmarkToRemove(file.getAbsolutePath());
	}

	/**
	 * Check if we have to write logging for a given loglevel
	 *
	 * @param loglvl the loglevel to check against
	 * @return true if we should log
	 */
	public boolean islog(int loglvl) {
		return log.islog(loglvl);
	}

	/**
	 * Write entry to logfile
	 *
	 * @param loglvl the level to report
	 * @param mid    the ID of the message
	 * @param msg    the message itself
	 */
	public void logging(int loglvl, String mid, String msg, Throwable th) {
		if (log != null) {
			if (lid != null) {
				if (mid != null) {
					mid = mid + "/" + lid;
				} else {
					mid = lid;
				}
			}
			if (msg != null) {
				log.out(loglvl, mid, msg);
			}

			Set<Throwable> seen = new HashSet<>();
			while (th != null) {
				StackTraceElement[] elements = th.getStackTrace();

				if (elements != null) {
					log.out(loglvl, mid, "Stacktrace for " + th.toString() + ":");
					for (StackTraceElement element : elements) {
						log.out(loglvl, mid, " at " + element.toString());
					}
				}
				seen.add(th);
				th = th.getCause();
				if ((th != null) && seen.contains(th)) {
					log.out(loglvl, mid, " ... recursive stack trace detected, aborting");
				}
			}
		}
	}

	public void logging(int loglvl, String mid, String msg) {
		logging(loglvl, mid, msg, null);
	}

	public void logging(String name, String msg) {
		if (log != null) {
			log.out(name, msg);
		}
	}

	/**
	 * Returns currently active logger
	 *
	 * @return active logger instance
	 */
	public Log getLogger() {
		return log;
	}
	
	public boolean shouldRemoveDuplicateEMails() {
		return removeDuplicateEMails && (!maildropStatus.isAdminMailing()) && (!maildropStatus.isTestMailing()) && (!maildropStatus.isPreviewMailing()) && (!maildropStatus.isVerificationMailing());
	}

	public String tempTablespace() {
		return dbHasTablespaces ? dbTempTablespace : null;
	}

	public int licenceID() {
		return licenceID;
	}

	private void iniValidate(String value, String name) {
		if (value == null) {
			logging(Log.ERROR, "config", "mailout:" + name + " is unset");
		}
	}

	public String dbDriver() {
		iniValidate(dbDriver, "db_driver");
		return dbDriver;
	}

	/**
	 * returns the database login
	 *
	 * @return login string
	 */
	public String dbLogin() {
		iniValidate(dbLogin, "db_login");
		return dbLogin;
	}

	/**
	 * returns the database password
	 *
	 * @return password string
	 */
	public String dbPassword() {
		iniValidate(dbPassword, "db_password");
		return dbPassword;
	}

	/**
	 * returns the connection string for the database
	 *
	 * @return connection string
	 */
	public String dbConnect() {
		iniValidate(dbConnect, "db_connect");
		return dbConnect;
	}

	public boolean isDirect() {
		return directPath && maildropStatus.isCampaignMailing() && (mailing.stepping() == 0) && (sendSeconds <= System.currentTimeMillis() / 1000 + 1);
	}

	/**
	 * returns the directory to write meta files to
	 *
	 * @return path to meta
	 */
	public String targetPath() {
		if (isDirect()) {
			return mailing.outputDirectory("direct");
		}
		return mailing.outputDirectory("meta");
	}

	/**
	 * returns the path to xmlback program
	 *
	 * @return path to xmlback
	 */
	public String xmlBack() {
		return xmlBack;
	}

	/**
	 * returns wether we should validate generated XML files
	 *
	 * @return true if validation should take place
	 */
	public boolean xmlValidate() {
		return xmlValidate;
	}

	/**
	 * returns the optional used sample receivers
	 *
	 * @return receiver list
	 */
	public String sampleEmails() {
		return sampleEmails;
	}
	/**
	 * returns the emails to be used by delivery check
	 */
	public String deliveryCheckEmails () {
		return company.isPermitted ("mia.premium") ? deliveryCheckEmails : null;
	}
	
	/*
	 * returns the pre delivery check address
	 */
	public String preDeliveryCheckEmails () {
		return preDeliveryCheckEmails;
	}

	/**
	 * if this is a dryrun test run
	 *
	 * @return true, if it is a dryrun
	 */
	public boolean isDryRun() {
		return maildropStatus.isTestMailing() && (workStatus != null) && workStatus.equals(MailingStatus.TEST.getDbKey()) && isWorkflowMailing;
	}

	public String getDefaultMediaType() {
		return maildropStatus != null && (maildropStatus.isCampaignMailing() || maildropStatus.isVerificationMailing()) ? Media.typeName(Media.TYPE_EMAIL) : null;
	}

	public long limitBlockOperations() {
		return limitBlockOperations;
	}

	public long limitBlockOperationsMax() {
		return limitBlockOperationsMax;
	}

	public long limitBlockChunks() {
		if (maildropStatus.isWorldMailing() || maildropStatus.isRuleMailing() || maildropStatus.isOnDemandMailing()) {
			long limit = limitBlockOperations();
			long limitMax = limitBlockOperationsMax();
			long chunks = limit > 0 ? totalReceivers / limit + 1 : 1;

			return (limitMax > 0 && chunks > limitMax) ? limitMax : chunks;
		}
		return 1;
	}
	
	public boolean enforceTestVIP () {
		return enforceTestVIP;
	}
	
	public boolean bounceBleed () {
		return bounceBleed;
	}
	
	public int bounceBleedDuration () {
		return bounceBleedDuration;
	}
	
	public boolean vouchersMandatory () {
		return vouchersMandatory;
	}

	protected Map <String, String> retrieveOverwrittenTestRecipientColumns () {
		Map <String, String>	rc = null;
		long			sourceCustomerID = maildropStatus.overwriteTestRecipient ();

		if (sourceCustomerID > 0) {
			rc = new HashMap <> ();
			List <Media> currentMedia = media ();
			
			if ((currentMedia != null) && (currentMedia.size () > 0)) {
				try (DBase.With with = dbase.with ()) {
					String	query = 
						"SELECT " + (currentMedia.stream ()
							.map (m -> m.profileField ())
							.filter (f -> f != null)
							.map (f -> f.toLowerCase ())
							.reduce ((s, e) -> s + ", " + e)
							.orElse ("NULL")
						) + " FROM customer_" + company.id () + "_tbl " +
						"WHERE customer_id = :customer_id";
					
					Map <String, Object> row = dbase.querys (with.cursor (), query, "customer_id", sourceCustomerID);
				
					if (row != null) {
						for (Media m : currentMedia) {
							if (m.profileField () != null) {
								rc.put (m.profileField (), dbase.asString (row.get (m.profileField ().toLowerCase ())));
							}
						}
					}
				} catch (SQLException e) {
					logging (Log.ERROR, "test", "Failed to retreive media information for customer " + sourceCustomerID + ": " + e.toString ());
				}
			}
		}
		return rc;
	}

	/**
	 * Set standard field to be retrieved from database
	 *
	 * @param predef the hashset to store field name to
	 */
	private void setStandardFields(Set<String> predef, Map<String, EMMTag> tags) {
		collectMediatypes(predef);
		predef.add(RecipientStandardField.DoNotTrack.getColumnName());
		targetExpression.requestFields(predef);
		for (EMMTag tag : tags.values()) {
			try {
				tag.requestFields(this, predef);
			} catch (Exception ex) {
				logging(Log.ERROR, "tag", "Failed to get required fields for tag " + tag.mTagFullname + ": " + ex.toString(), ex);
			}
		}
		String custom = company.info("preselect-columns", mailing.id());

		if (custom != null) {
			for (String c : custom.split(", *")) {
				if (c.length() > 0) {
					predef.add(c.toLowerCase());
				}
			}
		}

		if (dkimActive) {
			String dkimColumn = company.info("dkim-select-column");
			if ((dkimColumn != null) && (columnByName(dkimColumn) != null)) {
				dkimColumn = dkimColumn.toLowerCase();
				company.infoAdd("_dkim_column", dkimColumn);
				predef.add(dkimColumn);
			}
		}

		if (extendURL != null) {
			extendURL.requestFields(predef);
		}

		if (references != null) {
			for (Reference r : references.values()) {
				Column c = columnByName(r.name(), Reference.multiID);

				if (c != null) {
					predef.add(c.getQname());
					r.multi();
				}
			}
		}
		if (URLlist != null) {
			for (URL url : URLlist) {
				StringOps.findColumnsInHashtags(url.getUrl()).stream().forEach((e) -> {
					Column c = columnByName(e);

					if (c != null) {
						url.addColumn(c);
					}
				});
				url.requestFields(this, predef);
			}
		}
		predef.stream()
				.map((s) -> s.startsWith("cust.") ? s.substring(5) : s)
				.filter((s) -> columnByName(s) == null)
				.forEach((s) -> logging(Log.WARNING, "fields", "Unable to resolve \"" + s + "\""));
	}

	protected void collectMediatypes(Set<String> predef) {
		for (Media m : media()) {
			String profileField = m.profileField();

			if (profileField != null) {
				predef.add(profileField.toLowerCase());
			}
		}
	}

	/**
	 * Set standard columns, if they are not already found in database
	 *
	 * @param use already used column names
	 */
	public void setUsedFieldsInLayout(BlockCollection allBlocks, Map<String, EMMTag> tags) {
		int sanity = 0;
		Set<String> predef;
		Set<String> use = allBlocks.getUsedColumns();

		if (use != null) {
			predef = new HashSet<>(use);
		} else {
			predef = new HashSet<>();
		}
		setStandardFields(predef, tags);
		for (Column c : layout) {
			if (predef.contains(c.getQname())) {
				if (!c.getInuse()) {
					c.setInuse(true);
					++lusecount;
				}
				++sanity;
			} else {
				if (c.getInuse()) {
					c.setInuse(false);
					--lusecount;
				}
			}
		}
		if (sanity != lusecount) {
			logging(Log.ERROR, "layout", "Sanity check failed in setUsedFieldsInLayout");
		}
	}

	/**
	 * find a column by its alias
	 *
	 * @param alias
	 * @return the column on success, null otherwise
	 */
	public Column columnByAlias(String alias) {
		for (Column c : layout) {
			String calias = c.getAlias();

			if ((calias != null) && calias.equalsIgnoreCase(alias)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * find a column by its name
	 *
	 * @param name
	 * @return the column on success, null otherwise
	 */
	public Column columnByName(String ref, String name) {
		int n;

		if ((ref == null) && ((n = name.indexOf('.')) != -1)) {
			if (references == null) {
				return null;
			}
			ref = name.substring(0, n);
			name = name.substring(n + 1);

			Reference r = references.get(ref.toLowerCase());

			if ((r == null) || (!fullfillReference(r))) {
				return null;
			}
		}
		for (Column c : layout) {
			if (c.match(ref, name)) {
				return c;
			}
		}
		return null;
	}

	public Column columnByName(String name) {
		return columnByName(null, name);
	}

	public Column columnByIndex(int idx) {
		return (idx >= 0) && (idx < lcount) ? layout.get(idx) : null;
	}

	/**
	 * return the name of the column at a given position
	 *
	 * @param col the position in the column layout
	 * @return the column name
	 */
	public String columnName(int col) {
		return layout.get(col).getName();
	}

	/**
	 * return the type as string of the column at a given position
	 *
	 * @param col the position in the column layout
	 * @return the column type as string
	 */
	public String columnTypeStr(int col) {
		return layout.get(col).typeStr();
	}

	/**
	 * Set a column from a result set
	 *
	 * @param col   the position in the column layout
	 * @param rset  the result set
	 * @param index position in the result set
	 */
	public void columnSet(int col, ResultSet rset, int index) {
		layout.get(col).set(rset, index);
	}

	/**
	 * Get a value from a column
	 *
	 * @param col the position in the column layout
	 * @return the contents of that column
	 */
	public String columnGetStr(int col) {
		return layout.get(col).get();
	}

	/**
	 * Check wether a columns value is NULL
	 *
	 * @param col the position in the column layout
	 * @return true of column value is NULL
	 */
	public boolean columnIsNull(int col) {
		return layout.get(col).isnull();
	}

	/**
	 * Check wether a column is in use
	 *
	 * @param col the position in the column layout
	 * @return true if column is in use
	 */
	public boolean columnUse(int col) {
		return layout.get(col).getInuse();
	}

	public void columnUpdateQueryIndex(Column c, int index) {
		String ref = c.getRef();
		Reference r;

		if ((ref != null) && ((r = references.get(ref)) != null)) {
			r.setIdIndex(c.getName(), index);
		}
	}

	/**
	 * create a RFC compatible Date: line
	 *
	 * @param ts the input time
	 * @return the RFC representation
	 */
	public String RFCDate(Date ts) {
		SimpleDateFormat fmt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", new Locale("en"));
		
		fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		return fmt.format(ts != null ? ts : new Date ());
	}

	/**
	 * Optional string to add to filename generation
	 *
	 * @return optional string
	 */
	public String getFilenameDetail() {
		return "-" + licenceID;
	}

	public String getFilenameCompanyID() {
		return Long.toString(company.id());
	}

	public String getFilenameMailingID() {
		if (inc == 0) {
			inc = getIncarnation();
		}
		return inc + maildropStatus.statusField () + mailing.id ();
	}
	
	public Date currentSendDate (Date sendDate) {
		Date	now = new Date ();
		
		return (sendDate == null) || sendDate.before (now) ? now : sendDate;
	}
	
	public Date genericSendDate () {
		if (genericSendDate != null) {
			return genericSendDate;
		}
		if (maildropStatus.isCampaignMailing () || maildropStatus.isRuleMailing () || maildropStatus.isOnDemandMailing ()) {
			return currentSendDate;
		}
		return maildropStatus.genericSendDate (mailing.planDate ());
	}

	public String ahvTable() {
		return "ahv_" + company.id() + "_tbl";
	}

	private Boolean ahvEnabled = null;
	private Date ahvOldest = null;

	public boolean ahvEnabled() {
		if (ahvEnabled == null) {
			try {
				if ((maildropStatus.isCampaignMailing() ||
				     maildropStatus.isRuleMailing() ||
				     maildropStatus.isOnDemandMailing() ||
				     maildropStatus.isWorldMailing()) &&
				    Str.atob(company.info("ahv:is-enabled", mailing.id())) &&
				    dbase.tableExists(ahvTable())) {
					ahvEnabled = true;

					long maxAge = Str.atol(company.info("ahv:max-age"), 180);

					if (maxAge > 0) {
						long ref = (currentSendDate != null ? currentSendDate : new Date()).getTime();

						ahvOldest = new Date(ref - maxAge * 24 * 60 * 60 * 1000);
						logging(Log.DEBUG, "ahv", "Oldest entries to reactivate: " + ahvOldest.toString() + " due to maximum age of " + maxAge + " days");
					}
				}
			} catch (Exception e) {
				logging(Log.ERROR, "ahv", "Failed in setup AHV: " + e.toString(), e);
			}
			if (ahvEnabled == null) {
				ahvEnabled = false;
			}
		}
		return ahvEnabled;
	}

	public Date ahvOldestEntryToReactivate() {
		return ahvOldest;
	}

	public String omgTable() {
		return "omg_" + company.id() + "_tbl";
	}

	private Boolean omgEnabled = null;
	private Long omgTimediff = null;
	private int omgOffset = 15;

	public boolean omgEnabled() {
		if (omgEnabled == null) {
			omgEnabled = false;
			try {
				if ((maildropStatus.isRuleMailing() ||
				     maildropStatus.isWorldMailing()) &&
				    (maildropStatus.optimizeMailGeneration() != null) &&
				    Str.atob(company.info("omg:is-enabled", mailing.id())) &&
				    dbase.tableExists(omgTable())) {
					omgEnabled = true;
					omgOffset = Str.atoi(company.info("omg:offset", mailing.id()), omgOffset);
				}
			} catch (Exception e) {
				logging(Log.ERROR, "omg", "Failed to setup omg: " + e.toString(), e);
			}
		}
		return omgEnabled;
	}

	public void getControlColumns(StringBuffer selectString) {
		if (omgEnabled()) {
			selectString.append(", bind.omg");
		}
		for (Target t : targetExpression.resolveByDatabase()) {
			if (t.hasEvaluatedValue()) {
				selectString.append(", " + (t.evaluatedValue() ? "1" : "0") + " AS agn_tg_" + t.getID());
			} else {
				selectString.append(", agn_tg_" + t.getID() + ".value AS agn_tg_" + t.getID());
			}
		}
	}

	public int useControlColumns(Custinfo cinfo, ResultSet rset, int startIndex) throws Exception {
		if (omgEnabled()) {
			long timediff = rset.getLong(startIndex++);
			String usertype = cinfo.getUserType();

			if (((omgTimediff == null) || (timediff > omgTimediff)) && WORLD.getCodeString().equals(usertype)) {
				forceNewBlock = true;
				omgTimediff = timediff;
			}
		}
		return startIndex;
	}

	public List<String> getReduction() {
		List<String> rc = new ArrayList<>();

		if (maildropStatus.isAdminMailing() || maildropStatus.isTestMailing()) {
			String sql = maildropStatus.getAdminTestSQL();

			if (sql != null) {
				rc.add(sql);
			}
			sql = targetExpression.altgselect ();
			if (sql != null) {
				rc.add (sql);
			}
		}
		if (maildropStatus.isWorldMailing() || maildropStatus.isRuleMailing() || maildropStatus.isOnDemandMailing()) {
			String	mailingTypeName;
			
			if (mailingType == MailingType.NORMAL.getCode ()) {
				mailingTypeName = "world";
			} else if (mailingType == MailingType.ACTION_BASED.getCode ()) {
				mailingTypeName = "action";
			} else if (mailingType == MailingType.DATE_BASED.getCode ()) {
				mailingTypeName = "date";
			} else if (mailingType == MailingType.FOLLOW_UP.getCode ()) {
				mailingTypeName = "followup";
			} else if (mailingType == MailingType.INTERVAL.getCode ()) {
				mailingTypeName = "interval";
			} else {
				mailingTypeName = null;
			}
			Set <String> seen = new HashSet <> ();

			for (String extra : new String[]{"exact", mailingTypeName, maildropStatus.statusField (), ""}) {
				if (extra != null) {
					String	key = "fixed-target-clause";
					String	expr;
					
					if (extra.equals ("exact")) {
						expr = company.infoSubstituted (key + "[" + mailing.id () + "]");
					} else if (extra.length () > 0) {
						expr = company.infoSubstituted (key + "-" + extra.toLowerCase ());
					} else {
						expr = company.infoSubstituted (key);
					}
					if ((expr != null) && (! seen.contains (expr))) {
						seen.add (expr);
						logging (Log.INFO, "target", "Using fixed target clause " + (extra.length () > 0 ? "(using " + extra + ")" : "") + ": \"" + expr + "\"");
						if (! expr.equals ("-")) {
							rc.add (expr);
						}
					}
				}
			}
		}
		if (maildropStatus.isWorldMailing()) {
			String column = company.infoSubstituted("column-restrict-sending-per-day", mailing.id());

			if ((column != null) && (!column.equals("-"))) {
				if (column.indexOf('.') == -1) {
					column = "cust." + column;
				}

				StringBuffer buf = new StringBuffer();
				Calendar now = Calendar.getInstance();
				int wday = now.get(Calendar.DAY_OF_WEEK);

				buf.append(column + " IS NULL OR " + column + " LIKE '");
				for (int day = 0; day < 7; ++day) {
					int cmp = -1;

					switch (day) {
						case 0:
							cmp = Calendar.MONDAY;
							break;
						case 1:
							cmp = Calendar.TUESDAY;
							break;
						case 2:
							cmp = Calendar.WEDNESDAY;
							break;
						case 3:
							cmp = Calendar.THURSDAY;
							break;
						case 4:
							cmp = Calendar.FRIDAY;
							break;
						case 5:
							cmp = Calendar.SATURDAY;
							break;
						case 6:
							cmp = Calendar.SUNDAY;
							break;
						default:
							break;
					}
					buf.append(cmp == wday ? '1' : '_');
				}
				buf.append("'");
				rc.add(buf.toString());
			}
		}
		if (sendEncrypted && (maildropStatus.isWorldMailing() || maildropStatus.isRuleMailing() || maildropStatus.isOnDemandMailing() || maildropStatus.isCampaignMailing ())) {
			String	expr = company.infoSubstituted ("encrypted-sending-clause", mailing.id ());
			
			rc.add (expr != null ? expr : "cust.sys_encrypted_sending = 1");
		}
		return rc;
	}

	public void setBlocks(BlockCollection bc) {
		blocks = bc;
	}

	public BlockCollection getBlocks() {
		return blocks;
	}

	public void setupImagepool() {
		if (imagepool == null) {
			imagepool = new Imagepool(this);
			if (blocks != null) {
				for (int n = 0; n < blocks.blockCount(); ++n) {
					imagepool.addImage(blocks.getBlock(n), true);
				}
			}
		}
	}

	public BlockData requestImage(String imageName, byte[] imageDump) {
		BlockData rc = null;

		if (blocks != null) {
			setupImagepool();
			if (blockTable == null) {
				blockTable = new HashMap<>();

				for (int n = 0; n < blocks.blockCount(); ++n) {
					BlockData b = blocks.getBlock(n);

					if (b.type == BlockData.RELATED_BINARY) {
						blockTable.put(b.cid, b);
					}
				}
			}
			rc = blockTable.get(imageName);
			if (rc == null) {
				try {
					rc = blocks.newImage(imageName, imageDump);
					if (rc != null) {
						blockTable.put(imageName, rc);
						imagepool.addImage(rc, true);
					}
				} catch (Exception e) {
					logging(Log.ERROR, "rqimg", "Failed to create new image: " + e.toString(), e);
				}
			}
		}
		return rc;
	}

	private static Object lock = new Object();

	@DaoUpdateReturnValueCheck
	public URL requestURL(String rqurl, String name, boolean isAdminLink) {
		URL url;

		if (URLlist == null) {
			URLlist = new ArrayList<>();
		}
		if (URLTable == null) {
			URLTable = new HashMap<>();

			for (int n = 0; n < URLlist.size (); ++n) {
				url = URLlist.get(n);
				URLTable.put(url.getUrl(), url);
			}
		}
		url = URLTable.get(rqurl);
		if (url == null) {
			synchronized (lock) {
				String query = null;
				
				try (DBase.With with = dbase.with ()) {
					long	urlID = 0;
					int	measuredSeparately = Str.atob (company.info ("mailings.individualLinkTracking"), false) ? 1 : 0;
					
					for (int state = 0; (urlID == 0) && (state < 2); ++state) {
						List<Map<String, Object>> rq;
						Map<String, Object> row;

						query = "SELECT url_id FROM rdir_url_tbl WHERE mailing_id = :mailingID AND company_id = :companyID AND full_url = :fullURL AND (deleted IS NULL OR deleted = 0)";
						rq = dbase.query(with.cursor (), query, "mailingID", mailing.id(), "companyID", company.id(), "fullURL", rqurl);
						if (rq.size() > 0) {
							row = rq.get(0);
							urlID = dbase.asLong(row.get("url_id"));
						}
						if ((urlID == 0) && (state == 0)) {
							if (name != null && name.length() > 1000) {
								throw new Exception("Value for rdir_url_tbl.shortname is to long (Maximum: 1000, Current: " + name.length() + ")");
							}
							if (dbase.isOracle()) {
								query = "SELECT rdir_url_tbl_seq.nextval FROM dual";
								urlID = dbase.queryLong(with.cursor (), query);
								if (urlID > 0) {
									query = "INSERT INTO rdir_url_tbl " +
										"(url_id, full_url, mailing_id, company_id, usage, action_id, shortname, deep_tracking, alt_text, extend_url, admin_link, from_mailing, measured_separately) " +
										"VALUES " +
										"(:urlID, :fullURL, :mailingID, :companyID, :measure, :actionID, :shortname, :deepTracking, :altText, :extendURL, :adminLink, :fromMailing, :measuredSeparately)";
									dbase.update(with.cursor (), query,
										     "urlID", urlID,
										     "fullURL", rqurl,
										     "mailingID", mailing.id(),
										     "companyID", company.id(),
										     "measure", 3, "actionID", 0,
										     "shortname", (name == null ? "auto generated" : name),
										     "deepTracking", 0,
										     "altText", null,
										     "extendURL", 0,
										     "adminLink", (isAdminLink ? 1 : 0),
										     "fromMailing", 1,
										     "measuredSeparately", measuredSeparately);
								}
							} else {
								query = "INSERT INTO rdir_url_tbl " + 
									"(full_url, mailing_id, company_id, `usage`, action_id, shortname, deep_tracking, alt_text, extend_url, admin_link, from_mailing, measured_separately) " +
									"VALUES " + 
									"(:fullURL, :mailingID, :companyID, :measure, :actionID, :shortname, :deepTracking, :altText, :extendURL, :adminLink, :fromMailing, :measuredSeparately)";
								dbase.update(with.cursor (), query,
									     "fullURL", rqurl,
									     "mailingID", mailing.id(),
									     "companyID", company.id(),
									     "measure", 3, "actionID", 0,
									     "shortname", (name == null ? "auto generated" : name),
									     "deepTracking", 0,
									     "altText", null,
									     "extendURL", 0,
									     "adminLink", (isAdminLink ? 1 : 0),
									     "fromMailing", 1,
									     "measuredSeparately", measuredSeparately);
								query = "SELECT last_insert_id()";
								urlID = dbase.queryLong(with.cursor (), query);
							}
							if (urlID == 0) {
								logging(Log.WARNING, "rqurl", "Failed to retrieve url_id for inserted missing URL " + rqurl + ": retry reading it");
							}
						}
					}
					if (urlID > 0) {
						url = new URL(urlID, rqurl, 3);
						url.setAdminLink(isAdminLink);
						URLlist.add(url);
						URLTable.put(rqurl, url);
						logging(Log.VERBOSE, "rqurl", "Added missing URL " + rqurl);
					} else {
						logging(Log.ERROR, "rqurl", "Failed to add missing URL " + rqurl);
					}
				} catch (Exception e) {
					logging(Log.ERROR, "rqurl", "Failed to insert new URL " + rqurl + " into database: " + e.toString() + (query != null ? " (query " + query + ")" : ""), e);
				}
			}
		}
		return url;
	}

	public SWYN getSWYN() {
		if (swyn == null) {
			swyn = new SWYN(this);
			swyn.setup();
		}
		return swyn;
	}
	
	/**
	 * write a log line which triggers an external process for further actions
	 * 
	 * @param name the name of the trigger to address
	 * @param options further options for the triggered method
	 */
	public void trigger (String name, Map <String, String> options) {
		String	info =
			"company-id=" + (company != null ? company.id () : 0) + ";" +
			"mailinglist-id=" + (mailinglist != null ? mailinglist.id () : 0) + ";" +
			"mailing-id=" + (mailing != null ? mailing.id () : 0) + ";" +
			"status-id=" + (maildropStatus != null ? maildropStatus.id () : 0);
		logging (Log.TRIGGER, "trigger", "--== trigger[" + name + ":" + info + "]: " + (options != null ? ParameterParser.buildParameterString (options) : ""));
	}
	public void trigger (String name) {
		trigger (name, null);
	}
}
