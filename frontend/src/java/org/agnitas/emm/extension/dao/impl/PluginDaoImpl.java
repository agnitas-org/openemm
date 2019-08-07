/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.extension.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.extension.dao.PluginDao;
import org.agnitas.emm.extension.data.PluginData;
import org.agnitas.emm.extension.data.impl.PluginDataImpl;
import org.agnitas.emm.extension.exceptions.UnknownPluginException;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class PluginDaoImpl extends BaseDaoImpl implements PluginDao {

	private static class PluginDataRowMapper implements RowMapper<PluginData> {
		
		@Override
		public PluginData mapRow( ResultSet resultSet, int row) throws SQLException {
			PluginData pluginData = new PluginDataImpl();
			
			pluginData.setPluginId( resultSet.getString( "plugin_id"));
			pluginData.setActivatedOnStartup( resultSet.getBoolean( "activate_on_startup"));
			
			return pluginData;
		}
		
	}
	
	// --------------------------------------------------------------------- Business Logic
	private static final transient Logger logger = Logger.getLogger(PluginDaoImpl.class);
	
	private static final String GET_PLUGIN_DATA_SQL = "SELECT * FROM plugins_tbl WHERE plugin_id=?";
	private static final String UPDATE_PLUGIN_DATA_SQL = "UPDATE plugins_tbl SET activate_on_startup=? WHERE plugin_id=?";
	private static final String INSERT_PLUGIN_DATA_SQL = "INSERT INTO plugins_tbl (plugin_id, activate_on_startup) VALUES (?, ?)";
	private static final String DELETE_PLUGIN_DATA_SQL = "DELETE FROM plugins_tbl WHERE plugin_id=?";
	
	@Override
	public PluginData getPluginData( String pluginId) throws UnknownPluginException {
		List<PluginData> list = select(logger, GET_PLUGIN_DATA_SQL, new PluginDataRowMapper(), pluginId);
		
		if (list.size() == 0) {
			throw new UnknownPluginException( pluginId);
		} else {
			return list.get( 0);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void savePluginData(PluginData pluginData) {
		int updated = update(logger, UPDATE_PLUGIN_DATA_SQL, pluginData.isActivatedOnStartup(), pluginData.getPluginId());
		
		if (updated == 0) {
			update(logger, INSERT_PLUGIN_DATA_SQL, pluginData.getPluginId(), pluginData.isActivatedOnStartup());
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public void removePluginData( String pluginId) {
		update(logger, DELETE_PLUGIN_DATA_SQL, pluginId);
	}	
}
