/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.loginmanager.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.emm.core.loginmanager.enums.LoginStatus;

public final class LoginTrackData implements Iterable<LoginData> {

	private final List<LoginData> list;
	
	/**
	 * Do not use this constructor directly from outside this class.
	 * 
	 * @param loginDataList data sorted by login time descending
	 * 
	 * @see #from(List)
	 */
	private LoginTrackData(final List<LoginData> loginDataList) {
		this.list = loginDataList;
	}
	
	public static LoginTrackData from(List<LoginData> loginDataList) {
		return new LoginTrackData(copyAndSortList(loginDataList));
	}
	
	private static List<LoginData> copyAndSortList(List<LoginData> list) {
		final List<LoginData> newList = new ArrayList<>(list);
		
		// Sort by login time descending (newest first)
		newList.sort((d0, d1) -> -d0.getLoginTime().compareTo(d1.getLoginTime()));
		
		return newList;
	}

	@Override
	public Iterator<LoginData> iterator() {
		return list.iterator();
	}
	
	public Stream<LoginData> stream() {
		return list.stream();
	}
	
	public boolean isEmpty() {
		return this.list.isEmpty();
	}
	
	public int size() {
		return this.list.size();
	}
	
	public LoginData getLoginData(int index) {
		return this.list.get(index);
	}
	
	public LoginTrackData trimToFailuresBeforeSuccessfulLogin() {
		final int size = this.list.size();
		
		int lastIndex = size;

		// Find newest SUCCESS or UNLOCK
		for(int i = 0; i < size; i++) {
			final LoginData data = this.list.get(i);
			
			if(data.getLoginStatus() == LoginStatus.SUCCESS || data.getLoginStatus() == LoginStatus.UNLOCKED) {
				lastIndex = i;
				break;
			}
		}

		// Remove anything but FAIL
		final List<LoginData> newList = this.list.subList(0, lastIndex).stream()
				.filter(loginData -> loginData.getLoginStatus() == LoginStatus.FAIL)
				.collect(Collectors.toList());
		
		return new LoginTrackData(newList);
	}
	
}
