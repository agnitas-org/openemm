/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/**
 * Title:        Optimization
 * Copyright:    Copyright (c) AGNITAS AG
 * Company:      AGNITAS AG
 */

package com.agnitas.mailing.autooptimization.beans;

import java.util.Date;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationStatus;



public interface ComOptimization {

    int STATUS_NOT_STARTED = 0;
    int STATUS_TEST_SEND = 1;
    int STATUS_EVAL_IN_PROGRESS = 2;
    int STATUS_FINISHED = 3;
    int STATUS_SCHEDULED = 4;

    /**
     * Getter for property optimization.
     * @return Value of property optimization.
     */
    int getId();

    /**
     * Setter for property optimization.
     * @param optimization New value of property optimization.
     */
    void setId(int id);

    int getCompanyID();
    void setCompanyID(@VelocityCheck int companyID);

    int getCampaignID();
    void setCampaignID(int campaignID);

    int getMailinglistID();
    void setMailinglistID(int mailinglistID);

    String getShortname();
    void setShortname(String shortname);

    String getDescription();
    void setDescription(String description);

    WorkflowDecision.WorkflowAutoOptimizationCriteria getEvalType();
    void setEvalType(WorkflowDecision.WorkflowAutoOptimizationCriteria evalType);

    // getter and setters for group1...4
    // group is the mailingID in fact
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

    /**
     * Returns the ID of the result mailing sent to the remaining recipients.
     * This is the ID of the copy of the mailing from groupX.
     *
     * @return ID of result mailing sent to remaining recipients
     */
    int getFinalMailingId();

    /**
     * Sets the ID of the result mailing sent to the remaining recipients.
     * This is the ID of the copy of the mailing from groupX.
     *
     * @param mailingId ID of result mailing sent to remaining recipients
     */
    void setFinalMailingId( int mailingId);

    Date getSendDate();
    void setSendDate(java.util.Date sendDate);

    Date getTestMailingsSendDate();
    void setTestMailingsSendDate(Date testMailingsSendDate);

    /**
     * Convenience method to get the used test mailing ids.
     * You don't want to check all the groupX fields, don't ya ?
     * @return all groupXX which are > 0
     */
    List<Integer> getTestmailingIDs();

    /**
     * Before the send date of the final mailing is reached, you can check for a threshold.
     * If the given threshold is reached ( clicks, openings ) the final mailing will be sent.
     * @return
     */
    int getThreshold();
    void setThreshold(int threshold);

    boolean isDoubleCheckingActivated();
    void setDoubleCheckingActivated(boolean activated);

    String getTargetExpression();
    void setTargetExpression(String targetExpression);

    int getTargetMode();
    void setTargetMode(int targetMode);

    boolean isTestRun();
    void setTestRun(boolean testRun);

    int getWorkflowId();
    void setWorkflowId(int workflowId);

    AutoOptimizationStatus getAutoOptimizationStatus();
    void setAutoOptimizationStatus(AutoOptimizationStatus autoOptimizationStatus);
}
