/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.Mailinglist;
import org.agnitas.util.DbUtilities;
import org.agnitas.web.TargetForm;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import com.agnitas.emm.core.target.beans.TargetComplexityGrade;
import com.agnitas.web.ComTargetAction;

public class ComTargetForm extends TargetForm {
	private static final long serialVersionUID = 6278581214213995065L;

	/**
	 * Type code of "mailing revenue" rule node.
	 */
	@Deprecated
	public static final int COLUMN_TYPE_MAILING_REVENUE = 7;

	/**
	 * Type code of "clicked specific link in mailing" rule node
	 */
	@Deprecated
	public static final int COLUMN_TYPE_MAILING_CLICKED_SPECIFIC_LINK = 8;
	
	/**
	 * Set of ID marked for bulk action. Ensure this set is cleared
	 */
	private Set<Integer> bulkIDs = new HashSet<>();
	private boolean useForAdminAndTestDelivery;
	private boolean showWorldDelivery;
	private boolean showTestAdminDelivery;
	private boolean locked;
    private int mailinglistId;
    private List<Mailinglist> mailinglists;
    private String searchQueryText;
    private boolean searchNameChecked = true;
    private boolean searchDescriptionChecked = true;
    private boolean searchEnabled;
    private String eql;
	private int mailingId;
	private Map<Integer, TargetComplexityGrade> targetComplexities;

    @Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		super.reset(mapping, request);
		
		setEql("");
		
		clearBulkIds();
		
		this.useForAdminAndTestDelivery = false;
		this.setMailingId(0);
		this.setLocked(false);

		setNumberOfRows(-1);
	}

	public void setBulkID( int id, String value) {
		if( value != null && (value.equals( "on") || value.equals( "yes") || value.equals( "true"))) {
			this.bulkIDs.add(id);
		}
	}
	
	public String getBulkID( int id) {
		return this.bulkIDs.contains( id) ? "on" : "";
	}
	
	public Set<Integer> getBulkIds() {
		return this.bulkIDs;
	}
	
	public void clearBulkIds() {
		this.bulkIDs.clear();
	}
	
    public boolean isUseForAdminAndTestDelivery() {
		return useForAdminAndTestDelivery;
	}

	public void setUseForAdminAndTestDelivery(boolean useForAdminAndTestDelivery) {
		this.useForAdminAndTestDelivery = useForAdminAndTestDelivery;
	}

	@Override
    public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
        if (getAction() == ComTargetAction.ACTION_LIST) {
            ActionErrors actionErrors = new ActionErrors();
            if (searchNameChecked || searchDescriptionChecked) {
                for (String error : DbUtilities.validateFulltextSearchQueryText(searchQueryText)) {
                    actionErrors.add("invalid_search_query", new ActionMessage(error));
                }
            }
            return actionErrors;
        }
        
        return super.formSpecificValidate(mapping, request);
    }

	public boolean isShowWorldDelivery() {
		return showWorldDelivery || (!showWorldDelivery && !showTestAdminDelivery);
	}

	public void setShowWorldDelivery(boolean showWorldDelivery) {
		this.showWorldDelivery = showWorldDelivery;
	}

	public boolean isShowTestAndAdminDelivery() {
		return showTestAdminDelivery;
	}

	public void setShowTestAndAdminDelivery(boolean showTestAdminDelivery) {
		this.showTestAdminDelivery = showTestAdminDelivery;
	}
	
	public void setLocked( boolean locked) {
		this.locked = locked;
	}
	
	public boolean isLocked() {
		return this.locked;
	}

    public int getMailinglistId() {
        return mailinglistId;
    }

    public void setMailinglistId(int mailinglistId) {
        this.mailinglistId = mailinglistId;
    }

    public List<Mailinglist> getMailinglists() {
        return mailinglists;
    }

    public void setMailinglists(List<Mailinglist> mailinglists) {
        this.mailinglists = mailinglists;
    }
  
    public String getSearchQueryText() {
        return searchQueryText;
    }

    public void setSearchQueryText(String searchQueryText) {
        this.searchQueryText = searchQueryText;
    }

    public boolean isSearchNameChecked() {
        return searchNameChecked;
    }

    public void setSearchNameChecked(boolean searchNameChecked) {
        this.searchNameChecked = searchNameChecked;
    }

    public boolean isSearchDescriptionChecked() {
        return searchDescriptionChecked;
    }

    public void setSearchDescriptionChecked(boolean searchDescriptionChecked) {
        this.searchDescriptionChecked = searchDescriptionChecked;
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public void setSearchEnabled(boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }
    
    public void setEql(String eql) {
    	this.eql = eql;
    }
    
    public String getEql() {
    	return this.eql;
    }

	public int getMailingId() {
		return mailingId;
	}

	public void setMailingId(int mailingId) {
		this.mailingId = mailingId;
	}

	public Map<Integer, TargetComplexityGrade> getTargetComplexities() {
		return targetComplexities;
	}

	public void setTargetComplexities(Map<Integer, TargetComplexityGrade> targetComplexities) {
		this.targetComplexities = targetComplexities;
	}
}
