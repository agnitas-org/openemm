/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mediatypes.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.agnitas.beans.Mediatype;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;

import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.util.SpecialCharactersWorker;

public class ComMediatypesDaoImpl extends BaseDaoImpl implements MediatypesDao {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( ComMediatypesDaoImpl.class);
	
	@Override
	@DaoUpdateReturnValueCheck
	public void saveMediatypes(int mailingID, Map<Integer, Mediatype> mediatypes) throws Exception {
		for (Entry<Integer, Mediatype>entry : mediatypes.entrySet()) {
			Integer type = entry.getKey();
			Mediatype mediatype = entry.getValue();
			if (type == MediaTypes.EMAIL.getMediaCode() && mediatype instanceof MediatypeEmail) {
				// process special characters for subject
				MediatypeEmail mediatypeEmail = (MediatypeEmail) mediatype;
				String subject = mediatypeEmail.getSubject();
				subject = SpecialCharactersWorker.processString(subject, mediatypeEmail.getCharset());
				mediatypeEmail.setSubject(subject);
			}

			String sql = "SELECT COUNT(mediatype) FROM mailing_mt_tbl WHERE mailing_id = ? AND mediatype = ?";
			int total = selectInt(logger, sql, mailingID, type);

			if (total > 0) {
				sql = "UPDATE mailing_mt_tbl SET priority = ?, status = ?, param = ? WHERE mailing_id = ? AND mediatype = ?";
				update(logger, sql, mediatype.getPriority(), mediatype.getStatus(), mediatype.getParam(), mailingID, type);
			} else {
				sql = "INSERT INTO mailing_mt_tbl (mailing_id, mediatype, priority, status, param) VALUES (?, ?, ?, ?, ?)";
				update(logger, sql, mailingID, type, mediatype.getPriority(), mediatype.getStatus(), mediatype.getParam());
			}
		}
	}
	
	@Override
	public Map<Integer, Mediatype> loadMediatypes(int mailingID, @VelocityCheck int companyID) throws MediatypesDaoException {
		Map<Integer, Mediatype> mediatypes = new HashMap<>();
		
		String sql = "SELECT mediatype, priority, status, param FROM mailing_mt_tbl WHERE mailing_id = ?";

		try {
			List<Map<String, Object>> resultList = select(logger, sql, mailingID);
			for (Map<String, Object> listRow : resultList) {
				final int type = ((Number) listRow.get("mediatype")).intValue();
				final MediaTypes mediatype = MediaTypes.getMediaTypeForCode(type);
				final Mediatype mt = createMediatype(mediatype);

				mt.setCompanyID(companyID);
				mt.setPriority(((Number) listRow.get("priority")).intValue());
				mt.setStatus(((Number) listRow.get("status")).intValue());
				mt.setParam((String) listRow.get("param"));
				mediatypes.put(new Integer(type), mt);
			}
			return mediatypes;
		} catch(Exception e) {
			logger.error( "Error reading media types for mailing " + mailingID + ", company ID " + companyID, e);
			
			throw new MediatypesDaoException("Error reading media types for mailing " + mailingID + ", company ID " + companyID, e);
		}
	}
	
	protected Mediatype createMediatype(final MediaTypes mediatype) {
		if (mediatype == null) {
			return new MediatypeEmailImpl();
		} else {
			switch(mediatype) {
			case EMAIL:		return new MediatypeEmailImpl();

			default:
				logger.warn("Unhandled media type " + mediatype + " - falling back to EMAIL");
				return new MediatypeEmailImpl();
			}
		}
		
	}
}

