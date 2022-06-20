/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.factory.DatasourceDescriptionFactory;
import org.agnitas.dao.SourceGroupType;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.core.datasource.bean.DataSource;
import com.agnitas.emm.core.datasource.bean.impl.DataSourceImpl;

public class DatasourceDescriptionDaoImpl extends PaginatedBaseDaoImpl implements DatasourceDescriptionDao {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(DatasourceDescriptionDaoImpl.class);
	
	private static Map<String, Integer> sourceGroupTypeIdCache = null;

    private DatasourceDescriptionFactory datasourceDescriptionFactory;

    @Required
    public void setDatasourceDescriptionFactory(DatasourceDescriptionFactory datasourceDescriptionFactory) {
        this.datasourceDescriptionFactory = datasourceDescriptionFactory;
    }
    
    private int getSourceGroupTypeId(SourceGroupType sourceGroupType) {
    	if (sourceGroupTypeIdCache == null) {
    		Map<String, Integer> sourceGroupTypeIdCacheTemp = new HashMap<>();
    		for (Map<String, Object> row : select(logger, "SELECT * FROM sourcegroup_tbl")) {
    			sourceGroupTypeIdCacheTemp.put((String) row.get("sourcegroup_type"), ((Number) row.get("sourcegroup_id")).intValue());
    		}
    		sourceGroupTypeIdCache = Collections.unmodifiableMap(sourceGroupTypeIdCacheTemp);
    	}
    	if (sourceGroupTypeIdCache.containsKey(sourceGroupType.getStorageString())) {
    		return sourceGroupTypeIdCache.get(sourceGroupType.getStorageString());
    	} else {
    		return 0;
    	}
    }
    
    private SourceGroupType getSourceGroupTypeById(int id) throws Exception {
    	if (sourceGroupTypeIdCache == null) {
    		Map<String, Integer> sourceGroupTypeIdCacheTemp = new HashMap<>();
    		for (Map<String, Object> row : select(logger, "SELECT * FROM sourcegroup_tbl")) {
    			sourceGroupTypeIdCacheTemp.put((String) row.get("sourcegroup_type"), ((Number) row.get("sourcegroup_id")).intValue());
    		}
    		sourceGroupTypeIdCache = Collections.unmodifiableMap(sourceGroupTypeIdCacheTemp);
    	}
    	for (Entry<String, Integer> entry : sourceGroupTypeIdCache.entrySet()) {
    		if (entry.getValue() != null && entry.getValue() == id) {
    			return SourceGroupType.getUserSourceGroupType(entry.getKey());
    		}
    	}
    	throw new Exception("Unknown sourcegrouptypeid: " + id);
    }
    
    /**
     * Read the latest datasource description matching the exect companyid and descriptiontext or, if there is none for that companyid, matching companyid 0 (fallback) and descriptiontext
     */
    @Override
    public DatasourceDescription getByDescription(SourceGroupType sourceGroupType, @VelocityCheck int companyID, String description) {
    	int sourceGroupId = getSourceGroupTypeId(sourceGroupType);
    	if (sourceGroupId <= 0) {
    		throw new RuntimeException("Unknown sourcegrouptypename: " + sourceGroupType.name());
    	} else {
	        String sql = "SELECT datasource_id, company_id, sourcegroup_id, description, url, desc2 FROM datasource_description_tbl WHERE sourcegroup_id = ? AND company_id IN (0, ?) AND description = ? ORDER BY company_id DESC, datasource_id DESC";
	        List<DatasourceDescription> resultList = select(logger, sql, new DatasourceDescription_RowMapper(), sourceGroupId, companyID, description);
	        if (resultList.size() > 0) {
	        	return resultList.get(0);
	        } else {
	        	return null;
	        }
    	}
    }

    @Override
	@DaoUpdateReturnValueCheck
    public int save(DatasourceDescription dsDescription) {
    	validateDescription(dsDescription.getDescription());
    	validateDescription2(dsDescription.getDescription2());
    	
        String sql = "SELECT COUNT(*) FROM datasource_description_tbl WHERE datasource_id = ? AND company_id = ?";
        int numberOfExistingItems = selectInt(logger, sql, dsDescription.getId(), dsDescription.getCompanyID());
        if (numberOfExistingItems > 0) {
        	int sourceGroupId = getSourceGroupTypeId(dsDescription.getSourceGroupType());
            sql = "UPDATE datasource_description_tbl SET sourcegroup_id = ?, description = ?, url = ?, desc2 = ? WHERE datasource_id = ? AND company_id = ?";
            update(logger, sql, sourceGroupId, dsDescription.getDescription(), dsDescription.getUrl(), dsDescription.getDescription2(), dsDescription.getId(), dsDescription.getCompanyID());
        } else {
        	int sourceGroupId = getSourceGroupTypeId(dsDescription.getSourceGroupType());
            int newDatasourceID;
            if (isOracleDB()) {
            	newDatasourceID = selectInt(logger, "SELECT datasource_description_tbl_seq.NEXTVAL FROM DUAL");
                sql = "INSERT INTO datasource_description_tbl (datasource_id, company_id, sourcegroup_id, description, url, desc2, timestamp) VALUES (?, ?, ?, ?, ?, ?, SYSDATE)";
                update(logger, sql, newDatasourceID, dsDescription.getCompanyID(), sourceGroupId, dsDescription.getDescription(), dsDescription.getUrl(), dsDescription.getDescription2());
            } else {
            	newDatasourceID = insertIntoAutoincrementMysqlTable(logger,
        			"datasource_id",
        			"INSERT INTO datasource_description_tbl (company_id, sourcegroup_id, description, url, desc2, timestamp) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
        			dsDescription.getCompanyID(), sourceGroupId, dsDescription.getDescription(), dsDescription.getUrl(), dsDescription.getDescription2());
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
	public boolean resetByCompanyID(@VelocityCheck int companyId) {
		if (companyId > 0) {
			int rowAffected = update(logger, "DELETE FROM datasource_description_tbl WHERE company_id = ? AND datasource_id NOT IN (SELECT default_datasource_id FROM company_tbl WHERE company_id = ?)", companyId, companyId);
			if (rowAffected > 0) {
				return true;
			} else {
				return selectInt(logger, "SELECT COUNT(*) FROM datasource_description_tbl WHERE company_id = ? AND datasource_id NOT IN (SELECT default_datasource_id FROM company_tbl WHERE company_id = ?)", companyId, companyId) == 0;
			}
		}
		
		return false;
	}

    @Override
    public List<DataSource> getDataSources(final int companyId) {
        return select(logger,
                "SELECT datasource_id, description FROM datasource_description_tbl WHERE company_id = 0 OR company_id = ?",
                new DataSourceRowMapper(), companyId);
    }
	
	protected class DatasourceDescription_RowMapper implements RowMapper<DatasourceDescription> {
		@Override
		public DatasourceDescription mapRow(ResultSet resultSet, int row) throws SQLException {
			try {
				DatasourceDescription readItem = datasourceDescriptionFactory.newDatasourceDescription();
				readItem.setId(resultSet.getBigDecimal("datasource_id").intValue());
				readItem.setCompanyID(resultSet.getBigDecimal("company_id").intValue());
				readItem.setSourceGroupType(getSourceGroupTypeById(resultSet.getBigDecimal("sourcegroup_id").intValue()));
				readItem.setDescription(resultSet.getString("description"));
				readItem.setUrl(resultSet.getString("url"));
				readItem.setDescription2(resultSet.getString("desc2"));
				return readItem;
			} catch (Exception e) {
				throw new SQLException(e.getMessage(), e);
			}
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

	private void validateDescription(String description) {
		if (description != null && description.length() > 1000) {
			throw new RuntimeException("Value for datasource_description_tbl.description is to long (Maximum: 1000, Current: " + description.length() + ")");
		}
	}

	private void validateDescription2(String description2) {
		if (description2 != null && description2.length() > 500) {
			throw new RuntimeException("Value for datasource_description_tbl.desc2 is to long (Maximum: 500, Current: " + description2.length() + ")");
		}
	}
}
