/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
    
    private final TagType type;
    private final String name;

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
        return switch (type) {
            case STANDALONE -> String.format("[agnDYN name=\"%s\"/]", name);
            case OPENING -> String.format("[agnDYN name=\"%s\"]", name);
            case VALUE -> String.format("[agnDVALUE name=\"%s\"]", name);
            case CLOSING -> String.format("[/agnDYN name=\"%s\"]", name);
        };
    }
}
