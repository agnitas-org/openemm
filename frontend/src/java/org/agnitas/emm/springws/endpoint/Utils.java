/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.jaxb.Map;
import org.agnitas.emm.springws.jaxb.MapItem;
import org.agnitas.emm.springws.jaxb.ObjectFactory;
import org.agnitas.emm.springws.security.authorities.CompanyAuthority;
import org.agnitas.service.UserActivityLogService;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.w3c.dom.Element;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.impl.ComAdminImpl;
import com.agnitas.dao.ComAdminDao;
import com.agnitas.emm.springws.endpoint.BulkSizeLimitExeededExeption;

public class Utils {
	
	public static CaseInsensitiveMap<String, Object> toCaseInsensitiveMap(Map map, final boolean extractStringFromSubXml) {
		if (map == null || map.getItem() == null) {
			return null;
		}
		CaseInsensitiveMap<String, Object> resultMap = new CaseInsensitiveMap<>(map.getItem().size());
		
		for (MapItem item : map.getItem()) {
			final String key = (item.getKey() instanceof Element && extractStringFromSubXml) ? stringFromSubXml((Element) item.getKey()) : (String) item.getKey();
			final Object value = (item.getValue() instanceof Element && extractStringFromSubXml) ? stringFromSubXml((Element) item.getValue()) : item.getValue();
			
			resultMap.put(key, value);
		}
		return resultMap;
	}
	
	private static final String stringFromSubXml(final Element object) {
		return object.getTextContent();
	}
	
	public static final boolean isAuthorityGranted(final GrantedAuthority authority) {
		final Collection<? extends GrantedAuthority> allAuthorities = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getAuthorities();

		return allAuthorities.contains(authority);
	}
	
	public static int getUserCompany() {
		final Collection<? extends GrantedAuthority> allAuthorities = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getAuthorities();
		
		final Optional<? extends GrantedAuthority> authorityOptional = allAuthorities.stream().filter(auth -> auth instanceof CompanyAuthority).findFirst();
		
		return authorityOptional.isPresent()
				? Integer.valueOf(((CompanyAuthority) authorityOptional.get()).getCompanyID())
				: -1;
	}

    public static String getUserName(){
        return ((UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
    }

    private static ComAdmin getAdminForUserActivityLog() {
		ComAdmin admin = new ComAdminImpl();
		admin.setUsername(getUserName());
		return admin;
	}

	public static void writeLog(UserActivityLogService service, List<UserAction> userActions) {
		ComAdmin admin = Utils.getAdminForUserActivityLog();
		for (UserAction action : userActions) {
			service.writeUserActivityLog(admin, action);
		}
	}

	public static Map toJaxbMap(java.util.Map<String, Object> map, ObjectFactory objectFactory) {
		if (map == null) {
			return null;
		}
		Map resultMap = objectFactory.createMap();
		for (Entry<String, Object> entry : map.entrySet()) {
			MapItem mapItem = objectFactory.createMapItem();
			mapItem.setKey(entry.getKey());
			mapItem.setValue(entry.getValue());
			resultMap.getItem().add(mapItem);
		}
		return resultMap;
	}
	
	public static void checkBulkSizeLimit(String endpointName, ComAdminDao comAdminDao, int size) throws BulkSizeLimitExeededExeption {
		String username = Utils.getUserName();
		Integer bulkSizeLimit = comAdminDao.getWsBulkSizeLimit(username);
		if (bulkSizeLimit != null && bulkSizeLimit != 0 && bulkSizeLimit < size) {
			throw new BulkSizeLimitExeededExeption(endpointName, 
					username, bulkSizeLimit, size);
		}
	}
	
}
