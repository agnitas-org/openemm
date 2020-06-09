/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.util.HashMap;
import java.util.Map;

import org.agnitas.backend.Mailgun;
import org.agnitas.backend.MailgunImpl;

public class Cache {
	/** previous/next memeber */
	protected Cache		prev, next;
	/** creation date in epoch */
	protected long		ctime;
	/** mailingID we cache */
	protected long		mailingID;
	/** my instance of the mailout */
	private Mailgun		mailout;
	/** my options for executing the mailout */
	private Map <String, Object>
				opts;

	public Cache (long nMailingID, long nCtime, String text, boolean createAll, boolean cacheImages) throws Exception {
		prev = null;
		next = null;
		ctime = nCtime;
		mailingID = nMailingID;
		mailout = new MailgunImpl ();
		mailout.initialize ("preview:" + mailingID);
		opts = new HashMap<> ();
		if (text != null)
			opts.put ("preview-input", text);
		opts.put ("preview-create-all", createAll);
		opts.put ("preview-cache-images", cacheImages);
		Map<String, Object> mapOpts = opts;
		//TODO no such method when hash table is used?
		mailout.prepare (mapOpts);
	}

	protected void release () throws Exception {
		mailout.done ();
		mailout = null;
	}

	protected Page makePreview (long customerID, String selector, boolean anon,
				    boolean convertEntities, boolean ecsUIDs,
				    boolean cachable, long[] targetIDs
	) throws Exception {
		Page output = new PageImpl ();

		opts.put ("preview-for", customerID);
		opts.put ("preview-output", output);
		opts.put ("preview-anon", Boolean.valueOf (anon));
		if (selector != null)
			opts.put ("preview-selector", selector);
		opts.put ("preview-convert-entities", Boolean.valueOf (convertEntities));
		opts.put ("preview-ecs-uids", Boolean.valueOf (ecsUIDs));
		opts.put ("preview-cachable", Boolean.valueOf (cachable));
		opts.put ("preview-target-ids", targetIDs);
		mailout.execute (opts);
		return output;
	}
}
