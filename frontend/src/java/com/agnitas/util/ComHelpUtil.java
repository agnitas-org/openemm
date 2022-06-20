/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.dao.DocMappingDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.web.ComManualServlet;

public class ComHelpUtil {
	private static Map<String, String> docMapping = null;
	
	public static String getHelpPageUrl(HttpServletRequest request) {
		String langId = AgnUtils.getAdmin(request).getAdminLang().toLowerCase();
		String pageKey = (String) request.getAttribute("agnHelpKey");
		
		String helpFileName = null;
		if (StringUtils.isNotBlank(pageKey)) {
			helpFileName = getDocMappingEntry(request, pageKey);
		}
		
		StringBuilder manualUrl = new StringBuilder();
		// Some new SpringMVC based sites use url-context, so we must use the EMM systemurl for direct site root-context in manual links.
		String systemUrl = ConfigService.getInstance().getValue(ConfigValue.SystemUrl);
		if (StringUtils.isNotBlank(systemUrl)) {
			manualUrl.append(systemUrl);
			manualUrl.append("/");
		}
		manualUrl.append(ComManualServlet.MANUAL_CONTEXT);
		manualUrl.append("/");
		manualUrl.append(langId);
		manualUrl.append("/html/");
		if (StringUtils.isBlank(helpFileName)) {
			manualUrl.append("index.html");
		} else {
			manualUrl.append(helpFileName);
		}
		return manualUrl.toString();
	}

	private static Object getBean(HttpServletRequest request, String beanName) {
		return WebApplicationContextUtils.getWebApplicationContext(request.getServletContext()).getBean(beanName);
	}

	private static String getDocMappingEntry(HttpServletRequest request, String pageKey) {
		if (docMapping == null) {
			DocMappingDao docMappingDao = (DocMappingDao) getBean(request, "DocMappingDao");
			docMapping = docMappingDao.getDocMapping();
		}
		return docMapping.get(pageKey);
	}
}
