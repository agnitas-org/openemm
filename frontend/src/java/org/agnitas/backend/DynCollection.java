/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.agnitas.backend.dao.ContentDAO;
import org.agnitas.util.Log;

/**
 * Collection of all dynamic content
 */
public class DynCollection {
	/**
	 * Reference to configuration
	 */
	private Data data;
	/**
	 * keep track of all dynamic names with content
	 */
	private Map<Long, DynName> nameIds;

	/**
	 * sequence to create non database based content
	 */
	protected long extraIDSeq;

	/**
	 * Constructor
	 *
	 * @param nData the configuration
	 */
	public DynCollection(Data nData) {
		data = nData;
		nameIds = new HashMap<>();
		extraIDSeq = 0;
	}

	public Collection<DynName> names() {
		return nameIds.values();
	}

	public int nameCount() {
		return nameIds.size();
	}

	/**
	 * Collect all available dynamic parts from data base
	 *
	 * @return collection of DynName
	 * @throws SQLException
	 */
	protected Collection<DynName> setupNames() throws SQLException {
		ContentDAO contentDAO = new ContentDAO(data.company.id(), data.mailing.id());
		nameIds = contentDAO.getDynamicContent(data.dbase);
		return nameIds.values();
	}

	/**
	 * Collect all available dynamic parts which allocated in @see{@link #setupNames()}
	 */
	public void collectParts() throws SQLException {
		Collection<DynName> dynNames = setupNames();
		for (DynName tmp : dynNames) {
			for (int n = 0; n < tmp.clen; ++n) {
				DynCont cont = tmp.content.elementAt(n);

				if ((cont.targetID != DynCont.MATCH_ALWAYS) && (cont.targetID != DynCont.MATCH_NEVER)) {
					Target tgt;

					try {
						tgt = data.targetExpression.getTarget(cont.targetID, true);
					} catch (Exception e) {
						data.logging(Log.ERROR, "dyn", cont.id + " has invalid targetID " + cont.targetID + ": " + e.toString(), e);
						tgt = null;
					}
					if (tgt != null) {
						cont.condition = tgt.getSQL(false);
						if (cont.condition != null) {
							data.logging(Log.DEBUG, "dyn", cont.id + " condition is '" + cont.condition + "'");
						} else {
							String	sql = tgt.getSQL (true);

							data.logging(Log.DEBUG, "dyn", cont.id + " condition is marked as hidden from component" + (sql != null ? ": " + sql : ""));
						}
					} else {
						data.logging(Log.ERROR, "dyn", cont.id + " has invalid condition ID, disable block");
						cont.targetID = DynCont.MATCH_NEVER;
					}
				}
			}
		}
	}

	/**
	 * Add extra content that is not retrieved from the database
	 *
	 * @param name    the name for this dynamic content block
	 * @param content the content itself for this block
	 */
	public void addExtraContent(String name, String content) {
		long id = --extraIDSeq;
		DynName dn = new DynName(name, id);

		dn.add(new DynCont(id, DynCont.MATCH_ALWAYS, 0, content));
		nameIds.put(id, dn);
	}
}
