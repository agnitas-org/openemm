/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import org.springframework.context.ApplicationContext;

import com.agnitas.beans.impl.CampaignStatsImpl;

public interface ComOptimization {

    int EVAL_TYPE_CLICKS = 1;
    int EVAL_TYPE_OPENRATE = 2;

    int getId();

    void setId(int id);

    int getCompanyID();

    void setCompanyID(int companyID);

    int getCampaignID();

    void setCampaignID(int campaignID);

    int getMailinglistID();

    void setMailinglistID(int mailinglistID);

    String getShortname();

    void setShortname(String shortname);

    String getDescription();

    void setDescription(String description);

    int getEvalType();

    void setEvalType(int evalType);

    int getGroup1();

    void setGroup1(int group1);

    int getGroup2();

    void setGroup2(int group2);

    int getGroup3();

    void setGroup3(int group3);

    int getGroup4();

    void setGroup4(int group4);

    int getGroup5();

    void setGroup5(int group5);

    String getSplitType();

    void setSplitType(String splitType);

    int getStatus();

    void setStatus(int status);

    int getTargetID();

    void setTargetID(int targetID);

    int getResultMailingID();

    void setResultMailingID(int resultMailingID);

    java.util.Date getSendDate();

    void setSendDate(java.util.Date sendDate);

    boolean finishOptimization(boolean offPeak, ApplicationContext con);

    // public ComCampaign.Stats loadStats(boolean useMailtracking, ApplicationContext con);
    CampaignStatsImpl loadStats(boolean useMailtracking, ApplicationContext con);

    boolean startOptimization(boolean offPeak, ApplicationContext con);

}
