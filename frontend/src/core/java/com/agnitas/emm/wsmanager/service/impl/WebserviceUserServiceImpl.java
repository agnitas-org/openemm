/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue.Webservices;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;
import com.agnitas.emm.core.wsmanager.dto.WebserviceUserDto;
import com.agnitas.emm.core.wsmanager.dto.WebserviceUserEntryDto;
import com.agnitas.emm.core.wsmanager.form.WebserviceUserOverviewFilter;
import com.agnitas.emm.wsmanager.bean.WebservicePermissions;
import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.exception.WebserviceUserNotFoundException;
import com.agnitas.emm.wsmanager.common.WebserviceUser;
import com.agnitas.emm.wsmanager.common.WebserviceUserCredential;
import com.agnitas.emm.wsmanager.common.WebserviceUserListItem;
import com.agnitas.emm.wsmanager.common.impl.WebserviceUserCredentialImpl;
import com.agnitas.emm.wsmanager.dao.WebserviceUserDao;
import com.agnitas.emm.wsmanager.dao.WebserviceUserSettingsDao;
import com.agnitas.emm.wsmanager.exception.WebserviceUserCreateException;
import com.agnitas.emm.wsmanager.exception.WebserviceUserUpdateException;
import com.agnitas.emm.wsmanager.service.WebservicePermissionService;
import com.agnitas.emm.wsmanager.exception.WebserviceUserAlreadyExistsException;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("webserviceUserService")
public class WebserviceUserServiceImpl implements WebserviceUserService {

	private static final Logger logger = LogManager.getLogger(WebserviceUserServiceImpl.class);

	private static final String USER_DESCRIPTION_PATTERN = "WS2-User \"%s\"";
    private static final String DATA_SOURCE_URI = "";

	/** DAO accessing webservice user data. */
	private final WebserviceUserDao webserviceUserDao;
	
	/** DAO accessing settings. */
	private final WebserviceUserSettingsDao webserviceUserSettingsDao;
	
	/** Service handling data sources. */
	private final DataSourceService datasourceService;
	
	/** Configuration service. */
	private final ConfigService configService;

	private final ExtendedConversionService conversionService;
	
	private final WebservicePermissionService permissionService;
	
	public WebserviceUserServiceImpl(final WebserviceUserDao webserviceUserDao, final WebserviceUserSettingsDao webserviceUserSettingsDao, final DataSourceService datasourceService, final ConfigService configService, final ExtendedConversionService conversionService, final WebservicePermissionService permissionService) {
		this.webserviceUserDao = Objects.requireNonNull(webserviceUserDao);
		this.webserviceUserSettingsDao = Objects.requireNonNull(webserviceUserSettingsDao, "Webservice User Settings DAO is null");
		this.datasourceService = Objects.requireNonNull(datasourceService);
		this.configService = Objects.requireNonNull(configService);
		this.conversionService = Objects.requireNonNull(conversionService);
		this.permissionService = Objects.requireNonNull(permissionService, "Webservice permission service is null");
	}
	
	@Override
	public PaginatedList<WebserviceUserEntryDto> getPaginatedWSUserList(int companyID, String sort, String direction, int page, int rownums, boolean masterView) {
		PaginatedList<WebserviceUserListItem> listFromDb;
		if (masterView) {
			listFromDb = webserviceUserDao.getWebserviceUserMasterList(sort, AgnUtils.sortingDirectionToBoolean(direction), page, rownums);
		} else {
			listFromDb = webserviceUserDao.getWebserviceUserList(companyID, sort, AgnUtils.sortingDirectionToBoolean(direction), page, rownums);
		}

		return conversionService.convertPaginatedList(listFromDb, WebserviceUserListItem.class, WebserviceUserEntryDto.class);
	}

	@Override
	public PaginatedList<WebserviceUserEntryDto> getPaginatedWSUserList(WebserviceUserOverviewFilter filter, Admin admin) {
		if (!admin.permissionAllowed(Permission.MASTER_SHOW)) {
			filter.setCompanyId(admin.getCompanyID());
		}

		PaginatedList<WebserviceUserListItem> listFromDb = webserviceUserDao.getWebserviceUserList(filter);
		return conversionService.convertPaginatedList(listFromDb, WebserviceUserListItem.class, WebserviceUserEntryDto.class);
	}

	@Transactional
	protected void createWebserviceUser(WebserviceUserDto user) {
		String username = user.getUserName();
		if (webserviceUserDao.webserviceUserExists(username)) {
			logger.info("Webservice user '{}' already exists", username);
			throw new WebserviceUserAlreadyExistsException(username);
		}

		int dataSourceId = 0;
		try {
			WebserviceUserCredential convertedUser = conversionService.convert(user, WebserviceUserCredential.class);

			int companyId = convertedUser.getCompanyID();
			String dsDescription = String.format(USER_DESCRIPTION_PATTERN, username);

			dataSourceId = datasourceService.createDataSource(companyId, SourceGroupType.SoapWebservices, dsDescription, DATA_SOURCE_URI);

			((WebserviceUserCredentialImpl)convertedUser).setDefaultDatasourceID(dataSourceId);

			final int bulkSizeLimit = this.configService.getWebserviceBulkSizeLimit(user.getCompanyId());

			logger.info("Created datasource ID {} as default datasource for webservice user {}", convertedUser.getDefaultDatasourceID(), username);

			webserviceUserDao.createWebserviceUser(convertedUser, dataSourceId, bulkSizeLimit);
			saveGrantedPermissionsAndGroups(convertedUser);
		} catch (Exception e) {
			datasourceService.rolloutCreationDataSource(dataSourceId, username, user.getCompanyId());
			throw new WebserviceUserCreateException("Error creating new webservice user: " + username, e);
		}
	}

	@Transactional
	protected void updateWebserviceUser(WebserviceUserDto user) {
		String userName = user.getUserName();
		if(StringUtils.isEmpty(userName) || !webserviceUserDao.webserviceUserExists(userName)) {
			throw new WebserviceUserNotFoundException(userName);
		}

		WebserviceUserCredential convertedUser = conversionService.convert(user, WebserviceUserCredential.class);

		try {
			this.webserviceUserDao.updateUser(convertedUser);

			saveGrantedPermissionsAndGroups(convertedUser);

			String passwordHash = convertedUser.getPasswordHash();

			if(StringUtils.isNotEmpty(passwordHash)) {
				this.webserviceUserDao.updatePasswordHash(convertedUser.getUsername(), passwordHash);
			}
		} catch (Exception e) {
			throw new WebserviceUserUpdateException("Error updating webservice user " + user, e);
		}
	}

	@Transactional
	@Override
	public void saveWebServiceUser(Admin admin, WebserviceUserDto user, boolean isNew) {
		if (!admin.permissionAllowed(Permission.MASTER_COMPANIES_SHOW)) {
            user.setCompanyId(admin.getCompanyID());
        }
		
		if(isNew) {
			createWebserviceUser(user);
		} else {
			updateWebserviceUser(user);
		}
	}

	@Override
	public WebserviceUserDto getWebserviceUserByUserName(String username) {
		WebserviceUser user = webserviceUserDao.getWebserviceUser(username);
		if (user == null) {
			throw new WebserviceUserNotFoundException(username);
		}

		return conversionService.convert(user, WebserviceUserDto.class);
	}

	@Override
	public int getNumberOfWebserviceUsers(int companyID) {
		return webserviceUserDao.getNumberOfWebserviceUsers(companyID);
	}
	
	private void sanitizePermissions(WebserviceUser user) {
		final WebservicePermissions allPermissions = this.permissionService.listAllPermissions();
		final Set<String> allPermissionNames = allPermissions.getAllPermissions().stream().map(perm -> perm.getEndpointName()).collect(Collectors.toSet());
		
		// Retain only those permissions listed in database
		user.getGrantedPermissions().retainAll(allPermissionNames);
	}
	
	private void saveGrantedPermissionsAndGroups(WebserviceUser user) {
		// Do not modify permissions, if permissions are disabled
		if(configService.getBooleanValue(Webservices.WebserviceEnablePermissions, user.getCompanyID())) {
			sanitizePermissions(user);
			this.webserviceUserDao.saveGrantedPermissionsAndGroups(user);
		}
	}

	@Override
	public Optional<WebserviceUserSettings> findSettingsForWebserviceUser(String username) {
		return webserviceUserSettingsDao.findSettingsForWebserviceUser(username);
	}

	@Override
	public boolean deleteWebserviceUser(String username) {
		return webserviceUserDao.deleteWebserviceUser(username);
	}

	@Override
	public boolean webserviceUserExists(String username) {
		return webserviceUserDao.webserviceUserExists(username);
	}

	@Override
	public List<String> getUsernames(Integer companyId) {
		if (companyId == null) {
			return webserviceUserDao.getUsernames();
		}

		return webserviceUserDao.getUsernames(companyId);
	}

}
