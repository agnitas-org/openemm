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
    
    public static final int EVAL_TYPE_CLICKS = 1;
    public static final int EVAL_TYPE_OPENRATE = 2;
    
    public static final int STATUS_NOT_STARTED = 0;
    public static final int STATUS_TEST_SEND = 1;
    public static final int STATUS_EVAL_IN_PROGRESS = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_SCHEDULED = 4;

    /**
     * Getter for property optimization.
     * @return Value of property optimization.
     */
    public int getId();
    
    /**
     * Setter for property optimization.
     * @param optimization New value of property optimization.
     */
    public void setId(int id);
    
    public int getCompanyID();
    
    public void setCompanyID(int companyID);
    
    /** Getter for property campaignID.
     * @return Value of property campaignID.
     *
     */
    public int getCampaignID();
    
    /** Setter for property campaignID.
     * @param campaignID New value of property campaignID.
     *
     */
    public void setCampaignID(int campaignID);
    
    /**
     * Getter for property mailinglistID.
     * @return Value of property mailinglistID.
     */
    public int getMailinglistID();
    
    /**
     * Setter for property mailinglistID.
     * @param mailinglistID New value of property mailinglistID.
     */
    public void setMailinglistID(int mailinglistID);
    
    /** Getter for property shortname.
     * @return Value of property shortname.
     *
     */
    public String getShortname();
    
    /** Setter for property shortname.
     * @param shortname New value of property shortname.
     *
     */
    public void setShortname(String shortname);
    
    public String getDescription();
    
    public void setDescription(String description);
    
    /**
     * Getter for property evalType.
     * @return Value of property evalType.
     */
    public int getEvalType();
    
    /**
     * Setter for property evalType.
     * @param evalType New value of property evalType.
     */
    public void setEvalType(int evalType);
    
    /**
     * Getter for property group1.
     * @return Value of property group1.
     */
    public int getGroup1();
    
    /**
     * Setter for property group1.
     * @param group1 New value of property group1.
     */
    public void setGroup1(int group1);
    
    /**
     * Getter for property group2.
     * @return Value of property group2.
     */
    public int getGroup2();
    
    /**
     * Setter for property group2.
     * @param group2 New value of property group2.
     */
    public void setGroup2(int group2);
    
    /**
     * Getter for property group3.
     * @return Value of property group3.
     */
    public int getGroup3();
    
    /**
     * Setter for property group3.
     * @param group3 New value of property group3.
     */
    public void setGroup3(int group3);
    
    /**
     * Getter for property group4.
     * @return Value of property group4.
     */
    public int getGroup4();
    
    /**
     * Setter for property group4.
     * @param group4 New value of property group4.
     */
    public void setGroup4(int group4);
    
    /**
     * Getter for property group5.
     * @return Value of property group5.
     */
    public int getGroup5();
    
    /**
     * Setter for property group5.
     * @param group5 New value of property group5.
     */
    public void setGroup5(int group5);
    
    /** Getter for property targetID.
     * @return Value of property targetID.
     *
     */
    public String getSplitType();
    
    /** Setter for property targetID.
     * @param targetID New value of property targetID.
     *
     */
    public void setSplitType(String splitType);
    
    /**
     * Getter for property status.
     * @return Value of property status.
     */
    public int getStatus();
    
    /**
     * Setter for property status.
     * @param status New value of property status.
     */
    public void setStatus(int status);
    
    /**
     * Getter for property targetID.
     * @return Value of property targetID.
     */
    public int getTargetID();
    
    /**
     * Setter for property targetID.
     * @param targetID New value of property targetID.
     */
    public void setTargetID(int targetID);
    
    /**
     * Getter for property resultMailingID.
     * @return Value of property resultMailingID.
     */
    public int getResultMailingID();
    
    /**
     * Setter for property resultMailingID.
     * @param resultMailingID New value of property resultMailingID.
     */
    public void setResultMailingID(int resultMailingID);
    
    /**
     * Getter for property sendDate.
     * @return Value of property sendDate.
     */
    public java.util.Date getSendDate();
    
    /**
     * Setter for property sendDate.
     * @param sendDate New value of property sendDate.
     */
    public void setSendDate(java.util.Date sendDate);

    public boolean finishOptimization(boolean offPeak, ApplicationContext con);

    // public ComCampaign.Stats loadStats(boolean useMailtracking, ApplicationContext con);
    public CampaignStatsImpl loadStats(boolean useMailtracking, ApplicationContext con);

    public boolean startOptimization(boolean offPeak, ApplicationContext con);

}
