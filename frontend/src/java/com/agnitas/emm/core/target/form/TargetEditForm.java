/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.form;

import com.agnitas.emm.core.target.web.TargetgroupViewFormat;

public class TargetEditForm {
    private int targetId;
    private String shortname;
    private String description;
    private boolean useForAdminAndTestDelivery;
    private boolean accessLimitation;
    private TargetgroupViewFormat viewFormat;
    private TargetgroupViewFormat previousViewFormat;

    private String eql = "";
    private String queryBuilderRules = "";
    private String queryBuilderFilters = "";

    private int mailinglistId;

    public TargetEditForm() {
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUseForAdminAndTestDelivery() {
        return useForAdminAndTestDelivery;
    }

    public void setUseForAdminAndTestDelivery(boolean useForAdminAndTestDelivery) {
        this.useForAdminAndTestDelivery = useForAdminAndTestDelivery;
    }

    public boolean isAccessLimitation() {
        return accessLimitation;
    }

    public void setAccessLimitation(boolean accessLimitation) {
        this.accessLimitation = accessLimitation;
    }

    public TargetgroupViewFormat getViewFormat() {
        return viewFormat;
    }

    public TargetgroupViewFormat getViewFormatOrDefault() {
        if(viewFormat == null) {
            return getDefaultViewFormat();
        }
        return viewFormat;
    }

    public static TargetgroupViewFormat getDefaultViewFormat() {
        return TargetgroupViewFormat.QUERY_BUILDER;
    }

    public void setViewFormat(TargetgroupViewFormat presentationFormat) {
        this.viewFormat = presentationFormat;
    }

    public TargetgroupViewFormat getPreviousViewFormat() {
        return previousViewFormat;
    }

    public void setPreviousViewFormat(TargetgroupViewFormat previousViewFormat) {
        this.previousViewFormat = previousViewFormat;
    }

    public String getEql() {
        return eql;
    }

    public void setEql(String eql) {
        this.eql = eql;
    }

    public int getMailinglistId() {
        return mailinglistId;
    }

    public void setMailinglistId(int mailingListId) {
        this.mailinglistId = mailingListId;
    }

    public String getQueryBuilderRules() {
        return queryBuilderRules;
    }

    public void setQueryBuilderRules(String queryBuilderRules) {
        this.queryBuilderRules = queryBuilderRules;
    }

    public String getQueryBuilderFilters() {
        return queryBuilderFilters;
    }

    public void setQueryBuilderFilters(String queryBuilderFilters) {
        this.queryBuilderFilters = queryBuilderFilters;
    }
}
