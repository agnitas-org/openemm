/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.form;

import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.beans.impl.ComOptimizationImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.HtmlUtils;

public class OptimizationForm {

    private ComOptimization optimization = new ComOptimizationImpl();
    private String thresholdString;
    private String resultSendDateAsString;
    private String testMailingsSendDateAsString;
    private String targetExpression;
    private int targetMode;
    private int splitSize;
    private String reportUrl;

    public String getDescription() {
        return optimization.getDescription();
    }

    public void setDescription(String description) {
        optimization.setDescription(description);
    }

    public String getShortname() {
        return optimization.getShortname();
    }

    public void setShortname(String shortname) {
        optimization.setShortname(shortname);
    }

    public int getCampaignID() {
        return optimization.getCampaignID();
    }

    public void setCampaignID(int campaignID) {
        optimization.setCampaignID(campaignID);
    }

    public String getCampaignName() {
        return optimization.getCampaignName();
    }

    public void setCampaignName(String campaignName) {
        optimization.setCampaignName(campaignName);
    }

    public int getCompanyID() {
        return optimization.getCompanyID();
    }

    public void setCompanyID(@VelocityCheck int companyID) {
        optimization.setCompanyID(companyID);
    }

    public String getSplitType() {
        return optimization.getSplitType();
    }

    public void setSplitType(String splitType) {
        optimization.setSplitType(splitType);
    }

    public String getEvalType() {
        return optimization.getEvalType() != null ? optimization.getEvalType().name() : "";
    }

    public void setEvalType(String evalType) {
        optimization.setEvalType(HtmlUtils.parseEnumSafe(WorkflowDecision.WorkflowAutoOptimizationCriteria.class, evalType));
    }

    public int getGroup1() {
        return optimization.getGroup1();
    }

    public void setGroup1(int group1) {
        optimization.setGroup1(group1);
    }

    public int getGroup2() {
        return optimization.getGroup2();
    }

    public void setGroup2(int group2) {
        optimization.setGroup2(group2);
    }

    public int getGroup3() {
        return optimization.getGroup3();
    }

    public void setGroup3(int group3) {
        optimization.setGroup3(group3);
    }

    public int getGroup4() {
        return optimization.getGroup4();
    }

    public void setGroup4(int group4) {
        optimization.setGroup4(group4);
    }

    public int getGroup5() {
        return optimization.getGroup5();
    }

    public void setGroup5(int group5) {
        optimization.setGroup5(group5);
    }

    public int getResultMailingID() {
        return optimization.getResultMailingID();
    }

    public void setResultMailingID(int resultMailingID) {
        optimization.setResultMailingID(resultMailingID);
    }

    public int getOptimizationID() {
        return optimization.getId();
    }

    public void setOptimizationID(int optimizationID) {
        optimization.setId(optimizationID);
    }

    public int getTargetID() {
        return optimization.getTargetID();
    }

    public void setTargetID(int targetID) {
        optimization.setTargetID(targetID);
    }

    public int getMailinglistID() {
        return optimization.getMailinglistID();
    }

    public void setMailinglistID(int mailinglistID) {
        optimization.setMailinglistID(mailinglistID);
    }

    public int getStatus() {
        return optimization.getStatus();
    }

    public void setStatus(int status) {
        optimization.setStatus(status);
    }

    public ComOptimization getOptimization() {
        return optimization;
    }

    public void setOptimization(ComOptimization optimization) {
        this.optimization = optimization;
    }

    public String getThresholdString() {
        return thresholdString;
    }

    public void setThresholdString(String thresholdString) {
        this.thresholdString = thresholdString;
    }

    public boolean isDoublechecking() {
        return optimization.isDoubleCheckingActivated();
    }

    public void setDoublechecking(boolean activated) {
        optimization.setDoubleCheckingActivated(activated);
    }

    public String getResultSendDateAsString() {
        return resultSendDateAsString;
    }

    public void setResultSendDateAsString(String resultSendDateAsString) {
        this.resultSendDateAsString = resultSendDateAsString;
    }

    public String getTestMailingsSendDateAsString() {
        return testMailingsSendDateAsString;
    }

    public void setTestMailingsSendDateAsString(String testMailingsSendDateAsString) {
        this.testMailingsSendDateAsString = testMailingsSendDateAsString;
    }

    public String getTargetExpression() {
        return targetExpression;
    }

    public void setTargetExpression(String targetExpression) {
        this.targetExpression = targetExpression;
    }

    public int getTargetMode() {
        return targetMode;
    }

    public void setTargetMode(int targetMode) {
        this.targetMode = targetMode;
    }

    public int getSplitSize() {
        return this.splitSize;
    }

    public void setSplitSize(int splitSize) {
        this.splitSize = splitSize;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

}
