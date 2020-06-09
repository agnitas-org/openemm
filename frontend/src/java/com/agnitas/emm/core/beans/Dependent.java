/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.beans;

import java.util.Objects;

public class Dependent<T extends DependentEntityTypeEnum<T>> {
    private final T type;
    private final int id;
    private final String shortname;

    public Dependent(T type, int id, String shortname) {
        this.type = Objects.requireNonNull(type);
        this.id = id;
        this.shortname = shortname;
    }

    public T getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getShortname() {
        return shortname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependent<?> that = (Dependent<?>) o;
        return id == that.id &&
                type.equals(that.type) &&
                Objects.equals(shortname, that.shortname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, shortname);
    }

    @Override
    public String toString() {
        return "Dependent{" +
                "type=" + type +
                ", id=" + id +
                ", shortname='" + shortname + '\'' +
                '}';
    }
}
