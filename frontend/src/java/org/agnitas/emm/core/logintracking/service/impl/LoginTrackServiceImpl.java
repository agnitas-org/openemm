/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.loginmanager.entity.BlockedAddressData;
import com.agnitas.emm.util.SortDirection;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.util.OneOf;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.logintracking.LoginStatus;
import org.agnitas.emm.core.logintracking.bean.LoginData;
import org.agnitas.emm.core.logintracking.bean.LoginTrackData;
import org.agnitas.emm.core.logintracking.bean.LoginTrackSettings;
import org.agnitas.emm.core.logintracking.dao.LoginTrackDao;
import org.agnitas.emm.core.logintracking.dao.LoginTrackSettingsDao;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.util.Const;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of {@link LoginTrackService}.
 */
public class LoginTrackServiceImpl implements LoginTrackService {
	/**
	 * The logger.
	 */
	private static final transient Logger logger = LogManager.getLogger(LoginTrackServiceImpl.class);
	
	/**
	 * DAO for login tracking.
	 */
	private LoginTrackDao loginTrackDao;
	
	private LoginTrackSettingsDao settingsDao;

	/**
	 * Set DAO for login tracking.
	 *
	 * @param dao DAO for login tracking
	 */
	@Required
	public final void setLoginTrackDao(final LoginTrackDao dao) {
		this.loginTrackDao = Objects.requireNonNull(dao, "LoginTrackDao is null");
	}
	
	@Required
	public final void setLoginTrackSettingsDao(final LoginTrackSettingsDao dao) {
		this.settingsDao = Objects.requireNonNull(dao, "Login track settings DAO is null");
	}
	
	// -------------------------------------------------------------------------------------------------- Business Code
	@Override
	public void trackLoginSuccessful(final String ipAddress, final String username) {
		this.loginTrackDao.trackLoginStatus(ipAddress, username, LoginStatus.SUCCESS);
	}
	
	@Override
	public void trackLoginFailed(final String ipAddress, final String username) {
		this.loginTrackDao.trackLoginStatus(ipAddress, username, LoginStatus.FAIL);
	}
	
	@Override
	public void trackLoginSuccessfulButBlocked(final String ipAddress, final String username) {
		this.loginTrackDao.trackLoginStatus(ipAddress, username, LoginStatus.SUCCESS_BUT_BLOCKED);
	}
	
	/**
	 * Reads the login tracking data recorded for the IP address since given time (or all if given timestamp is <code>null</code>).
	 * 
	 * @param ipAddress IP address
	 * @param sinceOrNull timestamp or <code>null</code>
	 * 
	 * @return login tracking data
	 */
	private LoginTrackData readLoginTrackDataByIpAddress(final String ipAddress, final Date sinceOrNull) {
		final List<LoginData> loginTrackList = this.loginTrackDao.listLoginDataByIpAddress(ipAddress, sinceOrNull);
		
		return LoginTrackData.from(loginTrackList);
	}
	
	/**
	 * Reads the login tracking data recorded for the user name since given time (or all if given timestamp is <code>null</code>).
	 * 
	 * @param username user name
	 * @param sinceOrNull timestamp or <code>null</code>
	 * 
	 * @return login tracking data
	 */
	private LoginTrackData readLoginTrackDataByUsername(final String username, final Date sinceOrNull) {
		final List<LoginData> loginTrackList = this.loginTrackDao.listLoginDataByUsername(username, sinceOrNull);
		
		return LoginTrackData.from(loginTrackList);
	}

	@Override
	public boolean isIpAddressLocked(final String ipAddress, final int companyID) {
		final LoginTrackSettings settings = this.settingsDao.readLoginTrackSettings(companyID);
		
		return isIpAddressLocked(ipAddress, settings);
	}

	/**
	 * Checks, if given IP address is locked.
	 * 
	 * @param ipAddress IP address to check
	 * @param settings lock settings
	 * 
	 * @return <code>true</code> if IP address is locked, otherwise <code>false</code>
	 */
	private boolean isIpAddressLocked(final String ipAddress, final LoginTrackSettings settings) {
		// Compute time values
		final Date now = new Date();
		final long nowMillis = now.getTime();
		final Date observationStartTime = new Date(nowMillis - settings.getObservationTimeSeconds() * 1000);

		// Read the login track data
		final LoginTrackData allData = this.readLoginTrackDataByIpAddress(ipAddress, observationStartTime);
		final LoginTrackData failureData = allData.trimToFailuresBeforeSuccessfulLogin();
		
		// No failed login? IP is not locked
		if(failureData.isEmpty()) {
			return false;
		}
		
		// Count number of failed logins in observation period
		final long count = failureData.stream()
				.filter(data -> data.getLoginTime().after(observationStartTime))
				.count();
		
		// Count below maximum allowed number of failed logins? IP is not locked
		if(count <= settings.getMaxFailedLogins()) {
			return false;
		}
		
		// Maximum exceeded, check if last login far enough in the past
		final Date lockTimeEnd = new Date(failureData.getLoginData(0).getLoginTime().getTime() + settings.getLockTimeSeconds() * 1000);
		return !now.after(lockTimeEnd);
	}

	@Override
	public int countFailedLoginsSinceLastSuccess(final String username, final boolean skipFirstIfSuccess) {
		final LoginTrackData allData = this.readLoginTrackDataByUsername(username, null);

		final int startIndex = skipFirstIfSuccess && !allData.isEmpty() && allData.getLoginData(0).getLoginStatus() == LoginStatus.SUCCESS ? 1 : 0;
		
		int count = 0;
		for(int i = startIndex; i < allData.size(); i++) {
			final LoginData data = allData.getLoginData(i);
			
			if(data.getLoginStatus() == LoginStatus.FAIL || data.getLoginStatus() == LoginStatus.SUCCESS_BUT_BLOCKED) {
				count++;
			}
			
			if(data.getLoginStatus() == LoginStatus.SUCCESS) {
				break;
			}
		}
		
		return count;
	}
	
	@Override
	public Optional<LoginData> findLastSuccessfulLogin(String username, boolean skipFirstIfSuccess) {
		final LoginTrackData allData = this.readLoginTrackDataByUsername(username, null);
		final int startIndex = skipFirstIfSuccess && !allData.isEmpty() && allData.getLoginData(0).getLoginStatus() == LoginStatus.SUCCESS ? 1 : 0;
		
		return allData.stream()
				.skip(startIndex)
				.filter(loginData -> loginData.getLoginStatus() == LoginStatus.SUCCESS)
				.findFirst();
	}

	@Override
	public List<LoginData> listLoginAttemptsSince(final String username, final Date sinceOrNull) {
		final LoginTrackData allData = this.readLoginTrackDataByUsername(username, sinceOrNull);
		
		return allData.stream()
				.filter(data -> OneOf.oneObjectOf(data.getLoginStatus(), LoginStatus.SUCCESS, LoginStatus.FAIL, LoginStatus.SUCCESS_BUT_BLOCKED))
				.collect(Collectors.toList());
	}
	
	@Override
	public Optional<BlockedAddressData> findBlockedAddressByTrackingID(long trackingId) {
		final Optional<LoginData> optionalData = loginTrackDao.findLoginDataByTrackingID(trackingId);
		
		if(optionalData.isPresent()) {
			final LoginData data = optionalData.get();
			
			if(data.getLoginStatus() == LoginStatus.SUCCESS_BUT_BLOCKED) {
				return BlockedAddressData.fromLoginData(optionalData.get());
			} else {
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	// TODO: remove after EMMGUI-714 will be finished and old design will be removed
	public boolean unlockBlockedAddressByTrackingId(long blockedAddressId) {
		final Optional<BlockedAddressData> optionalData = findBlockedAddressByTrackingID(blockedAddressId);
		
		if(optionalData.isPresent()) {
			unlockBlockedAddress(optionalData.get());
			return true;
		}
		
		return false;
	}

	private void unlockBlockedAddress(BlockedAddressData entry) {
		final String usernameOrNull = entry.getUsername().orElse(null);
		loginTrackDao.trackLoginStatus(entry.getIpAddress(), usernameOrNull, LoginStatus.UNLOCKED);
	}

	@Override
	public List<BlockedAddressData> listBlockedIpAddresses() {
		/*
		 * TODO This algorithm need improvement.
		 * 
		 * It is not possible to detect blocked IP addresses without further information (IP-address, username or company ID) accurately.
		 * We can detect possibly blocked IP only by finding a SUCCESS_BUT_BLOCKED record without succeeding SUCCESS or UNLOCK.
		 */
		final List<LoginData> allLoginData = this.loginTrackDao.listLoginData(null);
		
		// All IP addresses with SUCCESS_BUT_BLOCKED record
		final Set<String> ipAddressesWithBlockedState = allLoginData.stream()
				.filter(data -> data.getLoginStatus() == LoginStatus.SUCCESS_BUT_BLOCKED)
				.map(data -> data.getLoginIP())
				.distinct()
				.collect(Collectors.toSet());
		
		final List<LoginData> filteredAndSortedLoginData = allLoginData.stream()
				.filter(data -> ipAddressesWithBlockedState.contains(data.getLoginIP()))
				.sorted((x,y) -> -x.getLoginTime().compareTo(y.getLoginTime()))	// Sort descending by login time
				.collect(Collectors.toList());
		
		final List<BlockedAddressData> result = new ArrayList<>();
		
		// Iterate over all IP addresses
		for(final String ipAddress : ipAddressesWithBlockedState) {
			// Find (newest) login track record
			final Optional<LoginData> loginDataOptional = filteredAndSortedLoginData.stream().filter(data -> ipAddress.equals(data.getLoginIP())).findFirst();
			
			// If no login data, then IP cannot be locked
			if(loginDataOptional.isPresent()) {
				final LoginData loginData = loginDataOptional.get();

				// IP-Address can only be locked for login status FAIL and SUCCESS_BUT_BLOCKED. UNLOCK or SUCCESS indicates an non-locked IP.
				if(OneOf.oneObjectOf(loginData.getLoginStatus(), LoginStatus.FAIL, LoginStatus.SUCCESS_BUT_BLOCKED)) {
					final Optional<LoginData> blockRecordOptional = filteredAndSortedLoginData.parallelStream().filter(data -> ipAddress.equals(data.getLoginIP()) && data.getLoginStatus() == LoginStatus.SUCCESS_BUT_BLOCKED).findFirst();
					
					assert blockRecordOptional.isPresent(); // Ensured by creation of filteredAndSortedLoginData. All records belongs to IP addresses that have at least one record with a SUCCESS_BUT_BLOCKED state
					final LoginData blockRecord = blockRecordOptional.get();
					assert ipAddress.equals(blockRecord.getLoginIP());
					
					final String usernameOrNull = blockRecord.getUsername().orElse(null);
					
					result.add(new BlockedAddressData(blockRecord.getLoginTrackId(), ipAddress, usernameOrNull));
				}
			}
		}
		
		return result;
	}
	
	// -------------------------------------------------------------------------------------------------- Business Code (deprecated methods)
	
	@Override
	public PaginatedListImpl<BlockedAddressData> getBlockedIPListAfterSuccessfulLogin(final LoginTrackSortCriterion criterion, final SortDirection direction, final int pageNumber0, final int pageSize0) {
		final List<BlockedAddressData> list = listBlockedIpAddresses();
		
		final int pageNumber = Math.max(pageNumber0, 1);
		final int pageSize = Math.max(pageSize0, 1);
		
		final int skip = (pageNumber - 1) * pageSize;
		
		final Comparator<BlockedAddressData> comparator = blockedAddressComparatorFrom(criterion, direction);
		
		final List<BlockedAddressData> sublist = list.stream().sorted(comparator).skip(skip).limit(pageSize).collect(Collectors.toList());
		
		return new PaginatedListImpl<>(sublist, list.size(), pageSize, pageNumber, criterion.getId(), direction.getId());
	}

	@Override
	public List<String> getBlockedIpAddresses(Set<Integer> ids) {
		return filterExistingBlockedAddresses(ids)
				.stream()
				.map(BlockedAddressData::getIpAddress)
				.collect(Collectors.toList());
	}

	@Override
	public ServiceResult<UserAction> unlockAddresses(Set<Integer> ids, Admin admin) {
		List<BlockedAddressData> addresses = filterExistingBlockedAddresses(ids);
		addresses.forEach(this::unlockBlockedAddress);

		List<String> ipAddresses = addresses.stream().map(BlockedAddressData::getIpAddress).collect(Collectors.toList());

		return ServiceResult.success(
				new UserAction(
						"unlock IPs",
						"IP addresses: " + StringUtils.join(ipAddresses, ", ")
				),
				Message.of(Const.Mvc.SELECTION_DELETED_MSG)
		);
	}

	private List<BlockedAddressData> filterExistingBlockedAddresses(Set<Integer> ids) {
		return ids.stream()
				.map(this::findBlockedAddressByTrackingID)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	private static Comparator<BlockedAddressData> blockedAddressComparatorFrom(final LoginTrackSortCriterion criterion, final SortDirection direction) {
		final Comparator<BlockedAddressData> comparator = blockedAddressComparatorFrom(criterion);
		
		switch(direction) {
		default:
			logger.error(String.format("No comparator for sort direction %s", direction));
			// Fall-through to ASCENDING
			//$FALL-THROUGH$
		case ASCENDING:
			return comparator;
			
		case DESCENDING:
			return (x,y) -> -comparator.compare(x, y);
		}
	}
	
	private static final Comparator<BlockedAddressData> blockedAddressComparatorFrom(final LoginTrackSortCriterion criterion) {
		switch(criterion) {
		case IP_ADDRESS:
			return (x,y) -> x.getIpAddress().compareTo(y.getIpAddress());

		default:
			logger.error(String.format("No comparator for sort criterion %s", criterion));
			// Fall-through to username
			//$FALL-THROUGH$
		case USERNAME:
			return (x,y) -> {
				if(x.getUsername().isPresent()) {
					if(y.getUsername().isPresent()) {
						return x.getUsername().get().compareTo(y.getUsername().get());
					} else {
						return 1;
					}
				} else {
					if(y.getUsername().isPresent()) {
						return -1;
					} else {
						return 0;
					}
				}
			};
		}
	}
}
