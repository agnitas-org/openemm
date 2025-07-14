/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields.form;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.web.forms.BirtStatisticForm;
import org.apache.commons.collections4.CollectionUtils;

public class ProfileFieldStatForm extends BirtStatisticForm {

    private static final String REPORT_NAME = "profiledb_evaluation.rptdesign";
    private static final int DEFAULT_MAX_DOMAIN_NUM = 5;

    public ProfileFieldStatForm() {
        setReportName(REPORT_NAME);
        setLimit(DEFAULT_MAX_DOMAIN_NUM);
    }

    private int companyId;
    private int limit;
    private Set<Integer> hiddenTargets;
    private String colName = RecipientStandardField.Email.getColumnName();
    private Locale locale = Locale.ENGLISH;

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Set<Integer> getHiddenTargets() {
        return hiddenTargets;
    }

    public void setHiddenTargets(Set<Integer> hiddenTargets) {
        this.hiddenTargets = hiddenTargets;
    }

    public String getHiddenTargetsCsv() {
        return CollectionUtils.emptyIfNull(hiddenTargets).stream()
                              .map(String::valueOf)
                              .collect(Collectors.joining(","));
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
