/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web;

public enum TargetgroupViewFormat {

	QUERY_BUILDER("qb"),
	EQL("eql");
	
	private final String code;
	
	TargetgroupViewFormat(final String code) {
		this.code = code;
	}
	
	public final String code() {
		return this.code;
	}
	
	public static final TargetgroupViewFormat fromCode(final String code) {
		for(TargetgroupViewFormat fmt : values()) {
			if(fmt.code.equals(code)) {
				return fmt;
			}
		}
		
		return null;
	}
	
	public static final TargetgroupViewFormat fromCode(final String code, final TargetgroupViewFormat fallback) {
		final TargetgroupViewFormat format = fromCode(code);
		
		return format != null ? format : fallback;
	}
}
