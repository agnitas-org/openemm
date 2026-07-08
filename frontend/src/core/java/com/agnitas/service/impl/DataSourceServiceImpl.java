/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.List;
import java.util.Objects;

import com.agnitas.beans.DatasourceDescription;
import com.agnitas.beans.impl.DatasourceDescriptionImpl;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.core.datasource.dto.DataSourceDto;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;
import com.agnitas.emm.wsmanager.dao.WebserviceUserDao;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.FailedCreateDataSourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

public class DataSourceServiceImpl implements DataSourceService {
	
	private static final Logger logger = LogManager.getLogger(DataSourceServiceImpl.class);
	
	private DatasourceDescriptionDao datasourceDescriptionDao;
	private WebserviceUserDao webserviceUserDao;

	@Override
	public int save(DatasourceDescription dsDescription) {
		return datasourceDescriptionDao.save(dsDescription);
	}

	@Override
	@Transactional
	public int createDataSource(int companyId, SourceGroupType sourceGroupType, String dsDescription, String uri) {
        logger.warn("Company: {} Group: {} Descr: {} URL: {}", companyId, sourceGroupType.getStorageString(), dsDescription, uri);
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
			return save(ds);
		} catch (Exception e) {
			throw new FailedCreateDataSourceException(e.getMessage(), e);
		}
	}
	
	@Override
	@Transactional
	public boolean rolloutCreationDataSource(int dataSourceId, String username, int companyId) {
		if (dataSourceId <= 0 || webserviceUserDao.webserviceUserExists(username)) {
			return false;
		}

		return datasourceDescriptionDao.delete(dataSourceId, companyId);
	}

	@Override
	public List<DataSourceDto> getDataSources(int companyId) {
		return datasourceDescriptionDao.getDataSources(companyId);
	}
    
    @Override
    public DatasourceDescription getDatasourceDescription(int datasourceId, int companyId) {
	    return datasourceDescriptionDao.getDatasourceDescription(datasourceId, companyId);
    }

	@Override
	public DatasourceDescription getDatasourceDescription(int datasourceId) {
		return datasourceDescriptionDao.getDatasourceDescription(datasourceId);
	}

	@Override
	public DatasourceDescription getByDescription(SourceGroupType sourceGroupType, String description, int companyID) {
		return datasourceDescriptionDao.getByDescription(sourceGroupType, companyID, description);
	}

	private int getDataSourceId(int companyId, SourceGroupType sourceGroupType, String dsDescription) {
		DatasourceDescription dataSource = getByDescription(sourceGroupType, dsDescription, companyId);
		return Objects.nonNull(dataSource) ? dataSource.getId() : 0;
	}
	
	private boolean isDataSourceExist(int companyId, SourceGroupType sourceGroupType, String dsDescription) {
		return getDataSourceId(companyId, sourceGroupType, dsDescription) > 0;
	}

	public void setDatasourceDescriptionDao(DatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = datasourceDescriptionDao;
	}
	
	public void setWebserviceUserDao(WebserviceUserDao webserviceUserDao) {
		this.webserviceUserDao = webserviceUserDao;
	}
}
