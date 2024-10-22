/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Builder {
	private Preview	preview;	
	private long	mailingID;
	private long	customerID;
	private String	selector;
	private String	text;
	private boolean	anon;
	private boolean	convertEntities;
	private boolean	ecsUIDs;
	private boolean	createAll;
	private boolean	cachable;
	private long[]	targetIDs;
	private boolean	isMobile;
	private long	sendDate;
	private boolean	onAnonPreserveLinks;
	public Builder (Preview preview) {
		this.preview = preview;
		mailingID = 0L;
		customerID = 0L;
		selector = null;
		text = null;
		anon = false;
		convertEntities = false;
		ecsUIDs = false;
		createAll = false;
		cachable = false;
		targetIDs = null;
		isMobile = false;
		sendDate = 0L;
		onAnonPreserveLinks = false;
	}
	
	public Builder mailingID (long mailingID) {
		this.mailingID = mailingID;
		return this;
	}
	public long mailingID () {
		return mailingID;
	}
	public Builder customerID (long customerID) {
		this.customerID = customerID;
		return this;
	}
	public long customerID () {
		return customerID;
	}
	public Builder selector (String selector) {
		this.selector = selector;
		return this;
	}
	public String selector () {
		return selector;
	}
	public Builder text (String text) {
		this.text = text;
		return this;
	}
	public String text () {
		return text;
	}
	public Builder anon (boolean anon) {
		this.anon = anon;
		return this;
	}
	public boolean anon () {
		return anon;
	}
	public Builder convertEntities (boolean convertEntities) {
		this.convertEntities = convertEntities;
		return this;
	}
	public boolean convertEntities () {
		return convertEntities;
	}
	public Builder ecsUIDs (boolean ecsUIDs) {
		this.ecsUIDs = ecsUIDs;
		return this;
	}
	public boolean ecsUIDs () {
		return ecsUIDs;
	}
	public Builder createAll (boolean createAll) {
		this.createAll = createAll;
		return this;
	}
	public boolean createAll () {
		return createAll;
	}
	public Builder cachable (boolean cachable) {
		this.cachable = cachable;
		return this;
	}
	public boolean cachable () {
		return cachable;
	}
	public Builder targetID (long targetID) {
		long[]	targetIDs = { targetID };
		
		return targetIDs (targetIDs);
	}
	public Builder targetIDs (long[] targetIDs) {
		if ((targetIDs != null) && (targetIDs.length > 0)) {
			long[]	newTargetIDs = new long[targetIDs.length + (this.targetIDs == null ? 0 : this.targetIDs.length)];
			int	pos = 0;
		
			for (int round = 0; round < 2; ++round) {
				long[]	current = round == 0 ? this.targetIDs : targetIDs;
			
				if ((current != null) && (current.length > 0)) {
					for (int n = 0; n < current.length; ++n) {
						newTargetIDs[pos++] = current[n];
					}
				}
			}
			this.targetIDs = newTargetIDs;
		}
		return this;
	}
	public long[] targetIDs () {
		return targetIDs;
	}
	public Builder isMobile (boolean isMobile) {
		this.isMobile = isMobile;
		return this;
	}
	public boolean isMobile () {
		return isMobile;
	}
	public Builder sendDate (long sendDate) {
		this.sendDate = sendDate;
		return this;
	}
	public long sendDate () {
		return sendDate;
	}
	public Builder onAnonPreserveLinks (boolean onAnonPreserveLinks) {
		this.onAnonPreserveLinks = onAnonPreserveLinks;
		return this;
	}
	public boolean onAnonPreserveLinks () {
		return onAnonPreserveLinks;
	}
	public String id () {
		return "[" + mailingID + "/" + customerID +
			(selector == null ? "" : ":" + selector) +
			(anon ? "A" : "") +
			(convertEntities ? "&" : "") +
			(ecsUIDs ? "^" : "") +
			(createAll ? "*" : "") +
			(cachable ? "C" : "") + 
			(targetIDs == null || targetIDs.length == 0 ? "" : ">" + targetIDs) +
			(isMobile ? "." : "") +
			(sendDate > 0 ? sendDate : "") +
			(onAnonPreserveLinks ? "$" : "") +
		"]" + (text == null ? "" : ", " + makeTextID (text));
	}
	private String makeTextID(String text) {
		String rc;

		if (text.length() < 32) {
			rc = text;
		} else {
			try {
				MessageDigest sha512Digest = MessageDigest.getInstance("SHA-512");
				byte[] digest;
				StringBuffer buf;
				String[] hd = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

				sha512Digest.update(text.getBytes(StandardCharsets.UTF_8));
				digest = sha512Digest.digest();
				buf = new StringBuffer(sha512Digest.getDigestLength());
				for (int n = 0; n < digest.length; ++n) {
					buf.append(hd[(digest[n] >> 4) & 0xf]);
					buf.append(hd[digest[n] & 0xf]);
				}
				rc = buf.toString();
			} catch (Exception e) {
				rc = text;
			}
		}
		return rc;
	}
}
