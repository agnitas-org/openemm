/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mimetypes.service;

import java.util.List;
import java.util.Objects;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.mimetypes.dao.MimeTypeWhitelistPatternDao;

public class MimeTypeWhitelistServiceImpl implements MimeTypeWhitelistService {

	private MimeTypeWhitelistPatternDao mimeTypeDao;
	
	@Override
	public boolean isMimeTypeWhitelisted(final MimeType mimeType) {
		final List<MimeType> list = this.mimeTypeDao.listWhitelistedMimeType();
		
		for(final MimeType listedMimeType : list) {
			if(mimeType.match(listedMimeType)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean isMimeTypeWhitelisted(final String mimeTypeString) {
		try {
			final MimeType mimeType = new MimeType(mimeTypeString);
			return isMimeTypeWhitelisted(mimeType);
		} catch (MimeTypeParseException e) {
			return false;
		}
	}
	
	@Required
	public void setMimeTypeWhitelistPatternDao(final MimeTypeWhitelistPatternDao dao) {
		this.mimeTypeDao = Objects.requireNonNull(dao, "MimeType whitelist pattern DAO is null");
	}

}
