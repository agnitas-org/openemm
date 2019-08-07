/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

import java.util.Map;

import com.agnitas.beans.MediatypeEmail;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;

public interface ComTemplateSettings {
    
    int getSettingsId();
    void setSettingsId(int settingsId);
    int getTemplateId();
    void setTemplateId(int templateId);
    int getCampaignId();
    void setCampaignId(int campaignId);
    int getMailingListId();
    void setMailingListId(int mailingListId);
    int getMailingType();
    void setMailingType(int mailingType);
    boolean isArchived();
    void setArchived(boolean archived);
    String getParam();
    void setParam(String param) throws Exception;
    MediatypeEmail getMediatypeEmail();
    void setMediatypeEmail(MediatypeEmail mediatypeEmail);
    String getTextTemplate();
    void setTextTemplate(String textTemplate);
    int getSplitId();
    void setSplitId(int splitId);
    int getParameterIdToRemove();
    void setParameterIdToRemove(int parameterIdToRemove);
    boolean isAddParameter();
    void setAddParameter(boolean addParameter);
    Map<Integer, ComMailingParameter> getParameters();
    void setParameters(Map<Integer, ComMailingParameter> parameters);

    String[] getTargets();
    void setTargets(String[] targets);
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

    void setNewNameMailingParam(String index, String name);
    void setNewValueMailingParam(String index, String value);
    void setNewDescriptionMailingParam(String index, String description);
    String getNewNameMailingParam(String index);
    String getNewValueMailingParam(String index);
    String getNewDescriptionMailingParam(String index);
}
