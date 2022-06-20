/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend.dao;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.agnitas.backend.DBase;
import org.agnitas.backend.EMail;
import org.agnitas.backend.StringOps;
import org.agnitas.util.Log;
import org.apache.commons.codec.binary.Base64;

/**
 * Accesses all information for DKIM generation
 */
public class DkimDAO {
	public class DKIM {
		@SuppressWarnings("unused")
		private long id;
		private boolean local;
		private String key;
		private String domain;
		private String selector;
		private boolean ident;

		private DKIM(long nId, boolean nLocal, String nKey, String nDomain, String nSelector) {
			id = nId;
			local = nLocal;
			key = nKey;
			domain = nDomain;
			selector = nSelector;
			ident = false;
		}

		private boolean valid() {
			return (key != null) && (domain != null) && (selector != null);
		}

		private boolean local() {
			return local;
		}

		private boolean match(EMail email) {
			if ((email != null) && (email.domain != null)) {
				return domain.equalsIgnoreCase(email.domain);
			}
			return false;
		}

		private boolean matchSubdomain(EMail email) {
			if ((email != null) && (email.domain != null)) {
				if ((email.pure_puny.length() > domain.length()) && (email.pure_puny.charAt(email.pure_puny.length() - domain.length() - 1) == '.')) {
					return email.pure_puny.endsWith(domain);
				}
			}
			return false;
		}

		private void ident(boolean nIdent) {
			ident = nIdent;
		}

		public String key() {
			return key;
		}

		public String domain() {
			return domain;
		}

		public String selector() {
			return selector;
		}

		public boolean ident() {
			return ident;
		}
	}

	private long companyID;
	private byte[] secretKey;
	private List<DKIM> dkims;

	public DkimDAO(DBase dbase, long forCompanyID, byte[] useSecretKey) throws SQLException {
		List<Map<String, Object>> rq;

		companyID = forCompanyID;
		secretKey = useSecretKey;
		dkims = new ArrayList <> ();
		if (dbase.exists ("dkim_key_tbl")) {
			try (DBase.With with = dbase.with ()) {
				rq = dbase.query (with.jdbc (),
						  "SELECT * FROM dkim_key_tbl " +
						  "WHERE company_id IN (0, :companyID) AND " +
						  "      ((valid_start IS NULL) OR (valid_start <= CURRENT_TIMESTAMP)) AND " +
						  "      ((valid_end IS NULL) OR (valid_end >= CURRENT_TIMESTAMP)) " +
						  "ORDER BY timestamp",
						  "companyID", companyID);
				for (int n = 0; n < rq.size (); ++n) {
					Map <String, Object>	row = rq.get (n);
					boolean			hasSecret = row.containsKey ("domain_key_encrypted");
					boolean			hasPlain = row.containsKey ("domain_key");
					long			dkimID = dbase.asLong (row.get ("dkim_id"));
					String			dkimKeySecret = hasSecret ? dbase.asString (row.get ("domain_key_encrypted")) : null;
					String			dkimKey = hasPlain ? dbase.asString (row.get ("domain_key")) : null;
					DKIM			dkim;
				
					if (dkimKeySecret != null) {
						try {
							dkimKey = decrypt(dkimKeySecret);
						} catch (Exception e) {
							dbase.logging((dkimKey != null ? Log.WARNING : Log.ERROR), "dkim", "Entry " + dkimID + " has invalid encrypted key (" + e.toString() + ")" + (dkimKey != null ? ", fall back to plain version" : ""));
						}
					} else if ((dkimKey != null) && hasSecret) {
						try {
							dbase.update (with.jdbc (),
								      "UPDATE dkim_key_tbl SET domain_key_encrypted = :domainKey WHERE dkim_id = :dkimID",
								      "dkimID", dkimID,
								      "domainKey", encrypt (dkimKey));
							dbase.logging (Log.DEBUG, "dkim", "Added encrypted key to " + dkimID);
						} catch (SQLException e) {
							dbase.logging(Log.ERROR, "dkim", "Failed to add encrypted key to " + dkimID + ": " + e);
						} catch (Exception e) {
							dbase.logging(Log.WARNING, "dkim", "Failed to encrypt entry " + dkimID + ": " + e);
						}
					}
					dkim = new DKIM(dkimID, dbase.asLong(row.get("company_id")) == companyID, dkimKey, StringOps.punycodeDomain(dbase.asString(row.get("domain"))), dbase.asString(row.get("selector")));
					if (dkim.valid()) {
						dkims.add(dkim);
					}
				}
			}
		}
	}

	public DKIM find(EMail email, boolean useLocal, boolean useGlobal) {
		DKIM local = null;
		DKIM global = null;

		for (DKIM dkim : dkims) {
			if (dkim.local() && dkim.match(email)) {
				dkim.ident(true);
				return dkim;
			}
		}
		for (DKIM dkim : dkims) {
			if (dkim.local()) {
				if (dkim.matchSubdomain(email)) {
					dkim.ident(true);
					return dkim;
				}
				if (useLocal && (local == null)) {
					local = dkim;
				}
			} else {
				if (useGlobal && (global == null)) {
					global = dkim;
				}
			}
		}

		DKIM fallback = local != null ? local : global;

		if (fallback != null) {
			fallback.ident(false);
		}
		return fallback;
	}

	private String decrypt(String content) throws Exception {
		SecretKey secKey = new SecretKeySpec(secretKey, "AES");
		Cipher cipher = Cipher.getInstance("AES");

		cipher.init(Cipher.DECRYPT_MODE, secKey);
		return new String(cipher.doFinal(Base64.decodeBase64(content.getBytes(StandardCharsets.UTF_8))));
	}

	private String encrypt(String content) throws Exception {
		SecretKey secKey = new SecretKeySpec(secretKey, "AES");
		Cipher cipher = Cipher.getInstance("AES");

		cipher.init(Cipher.ENCRYPT_MODE, secKey);
		return new String(Base64.encodeBase64(cipher.doFinal(content.getBytes(StandardCharsets.UTF_8))));
	}
}
