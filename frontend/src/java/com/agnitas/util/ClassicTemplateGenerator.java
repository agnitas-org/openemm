/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.Enumeration;
import java.util.Vector;

import jakarta.servlet.http.HttpServletRequest;

import com.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.dao.MailingDao;
import org.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This class bind template and content modules with emm`s mailings dyn tag content.
 */
public class ClassicTemplateGenerator implements ApplicationContextAware {
	private static final transient Logger logger = LogManager.getLogger(ClassicTemplateGenerator.class);

	private ApplicationContext applicationContext;
	private MailingDao mailingDao;

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void generate(int mailingId, HttpServletRequest request) {
		// by default checkMailingType=true, copyImages=false
		generate(mailingId, request, true, false);
	}

	public void generate(int mailingId, HttpServletRequest request, boolean checkMailingType) {
		generate(mailingId, request, checkMailingType, false);
	}

	public void generate(int mailingId, HttpServletRequest request, boolean checkMailingType, boolean copyImages) {
		final int adminId = AgnUtils.getAdmin(request).getAdminID();
		final int companyId = AgnUtils.getCompanyID(request);
		this.generate(mailingId, adminId, companyId, checkMailingType, copyImages);
	}

	/**
	 * Method perform only mailings contained elements, clears previous
	 * mailing`s contetn and write new from template
	 * and content modules, if cmTemplate dosen`t exist it adds
	 * default tag`s name.
	 *
	 * @param mailingId mailing`s id to attach classic template content
	 * @param checkMailingType do we need to check that it is CMS-mailing?
	 */
	public void generate(int mailingId, int adminId, int companyId, boolean checkMailingType, boolean copyImages) {
		// Mailing IDs start from 1. Mailing ID = 0 is invalid situation.
		// generating a preview for mailing with id 0 will cause creating a new mailing with companyId o
		if (mailingId == 0) {
			return;
		}

		final Mailing mailing = mailingDao.getMailing(mailingId, companyId);
		if (mailing != null) {
			if (!checkMailingType) {
				cleanMailingContent(mailing);
				try {
					mailing.buildDependencies(true, applicationContext);
				} catch (Exception e) {
					logger.warn("Can`t build mailing dependencies", e);
				}
				mailingDao.saveMailing(mailing, false);
			}
		}
	}
	
	public static void generateClassicTemplate(final int mailingId, final HttpServletRequest request, final ApplicationContext aContext) {
		final ClassicTemplateGenerator classicTemplateGenerator = (ClassicTemplateGenerator) aContext.getBean("ClassicTemplateGenerator");
		classicTemplateGenerator.generate(mailingId, request, true, true);
	}

	private void cleanMailingContent(Mailing mailing) {
		// mailing.cleanupTrackableLinks(new Vector());
		MailingComponent htmlTemplate = mailing.getHtmlTemplate();
		if (htmlTemplate != null) {
			htmlTemplate.setEmmBlock("[agnDYN name=\"" + AgnUtils.DEFAULT_MAILING_HTML_DYNNAME + "\"/]", "text/plain");
		}
		MailingComponent textTemplate = mailing.getTextTemplate();
		if (textTemplate != null) {
			textTemplate.setEmmBlock("", "text/plain");
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void removeMailingImageComponents(Mailing mailing) {
		Vector<String> remove = new Vector<>();
		for (MailingComponent commponent : mailing.getComponents().values()) {
			if (commponent.getType() == MailingComponentType.Image || commponent.getType() == MailingComponentType.HostedImage) {
				remove.add(commponent.getComponentName());
			}
		}
		Enumeration<String> e = remove.elements();
		while (e.hasMoreElements()) {
			mailing.getComponents().remove(e.nextElement());
		}
	}
}
