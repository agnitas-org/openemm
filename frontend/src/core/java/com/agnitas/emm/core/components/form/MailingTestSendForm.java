/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.form;

import com.agnitas.emm.core.components.entity.TestRunOption;

public class MailingTestSendForm {

    private int adminTargetGroupID;
    private String[] mailingTestRecipients = new String[] {};
    private TestRunOption testRunOption;
    private String targetName;
    private boolean requestApproval; // GWUA-5738 approval via mailing link

    public int getAdminTargetGroupID() {
        return adminTargetGroupID;
    }

    public void setAdminTargetGroupID(int adminTargetGroupID) {
        this.adminTargetGroupID = adminTargetGroupID;
    }

    public String[] getMailingTestRecipients() {
        return mailingTestRecipients;
    }

    public void setMailingTestRecipients(String[] mailingTestRecipients) {
        this.mailingTestRecipients = mailingTestRecipients;
    }

    public TestRunOption getTestRunOption() {
        return testRunOption;
    }

    public void setTestRunOption(TestRunOption testRunOption) {
        this.testRunOption = testRunOption;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public boolean isRequestApproval() {
        return requestApproval;
    }

    public void setRequestApproval(boolean requestApproval) {
        this.requestApproval = requestApproval;
    }
}
