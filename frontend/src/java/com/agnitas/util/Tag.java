/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

public class Tag extends Span {
	public enum TagType {
        STANDALONE,
        OPENING,
        VALUE,
        CLOSING
    }
    
    private TagType type;
    private String name;

    public Tag(int begin, int end, TagType type, String name) {
        super(begin, end);
        this.type = type;
        this.name = name;
    }

    public TagType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        switch (type) {
            case STANDALONE:
                return String.format("[agnDYN name=\"%s\"/]", name);
            case OPENING:
                return String.format("[agnDYN name=\"%s\"]", name);
            case VALUE:
                return String.format("[agnDVALUE name=\"%s\"]", name);
            case CLOSING:
                return String.format("[/agnDYN name=\"%s\"]", name);
			default:
				break;
        }

        throw new RuntimeException("Unexpected type value (" + type + ")");
    }
}
