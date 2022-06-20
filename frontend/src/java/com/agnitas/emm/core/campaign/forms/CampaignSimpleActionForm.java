/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.campaign.forms;

import org.agnitas.web.forms.SimpleActionForm;

public class CampaignSimpleActionForm extends SimpleActionForm {

    private boolean isCampaign;
    private boolean isTemplate;
    private int campaignId;
    private int mailingId;

    public boolean isIsCampaign() {
        return isCampaign;
    }

    public void setIsCampaign(boolean campaign) {
        isCampaign = campaign;
    }

    public boolean isIsTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean template) {
        isTemplate = template;
    }

    public int getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(int campaignId) {
        this.campaignId = campaignId;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }
}
