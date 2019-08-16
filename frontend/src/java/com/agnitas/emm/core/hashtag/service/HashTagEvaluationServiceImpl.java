/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.hashtag.service;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.hashtag.HashTag;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.exception.HashTagException;

public final class HashTagEvaluationServiceImpl implements HashTagEvaluationService {
	
	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(HashTagEvaluationServiceImpl.class);
	
	private HashTagRegistry registry;
	private HashTag fallbackHashTag;

	@Override
	public final String evaluateHashTag(final HashTagContext context, final String string) throws NoMatchingHashTagException, HashTagException {
		final HashTag hashTag = this.registry.findHashTagFor(context, string);
		
		return hashTag.handle(context, string);
	}
	
	private final HashTag findHashTagOrFallbackFor(final HashTagContext context, final String string) {
		try {
			final HashTag hashTag = this.registry.findHashTagFor(context, string);
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format("Hash tag of type '%s' can handle tag string '%s'", hashTag.getClass().getCanonicalName(), string));
			}
			
			return hashTag;
		} catch(final NoMatchingHashTagException e) {
			LOGGER.warn(String.format("No hash tag found to handle tag string '%s', returning fallback hash tag", string));
			
			return this.fallbackHashTag;
		}
	}
	
	
	@Required
	public final void setHashTagRegistry(final HashTagRegistry registry) {
		this.registry = Objects.requireNonNull(registry, "Hash tag registry is null");
	}
	
	@Required
	public final void setFallbackHashTag(final HashTag hashTag) {
		this.fallbackHashTag = Objects.requireNonNull(hashTag, "Fallback hash tag is null");
	}

}
