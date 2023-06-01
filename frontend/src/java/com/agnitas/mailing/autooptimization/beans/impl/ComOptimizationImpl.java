/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


/**
 * Title:        Optimization
 * Copyright:    Copyright (c) AGNITAS AG
 * Company:      AGNITAS AG
 */

package com.agnitas.mailing.autooptimization.beans.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;

public class ComOptimizationImpl implements ComOptimization {
	
	private static final Logger logger = LogManager.getLogger(ComOptimizationImpl.class);

    private int companyID;
    private String description;
    private int finalMailingId;

    private int campaignID;
    private String campaignName;

    private String shortname;

    private String splitType;

    private int optimizationID;

    private int resultMailingID;

    private int group1;

    private int group2;

    private int group3;

    private int group4;

    private int group5;

    private WorkflowDecision.WorkflowAutoOptimizationCriteria evalType;

    private int status;

    private int targetID;

    private int mailinglistID;

    private Date sendDate;

    private Date testMailingsSendDate;
    
    private int threshold;
    
    private boolean doubleCheckingActivated;

    private String targetExpression = "";

    private int targetMode;

    private boolean testRun;

    private AutoOptimizationStatus autoOptimizationStatus;

    @Override
    public AutoOptimizationStatus getAutoOptimizationStatus() {
        return autoOptimizationStatus;
    }

    @Override
    public void setAutoOptimizationStatus(AutoOptimizationStatus autoOptimizationStatus) {
        this.autoOptimizationStatus = autoOptimizationStatus;
    }

    /**
     * Holds value of property workflowID (non-zero value if optimization
     * is created and managed by campaign manager).
     */
    private int workflowID;

    @Override
    public int getThreshold() {
		return threshold;
	}

    @Override
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getCompanyID() {
        return companyID;
    }

    @Override
    public void setCompanyID(@VelocityCheck int companyID) {
        this.companyID = companyID;
    }

    @Override
    public int getCampaignID() {
        return this.campaignID;
    }

    @Override
    public void setCampaignID(int campaignID) {
        this.campaignID = campaignID;
    }

    @Override
    public String getCampaignName() {
        return this.campaignName;
    }

    @Override
    public void setCampaignName(String name) {
        this.campaignName = name;
    }

    @Override
    public String getShortname() {
        return this.shortname;
    }

    @Override
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    @Override
    public String getSplitType() {
        return this.splitType;
    }

    @Override
    public void setSplitType(String splitType) {
        this.splitType = splitType;
    }

    @Override
    public int getId() {
        return this.optimizationID;
    }

    @Override
    public void setId(int optimizationID) {
        this.optimizationID = optimizationID;
    }

    @Override
    public int getResultMailingID() {
        return this.resultMailingID;
    }

    @Override
    public void setResultMailingID(int resultMailingID) {
        this.resultMailingID = resultMailingID;
    }

    @Override
    public int getGroup1() {
        return this.group1;
    }

    @Override
    public void setGroup1(int group1) {
        this.group1 = group1;
    }

    @Override
    public int getGroup2() {
        return this.group2;
    }

    @Override
    public void setGroup2(int group2) {
        this.group2 = group2;
    }

    @Override
    public int getGroup3() {
        return this.group3;
    }

    @Override
    public void setGroup3(int group3) {
        this.group3 = group3;
    }

    @Override
    public int getGroup4() {
        return this.group4;
    }

    @Override
    public void setGroup4(int group4) {
        this.group4 = group4;
    }

    @Override
    public int getGroup5() {
        return this.group5;
    }

    @Override
    public void setGroup5(int group5) {
        this.group5 = group5;
    }
    
    @Override
    public List<Integer> getTestmailingIDs() {
    	List<Integer> testmailingIDs =  new ArrayList<>();
    	
    	if(group1 > 0 ) {
    		testmailingIDs.add(group1);
    	}
    	
    	if(group2 > 0 ) {
    		testmailingIDs.add(group2);
    	}
    	
    	if(group3 > 0 ) {
    		testmailingIDs.add(group3);
    	}
    	
    	if(group4 > 0 ) {
    		testmailingIDs.add(group4);
    	}
    	
    	if(group5 > 0 ) {
    		testmailingIDs.add(group5);
    	}
    	
    	return testmailingIDs;
    }
    

    @Override
    public WorkflowDecision.WorkflowAutoOptimizationCriteria getEvalType() {
        return this.evalType;
    }

    @Override
    public void setEvalType(WorkflowDecision.WorkflowAutoOptimizationCriteria evalType) {
    	// The following block is for debugging EMM-4905
    	if(evalType == null) {
    		try {
    			throw new IllegalArgumentException("Setting evaluation type of auto-optimization to null");
    		} catch(final IllegalArgumentException e) {
    			logger.error("Setting evaluation type of auto-optimization to null", e);
    		}
    	}
    	
        this.evalType = evalType;
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int getTargetID() {
        return this.targetID;
    }

    @Override
    public void setTargetID(int targetID) {
        this.targetID = targetID;
    }

    @Override
    public int getMailinglistID() {
        return this.mailinglistID;
    }

    @Override
    public void setMailinglistID(int mailinglistID) {
        this.mailinglistID = mailinglistID;
    }

    @Override
    public java.util.Date getSendDate() {
        return this.sendDate;
    }

    @Override
    public void setSendDate(java.util.Date sendDate) {
        this.sendDate = sendDate;
    }

    @Override
    public Date getTestMailingsSendDate() {
    	return testMailingsSendDate;
    }
    
    @Override
    public void setTestMailingsSendDate(Date testMailingsSendDate) {
    	this.testMailingsSendDate = testMailingsSendDate;
    }
    
    @Override
    public String toString() { // provides some more information than object@<number>
    	StringBuffer outputBuffer = new StringBuffer(super.toString());
    	
    	outputBuffer.append("\nid :").append(optimizationID);
    	outputBuffer.append("\ncompanyID :").append(companyID);
    	outputBuffer.append("\ncampaignID :").append(campaignID);
    	outputBuffer.append("\nmailinglistID :").append(mailinglistID);
    	outputBuffer.append("\nshortname :").append(shortname);
    	outputBuffer.append("\ndescription :").append(description);
    	outputBuffer.append("\nevalType :").append(evalType == null ? 0 : evalType.getId());
    	outputBuffer.append("\ngroup1 :").append(group1);
    	outputBuffer.append("\ngroup2 :").append(group2);
    	outputBuffer.append("\ngroup3 :").append(group3);
    	outputBuffer.append("\ngroup4:").append(group4);
    	outputBuffer.append("\ngroup5:").append(group5);
    	outputBuffer.append("\nsplitType:").append(splitType);
    	outputBuffer.append("\nstatus:").append(status);
    	outputBuffer.append("\ntargetExpression:").append(targetExpression);
    	outputBuffer.append("\nresultMailingID:").append(resultMailingID);
    	outputBuffer.append("\nsendDate:").append(sendDate);
    	outputBuffer.append("\ntestMailingsSendDate: ").append(testMailingsSendDate);
        outputBuffer.append("\ntestRun: ").append(testRun);

    	return outputBuffer.toString();
    }

    @Override
	public boolean isDoubleCheckingActivated() {
		return doubleCheckingActivated;
	}

    @Override
	public void setDoubleCheckingActivated(boolean doubleCheckingActivated) {
		this.doubleCheckingActivated = doubleCheckingActivated;
	}

    @Override
    public String getTargetExpression() {
        return targetExpression;
    }

    @Override
    public void setTargetExpression(String targetExpression) {
		/*
		 * EMM-8756
		 * This pre-processing is needed, because we receive a list of target group ids
		 * here from UI that starts with a ",".
		 * This is sent in that form by the browser, so there must be some issue with the JSP.
		 * 
		 * This faulty list is stored in DB unchecked (!!!), so the leading comma can be found there, too.
		 */

		if(targetExpression != null) {
			while(targetExpression.startsWith(",")) {
				targetExpression = targetExpression.substring(1).trim();
			}
		}

        this.targetExpression = targetExpression;
    }

    @Override
    public int getTargetMode() {
        return targetMode;
    }

    @Override
    public void setTargetMode(int targetMode) {
        this.targetMode = targetMode;
    }

	@Override
	public int getFinalMailingId() {
		return this.finalMailingId;
	}

	@Override
	public void setFinalMailingId( int mailingId) {
		this.finalMailingId = mailingId;
	}

    @Override
    public boolean isTestRun() {
        return testRun;
    }

    @Override
    public void setTestRun(boolean testRun) {
        this.testRun = testRun;
    }

    @Override
    public int getWorkflowId() {
        return workflowID;
    }

    @Override
    public void setWorkflowId(int workflowId) {
        this.workflowID = workflowId;
    }
}
