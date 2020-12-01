/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import org.agnitas.beans.Mediatype;
import org.agnitas.util.importvalues.MailType;

import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.json.JsonObject;

public class MailingImporterMediatypeFactory {

	public Mediatype createMediatypeFromJson(final JsonObject mediatypeJsonObject) throws Exception {
		final MediaTypes mediaType = MediaTypes.getMediatypeByName((String) mediatypeJsonObject.get("type"));
		
		switch(mediaType) {
		case EMAIL: {
			final MediatypeEmailImpl emailType = new MediatypeEmailImpl();
			emailType.setSubject((String) mediatypeJsonObject.get("subject"));
			emailType.setFromEmail((String) mediatypeJsonObject.get("from_address"));
			emailType.setFromFullname((String) mediatypeJsonObject.get("from_fullname"));
			emailType.setReplyEmail((String) mediatypeJsonObject.get("reply_address"));
			emailType.setReplyFullname((String) mediatypeJsonObject.get("reply_fullname"));
			emailType.setCharset((String) mediatypeJsonObject.get("charset"));
			emailType.setMailFormat(MailType.getFromString((String) mediatypeJsonObject.get("mailformat")));
			emailType.setFollowupFor((String) mediatypeJsonObject.get("followup_for"));
			emailType.setFollowUpMethod((String) mediatypeJsonObject.get("followup_method"));
			emailType.setOnepixel((String) mediatypeJsonObject.get("onepixel"));
			emailType.setLinefeed((Integer) mediatypeJsonObject.get("linefeed"));
			emailType.setEnvelopeEmail((String) mediatypeJsonObject.get("envelope"));
			return emailType;
		}

		default:
			throw new Exception("Invalid mediatype code: " + mediaType);
		}
	}
	
}
