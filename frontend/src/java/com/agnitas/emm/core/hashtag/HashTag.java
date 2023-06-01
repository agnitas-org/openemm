/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag;

import com.agnitas.emm.core.hashtag.exception.HashTagException;

/**
 * Interface for any kind of hash tag.
 *
 * If you want to write complex tags supporting colons, consider using class {@link AbstractColonHashTag}.
 * 
 * @see AbstractColonHashTag
 */
public interface HashTag {

	/**
	 * Checks, if the implementation can handle the given tag string.
	 * 
	 * @param context {@link HashTagContext}
	 * @param tagString tag string
	 * 
	 * @return true if tag string can be handled, otherwise false
	 */
	public boolean canHandle(HashTagContext context, String tagString);
	
	/**
	 * Handles the tag string.
	 * 
	 * @param context {@link HashTagContext}
	 * @param tagString tag string
	 * 
	 * @return replacement text for hash tag
	 * 
	 * @throws HashTagException on errors during processing the hash tag
	 */
	public String handle(HashTagContext context, String tagString) throws HashTagException;
}
