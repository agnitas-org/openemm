/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.util.Date;

import org.agnitas.beans.MailingBase;
import org.agnitas.beans.Mailinglist;
import org.agnitas.emm.core.velocity.VelocityCheck;

public class MailingBaseImpl implements MailingBase {

	protected int mailinglistID;
	protected int id;
	protected int companyID;
	private boolean isGridMailing;
	protected int campaignID;
	protected boolean onlyPostType;
	private Mailinglist mailinglist;
	private Date sendDate;
	private boolean hasActions;
	
	/**
	 * Holds value of property description.
	 */
	protected String description;
	/**
	 * Holds value of property shortname.
	 */
	protected String shortname;

	private boolean useDynamicTemplate;
	
	public MailingBaseImpl() {
		super();
	}

	@Override
	public void setCompanyID( @VelocityCheck int tmpid) {
	    companyID=tmpid;
	}

	@Override
	public void setCampaignID(int tmpid) {
	    campaignID=tmpid;
	}

	@Override
	public void setId(int tmpid) {
	    id=tmpid;
	}

	@Override
	public void setMailinglistID(int tmpid) {
	    mailinglistID=tmpid;
	}

	/** Getter for property description.
	 * @return Value of property description.
	 */
	@Override
	public String getDescription() {
	    return description;
	}

	/** Setter for property description.
	 * @param description New value of property description.
	 */
	@Override
	public void setDescription(String description) {
	    this.description = description;
	}

	/** Getter for property shortname.
	 * @return Value of property shortname.
	 */
	@Override
	public String getShortname() {
	    return shortname;
	}

	/** Setter for property shortname.
	 * @param shortname New value of property shortname.
	 */
	@Override
	public void setShortname(String shortname) {
	    this.shortname = shortname;
	}

    @Override
	public void setHasActions(boolean hasActions) {
        this.hasActions = hasActions;
    }

    @Override
	public boolean isHasActions() {
        return hasActions;
    }

    @Override
	public int getId() {
	    return id;
	}

	@Override
	public int getMailinglistID() {
	
	    return mailinglistID;
	}

	@Override
	public int getCompanyID() {
	    return companyID;
	}

	@Override
	public int getCampaignID() {
	    return campaignID;
	}


	@Override
	public Mailinglist getMailinglist() {
		return mailinglist;
	}


	@Override
	public void setMailinglist(Mailinglist mailinglist) {
		this.mailinglist = mailinglist;
	}

	@Override
	public Date getSenddate() {
		return sendDate;
	}

	@Override
	public void setSenddate(Date sendDate) {
		this.sendDate = sendDate;
	}

	@Override
	public boolean getUseDynamicTemplate() {
		return this.useDynamicTemplate;
	}

	@Override
	public void setUseDynamicTemplate(boolean useDynamicTemplate) {
		this.useDynamicTemplate = useDynamicTemplate;
	}

	@Override
	public boolean isOnlyPostType() {
		return onlyPostType;
	}

	@Override
	public void setOnlyPostType(boolean isOnlyPostType) {
		this.onlyPostType = isOnlyPostType;
	}

	@Override
	public void setGridMailing(boolean isGridMailing) {
		this.isGridMailing = isGridMailing;
	}

	@Override
	public boolean isGridMailing() {
		return isGridMailing;
	}
}
