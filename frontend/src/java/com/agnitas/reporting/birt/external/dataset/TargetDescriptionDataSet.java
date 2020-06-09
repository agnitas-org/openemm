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
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.messages.I18nString;

public class TargetDescriptionDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(TargetDescriptionDataSet.class);
	
	private static final String ALL_TARGETS = "statistic.all_subscribers";

	public List<String> getTargetDescription (String targetID, String language){

		if(StringUtils.isBlank(language)){
			language = "EN";
		}
				
		List<String> targetDescription = new ArrayList<>();
		if(StringUtils.isBlank(targetID)){
			targetDescription.add(I18nString.getLocaleString(ALL_TARGETS, language));
			return targetDescription;
		}
		String query = getTargetDescriptionQuery(targetID);
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			while (resultSet.next()){
				targetDescription.add(resultSet.getString("target_name"));
			}
		} catch (SQLException e) {
			logger.error(" SQL-Exception ! Target-Description-Query is: " + query , e);
		} finally {
            DbUtilities.closeQuietly(connection, "Could not close DB connection ");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
		}
		return targetDescription;
	}

	private String getTargetDescriptionQuery(String targetID) {
		return " select target_shortname target_name from dyn_target_tbl where target_id in (" + targetID +")";
	}

}
