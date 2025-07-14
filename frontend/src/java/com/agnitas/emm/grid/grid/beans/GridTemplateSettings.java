/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

import java.util.Map;

import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.bean.MailingParameter;

public interface GridTemplateSettings {
    
    int getSettingsId();
    void setSettingsId(int settingsId);
    int getTemplateId();
    void setTemplateId(int templateId);
    int getCampaignId();
    void setCampaignId(int campaignId);
    int getMailingListId();
    void setMailingListId(int mailingListId);
    MailingType getMailingType();
    void setMailingType(MailingType mailingType);
    boolean isArchived();
    void setArchived(boolean archived);
    String getParam();
    void setParam(String param);
    MediatypeEmail getMediatypeEmail();
    void setMediatypeEmail(MediatypeEmail mediatypeEmail);
    String getTextTemplate();
    void setTextTemplate(String textTemplate);
    int getSplitId();
    void setSplitId(int splitId);
    boolean isAddParameter();
    void setAddParameter(boolean addParameter);
    Map<Integer, MailingParameter> getParameters();
    void setParameters(Map<Integer, MailingParameter> parameters);

    String[] getTargets();
    void setTargets(String[] targets);
    String[] getAltgs();
    void setAltgs(String[] altgs);
    int getCompanyID();
    void setCompanyID(int companyID);
    String getFollowUpMailingType();
    void setFollowUpMailingType(String followUpMailingType);
    Integer getParentMailing();
    void setParentMailing(Integer parentMailing);
    String getTargetExpression();
    void setTargetExpression(String targetExpression);
    String getMailingParameters();
    void setMailingParameters(String mailingParameters);
	boolean isNeedsTarget();
	void setNeedsTarget(boolean needsTarget);
}
