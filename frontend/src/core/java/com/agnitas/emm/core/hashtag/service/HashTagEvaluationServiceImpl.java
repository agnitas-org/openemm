/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag.service;

import java.util.Objects;

import com.agnitas.emm.core.hashtag.HashTag;
import com.agnitas.emm.core.hashtag.HashTagContext;
import com.agnitas.emm.core.hashtag.exception.HashTagException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class HashTagEvaluationServiceImpl implements HashTagEvaluationService {

	private static final Logger logger = LogManager.getLogger(HashTagEvaluationServiceImpl.class);

	private HashTagRegistry registry;
	private HashTag fallbackHashTag;

	@Override
	public String evaluateHashTag(HashTagContext context, String string) throws HashTagException {
		final HashTag hashTag = findHashTagOrFallbackFor(context, string);
		return hashTag.handle(context, string);
	}

	private HashTag findHashTagOrFallbackFor(HashTagContext context, String string) {
		try {
			final HashTag hashTag = this.registry.findHashTagFor(context, string);
			
			logger.debug("Hash tag of type '{}' can handle tag string '{}'", hashTag.getClass().getCanonicalName(), string);

			return hashTag;
		} catch (NoMatchingHashTagException e) {
			logger.info("No hash tag found to handle tag string '{}', returning fallback hash tag", string);
			return this.fallbackHashTag;
		}
	}

	public void setHashTagRegistry(HashTagRegistry registry) {
		this.registry = Objects.requireNonNull(registry, "Hash tag registry is null");
	}

	public void setFallbackHashTag(HashTag hashTag) {
		this.fallbackHashTag = Objects.requireNonNull(hashTag, "Fallback hash tag is null");
	}

}
