/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.util.Vector;

import org.agnitas.util.Log;

/**
 * Represents a name for a dynamic content
 */
public class DynName {
	/**
	 * the unqiue name for this entry
	 */
	public String name;
	/**
	 * the unique ID
	 */
	public long id;
	/**
	 * all content with the same name
	 */
	public Vector<DynCont> content;
	/**
	 * number of entries in content
	 */
	public int clen;
	/**
	 * if this dynamic content has an interest property
	 */
	protected String interest;
	/**
	 * if set, no link extension is added to URLs
	 */
	protected boolean disableLinkExtension;

	/**
	 * The Constructor
	 *
	 * @param nName the name
	 * @param nId   the ID
	 */
	public DynName(String nName, long nId) {
		name = nName;
		id = nId;
		content = new Vector<>();
		clen = 0;
		interest = null;
		disableLinkExtension = false;
	}

	public void setInterest(String nInterest) {
		interest = nInterest == null ? null : nInterest.toLowerCase();
	}

	public String getInterest() {
		return interest;
	}

	public void setDisableLinkExtension(boolean nDisableLinkExtension) {
		disableLinkExtension = nDisableLinkExtension;
	}

	public boolean getDisableLinkExtension() {
		return disableLinkExtension;
	}

	/**
	 * Add a dynamic block for this name
	 *
	 * @param cont the content to add
	 */
	public void add(DynCont cont, Data data) {
		int n;
		DynCont tmp;

		tmp = null;
		for (n = 0; n < clen; ++n) {
			tmp = content.elementAt(n);

			if (tmp.order >= cont.order)
				break;
		}
		if ((n < clen) && (tmp != null) && (tmp.order == cont.order)) {
			if (data != null) {
				data.logging(Log.ERROR, "dyn", "DB inconsistence: Textpart \"" + name + "\" has additional content with same order " + cont.order + ", use the one with higher ID (" + tmp.id + " vs. " + cont.id + ")");
			}
			if (cont.id > tmp.id)
				content.set(n, cont);
		} else {
			content.add(n, cont);
			++clen;
		}
	}

	/**
	 * Add a dynamic block for this name
	 *
	 * @param cont the content to add
	 */
	public void add(DynCont cont) {
		add(cont, null);
	}
}
