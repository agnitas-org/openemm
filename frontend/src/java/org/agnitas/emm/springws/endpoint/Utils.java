/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.endpoint;

import java.util.List;
import java.util.Map.Entry;
import java.util.OptionalInt;

import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.jaxb.Map;
import org.agnitas.emm.springws.jaxb.MapItem;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.agnitas.emm.springws.util.UserActivityLogAccess;
import org.agnitas.service.UserActivityLogService;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.w3c.dom.Element;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.impl.ComAdminImpl;
import com.agnitas.emm.springws.WebserviceUserDetails;
import com.agnitas.emm.springws.exception.BulkSizeLimitExeededExeption;
import com.agnitas.emm.wsmanager.bean.WebserviceUserSettings;
import com.agnitas.emm.wsmanager.service.WebserviceUserService;

public class Utils {

	public static final String NAMESPACE_ORG = "http://agnitas.org/ws/schemas";
	public static final String NAMESPACE_COM = "http://agnitas.com/ws/schemas";

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

	public static CaseInsensitiveMap<String, Object> toCaseInsensitiveComMap(com.agnitas.emm.springws.jaxb.Map map, final boolean extractStringFromSubXml) {
		if (map == null || map.getItem() == null) {
			return null;
		}
		CaseInsensitiveMap<String, Object> resultMap = new CaseInsensitiveMap<>(map.getItem().size());
		
		for (com.agnitas.emm.springws.jaxb.MapItem item : map.getItem()) {
			final String key = (item.getKey() instanceof Element && extractStringFromSubXml) ? stringFromSubXml((Element) item.getKey()) : (String) item.getKey();
			final Object value = (item.getValue() instanceof Element && extractStringFromSubXml) ? stringFromSubXml((Element) item.getValue()) : item.getValue();
			
			resultMap.put(key, value);
		}
		return resultMap;
	}
	
	private static final String stringFromSubXml(final Element object) {
		return object.getTextContent();
	}
	
	/**
	 * @see SecurityContextAccess#isAuthorityGranted(GrantedAuthority)
	 */
	@Deprecated	// TODO Remove after migrating all endpoints to use SecurityContextAccess
	public static final boolean isAuthorityGranted(final GrantedAuthority authority) {
		return getWebserviceUserDetails().getAuthorities().contains(authority);
	}
	
	/**
	 * @see SecurityContextAccess#getWebserviceUserCompanyId() 
	 */
	@Deprecated	// TODO Remove after migrating all endpoints to use SecurityContextAccess
	public static int getUserCompany() {
		return getWebserviceUserDetails().getCompanyID();
	}

	/**
	 * @see SecurityContextAccess#getWebserviceUserName() 
	 */
	@Deprecated	// TODO Remove after migrating all endpoints to use SecurityContextAccess
    public static String getUserName(){
    	return getWebserviceUserDetails().getUsername();
    }
    
	/**
	 * @see SecurityContextAccess#getWebserviceUserDetails() 
	 */
	@Deprecated	// TODO Remove after migrating all endpoints to use SecurityContextAccess
    public static final WebserviceUserDetails getWebserviceUserDetails() {
    	return ((WebserviceUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

	@Deprecated // TODO Remove after migrating all endpoints to use SecurityContextAccess
    private static ComAdmin getAdminForUserActivityLog() {
		ComAdmin admin = new ComAdminImpl();
		admin.setUsername(getUserName());
		return admin;
	}

	/**
	 * @see UserActivityLogAccess#writeLog(List)
	 */
	@Deprecated // TODO Remove after migrating all endpoints to use SecurityContextAccess
	public static void writeLog(UserActivityLogService service, List<UserAction> userActions) {
		ComAdmin admin = Utils.getAdminForUserActivityLog();
		for (UserAction action : userActions) {
			service.writeUserActivityLog(admin, action);
		}
	}

	public static Map toJaxbMap(java.util.Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		Map resultMap = new Map();
		for (Entry<String, Object> entry : map.entrySet()) {
			MapItem mapItem = new MapItem();
			mapItem.setKey(entry.getKey());
			mapItem.setValue(entry.getValue());
			resultMap.getItem().add(mapItem);
		}
		return resultMap;
	}

	public static com.agnitas.emm.springws.jaxb.Map toJaxbComMap(java.util.Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		com.agnitas.emm.springws.jaxb.Map resultMap = new com.agnitas.emm.springws.jaxb.Map();
		for (Entry<String, Object> entry : map.entrySet()) {
			com.agnitas.emm.springws.jaxb.MapItem mapItem = new com.agnitas.emm.springws.jaxb.MapItem();
			mapItem.setKey(entry.getKey());
			mapItem.setValue(entry.getValue());
			resultMap.getItem().add(mapItem);
		}
		return resultMap;
	}
	
	public static void checkBulkSizeLimit(final String endpointName, final WebserviceUserService userService, final int size) throws BulkSizeLimitExeededExeption {
		final String username = Utils.getUserName();
		final OptionalInt bulkSizeLimitOptional = readBulkSizeLimitForWebserviceUser(userService, username);
		
		if (bulkSizeLimitOptional.isPresent() && bulkSizeLimitOptional.getAsInt() != 0 && bulkSizeLimitOptional.getAsInt() < size) {
			throw new BulkSizeLimitExeededExeption(endpointName, username, bulkSizeLimitOptional.getAsInt(), size);
		}
	}
	
	private static final OptionalInt readBulkSizeLimitForWebserviceUser(final WebserviceUserService userService, final String username) {
		try {
			final WebserviceUserSettings settings = userService.findSettingsForWebserviceUser(username);
			return settings.getBulkSizeLimit();
		} catch(final Exception e) {
			return OptionalInt.empty();
		}
	} 
	
}
