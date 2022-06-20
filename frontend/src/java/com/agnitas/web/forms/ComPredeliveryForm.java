/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.Collection;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.web.forms.StrutsFormBase;
import org.apache.struts.action.ActionMapping;

import com.agnitas.beans.ComPredeliveryProvider;

// TODO Not longer used? GWUA-4803 delete when migration is finished
public class ComPredeliveryForm extends StrutsFormBase {

	private static final long serialVersionUID = -7395594021916269872L;

	/**
	 * Holds value of property action.
	 */
	private int action;

	/**
	 * Holds value of property mailingID.
	 */
	private int mailingID;

	/**
	 * Holds value of property shortname. 
	 */
	private String shortname;
    
	/**
	 * The list holding the provider info.
	 */
	private Collection<ComPredeliveryProvider>	providers=null;

	/**
	 * Reset all properties to their default values.
	 *
	 * @param mapping The mapping used to select this instance
	 * @param request The servlet request we are processing
	 */
	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		this.mailingID=0;
		this.shortname = "";
	}
    
	/**
	 * Getter for property action.
	 *
	 * @return Value of property action.
	 */
	public int getAction() {
		return this.action;
	}

	/**
	 * Setter for property action.
	 *
	 * @param action New value of property action.
	 */
	public void setAction(int action) {
		this.action = action;
	}

	/**
	 * Getter for property mailingID.
	 *
	 * @return Value of property mailingID.
	 */
	public int getMailingID() {
		return this.mailingID;
	}

	/**
	 * Getter for property shortname.
	 *
	 * @return Value of property shortname.
	 */
	public String getShortname() {
		return this.shortname;
	}
    
	/**
	 * Setter for property shortname.
	 *
	 * @param shortname New value of property shortname.
	 */
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	/**
	 * Setter for property mailingID.
	 *
	 * @param mailingID New value of property mailingID.
	 */
	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	/**
	 * Holds value of property isTemplate.
	 */
	private boolean isTemplate;

	/**
	 * Getter for property isTemplate.
	 *
	 * @return Value of property isTemplate.
	 */
	public boolean isIsTemplate() {
		return this.isTemplate;
	}

	/**
	 * Setter for property isTemplate.
	 *
	 * @param isTemplate New value of property isTemplate.
	 */
	public void setIsTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

	/**
	 * Holds value of property started.
	 */
	private boolean	started;

	/**
	 * Getter for property started.
	 *
	 * @return Value of property started.
	 */
	public boolean isStarted() {
		return this.started;
	}

	/**
	 * Setter for property started.
	 *
	 * @param started New value of property started.
	 */
	public void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * Setter for the provider information.
	 */
	public void	setProviders(Collection<ComPredeliveryProvider> providers)	{
		this.providers=providers;
	}

	/**
	 * Getter for the provider information.
	 */
	public Collection<ComPredeliveryProvider>	getProviders()	{
		return providers;
	}
	
	/**
	 * Holds value of property providerID.
	 */
	private String providerID=null;

	/**
	 * Setter for the providerID.
	 */
	public void	setProviderID(String providerID)	{
		this.providerID=providerID;
	}

	/**
	 * Getter for the providerID.
	 */
	public String getProviderID()	{
		return providerID;
	}

	/**
	 * Holds value of property sendDate.
	 */
	private Date sendDate=null;

	/**
	 * Setter for the sendDate.
	 */
	public void	setSendDate(Date sendDate)	{
		this.sendDate=sendDate;
	}

	/**
	 * Getter for the sendDate.
	 */
	public Date getSendDate()	{
		return sendDate;
	}

	/**
	 * Getter for the current provider.
	 */
	public ComPredeliveryProvider getProvider()	{
		for (ComPredeliveryProvider	provider : providers) {
			if (provider.getProviderID().equals(providerID)) {
				return provider;
			}
		}
		return null;
	}
}
