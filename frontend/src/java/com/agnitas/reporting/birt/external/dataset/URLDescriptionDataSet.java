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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class URLDescriptionDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(URLDescriptionDataSet.class);

	public List<String> getURLDescription (int urlID){

		List<String> urlDescription = new ArrayList<>();
		String query = getURLDescriptionQuery(urlID);
        try (Connection connection = getDataSource().getConnection();
        		Statement statement = connection.createStatement();
        		ResultSet resultSet = statement.executeQuery(query)) {
			if (resultSet.next()){
				urlDescription.add(resultSet.getString("url_name"));
			}
		} catch (SQLException e) {
			logger.error(" SQL-Exception ! URL-Description-Query is: " + query , e);
		}
		return urlDescription;
	}

	private String getURLDescriptionQuery(int urlID) {
		return "select full_url url_name from rdir_url_tbl where url_id = " + (Integer.toString(urlID)) ;
	}

}
