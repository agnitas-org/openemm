/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.Objects;

import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.impl.DatasourceDescriptionImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.dao.ComDatasourceDescriptionDao;
import com.agnitas.emm.core.datasource.bean.DataSource;
import com.agnitas.emm.wsmanager.dao.WebserviceUserDao;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.FailedCreateDataSourceException;

public class DataSourceServiceImpl implements DataSourceService {
	
	private static final Logger logger = Logger.getLogger(DataSourceServiceImpl.class);
	
	private ComDatasourceDescriptionDao datasourceDescriptionDao;
	private WebserviceUserDao webserviceUserDao;
 

	@Override
	@Transactional
	public int createDataSource(int companyId, int dsGroup, String dsDescription, String uri) {
		logger.warn("Company: " + companyId + " Group: " + dsGroup + " Descr: " + dsDescription + " URL: " + uri);
		try {
			if(isDataSourceExist(companyId, dsGroup, dsDescription)) {
				throw new FailedCreateDataSourceException("Cannot create data source - duplicate description");
			}
		} catch (Exception e) {
			throw new FailedCreateDataSourceException(e.getMessage(), e);
		}
		
		DatasourceDescription ds = new DatasourceDescriptionImpl();
		ds.setCompanyID(companyId);
		ds.setDescription(dsDescription);
		ds.setSourcegroupID(dsGroup);
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
	public PaginatedListImpl<DataSource> getPaginatedDataSources(@VelocityCheck int companyId, String sortColumn,
												int pageNumber, int pageSize, String direction) {
		pageNumber = pageNumber <= 0 ? 1 : pageNumber;
		pageSize = pageSize <= 0 ? 20 : pageSize;
		boolean isAscending = AgnUtils.sortingDirectionToBoolean(direction, false);
		sortColumn = StringUtils.defaultIfEmpty(sortColumn, "datasource_id");
		return datasourceDescriptionDao.getPaginatedDataSources(companyId, sortColumn, pageNumber, pageSize, isAscending);
	}
	
	private int getDataSourceId(int companyId, int dsGroup, String dsDescription) {
		DatasourceDescription dataSource = datasourceDescriptionDao.getByDescription(dsGroup, companyId, dsDescription);
		
		return Objects.nonNull(dataSource) ? dataSource.getId() : 0;
	}
	
	private boolean isDataSourceExist(int companyId, int dsGroup, String dsDescription) {
		return getDataSourceId(companyId, dsGroup, dsDescription) > 0;
	}

	@Required
	public void setDatasourceDescriptionDao(ComDatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = datasourceDescriptionDao;
	}
	
	@Required
	public void setWebserviceUserDao(WebserviceUserDao webserviceUserDao) {
		this.webserviceUserDao = webserviceUserDao;
	}
}
