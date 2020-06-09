/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.factory.DatasourceDescriptionFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.ComDatasourceDescriptionDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.datasource.bean.DataSource;
import com.agnitas.emm.core.datasource.bean.impl.DataSourceImpl;

public class ComDatasourceDescriptionDaoImpl extends PaginatedBaseDaoImpl implements ComDatasourceDescriptionDao {
	private static final transient Logger logger = Logger.getLogger(ComDatasourceDescriptionDaoImpl.class);

    private DatasourceDescriptionFactory datasourceDescriptionFactory;

    @Required
    public void setDatasourceDescriptionFactory(DatasourceDescriptionFactory datasourceDescriptionFactory) {
        this.datasourceDescriptionFactory = datasourceDescriptionFactory;
    }
    
    @Override
    public DatasourceDescription getByDescription(String groupName, @VelocityCheck int companyID, String description) throws Exception {
    	int sourceGroupId = selectInt(logger, "SELECT sourcegroup_id FROM sourcegroup_tbl WHERE sourcegroup_type = ?", groupName);
    	if (sourceGroupId <= 0) {
    		throw new Exception("Unknown sourcegroupname: " + groupName);
    	}
        String sql = "SELECT datasource_id, company_id, sourcegroup_id, description, url, desc2 FROM datasource_description_tbl WHERE sourcegroup_id = ? AND company_id = ? AND description = ? ORDER BY datasource_id DESC";
        List<DatasourceDescription> resultList = select(logger, sql, new DatasourceDescription_RowMapper(), sourceGroupId, companyID, description);
        if (resultList.size() > 0) {
        	return resultList.get(0);
        } else {
        	return null;
        }
    }
    
    @Override
    public DatasourceDescription getByDescription(int group, @VelocityCheck int companyID, String description) {
        if (companyID == 0) {
            return null;
        } else {
	        String sql = "SELECT datasource_id, company_id, sourcegroup_id, description, url, desc2 FROM datasource_description_tbl WHERE sourcegroup_id = ? AND company_id = ? AND description = ? ORDER BY datasource_id DESC";
	        List<DatasourceDescription> resultList = select(logger, sql, new DatasourceDescription_RowMapper(), group, companyID, description);
	        if (resultList.size() > 0) {
	        	return resultList.get(0);
	        } else {
	        	return null;
	        }
        }
    }

    @Override
    public DatasourceDescription get(int dsDescriptionID, @VelocityCheck int companyID) {
        if (dsDescriptionID == 0 || companyID == 0) {
            return null;
        } else {
	        String sql = "SELECT datasource_id, company_id, sourcegroup_id, description, url, desc2 FROM datasource_description_tbl WHERE datasource_id = ? AND company_id = ?";
	        return selectObjectDefaultNull(logger, sql, new DatasourceDescription_RowMapper(), dsDescriptionID, companyID);
        }
    }

    @Override
	@DaoUpdateReturnValueCheck
    public int save(DatasourceDescription dsDescription) {
        String sql = "SELECT COUNT(*) FROM datasource_description_tbl WHERE datasource_id = ? AND company_id = ?";
        int numberOfExistingItems = selectInt(logger, sql, dsDescription.getId(), dsDescription.getCompanyID());
        if (numberOfExistingItems > 0) {
            sql = "UPDATE datasource_description_tbl SET sourcegroup_id = ?, description = ?, url = ?, desc2 = ? WHERE datasource_id = ? AND company_id = ?";
            update(logger, sql, dsDescription.getSourcegroupID(), dsDescription.getDescription(), dsDescription.getUrl(), dsDescription.getDescription2(), dsDescription.getId(), dsDescription.getCompanyID());
        } else {
            int newDatasourceID;
            if (isOracleDB()) {
            	newDatasourceID = selectInt(logger, "SELECT datasource_description_tbl_seq.NEXTVAL FROM DUAL");
                sql = "INSERT INTO datasource_description_tbl (datasource_id, company_id, sourcegroup_id, description, url, desc2, timestamp) VALUES (?, ?, ?, ?, ?, ?, SYSDATE)";
                update(logger, sql, newDatasourceID, dsDescription.getCompanyID(), dsDescription.getSourcegroupID(), dsDescription.getDescription(), dsDescription.getUrl(), dsDescription.getDescription2());
            } else {
            	newDatasourceID = insertIntoAutoincrementMysqlTable(logger, 
        			"datasource_id", 
        			"INSERT INTO datasource_description_tbl (company_id, sourcegroup_id, description, url, desc2, timestamp) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
        			dsDescription.getCompanyID(), dsDescription.getSourcegroupID(), dsDescription.getDescription(), dsDescription.getUrl(), dsDescription.getDescription2());
            }
            dsDescription.setId(newDatasourceID);
        }
        return dsDescription.getId();
    }
	
	@Override
	public boolean delete(int dataSourceId, @VelocityCheck int companyId) {
		if(dataSourceId > 0) {
			int rowAffected = update(logger, "DELETE FROM datasource_description_tbl WHERE datasource_id = ? AND company_id = ?", dataSourceId, companyId);
			return rowAffected > 0;
		}
		
		return false;
	}
	
	@Override
	public boolean deleteByCompanyID(@VelocityCheck int companyId) {
		if (companyId > 0) {
			int rowAffected = update(logger, "DELETE FROM datasource_description_tbl WHERE company_id = ?", companyId);
			if (rowAffected > 0) {
				return true;
			} else {
				return selectInt(logger, "SELECT COUNT(*) FROM datasource_description_tbl WHERE company_id = ?", companyId) == 0;
			}
		}
		
		return false;
	}

	@Override
    public PaginatedListImpl<DataSource> getPaginatedDataSources(@VelocityCheck int companyId, String sortColumn,
                                                int pageNumber, int pageSize, boolean isAscending) {
        String sql = "SELECT datasource_id, description FROM datasource_description_tbl WHERE company_id = ?";
        return selectPaginatedList(logger, sql,
                "datasource_description_tbl",
                sortColumn,
                isAscending,
                pageNumber,
                pageSize,
                new DataSourceRowMapper(),
                companyId);
    }
	
	protected class DatasourceDescription_RowMapper implements RowMapper<DatasourceDescription> {
		@Override
		public DatasourceDescription mapRow(ResultSet resultSet, int row) throws SQLException {
			DatasourceDescription readItem = datasourceDescriptionFactory.newDatasourceDescription();
			readItem.setId(resultSet.getBigDecimal("datasource_id").intValue());
			readItem.setCompanyID(resultSet.getBigDecimal("company_id").intValue());
			readItem.setSourcegroupID(resultSet.getBigDecimal("sourcegroup_id").intValue());
			readItem.setDescription(resultSet.getString("description"));
			readItem.setUrl(resultSet.getString("url"));
			readItem.setDescription2(resultSet.getString("desc2"));
			return readItem;
		}
	}

	protected static class DataSourceRowMapper implements RowMapper<DataSource> {
		@Override
		public DataSource mapRow(ResultSet resultSet, int row) throws SQLException {
            DataSource datasource = new DataSourceImpl();
            datasource.setId(resultSet.getBigDecimal("datasource_id").intValue());
            datasource.setDescription(resultSet.getString("description"));
			return datasource;
		}
	}
}
