/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.SendStatRow;

public class MailingDeliveryStatDataSet extends BIRTDataSet {
	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(MailingDeliveryStatDataSet.class);
	
	public MailingDeliveryStatDataSet() {
		super();
	}
	
	public MailingDeliveryStatDataSet(DataSource dataSource) {
		super();
		setDataSource(dataSource);
	}
	
	public List<SendStatRow> getDeliveryStat(@VelocityCheck int companyID, int mailingID, String useMailTrackingStr, String targetIDs, String language, Boolean includeAdminAndTestMails) {
		if (StringUtils.isBlank(language)) {
			language = "EN";
		}
		
		List<SendStatRow> statList = new ArrayList<>();
		List<SendStatRow> openedList = new MailingOpenedDataSet(getDataSource()).getTotalOpened(companyID, mailingID, useMailTrackingStr, targetIDs, language, includeAdminAndTestMails );
		
		// add total opened only if there have been opened mails ( text only , onepixel not enabled ,... )
		SendStatRow totalOpenedRow = openedList.get(0);
		if (totalOpenedRow.getCount() > 0) {
			String openedcategory = I18nString.getLocaleString("statistic.Opened_Mails", language);
			
			for (SendStatRow openedrow: openedList ) {
				openedrow.setCategoryindex(0);
				openedrow.setCategory(openedcategory);
			}		
			statList.addAll(openedList);
		}
		
		List<SendStatRow> optOutList = new MailingOptOutDataSet(getDataSource()).getTotalOptOut(companyID, mailingID, useMailTrackingStr, targetIDs, language, includeAdminAndTestMails);
		String optoutcategory = I18nString.getLocaleString("statistic.Opt_Outs", language);
		for (SendStatRow optoutrow: optOutList ) {
			optoutrow.setCategoryindex(1);
			optoutrow.setCategory(optoutcategory);
		}		
		statList.addAll(optOutList);
		
		
		List<SendStatRow> bouncesList = new MailingBouncesDataSet(getDataSource()).getTotalBounces(companyID, mailingID, useMailTrackingStr, targetIDs, language, includeAdminAndTestMails);
		String bouncescategory = I18nString.getLocaleString("Bounces", language);
		for (SendStatRow bouncesrow: bouncesList ) {
			bouncesrow.setCategoryindex(2);
			bouncesrow.setCategory(bouncescategory);
		}		
		statList.addAll(bouncesList);
		
		return statList;
	}
}
