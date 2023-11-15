/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public final class PollingUid {

    private static final boolean IS_RETAINED_BY_DEFAULT = true;
    private final String sessionId;
    private final String name;
    private final Object[] arguments;
    private final int hashValue;
    private boolean isRetained = IS_RETAINED_BY_DEFAULT;

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

    public boolean isRetained() {
        return isRetained;
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
        private List<Object> arguments = new ArrayList<>();

        private boolean isRetained = IS_RETAINED_BY_DEFAULT;

        public Builder(String sessionId, String name) {
            this.sessionId = sessionId;
            this.name = name;
        }

        public Builder arguments(Object... args) {
            arguments.addAll(Arrays.asList(args));
            return this;
        }

        public Builder setRetained(boolean retained) {
            isRetained = retained;
            return this;
        }

        public PollingUid build() {
            PollingUid pollingUid = new PollingUid(sessionId, name, arguments.toArray());
            pollingUid.isRetained = this.isRetained;

            return pollingUid;
        }
    }
}
