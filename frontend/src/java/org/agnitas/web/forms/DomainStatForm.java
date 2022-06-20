/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web.forms;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

public class DomainStatForm extends StrutsFormBase {
    private static final long serialVersionUID = 7400928630109355568L;
	private int listID;
    private int targetID;
    private int action;
    private int lines;    
    private int rest;
    private int total;
    private int maxDomains = 20;
    private List<String> domains;
    private List<Integer> subscribers;    
    private String csvfile;
    
    /**
     * Holds value of property loaded.
     */
    private boolean loaded;
    
    /**
     * Holds value of property statReady.
     */
    private boolean statReady;
    
    /**
     * Holds value of property statInProgress.
     */
    private boolean statInProgress;
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    @Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {

        // this.listID = 0;
        // this.targetID = 0;
        // Locale aLoc=AgnUtils.getLocale(request);
        // MessageResources text=this.getServlet().getResources();
    }


    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     * 
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     * @return errors
     */
    @Override
	public ActionErrors formSpecificValidate(ActionMapping mapping,
                                 HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();
        
        return errors;
    }

    /**
     * Setter for property listID.
     *
     * @param listID New value of property listID.
     */
    public void setListID(int listID) {
        this.listID = listID;
    }
    
     /**
     * Setter for property targetID.
     *
     * @param targetID New value of property targetID.
     */
    public void setTargetID(int targetID) {
        this.targetID = targetID;
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
     * Setter for property lines.
     *
     * @param lines New value of property lines.
     */
    public void setLines(int lines) {
        this.lines = lines;
    }
    
     /**
     * Setter for property rest.
     *
     * @param rest New value of property rest.
     */
    public void setRest(int rest) {
        this.rest = rest;
    }
    
     /**
     * Setter for property total.
     *
     * @param total New value of property total.
     */
    public void setTotal(int total) {
        this.total = total;
    }
    
     /**
     * Setter for property domains.
     *
     * @param domains New value of property domains.
     */
    public void setDomains(List<String> domains) {
        this.domains = domains;
    }
    
     /**
     * Setter for property subscribers.
     *
     * @param subscribers New value of property subscribers.
     */
    public void setSubscribers(List<Integer> subscribers) {
        this.subscribers = subscribers;
    }
    
     /**
     * Setter for property maxDomains.
     *
     * @param maxDomains New value of property maxDomains.
     */
    public void setMaxDomains(int maxDomains) {
        this.maxDomains = maxDomains;
    }

     /**
     * Setter for property csvfile.
     *
     * @param csvfile New value of property csvfile.
     */
    public void setCsvfile(String csvfile) {
        this.csvfile = csvfile;
    }

     /**
     * Setter for property loaded.
     *
     * @param loaded New value of property loaded.
     */
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    
    
    /**
     * Getter for property listID.
     *
     * @return Value of property listID.
     */
    public int getListID() {
        return this.listID;
    }
   
    /**
     * Getter for property targetID.
     *
     * @return Value of property targetID.
     */
    public int getTargetID() {
        return this.targetID;
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
     * Getter for property lines.
     *
     * @return Value of property lines.
     */
    public int getLines() {
        return this.lines;
    }
    
    /**
     * Getter for property rest.
     *
     * @return Value of property rest.
     */
    public int getRest() {
        return this.rest;
    }
    
    /**
     * Getter for property total.
     *
     * @return Value of property total.
     */
    public int getTotal() {
        return this.total;
    }
    
    /**
     * Getter for property domains.
     *
     * @return Value of property domains.
     */
    public String getDomains(int ndx) {
        return domains.get(ndx);
    }
    
    /**
     * Getter for property domains.
     *
     * @return Value of property domains.
     */
    public List<String> getDomains() {
        return domains;
    }

    /**
     * Getter for property subscribers.
     *
     * @return Value of property subscribers.
     */
    public List<Integer> getSubscribers() {
        return subscribers;
    }
    
    /**
     * Getter for property subscribers.
     *
     * @return Value of property subscribers.
     */
    public int getSubscribers(int ndx) {
        return subscribers.get(ndx).intValue();
    }

    /**
     * Getter for property maxDomains.
     *
     * @return Value of property maxDomains.
     */
    public int getMaxDomains() {
        return this.maxDomains;
    }    
    
    /**
     * Getter for property csvfile.
     *
     * @return Value of property csvfile.
     */
    public String getCsvfile() {
        return this.csvfile;
    }
    
    /**
     * Getter for property loaded.
     *
     * @return Value of property loaded.
     */
    public boolean isLoaded() {
        return this.loaded;
    }
   
    /**
     * Getter for property statReady.
     *
     * @return Value of property statReady.
     */
    public boolean isStatReady() {
        return this.statReady;
    }
    
    /**
     * Setter for property statReady.
     *
     * @param statReady New value of property statReady.
     */
    public void setStatReady(boolean statReady) {
        this.statReady = statReady;
    }
    
    /**
     * Getter for property statInProgress.
     *
     * @return Value of property statInProgress.
     */
    public boolean isStatInProgress() {
        return this.statInProgress;
    }
    
    /**
     * Setter for property statInProgress.
     *
     * @param statInProgress New value of property statInProgress.
     */
    public void setStatInProgress(boolean statInProgress) {
        this.statInProgress = statInProgress;
    }
    
}
