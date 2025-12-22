/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.mobilephone.service;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.emm.mobilephone.MobilephoneNumber;
import com.agnitas.emm.mobilephone.dao.MobilephoneNumberWhitelistDao;

/**
 * Implementation of {@link MobilephoneNumberWhitelist} interface.
 */
public final class MobilephoneNumberWhitelistImpl implements MobilephoneNumberWhitelist {
	
	private static final Logger LOGGER = LogManager.getLogger(MobilephoneNumberWhitelistImpl.class);

	private MobilephoneNumberWhitelistDao whitelistDao;
	
	@Override
	public final boolean isWhitelisted(final MobilephoneNumber number, final int companyID) {
		final List<String> list = whitelistDao.readWhitelistPatterns(companyID);
		
		for(final String patternString : list) {
			try {
				final Pattern pattern = Pattern.compile(patternString);
				
				// If phone number matches patten, we can stop here
				if(pattern.matcher(number.toString()).matches()) {
					return true;
				}
			} catch(final PatternSyntaxException e) {
				// Log invalid pattern and proceed
				LOGGER.warn(String.format("Found invalid mobilephone whitelist pattern for company ID %d: %s", companyID, patternString));
			}
		}

		// No match found, so number is not whitelisted.
		return false;
	}
	
	/**
	 * Sets the DAO for accessing whitelist.
	 * 
	 * @param dao DAO accessing whitelist
	 */
	public final void setMobilephoneNumberWhitelistDao(final MobilephoneNumberWhitelistDao dao) {
		this.whitelistDao = Objects.requireNonNull(dao, "Whitelist DAO is null");
	}
}
