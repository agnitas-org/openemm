/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.hashtag.service;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.emm.core.hashtag.HashTag;
import com.agnitas.emm.core.hashtag.HashTagContext;

public final class HashTagRegistryImpl implements HashTagRegistry {

	/** List of registered hash tags. */
	private final List<HashTag> hashTagList;
	
	public HashTagRegistryImpl() {
		this.hashTagList = new ArrayList<>();
	}
	
	public final void setHashTags(final List<HashTag> list) {
		if(list != null) {
			this.hashTagList.clear();
			this.hashTagList.addAll(list);
		}
	}
	
	@Override
	public final HashTag findHashTagFor(final HashTagContext context, final String string) throws NoMatchingHashTagException {
		for(final HashTag hashTag : hashTagList) {
			if(hashTag.canHandle(context, string)) {
				return hashTag;
			}
		}
		
		throw new NoMatchingHashTagException();
	}
}
