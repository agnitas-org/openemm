/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.Map;

import org.agnitas.web.forms.CompareMailingForm;

public final class ComCompareMailingForm extends CompareMailingForm {
	private static final long serialVersionUID = 4740997535327896353L;
	
	private String reportUrl;
    private String reportFormat;
    private String[] selectedTargets;
    private String recipientType = "ALL_SUBSCRIBERS";
    private int frameHeight;

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public String getReportFormat() {
        return reportFormat;
    }

    public void setReportFormat(String reportFormat) {
        this.reportFormat = reportFormat;
    }
    
    // the properties and methods bellow are not needed for BIRT mailing-compare statistics
    // the are not removed in order to have ComCompareMailingForm to be compatible with ComCompareMailingStatsImpl

    private double biggestRevenue = 0.0;		// biggest value from       - for the correct graphical representation in the JSP
    // for revenue
    /**
     * numRevenue is a Hashtable with MailingID as Key and the Revenues as Value.
     */
    private Map<Integer, Number> numRevenue = null;

	public Map<Integer, Number> getNumRevenue() {
		return numRevenue;
	}

	public void setNumRevenue(Map<Integer, Number> revenue) {
		this.numRevenue = revenue;
	}

	public double getBiggestRevenue() {
		return biggestRevenue;
	}

	public void setBiggestRevenue(double biggestRevenue) {
		this.biggestRevenue = biggestRevenue;
	}

    public String[] getSelectedTargets() {
        return selectedTargets;
    }

    public void setSelectedTargets(String[] selectedTargets) {
        this.selectedTargets = selectedTargets;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }
}
