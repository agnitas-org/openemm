/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.SafeString;
import org.agnitas.web.forms.CampaignForm;
import org.apache.struts.action.ActionMapping;

public class ComCampaignForm extends CampaignForm {
		
	private static final long serialVersionUID = -8197053872426611232L;

	// for the biggest revenue
    private double biggestRevenue=0.0;		// biggest value from       - for the correct graphical representation in the JSP
    private double totalRevenue = 0.0;		// the Sum of all Revenues.
    
    // for revenue
    /**
     * numRevenue is a Hashtable with MailingID as Key and the Revenues as Value.
     */
    private Map<Integer, Double> numRevenue = null;
    private int mailingID;

	 /**
     * returns the numRevenue Hashtable with mailID as Key and revenue as Value
     * @return
     */
	public Map<Integer, Double> getNumRevenue() {
		return numRevenue;
	}

	/**
	 * Sets the Hashtable for numRevenue. 
	 * @param revenue
	 */
	public void setNumRevenue(Map<Integer, Double> revenue) {
		this.numRevenue = revenue;
	}
	
	/**
	 * Getter for property biggestRevenue
	 * @return
	 */
	public double getBiggestRevenue() {
		return biggestRevenue;
	}

	/**
	 * Setter for property biggestRevenue
	 * @param biggestRevenue
	 */
	public void setBiggestRevenue(double biggestRevenue) {
		this.biggestRevenue = biggestRevenue;
	}
	
	/**
	 * Getter for the total Revenue	  
	 */
	public double getTotalRevenue() {
		return this.totalRevenue;
	}
	
	/**
	 * Setter for the total Revenue
	 */
	public void setTotalRevenue(double in_Revenue) {
		this.totalRevenue = in_Revenue;
	}

    public int getMailingID() {
        return mailingID;
    }

    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

    /**
	 * Reset all properties to their default values.
	 *
	 * @param mapping
	 *            The mapping used to select this instance
	 * @param request
	 *            The servlet request we are processing
	 */
	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		super.reset(mapping, request);

        Locale locale = AgnUtils.getLocale(request);
		this.shortname = SafeString.getLocaleString("default.Name", locale);
		this.description = SafeString.getLocaleString("default.description", locale);

		// Revenue-Values
		this.biggestRevenue = 0.0;
		this.numRevenue = new Hashtable<>();
        this.mailingID = 0;
	}
}
