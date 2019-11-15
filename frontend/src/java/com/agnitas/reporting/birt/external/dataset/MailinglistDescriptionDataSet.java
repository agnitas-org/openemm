/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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

import org.agnitas.util.DbUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.messages.I18nString;

public class MailinglistDescriptionDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailinglistDescriptionDataSet.class);
	
	private static final String ALL_MAILINGLISTS = "statistic.All_Mailinglists";

	public List<String> getMailinglistDescription (int mailinglistID, String language){
		if (StringUtils.isBlank(language)){
			language = "EN";
		}

		List<String> mailinglistDescription = new ArrayList<>();
		if (mailinglistID == 0){
			mailinglistDescription.add(I18nString.getLocaleString(ALL_MAILINGLISTS, language));
			return mailinglistDescription;
		}
		String query = getMailinglistDescriptionQuery(mailinglistID);
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			if (resultSet.next()){
				mailinglistDescription.add(resultSet.getString("mailinglist_name"));
			}
		} catch (SQLException e) {
			logger.error(" SQL-Exception ! Mailinglist-Description-Query is: " + query , e);
		} finally {
            DbUtilities.closeQuietly(connection, "Could not close DB connection ");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
		}
		return mailinglistDescription;
	}

	private String getMailinglistDescriptionQuery(int mailinglistID) {
		return " select shortname mailinglist_name from mailinglist_tbl where mailinglist_id = " + (Integer.toString(mailinglistID)) ;
	}
}
