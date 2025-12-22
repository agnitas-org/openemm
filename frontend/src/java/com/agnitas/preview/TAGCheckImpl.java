/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.preview;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.agnitas.backend.BlockCollection;
import com.agnitas.backend.BlockData;
import com.agnitas.backend.Data;
import com.agnitas.backend.EMMTag;
import com.agnitas.backend.exceptions.EMMTagException;
import com.agnitas.messages.I18nString;
import com.agnitas.util.Log;
import org.apache.commons.lang3.StringUtils;

public class TAGCheckImpl implements TAGCheck {
	static class Seen {
		boolean status;
		String report;

		public Seen() {
			status = false;
			report = null;
		}
	}

	private Data data;
	private final Map<String, Seen> seen;

	private final Locale locale;

	public TAGCheckImpl(int mailingId, Locale locale) throws Exception {
		data = new Data("tagcheck", "silent");
		data.setup ("preview:" + mailingId, null);

		BlockCollection bc = new BlockCollection();
		data.setBlocks(bc);
		bc.setupBlockCollection(data, null);
		seen = new HashMap<>();

		this.locale = locale;
	}

	public TAGCheckImpl(int companyId, int mailinglistId, Locale locale) throws Exception {
		data = new Data("tagcheck", "silent");
		data.setup ("preview:0," + companyId + "," + mailinglistId, null);

		BlockCollection bc = new BlockCollection();
		data.setBlocks(bc);
		bc.setupBlockCollection(data, null);
		seen = new HashMap<>();

		this.locale = locale;
	}

	public TAGCheckImpl(int companyId, int mailingId, int mailinglistId, Locale locale) throws Exception {
		data = new Data("tagcheck", "silent");
		data.setup ("preview:" + mailingId + "," + companyId + "," + mailinglistId, null);

		BlockCollection bc = new BlockCollection();
		data.setBlocks(bc);
		bc.setupBlockCollection(data, null);
		seen = new HashMap<>();

		this.locale = locale;
	}

	@Override
	public void done() {
		if (data != null) {
			try {
				data.done();
			} catch (Exception e) {
				// do nothing
			}
			data = null;
		}
	}

	@Override
	public boolean check(String tag, StringBuffer report) {
		Seen s = seen.get(tag);

		if (s == null) {
			s = new Seen();
			try {
				EMMTag t = new EMMTag(data, tag);
				Set<String> dummy = new HashSet<>();

				t.initialize(data, true);
				t.requestFields(data, dummy);
				s.status = true;
			} catch (EMMTagException e) {
				data.logging(Log.ERROR, "tc", "Failed to check \"" + tag + "\": " + e.toString(), e);
				if (StringUtils.isBlank(e.getMessageKey())) {
					s.report = tag + ": " + StringUtils.defaultIfBlank(e.getMessage(), e.message());
				} else {
					s.report = tag + ": " + I18nString.getLocaleString(e.getMessageKey(), locale, (Object[]) e.getMessageArgs());
				}
			} catch (Exception e) {
				data.logging(Log.ERROR, "tc", "Failed to check \"" + tag + "\": " + e.toString(), e);
				s.report = tag + ": " + e.getMessage();
			}
		}
		if (!s.status) {
			addToReport(report, s.report);
		}
		return s.status;
	}

	@Override
	public boolean check(String tag) {
		return check(tag, null);
	}

	@Override
	public boolean checkContent(String content, StringBuffer report, List<String> failures) {
		BlockData bd = new BlockData(content, null, null, -1, -1, -1, null, true, true, false, false, false, false);
		boolean rc = true;
		String tag;

		do {
			try {
				tag = bd.getNextTag();
			} catch (Exception e) {
				data.logging(Log.ERROR, "tc", "Failed to parse \"" + content + "\": " + e.toString(), e);
				tag = null;
				rc = false;
				addToReport(report, "failed to parse next tag: " + e.toString());
			}
			if ((tag != null) && (!check(tag, report))) {
				rc = false;
				if (failures != null) {
					failures.add(tag);
				}
			}
		} while (tag != null);
		return rc;
	}

	private void addToReport(StringBuffer report, String msg) {
		if ((report != null) && (msg != null)) {
			if (report.length() > 0) {
				report.append("\n");
			}
			report.append(msg);
		}
	}
}
