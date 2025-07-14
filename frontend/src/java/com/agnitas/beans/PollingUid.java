/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class PollingUid {

    private final String sessionId;
    private final String name;
    private final Object[] arguments;
    private final int hashValue;

    public static Builder builder(String sessionId, String name) {
        return new Builder(sessionId, name);
    }

    public PollingUid(String sessionId, String name, Object... arguments) {
        this.sessionId = sessionId;
        this.name = name;
        this.arguments = arguments;
        this.hashValue = Objects.hash(sessionId, name) ^ Objects.hash(arguments);
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return hashValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != getClass()) {
            return false;
        }

        if (this == o) {
            return true;
        }

        PollingUid other = PollingUid.class.cast(o);

        return StringUtils.equals(other.sessionId, sessionId) &&
            StringUtils.equals(other.name, name) &&
            Arrays.equals(other.arguments, arguments);
    }

    public static class Builder {
        private final String sessionId;
        private final String name;
        private final List<Object> arguments = new ArrayList<>();

        public Builder(String sessionId, String name) {
            this.sessionId = sessionId;
            this.name = name;
        }

        public Builder arguments(Object... args) {
            arguments.addAll(Arrays.asList(args));
            return this;
        }

        public PollingUid build() {
            return new PollingUid(sessionId, name, arguments.toArray());
        }
    }
}
