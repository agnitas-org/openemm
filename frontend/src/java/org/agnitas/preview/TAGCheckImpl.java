/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.backend.BlockCollection;
import org.agnitas.backend.BlockData;
import org.agnitas.backend.Data;
import org.agnitas.backend.EMMTag;
import org.agnitas.util.Log;

public class TAGCheckImpl implements TAGCheck {
	static class Seen {
		boolean status;
		String	report;

		public Seen () {
			status = false;
			report = null;
		}
	}

	private Data			data;
	private Map <String, Seen>	seen;

	public TAGCheckImpl (long mailingID) throws Exception {
		data = new Data ("tagcheck", "preview:" + mailingID, "silent");
		
		BlockCollection	bc = new BlockCollection ();
		data.setBlocks (bc);
		bc.setupBlockCollection (data, null);
		seen = new HashMap<>();
	}

	public TAGCheckImpl () throws Exception {
		this (0);
	}

	@Override
	public void done () {
		if (data != null) {
			try {
				data.done ();
			} catch (Exception e) {
				// do nothing
			}
			data = null;
		}
	}

	@Override
	public boolean check (String tag, StringBuffer report) {
		Seen	s = seen.get (tag);

		if (s == null) {
			s = new Seen ();
			try {
				EMMTag		t = new EMMTag (data, tag);
				Set <String>	dummy = new HashSet<>();

				t.initialize (data, true);
				t.requestFields (data, dummy);
				s.status = true;
			} catch (Exception e) {
				data.logging (Log.ERROR, "tc", "Failed to check \"" + tag + "\": " + e.toString (), e);
				s.report = tag + ": " + e.getMessage ();
			}
		}
		if (! s.status) {
			addToReport (report, s.report);
		}
		return s.status;
	}

	@Override
	public boolean check (String tag) {
		return check (tag, null);
	}

	@Override
	public boolean checkContent (String content, StringBuffer report, List <String> failures) {
		BlockData	bd = new BlockData (content, null, null, -1, -1, -1, null, true, true, false, false, false, false);
		boolean		rc = true;
		String		tag;

		do {
			try {
				tag = bd.getNextTag();
			} catch (Exception e) {
				data.logging (Log.ERROR, "tc", "Failed to parse \"" + content + "\": " + e.toString (), e);
				tag = null;
				rc = false;
				addToReport (report, "failed to parse next tag: " + e.toString ());
			}
			if ((tag != null) && (! check (tag, report))) {
				rc = false;
				if (failures != null) {
					failures.add (tag);
				}
			}
		}	while (tag != null);
		return rc;
	}
	
	private void addToReport (StringBuffer report, String msg) {
		if ((report != null) && (msg != null)) {
			if (report.length () > 0) {
				report.append ("\n");
			}
			report.append (msg);
		}
	}
}
