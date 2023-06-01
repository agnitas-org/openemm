/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.messages.I18nString;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MailinglistDescriptionDataSet extends BIRTDataSet {

    private static final Logger logger = LogManager.getLogger(MailinglistDescriptionDataSet.class);

    public List<String> getMailinglistDescription(int mailinglistID, String language) {
        List<String> mailinglistDescription = new ArrayList<>();

        if (mailinglistID == 0) {
            if (StringUtils.isBlank(language)) {
                language = "EN";
            }

            mailinglistDescription.add(I18nString.getLocaleString(CommonKeys.ALL_MAILINGLISTS, language));
            return mailinglistDescription;
        }

        String query = "SELECT shortname FROM mailinglist_tbl WHERE mailinglist_id = ?";
        String name = selectWithDefaultValue(logger, query, String.class, "", mailinglistID);

        if (!name.isBlank()) {
            mailinglistDescription.add(name);
        }

        return mailinglistDescription;
    }
}
