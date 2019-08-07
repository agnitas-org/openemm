/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.service.impl;

import java.util.Optional;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto;
import com.agnitas.emm.core.wsmanager.dto.WebserviceUserEntryDto;
import com.agnitas.emm.wsmanager.common.UnknownWebserviceUsernameException;
import com.agnitas.emm.wsmanager.common.WebserviceUserCredential;
import com.agnitas.emm.wsmanager.common.WebserviceUserException;
import com.agnitas.emm.wsmanager.common.WebserviceUserListItem;
import com.agnitas.emm.wsmanager.common.impl.WebserviceUserCredentialImpl;
import com.agnitas.emm.wsmanager.dao.WebserviceUserDao;
import com.agnitas.emm.wsmanager.dao.WebserviceUserDaoException;
import com.agnitas.emm.wsmanager.service.WebserviceUserAlreadyExistsException;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;
import com.agnitas.emm.wsmanager.service.WebserviceUserServiceException;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.ExtendedConversionService;

/**
 * Implementation of {@link WebserviceUserService} interface.
 */
@Service("WebserviceUserService")
public class WebserviceUserServiceImpl implements WebserviceUserService {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(WebserviceUserServiceImpl.class);

	private static final String USER_DESCRIPTION_PATTERN = "WS2-User \"%s\"";
    private static final String DATA_SOURCE_URI = "";

	/** DAO accessing webservice user data. */
	private WebserviceUserDao webserviceUserDao;
	
	/** Service handling data sources. */
	private DataSourceService datasourceService;
	
	/** Configuration service. */
	private ConfigService configService;

	private ExtendedConversionService conversionService;
	
	public WebserviceUserServiceImpl(WebserviceUserDao webserviceUserDao, DataSourceService datasourceService, ConfigService configService, ExtendedConversionService conversionService) {
		this.webserviceUserDao = webserviceUserDao;
		this.datasourceService = datasourceService;
		this.configService = configService;
		this.conversionService = conversionService;
	}
	
	@Override
	public PaginatedListImpl<WebserviceUserEntryDto> getPaginatedWSUserList(int companyID, String sort, String direction, int page, int rownums, boolean masterView) throws WebserviceUserServiceException {

		try {
			PaginatedListImpl<WebserviceUserListItem> listFromDb;
			if (masterView) {
				listFromDb = webserviceUserDao.getWebserviceUserMasterList(sort, AgnUtils.sortingDirectionToBoolean(direction), page, rownums);
			} else {
				listFromDb = webserviceUserDao.getWebserviceUserList(companyID, sort, AgnUtils.sortingDirectionToBoolean(direction), page, rownums);
			}

			return conversionService.convertPaginatedList(listFromDb, WebserviceUserListItem.class, WebserviceUserEntryDto.class);
		} catch(WebserviceUserDaoException e) {
			logger.error("Error accessing webservice user list", e);

			throw new WebserviceUserServiceException("Error accessing webservice user list", e);
		}
	}

	@Override
	@Transactional
	public void createWebserviceUser(WebserviceUserDto user) throws WebserviceUserException, WebserviceUserServiceException {
		String username = user.getUserName();
		if (webserviceUserDao.webserviceUserExists(username)) {
			logger.info("Webservice user '" + username + "' already exists");
			throw new WebserviceUserAlreadyExistsException(username);
		}
		
		int dataSourceId = 0;
		try {
			WebserviceUserCredential convertedUser = conversionService.convert(user, WebserviceUserCredential.class);
			
			int companyId = convertedUser.getCompanyID();
			int dsGroup = configService.getIntegerValue(ConfigValue.WebserviceDatasourceGroupId);
			String dsDescription = String.format(USER_DESCRIPTION_PATTERN, username);
			
			dataSourceId = datasourceService.createDataSource(companyId, dsGroup, dsDescription, DATA_SOURCE_URI);
			
			((WebserviceUserCredentialImpl)convertedUser).setDefaultDatasourceID(dataSourceId);

			final int bulkSizeLimit = this.configService.getWebserviceBulkSizeLimit(user.getCompanyId());

			logger.info(String.format("Created datasource ID %d as default datasource for webservice user %s", convertedUser.getDefaultDatasourceID(), username));

			webserviceUserDao.createWebserviceUser(convertedUser, dataSourceId, bulkSizeLimit);
		} catch (Exception e) {
			logger.error("Error creating new webservice user: " + username, e);
			
			if(dataSourceId > 0) {
				datasourceService.rolloutCreationDataSource(dataSourceId, username, user.getCompanyId());
			}

			throw new WebserviceUserServiceException("Error creating new webservice user: " + username, e);
		}
	}

	@Override
	public void updateWebserviceUser(WebserviceUserDto user) throws WebserviceUserException, WebserviceUserServiceException {
		String userName = user.getUserName();
		if(StringUtils.isEmpty(userName) || !webserviceUserDao.webserviceUserExists(userName)) {
			throw new UnknownWebserviceUsernameException(userName);
		}

		WebserviceUserCredential convertedUser = conversionService.convert(user, WebserviceUserCredential.class);

		try {
			this.webserviceUserDao.updateUser(convertedUser);

			String passwordHash = convertedUser.getPasswordHash();

			if(StringUtils.isNotEmpty(passwordHash)) {
				this.webserviceUserDao.updatePasswordHash(convertedUser.getUsername(), passwordHash);
			}
		} catch(WebserviceUserDaoException e) {
			logger.error("Error updating webservice user " + user, e);

			throw new WebserviceUserServiceException("Error updating webservice user " + user, e);
		}
	}

	@Override
	public void saveWebServiceUser(WebserviceUserDto user, boolean isNew) throws WebserviceUserException, WebserviceUserServiceException {
		if(isNew) {
			createWebserviceUser(user);
		} else {
			updateWebserviceUser(user);
		}
	}

	@Override
	public WebserviceUserDto getWebserviceUserByUserName(String username) throws WebserviceUserServiceException, WebserviceUserException {
		try {
			WebserviceUserDto webserviceUser = conversionService.convert(webserviceUserDao.getWebserviceUser(username), WebserviceUserDto.class);
			return Optional.ofNullable(webserviceUser).orElse(new WebserviceUserDto());
		} catch(WebserviceUserDaoException e) {
			logger.error("Error accessing webservice user " + username, e);

			throw new WebserviceUserServiceException("Error accessing webservice user " + username, e);
		}
	}

	@Override
	public int getNumberOfWebserviceUsers() {
		return webserviceUserDao.getNumberOfWebserviceUsers();
	}
}
