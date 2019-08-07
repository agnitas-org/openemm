/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailloop.web;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.Mailloop;
import org.agnitas.beans.Mediatype;
import org.agnitas.beans.impl.MailingComponentImpl;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.MailloopDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;

import com.agnitas.beans.ComMailing;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.impl.ComMailingImpl;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailloop.util.SecurityTokenGenerator;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

// TODO: Remove after transition phase (EMM-2693)
public class ConvertMailloopAutoresponderAction extends Action {
	
	private static final transient Logger logger = Logger.getLogger(ConvertMailloopAutoresponderAction.class);

	
	private MailinglistDao mailinglistDao;
	private MailloopDao mailloopDao;
	private ComMailingDao mailingDao;
	private ConfigService configService;
	private DataSource dataSource;
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		int adminID = AgnUtils.getAdminId(request);
		
		response.setContentType("text/plain");
		
		if(adminID != 1) {
			response.getWriter().println("ERROR: Access denied");
		} else {
			int companyID = getCompanyID(request);
			
			if(companyID == -1) {
				response.getWriter().println("ERROR: companyID is missing");
			} else {
				convert(companyID, response);
			}
		}
	
		return null;
	}
	
	private static int getCompanyID(HttpServletRequest request) {
		String value = request.getParameter("companyID");
		
		if(value == null) {
			return -1;
		} else {
			try {
				return Integer.parseInt(value);
			} catch(NumberFormatException e) {
				return -1;
			}
		}
	}
	
	private void convert(int companyID, HttpServletResponse response) throws IOException {
		if(companyID > 0) {
			convertCompany(companyID, response);
		} else {
			JdbcTemplate template = new JdbcTemplate(this.dataSource);
			
			List<Integer> list = template.queryForList("SELECT company_id FROM mailloop_tbl ORDER BY company_id", Integer.class);
			
			for(int company : list) {
				convertCompany(company, response);
			}
		}
	}
	
	private void convertCompany(int companyID, HttpServletResponse response) throws IOException {
		response.getWriter().println("Converting company " + companyID);
		
		if(!configService.isActionbasedMailloopAutoresponderInUiEnabled(companyID)) {
			response.getWriter().println("  WARNING: Creating action-based autoresponder in UI not enabled");
		}
			
		int mailinglistID = getLowestMailinglistId(companyID);
		
		if(mailinglistID == -1) {
			response.getWriter().println("  ERROR: Cannot convert company " + companyID + " - no mailinglist found!?");
		} else {
			convertMailloops(companyID, mailinglistID, response);
		}
	}
	
	private void convertMailloops(int companyID, int mailinglistID, HttpServletResponse response) throws IOException {
		List<Mailloop> list = this.mailloopDao.getMailloops(companyID);
		
		for(Mailloop ml : list) {
			convertMailloop(ml, mailinglistID, response);
		}
	}
	
	private void convertMailloop(Mailloop mailloop, int mailinglistID, HttpServletResponse response) throws IOException {
		
		if(mailloop.getAutoresponderMailingId() > 0) {
			response.getWriter().println("  Skipping mailloop " + mailloop.getId() + " - action-based autoresponder already defined");
		} else {
			if(StringUtils.isBlank(mailloop.getArSender())) {
				response.getWriter().println("  Skipping mailloop " + mailloop.getId() + " - no autoresponder defined");
			} else {
				response.getWriter().println("  Converting mailloop " + mailloop.getId());
	
				try {
					ComMailing mailing = createMailing(mailloop, mailinglistID);
					activateMailing(mailing);
					
					int mailingID = mailingDao.saveMailing(mailing, false);
					
					response.getWriter().println("    Created action-based autoresponder mailing ID " + mailingID);
					
					updateMailloop(mailloop, mailingID);
				} catch(Exception e) {
					logger.error("Error converting auto-responder mailing", e);
					response.getWriter().println("    ERROR: Cannot create mailing (" + e.getClass().getCanonicalName() + ", " + e.getMessage() + ")");
				}
			}
		}
	}
	
	private void updateMailloop(Mailloop mailloop, int mailingID) {
		mailloop.setAutoresonderMailingId(mailingID);
		mailloop.setSecurityToken(SecurityTokenGenerator.generateSecurityToken());
		
		this.mailloopDao.saveMailloop(mailloop);
	}
	
	private ComMailing createMailing(Mailloop mailloop, int mailinglistID) throws AddressException {

		InternetAddress[] fromList = InternetAddress.parse(mailloop.getArSender());
				
		MediatypeEmailImpl mt = new MediatypeEmailImpl();
		mt.setCharset("UTF-8");
		mt.setCompanyID(mailloop.getCompanyID());
		mt.setFromEmail(fromList[0].getAddress());
		mt.setFromFullname(fromList[0].getPersonal());
		mt.setStatus(Mediatype.STATUS_ACTIVE);
		mt.setMailFormat(StringUtils.isEmpty(mailloop.getArHtml()) ? Mailing.INPUT_TYPE_TEXT : Mailing.INPUT_TYPE_HTML);
		
		Map<Integer, Mediatype> mediatypes = new HashMap<>();
		mediatypes.put(MediaTypes.EMAIL.getMediaCode(), mt);
		

		MailingComponentImpl htmlTemplate = new MailingComponentImpl();
		htmlTemplate.setCompanyID(mailloop.getCompanyID());
		htmlTemplate.setComponentName("agnHtml");
		htmlTemplate.setEmmBlock(mailloop.getArHtml(),"text/html");
		htmlTemplate.setType(MailingComponent.TYPE_TEMPLATE);

		MailingComponentImpl asciiTemplate = new MailingComponentImpl();
		asciiTemplate.setCompanyID(mailloop.getCompanyID());
		asciiTemplate.setComponentName("agnText");
		asciiTemplate.setEmmBlock(mailloop.getArText(), "text/plain");
		asciiTemplate.setType(MailingComponent.TYPE_TEMPLATE);
		
		ComMailingImpl mailing = new ComMailingImpl();
		mailing.setCompanyID(mailloop.getCompanyID());
		mailing.setMailinglistID(mailinglistID);
		mailing.setMailingType(Mailing.TYPE_ACTIONBASED);
		mailing.setShortname("Auto-responder mailing for mailloop " + mailloop.getId());
		mailing.setDescription("Auto-responder mailing created by automatic conversion of mailloop " + mailloop.getId());
		mailing.setMediatypes(mediatypes);
		mailing.setHtmlTemplate(htmlTemplate);
		mailing.setTextTemplate(asciiTemplate);
		
		return mailing;
	}
	
	private void activateMailing(ComMailing mailing) {
		Set<MaildropEntry> set = new HashSet<>();
		
		MaildropEntry entry = new MaildropEntryImpl();
		
		entry.setCompanyID(mailing.getCompanyID());
		entry.setMailingID(mailing.getId());
		entry.setStatus(MaildropStatus.ACTION_BASED.getCode());
		entry.setGenStatus(1);
		entry.setGenChangeDate(new Date());
		entry.setGenDate(new Date());
		
		mailing.setMaildropStatus(set);
	}
	
	private int getLowestMailinglistId(int companyID) {
		List<Mailinglist> list = this.mailinglistDao.getMailinglists(companyID);
		
		if(list.size() == 0) {
			return -1;
		} else {
			Optional<Mailinglist> lowest = list.stream().min(new Comparator<Mailinglist>() {

				@Override
				public int compare(Mailinglist o1, Mailinglist o2) {
					return o1.getId() - o2.getId();
				}});
			
			if(lowest.isPresent()) {
				return lowest.get().getId();
			} else {
				return -1;
			}
		}
	}
	
	@Required
	public void setMailinglistDao(MailinglistDao dao) {
		this.mailinglistDao = dao;
	}
	
	@Required
	public void setMailloopDao(MailloopDao dao) {
		this.mailloopDao = dao;
	}
	
	@Required
	public void setMailingDao(ComMailingDao dao) {
		this.mailingDao = dao;
	}
	
	@Required
	public void setConfigService(ConfigService service) {
		this.configService = service;
	}
	
	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
