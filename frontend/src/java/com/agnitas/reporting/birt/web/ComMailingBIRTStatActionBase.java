/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.impl.SimpleKeyValueBean;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.EmmCalendar;
import org.agnitas.web.StrutsActionBase;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.reporting.birt.service.ComMailingBIRTStatService;
import com.agnitas.reporting.birt.util.RSACryptUtil;
import com.agnitas.reporting.birt.util.UIDUtils;
import com.agnitas.reporting.birt.util.URLUtils;

public abstract class ComMailingBIRTStatActionBase extends StrutsActionBase {
	protected String publicKeyFilename;
	protected final String DATE_PATTERN = "yyyyMMdd"; 
	protected ComMailingBIRTStatService birtservice;
	protected ConfigService configService;
	
	protected void rollCalendarOneWeekForward(EmmCalendar startCalendar) {
		startCalendar.add(EmmCalendar.DAY_OF_YEAR, 7);
		startCalendar.add(EmmCalendar.SECOND, -1);
	}

	protected void normalizeTime(EmmCalendar startCalendar) {
		startCalendar.set(EmmCalendar.HOUR_OF_DAY, 0);
		startCalendar.set(EmmCalendar.MINUTE, 0);
		startCalendar.set(EmmCalendar.SECOND, 0);
	}
		
	protected void setCommonRequestAttributes(HttpServletRequest request, @VelocityCheck int companyID, int mailingID,List<SimpleKeyValueBean> allTargets, List<SimpleKeyValueBean> selectedTargets, boolean includeAdminAndTestMails) throws UnsupportedEncodingException, Exception, IOException, MalformedURLException {
		request.setAttribute("targets",allTargets );
		request.setAttribute("selected",selectedTargets );	
		request.setAttribute("emmsession",request.getSession(false).getId()); // ;jsessionid= must be provided in subreports 
		request.setAttribute("companyID", companyID);
		request.setAttribute("mailingID",mailingID);
		
		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin != null) {
			request.setAttribute("language", admin.getAdminLang());
			request.setAttribute("useMailtracking", birtservice.useMailtracking(companyID));
			request.setAttribute("useDeepTracking", birtservice.deepTracking(admin.getAdminID(), companyID));
		}	
		
		if( selectedTargets != null && ! selectedTargets.isEmpty()) {
			StringBuffer buffer = new StringBuffer();
			for( SimpleKeyValueBean bean: selectedTargets ) {
				buffer.append( bean.getKey() );
				buffer.append(",");
			}
			
			String selectedtargetIDs  = buffer.substring(0, buffer.lastIndexOf(",") );			
			request.setAttribute("selectedtargetIDs", selectedtargetIDs );
		}
		
		request.setAttribute("uid", URLUtils.encodeURL( RSACryptUtil.encrypt(UIDUtils.createUID(AgnUtils.getAdmin(request)),RSACryptUtil.getPublicKey(publicKeyFilename))));
		request.setAttribute("targetbaseurl", URLUtils.encodeURL(configService.getValue(ConfigValue.BirtDrilldownUrl)));
		request.setAttribute("includeAdminAndTestMails", includeAdminAndTestMails);
	}
	
	// spring stuff
	public void setPublicKeyFilename(String publicKeyFilename) {
		this.publicKeyFilename = publicKeyFilename;
	}

	public void setBirtservice(ComMailingBIRTStatService birtservice) {
		this.birtservice = birtservice;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
}
