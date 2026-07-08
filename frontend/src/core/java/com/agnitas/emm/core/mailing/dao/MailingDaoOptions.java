/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dao;

import java.util.Date;

public class MailingDaoOptions {

    private int limit = -1;
    private boolean includeMailinglistName;
    private Date startIncl;
    private Date endExcl;

    public static Builder builder() {
        return new Builder();
    }

    public int getLimit() {
        return limit;
    }

    public boolean isIncludeMailinglistName() {
        return includeMailinglistName;
    }

    public Date getStartIncl() {
        return startIncl;
    }

    public Date getEndExcl() {
        return endExcl;
    }

    public String getAdditionalCols() {
        StringBuilder additionalCols = new StringBuilder();
        if (isIncludeMailinglistName()) {
            additionalCols.append(", ml.shortname mailinglist_name");
        }
        return additionalCols.toString();
    }

    public String getGroupByCols() {
        StringBuilder groupByCols = new StringBuilder();
        if (isIncludeMailinglistName()) {
            groupByCols.append(", ml.shortname");
        }
        return groupByCols.toString();
    }

    public static class Builder {
        private MailingDaoOptions options = new MailingDaoOptions();

        public Builder limit(int limit) {
            options.limit = limit;
            return this;
        }

        public Builder includeMailinglistName(boolean includeMailinglistName) {
            options.includeMailinglistName = includeMailinglistName;
            return this;
        }

        public Builder setStartIncl(Date startIncl) {
            options.startIncl = startIncl;
            return this;
        }

        public Builder setEndExcl(Date endExcl) {
            options.endExcl = endExcl;
            return this;
        }

        public MailingDaoOptions build() {
            MailingDaoOptions result = this.options;
            this.options = null;
            return result;
        }
    }
}
