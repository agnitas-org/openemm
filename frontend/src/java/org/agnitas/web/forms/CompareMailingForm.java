/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.agnitas.web.ComCompareMailingAction;

public class CompareMailingForm extends StrutsFormBase {
	private static final long serialVersionUID = 8456061813681855065L;

	private int targetID;
	private int action;

	private int biggestOptouts; // biggest value from optouts[] - for the correct graphical representation in the JSP
	private int biggestBounce; // biggest value from bounces[] - for the correct graphical representation in the JSP
	private int biggestOpened; // biggest value from opened[] - for the correct graphical representation in the JSP
	private int biggestRecipients; // biggest value from recipients[] - for the correct graphical representation in the JSP
	private int biggestClicks; // biggest value from clicks[] - for the correct graphical representation in the JSP

	private String cvsfile;

	/**
	 * Holds value of property numOpen.
	 */
	private Map<Integer, Integer> numOpen;

	/**
	 * Holds value of property mailings.
	 */
	protected List<Integer> mailings;

	/**
	 * Holds value of property numBounce.
	 */
	private Map<Integer, Integer> numBounce;

	/**
	 * Holds value of property numRecipients.
	 */
	private Map<Integer, Integer> numRecipients;

	/**
	 * Holds value of property numOptout.
	 */
	private Map<Integer, Integer> numOptout;

	/**
	 * Holds value of property numClicks.
	 */
	private Map<Integer, Integer> numClicks;

	/**
	 * Holds value of property mailingName.
	 */
	private Map<Integer, String> mailingName;

	/**
	 * Holds value of property mailingDescription.
	 */
	private Map<Integer, String> mailingDescription;

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
		if (mailings == null) {
			resetHashTables();
		}
	}

	protected void resetHashTables() {
		mailings = new LinkedList<>();
		mailingDescription = new Hashtable<>();
		mailingName = new Hashtable<>();
		numBounce = new Hashtable<>();
		numClicks = new Hashtable<>();
		numOpen = new Hashtable<>();
		numOptout = new Hashtable<>();
		numRecipients = new Hashtable<>();
	}

	public void resetForNewCompare() {
		resetResults();
		resetHashTables();
	}

	/**
	 * Validate the properties that have been set from this HTTP request, and
	 * return an <code>ActionErrors</code> object that encapsulates any
	 * validation errors that have been found. If no errors are found, return
	 * <code>null</code> or an <code>ActionErrors</code> object with no recorded
	 * error messages.
	 *
	 * @param mapping
	 *            The mapping used to select this instance
	 * @param request
	 *            The servlet request we are processing
	 */

	@Override
	public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if (action == ComCompareMailingAction.ACTION_COMPARE) {
			String curr;
			Integer tmpInt = null;
			boolean isFirst = true;

			// get all Parameters from Form.
			Enumeration<String> params = request.getParameterNames();
			while (params.hasMoreElements()) {
				curr = params.nextElement();
				if (curr.startsWith("MailCompID_")) { // Form uses "MailCompID_" for storing properties.
					if (isFirst) { // if selection done, reset lists first
						mailings = null;
						reset(mapping, request);
						isFirst = false;
					}
					tmpInt = new Integer(curr.substring(11, curr.length()));
					validateCleanUp(tmpInt);

				}
			}

			if (mailings.size() < 2 || mailings.size() > 10) {
				errors.add("shortname", new ActionMessage("error.NrOfMailings"));
			}

			Collections.sort(mailings, new CompareDescending());
		}

		return errors;
	}

	protected void validateCleanUp(Integer tmpInt) {
		mailings.add(tmpInt);
		numBounce.put(tmpInt, new Integer(0));
		numClicks.put(tmpInt, new Integer(0));
		numOpen.put(tmpInt, new Integer(0));
		numOptout.put(tmpInt, new Integer(0));
		numRecipients.put(tmpInt, new Integer(0));
	}

	/**
	 * Clear results from previous run.
	 */
	public void resetResults() {
		for (Integer id : mailings) {
			resetNumResults(id);
		}

		resetBiggestResults();
	}

	protected void resetBiggestResults() {
		biggestBounce = 0;
		biggestClicks = 0;
		biggestOpened = 0;
		biggestOptouts = 0;
		biggestRecipients = 0;
	}

	protected void resetNumResults(Integer id) {
		numBounce.put(id, new Integer(0));
		numClicks.put(id, new Integer(0));
		numOpen.put(id, new Integer(0));
		numOptout.put(id, new Integer(0));
		numRecipients.put(id, new Integer(0));
	}

	/**
	 * Getter for property targetID.
	 *
	 * @return Value of property targetID.
	 */
	public int getTargetID() {
		return targetID;
	}

	/**
	 * Setter for property targetID.
	 *
	 * @param targetID
	 *            New value of property targetID.
	 */
	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}

	/**
	 * Getter for property action.
	 *
	 * @return Value of property action.
	 */
	public int getAction() {
		return action;
	}

	/**
	 * Setter for property action.
	 *
	 * @param action
	 *            New value of property action.
	 */
	public void setAction(int action) {
		this.action = action;
	}

	/**
	 * Getter for property biggestOptouts.
	 *
	 * @return Value of property biggestOptouts.
	 */
	public int getBiggestOptouts() {
		return biggestOptouts;
	}

	/**
	 * Setter for property biggestOptouts.
	 *
	 * @param biggestOptouts
	 *            New value of property biggestOptouts.
	 */
	public void setBiggestOptouts(int biggestOptouts) {
		this.biggestOptouts = biggestOptouts;
	}

	/**
	 * Getter for property biggestBounce.
	 *
	 * @return Value of property biggestBounce.
	 */
	public int getBiggestBounce() {
		return biggestBounce;
	}

	/**
	 * Setter for property biggestBounce.
	 *
	 * @param biggestBounce
	 *            New value of property biggestBounce.
	 */
	public void setBiggestBounce(int biggestBounce) {
		this.biggestBounce = biggestBounce;
	}

	/**
	 * Getter for property biggestOpened.
	 *
	 * @return Value of property biggestOpened.
	 */
	public int getBiggestOpened() {
		return biggestOpened;
	}

	/**
	 * Setter for property biggestOpened.
	 *
	 * @param biggestOpened
	 *            New value of property biggestOpened.
	 */
	public void setBiggestOpened(int biggestOpened) {
		this.biggestOpened = biggestOpened;
	}

	/**
	 * Getter for property biggestRecipients.
	 *
	 * @return Value of property biggestRecipients.
	 */
	public int getBiggestRecipients() {
		return biggestRecipients;
	}

	/**
	 * Setter for property biggestRecipients.
	 *
	 * @param biggestRecipients
	 *            New value of property biggestRecipients.
	 */
	public void setBiggestRecipients(int biggestRecipients) {
		this.biggestRecipients = biggestRecipients;
	}

	/**
	 * Getter for property biggestClicks.
	 *
	 * @return Value of property biggestClicks.
	 */
	public int getBiggestClicks() {
		return biggestClicks;
	}

	/**
	 * Setter for property biggestClicks.
	 *
	 * @param biggestClicks
	 *            New value of property biggestClicks.
	 */
	public void setBiggestClicks(int biggestClicks) {
		this.biggestClicks = biggestClicks;
	}

	/**
	 * Getter for property biggestCvsfile.
	 *
	 * @return Value of property biggestCvsfile.
	 */
	public String getCvsfile() {
		return cvsfile;
	}

	/**
	 * Setter for property cvsfile.
	 *
	 * @param cvsfile
	 *            New value of property cvsfile.
	 */
	public void setCvsfile(String cvsfile) {
		this.cvsfile = cvsfile;
	}

	/**
	 * Getter for property numOpen.
	 *
	 * @return Value of property numOpen.
	 */
	public Map<Integer, Integer> getNumOpen() {
		return numOpen;
	}

	/**
	 * Setter for property numOpen.
	 *
	 * @param numOpen
	 *            New value of property numOpen.
	 */
	public void setNumOpen(Map<Integer, Integer> numOpen) {
		this.numOpen = numOpen;
	}

	/**
	 * Getter for property mailings.
	 *
	 * @return Value of property mailings.
	 */
	public List<Integer> getMailings() {
		return mailings;
	}

	/**
	 * Setter for property mailings.
	 *
	 * @param mailings
	 *            New value of property mailings.
	 */
	public void setMailings(List<Integer> mailings) {
		this.mailings = mailings;
	}

	/**
	 * Getter for property numBounce.
	 *
	 * @return Value of property numBounce.
	 */
	public Map<Integer, Integer> getNumBounce() {
		return numBounce;
	}

	/**
	 * Setter for property numBounce.
	 *
	 * @param numBounce
	 *            New value of property numBounce.
	 */
	public void setNumBounce(Map<Integer, Integer> numBounce) {
		this.numBounce = numBounce;
	}

	/**
	 * Getter for property numReceipients.
	 *
	 * @return Value of property numReceipients.
	 */
	public Map<Integer, Integer> getNumRecipients() {
		return numRecipients;
	}

	/**
	 * Setter for property numReceipients.
	 *
	 * @param numRecipients
	 *            New value of property numReceipients.
	 */
	public void setNumRecipients(Map<Integer, Integer> numRecipients) {
		this.numRecipients = numRecipients;
	}

	/**
	 * Getter for property numOptout.
	 *
	 * @return Value of property numOptout.
	 */
	public Map<Integer, Integer> getNumOptout() {
		return numOptout;
	}

	/**
	 * Setter for property numOptout.
	 *
	 * @param numOptout
	 *            New value of property numOptout.
	 */
	public void setNumOptout(Map<Integer, Integer> numOptout) {
		this.numOptout = numOptout;
	}

	/**
	 * Getter for property numClicks.
	 *
	 * @return Value of property numClicks.
	 */
	public Map<Integer, Integer> getNumClicks() {
		return numClicks;
	}

	/**
	 * Setter for property numClicks.
	 *
	 * @param numClicks
	 *            New value of property numClicks.
	 */
	public void setNumClicks(Map<Integer, Integer> numClicks) {
		this.numClicks = numClicks;
	}

	/**
	 * Getter for property mailingName.
	 *
	 * @return Value of property mailingName.
	 */
	public Map<Integer, String> getMailingName() {
		return mailingName;
	}

	/**
	 * Setter for property mailingName.
	 *
	 * @param mailingName
	 *            New value of property mailingName.
	 */
	public void setMailingName(Map<Integer, String> mailingName) {
		this.mailingName = mailingName;
	}

	/**
	 * Getter for property mailingDescription.
	 *
	 * @return Value of property mailingDescription.
	 */
	public Map<Integer, String> getMailingDescription() {
		return mailingDescription;
	}

	/**
	 * Setter for property mailingDescription.
	 *
	 * @param mailingDescription
	 *            New value of property mailingDescription.
	 */
	public void setMailingDescription(Map<Integer, String> mailingDescription) {
		this.mailingDescription = mailingDescription;
	}

	private class CompareDescending implements Comparator<Integer> {
		@Override
		public int compare(Integer a, Integer b) {
			return (b.intValue() - a.intValue());
		}
	}
}
