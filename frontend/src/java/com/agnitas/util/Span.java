/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

public class Span {
    private final int begin;
    private final int end;

    public Span(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public boolean contains(int x) {
        return begin <= x && x <= end;
    }

    public boolean contains(Span rhs) {
        return rhs != null && contains(rhs.getBegin()) && contains(rhs.getEnd());
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + begin;
		result = prime * result + end;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Span other = (Span) obj;
		if (begin != other.begin) {
			return false;
		}
		if (end != other.end) {
			return false;
		}
		return true;
	}
}
