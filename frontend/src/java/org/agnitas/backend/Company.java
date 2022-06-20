/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.backend.dao.CompanyDAO;
import org.agnitas.util.Log;
import org.agnitas.util.Str;

/**
 * This class keeps track of all company related
 * configuration and parameter
 */
public class Company {
	/**
	 * refrence to global configuration
	 */
	private Data data;
	/**
	 * the company table company_id
	 */
	private long id;
	/**
	 * name of the company (for logfile display only)
	 */
	private String name;
	/**
	 * if this is derrivated company from another one
	 */
	private long baseID;
	/**
	 * the content of the company info table
	 */
	private Map<String, String> info;
	/**
	 * configuration from company table
	 */
	private boolean mailtracking;
	private boolean mailtrackingExtended;
	private String mailtrackingTable;
	private String rdirDomain;
	private String mailloopDomain;
	/**
	 * Secrect items from database
	 */
	private String secretKey;
	private long secretTimestamp;
	private long uidVersion;
	private int priorityCount;
	private String token;
	/**
	 * if we have a limited number of mails per day per recipient
	 */
	private String mailsPerDay;
	private String baseMailsPerDay;
	private Map<String, String> mpd;

	public Company(Data nData) {
		data = nData;
	}

	/**
	 * Cleanup open resources etc.
	 */
	public Company done() {
		return null;
	}

	public long id() {
		return id;
	}

	public void id(long nID) {
		if (nID != id) {
			id = nID;
			baseID = id;
		}
	}

	public long baseID() {
		return baseID;
	}

	public String name() {
		return name;
	}

	public boolean mailtracking() {
		return mailtracking;
	}

	public boolean mailtrackingExtended() {
		return mailtrackingExtended;
	}

	public String mailtrackingTable() {
		return mailtrackingTable;
	}

	public String rdirDomain() {
		return rdirDomain;
	}

	public String mailloopDomain() {
		return mailloopDomain;
	}

	public String secretKey() {
		return secretKey;
	}

	public long secretTimestamp() {
		return secretTimestamp;
	}

	public void secretTimestamp(long nSecretTimestamp) {
		secretTimestamp = nSecretTimestamp;
	}

	public long uidVersion() {
		return uidVersion;
	}

	public int priorityCount() {
		return priorityCount;
	}
	
	public String token () {
		return token;
	}

	private static final String mpdAll = "*";
	private static final String mpdTZ = "TZ";

	private void setupLimitMailsPerDay() {
		if (mpd == null) {
			mpd = new HashMap<>();
			if (mailsPerDay != null) {
				for (String elem : mailsPerDay.split("; *", 0)) {
					String[] vv = elem.split(" *: *", 2);

					if (vv.length == 2) {
						mpd.put(vv[0].toUpperCase(), vv[1]);
					} else {
						mpd.put(mpdAll, elem);
					}
				}
			}
		}
	}

	public int mailsPerDay(String mt) {
		setupLimitMailsPerDay();
		String val = mpd.get(mt);

		return val == null ? 0 : Str.atoi(val, 0);
	}

	public int mailsPerDayLocal() {
		return mailsPerDay(data.maildropStatus.statusField());
	}

	public int mailsPerDayGlobal() {
		return mailsPerDay(mpdAll);
	}

	public String mailsPerDayTZ() {
		setupLimitMailsPerDay();

		return mpd.get(mpdTZ);
	}

	public boolean limitMailsPerDay() {
		return mailsPerDayLocal() > 0 || mailsPerDayGlobal() > 0;
	}

	public String info(String key) {
		return info == null ? null : info.get(key);
	}

	public String info(String key, int index) {
		String ci;

		if ((ci = info(key + "[" + index + "]")) == null) {
			ci = info(key);
		}
		return ci;
	}

	public String info(String key, long index) {
		return info(key, (int) index);
	}

	public String infoSubstituted(String key, Map<String, String> extra, String defaultValue) {
		return data.substituteString(info(key), extra, defaultValue);
	}

	public String infoSubstituted(String key, Map<String, String> extra) {
		return infoSubstituted(key, extra, null);
	}

	public String infoSubstituted(String key) {
		return infoSubstituted(key, null, null);
	}

	public String infoSubstituted(String key, long index, Map<String, String> extra, String defaultValue) {
		String ci;

		if ((ci = infoSubstituted(key + "[" + index + "]", extra)) == null) {
			ci = infoSubstituted(key, extra, defaultValue);
		}
		return ci;
	}

	public String infoSubstituted(String key, long index, Map<String, String> extra) {
		return infoSubstituted(key, index, extra, null);
	}

	public String infoSubstituted(String key, long index, String defaultValue) {
		return infoSubstituted(key, index, null, defaultValue);
	}

	public String infoSubstituted(String key, long index) {
		return infoSubstituted(key, index, null, null);
	}

	public String infoSubstituted(String key, int index, Map<String, String> extra, String defaultValue) {
		return infoSubstituted(key, (long) index, extra, defaultValue);
	}

	public String infoSubstituted(String key, int index, Map<String, String> extra) {
		return infoSubstituted(key, (long) index, extra, null);
	}

	public String infoSubstituted(String key, int index, String defaultValue) {
		return infoSubstituted(key, (long) index, null, defaultValue);
	}

	public String infoSubstituted(String key, int index) {
		return infoSubstituted(key, (long) index, null, null);
	}

	public String[] infoList(String key, long index, Map<String, String> extra) {
		String[] rc = null;
		String ref;

		if (index != -1) {
			ref = infoSubstituted(key, index, extra);
		} else {
			ref = infoSubstituted(key, extra);
		}
		if (!key.endsWith("+")) {
			String add = infoSubstituted(key + "+", extra);

			if (add != null) {
				if (ref != null) {
					ref += ", " + add;
				} else {
					ref = add;
				}
			}
		}
		if (ref != null) {
			List<String> collect = new ArrayList<>();

			for (String elem : ref.split(", *")) {
				String value;

				if (index != -1) {
					value = infoSubstituted(elem, index, extra);
				} else {
					value = infoSubstituted(elem, extra);
				}
				if ((value != null) && (value.length() > 0)) {
					collect.add(value);
				}
			}
			rc = new String[collect.size()];
			for (int n = 0; n < collect.size(); ++n) {
				rc[n] = collect.get(n);
			}
		}
		return rc;
	}

	public void infoAdd(String key, String value) {
		if (info == null) {
			info = new HashMap<>();
		}
		info.put(key, value == null ? "" : value);
	}

	public boolean infoAvailable() {
		return info != null;
	}

	public Set<String> infoKeys() {
		return info != null ? info.keySet() : new HashSet<>();
	}

	public String infoValue(String key) {
		return info != null ? info.get(key) : null;
	}

	/**
	 * Write all company related settings to logfile
	 */
	public void logSettings() {
		data.logging(Log.DEBUG, "init", "\tcompany.id = " + id);
		if (name != null) {
			data.logging(Log.DEBUG, "init", "\tcompany.name = " + name);
		}
		data.logging(Log.DEBUG, "init", "\tcompany.rdirDomain = " + (rdirDomain == null ? "*unset*" : rdirDomain));
		data.logging(Log.DEBUG, "init", "\tcompany.mailloopDomain = " + (mailloopDomain == null ? "*unset*" : mailloopDomain));
		data.logging(Log.DEBUG, "init", "\tcompany.mailsPerDay = " + (mailsPerDay == null ? "*unset*" : mailsPerDay));
		data.logging(Log.DEBUG, "init", "\tcompany.baseID = " + baseID);
		data.logging(Log.DEBUG, "init", "\tcompany.baseMailsPerDay = " + (baseMailsPerDay == null ? "*unset*" : baseMailsPerDay));
		data.logging(Log.DEBUG, "init", "\tcompany.priorityCount = " + priorityCount);
		data.logging(Log.DEBUG, "init", "\tcompany.token = " + (token == null ? "*unset*" : token));
		data.logging(Log.DEBUG, "init", "\tcompany.mailtracking = " + mailtracking);
		data.logging(Log.DEBUG, "init", "\tcompany.mailtrackingExtended = " + mailtrackingExtended);
		data.logging(Log.DEBUG, "init", "\tcompany.mailtrackingTable = " + mailtrackingTable);
	}

	/**
	 * Configure from external resource
	 *
	 * @param cfg the configuration
	 */
	public void configure(Config cfg) {
		// nothing to do
	}

	/**
	 * Retrieves all company realted information from available resources
	 */
	public void retrieveInformation() throws Exception {
		CompanyDAO company = new CompanyDAO(data.dbase, id);

		if (company.companyID() == 0L) {
			throw new Exception("No database entry for companyID " + id + " found");
		}
		String status = company.status();
		if ((status == null) || (!status.equals("active"))) {
			throw new Exception("Abort creating mailing for inactive company: " + (status == null ? "*unset*" : status));
		}
		retrieveCompanyStaticConfiguration(company);
		retrieveCompanyDynamicConfiguration(company);
	}

	private void retrieveCompanyStaticConfiguration(CompanyDAO company) throws SQLException {
		name = company.shortName();
		mailtracking = company.mailTracking();
		mailtrackingExtended = company.mailTrackingExtended();
		mailtrackingTable = company.mailTrackingTable();
		secretKey = company.secretKey();
		uidVersion = company.uidVersion();
		rdirDomain = company.rdirDomain();
		mailloopDomain = company.mailloopDomain();
		baseID = company.companyBaseID();
		mailsPerDay = company.mailsPerDay();
		if ((id != baseID()) && (mailsPerDay == null)) {
			CompanyDAO baseCompany = company.baseCompany(data.dbase);

			if ((baseCompany.companyID() != 0L) && (baseCompany.mailsPerDay() != null)) {
				mailsPerDay = baseCompany.mailsPerDay();
			}
		}
		priorityCount = company.priorityCount();
		token = company.token ();
	}

	private void retrieveCompanyDynamicConfiguration(CompanyDAO company) {
		Map<String, String> companyInfo = company.info();

		if (companyInfo != null) {
			for (Map.Entry<String, String> kv : companyInfo.entrySet()) {
				String key = kv.getKey();
				String value = kv.getValue();
				if (!key.startsWith("_")) {
					infoAdd(key, value);
				}
			}
		}
		if ((rdirDomain == null) || ("".equals (rdirDomain))) {
			rdirDomain = Data.syscfg.get ("rdir-domain");
		}
		if ((mailloopDomain == null) || "".equals (mailloopDomain)) {
			mailloopDomain = Data.syscfg.get ("filter-name");
		}
	}
}
