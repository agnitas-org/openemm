/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.preview;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.backend.Mailgun;
import com.agnitas.backend.MailgunImpl;

public class Cache {
	/**
	 * previous/next memeber
	 */
	protected Cache prev, next;
	/**
	 * creation date in epoch
	 */
	protected long ctime;
	/**
	 * mailingID we cache
	*/
	protected long mailingID;
	
	/**
	 * my instance of the mailout
	 */
	private Mailgun mailout;
	/**
	 * my options for executing the mailout
	 */
	private Map<String, Object> opts;

	public Cache (Builder builder, long nCtime) throws Exception {
		prev = null;
		next = null;
		ctime = nCtime;
		mailout = new MailgunImpl();
		opts = new HashMap<>();
		if (builder.text () != null) {
			opts.put("preview-input", builder.text ());
		}
		opts.put("preview-create-all", builder.createAll ());
		opts.put("preview-cache-images", builder.cachable ());
		opts.put("preview-for-mobile", builder.isMobile ());
		if (builder.rdirDomainForImages () != null) {
			opts.put ("rdir-domain-for-images", builder.rdirDomainForImages ());
		}
		//TODO no such method when hash table is used?
		Map<String, Object> mapOpts = opts;
		mailout.initialize("preview:" + builder.mailingID (), opts);
		mailout.prepare(mapOpts);
	}

	protected void release() throws Exception {
		mailout.done();
		mailout = null;
	}

	protected Page makePreview (Builder builder) throws Exception {
		Page output = new PageImpl ();

		opts.put("preview-for", builder.customerID ());
		opts.put("preview-output", output);
		opts.put("preview-anon", Boolean.valueOf(builder.anon ()));
		opts.put("preview-anon-preserve-links", Boolean.valueOf (builder.onAnonPreserveLinks ()));
		if (builder.sendDate () > 0) {
			opts.put ("preview-senddate", builder.sendDate ());
		}
		if (builder.selector () != null) {
			opts.put("preview-selector", builder.selector ());
		}
		opts.put("preview-convert-entities", Boolean.valueOf(builder.convertEntities ()));
		opts.put("preview-ecs-uids", Boolean.valueOf(builder.ecsUIDs ()));
		opts.put("preview-cachable", Boolean.valueOf(builder.cachable ()));
		opts.put("preview-target-ids", builder.targetIDs ());
		mailout.execute(opts);
		return output;
	}
}
