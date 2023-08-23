/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.utils.EmailParamExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailingDescriptionDataSet extends BIRTDataSet {

    private static final Logger logger = LogManager.getLogger(MailingDescriptionDataSet.class);

    public List<String> getMailingDescription(int mailingID) {
        List<String> mailingDescription = new ArrayList<>();
        String query = "SELECT shortname FROM mailing_tbl WHERE mailing_id = ?";

        String name = selectWithDefaultValue(logger, query, String.class, "", mailingID);

        if (!name.isBlank()) {
            mailingDescription.add(name);
        }

        return mailingDescription;
    }

    /**
     * @return list of email params
     * 1st element  emailFormat[0] value[1]
     * 2nd element onepixel enabled[0] value[1]
     */
    public List<String[]> getEmailParams(int mailingID, String language) {
        if (StringUtils.isBlank(language)) {
            language = "EN";
        }

        List<String[]> paramsList = new ArrayList<>();
        String paramsQuery = getEmailParamsQuery(mailingID);

        try (Connection connection = getDataSource().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(paramsQuery)) {

            if (resultSet.next()) {
                String emailParams = resultSet.getString("param");
                int mailFormat = Integer.parseInt(EmailParamExtractor.getMailformat(emailParams));
                String mailFormatStr = (mailFormat == 0 ? I18nString.getLocaleString("only_Text", language) : (mailFormat == 1 ? I18nString.getLocaleString("Text_HTML", language) : I18nString.getLocaleString("Text_HTML_OfflineHTML", language)));

                String[] mailFormatValueStringArray = new String[]{I18nString.getLocaleString("action.Format", language), mailFormatStr};
                paramsList.add(mailFormatValueStringArray);

                String onepixelEnabled = EmailParamExtractor.getOnepixelParam(emailParams);
                String[] onePixelValueStringArray = new String[]{I18nString.getLocaleString("openrate.measure", language), I18nString.getLocaleString("openrate." + onepixelEnabled, language)};
                paramsList.add(onePixelValueStringArray);
            }
        } catch (SQLException e) {
            logger.error(" SQL-Exception ! Mailing-Params-Query is: " + paramsQuery, e);
        } catch (Exception e) {
            logger.error(" Just another exception ?", e);
        }

        return paramsList;
    }

    private String getEmailParamsQuery(int mailingID) {
        return String.format("SELECT param FROM mailing_mt_tbl WHERE mailing_id = %d AND mediatype = 0", mailingID);
    }
}
