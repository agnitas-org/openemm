/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Date;


public interface MailingBase {
	void setCompanyID( int id);
	int getCompanyID();

	void setCampaignID(int id);
	int getCampaignID();

	void setDescription(String description);
	String getDescription();

	void setId(int id);
	int getId();

	void setMailinglistID(int id);
	int getMailinglistID();

	void setMailinglist(Mailinglist mailinglist);
	Mailinglist getMailinglist();

	void setSenddate(Date sendDate);
	Date getSenddate();

	void setShortname(String shortname);
	String getShortname();

	void setHasActions(boolean hasActions);
    boolean isHasActions();

	boolean getUseDynamicTemplate();
	void setUseDynamicTemplate(boolean useDynamicTemplate);

	void setOnlyPostType(boolean isOnlyPostType);
	boolean isOnlyPostType();

	void setGridMailing(boolean isGridMailing);
	boolean isGridMailing();
}
