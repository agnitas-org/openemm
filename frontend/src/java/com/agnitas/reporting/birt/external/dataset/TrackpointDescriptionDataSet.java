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
import org.apache.log4j.Logger;

public class TrackpointDescriptionDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(TrackpointDescriptionDataSet.class);

	public List<String> getTrackpointDescription (String pagetag){

		List<String> trackpointDescription = new ArrayList<>();
		String query = getTrackpointDescriptionQuery(pagetag);
		Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
			connection = getDataSource().getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			if (resultSet.next()){
				trackpointDescription.add(resultSet.getString("trackpoint_name"));
			}
		} catch (SQLException e) {
			logger.error(" SQL-Exception ! Trackpoint-Description-Query is: " + query , e);
		} finally {
            DbUtilities.closeQuietly(connection, "Could not close DB connection ");
            DbUtilities.closeQuietly(statement, "Could not close DB-statement !");
            DbUtilities.closeQuietly(resultSet, "Could not close result set !");
		}
		return trackpointDescription;
	}

	private String getTrackpointDescriptionQuery(String pagetag) {
		return "select shortname trackpoint_name from trackpoint_def_tbl where pagetag = '" + pagetag +"'";
	}

}
