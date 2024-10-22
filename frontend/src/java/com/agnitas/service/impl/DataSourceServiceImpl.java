/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.core.datasource.bean.DataSource;
import com.agnitas.emm.core.datasource.enums.DataSourceType;
import com.agnitas.emm.wsmanager.dao.WebserviceUserDao;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.FailedCreateDataSourceException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.impl.DatasourceDescriptionImpl;
import org.agnitas.dao.SourceGroupType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class DataSourceServiceImpl implements DataSourceService {
	
	private static final Logger logger = LogManager.getLogger(DataSourceServiceImpl.class);
	
	private DatasourceDescriptionDao datasourceDescriptionDao;
	private WebserviceUserDao webserviceUserDao;
 

	@Override
	@Transactional
	public int createDataSource(int companyId, SourceGroupType sourceGroupType, String dsDescription, String uri) {
		logger.warn("Company: " + companyId + " Group: " + sourceGroupType.getStorageString() + " Descr: " + dsDescription + " URL: " + uri);
		try {
			if(isDataSourceExist(companyId, sourceGroupType, dsDescription)) {
				throw new FailedCreateDataSourceException("Cannot create data source - duplicate description");
			}
		} catch (Exception e) {
			throw new FailedCreateDataSourceException(e.getMessage(), e);
		}
		
		DatasourceDescription ds = new DatasourceDescriptionImpl();
		ds.setCompanyID(companyId);
		ds.setDescription(dsDescription);
		ds.setSourceGroupType(sourceGroupType);
		ds.setUrl(uri);
		
		try {
			return datasourceDescriptionDao.save(ds);
		} catch (Exception e) {
			throw new FailedCreateDataSourceException(e.getMessage(), e);
		}
	}
	
	@Override
	@Transactional
	public boolean rolloutCreationDataSource(int dataSourceId, String username, int companyId) {
		if(webserviceUserDao.webserviceUserExists(username)) {
			return false;
		}
		
		if(dataSourceId <= 0) {
			return false;
		}
		
		return datasourceDescriptionDao.delete(dataSourceId, companyId);
	}
	
    @Override
    public JSONArray getDataSourcesJson(Admin admin) {
        JSONArray jsonArray = new JSONArray();
        for (DataSource dataSource : datasourceDescriptionDao.getDataSources(admin.getCompanyID())) {
            JSONObject entry = new JSONObject();
            entry.put("id", dataSource.getId());
            entry.put("description", dataSource.getDescription());
			if (admin.isRedesignedUiUsed()) {
				entry.put("timestamp", dataSource.getTimestamp().getTime());
				entry.put("type", DataSourceType.findBySourceGroupType(dataSource.getSourceGroupType()).orElse(null));
				entry.put("sourceGroupType", dataSource.getSourceGroupType());
				entry.put("extraData", dataSource.getExtraData());
			}
            jsonArray.add(entry);
        }
        return jsonArray;
    }
    
    @Override
    public DatasourceDescription getDatasourceDescription(int datasourceId, int companyId) {
	    return datasourceDescriptionDao.getDatasourceDescription(datasourceId, companyId);
    }

	@Override
	public DatasourceDescription getDatasourceDescription(int datasourceId) {
		return datasourceDescriptionDao.getDatasourceDescription(datasourceId);
	}

	private int getDataSourceId(int companyId, SourceGroupType sourceGroupType, String dsDescription) {
		DatasourceDescription dataSource = datasourceDescriptionDao.getByDescription(sourceGroupType, companyId, dsDescription);
		
		return Objects.nonNull(dataSource) ? dataSource.getId() : 0;
	}
	
	private boolean isDataSourceExist(int companyId, SourceGroupType sourceGroupType, String dsDescription) {
		return getDataSourceId(companyId, sourceGroupType, dsDescription) > 0;
	}

	@Required
	public void setDatasourceDescriptionDao(DatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = datasourceDescriptionDao;
	}
	
	@Required
	public void setWebserviceUserDao(WebserviceUserDao webserviceUserDao) {
		this.webserviceUserDao = webserviceUserDao;
	}
}
