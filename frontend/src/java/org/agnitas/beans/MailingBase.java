/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;

public interface MailingBase {

	int getCompanyID();

	int getCampaignID();

	String getDescription();

	int getId();

	int getMailinglistID();
	
	Mailinglist getMailinglist();
	
	Date getSenddate();

	String getShortname();

	void setCompanyID( @VelocityCheck int id);

	void setCampaignID(int id);

	void setDescription(String description);

	void setId(int id);

	void setMailinglistID(int id);
	
	void setMailinglist(Mailinglist mailinglist);

	void setSenddate(Date sendDate);
	
	void setShortname(String shortname);

	void setHasActions(boolean hasActions);

    boolean isHasActions();

	boolean getUseDynamicTemplate();
	void setUseDynamicTemplate(boolean useDynamicTemplate);

	void setOnlyPostType(boolean isOnlyPostType);
	boolean isOnlyPostType();
}
