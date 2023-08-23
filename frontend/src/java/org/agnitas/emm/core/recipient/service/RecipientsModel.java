/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service;

import java.util.List;



public class RecipientsModel {

	private int companyId;
	protected boolean matchAll;

    private List<CriteriaEquals> criteriaEquals;
    
    private String eql;

    public int getCompanyId() {
		return companyId;
	}
	
	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}
	
    public boolean isMatchAll() {
        return matchAll;
    }

    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }
    
    public List<CriteriaEquals> getCriteriaEquals() {
        return criteriaEquals;
    }

    public void setCriteriaEquals(List<CriteriaEquals> criteriaEquals) {
        this.criteriaEquals = criteriaEquals;
    }
	
    public static class CriteriaEquals {
	    protected String profilefield;
	    protected String value;
	    protected String dateformat;
	    
        public CriteriaEquals(String profilefield, String value, String dateformat) {
            super();
            this.profilefield = profilefield;
            this.value = value;
            this.dateformat = dateformat;
        }
        
        public String getProfilefield() {
            return profilefield;
        }
        public void setProfilefield(String profilefield) {
            this.profilefield = profilefield;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public String getDateformat() {
            return dateformat;
        }
        public void setDateformat(String dateformat) {
            this.dateformat = dateformat;
        }
	}

    public void setEql(final String eql) {
    	this.eql = eql;
    }
    
    public String getEql() {
    	return this.eql;
    }
	
}
