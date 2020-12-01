/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.agnitas.beans.Title;
import org.agnitas.emm.core.velocity.VelocityCheck;

public class TitleImpl implements Title, Serializable {
    private static final long serialVersionUID = 2291851535887967372L;
	protected int companyID=-1;
    protected int Id;
    protected String description;
    protected Map<Integer, String> titleGender=new HashMap<>();
   
    // CONSTRUCTOR:
    public TitleImpl() {
    }
    
    // * * * * *
    //  SETTER:
    // * * * * *
    @Override
	public void setCompanyID( @VelocityCheck int company) {
        this.companyID=company;
    }
    
    @Override
	public void setId(int title) {
        this.Id=title;
    }
    
    @Override
	public void setDescription(String desc) {
        this.description = desc;
    }
    
    @Override
	public void setTitleGender(Map<Integer, String> titleGender) {
        this.titleGender=titleGender;
    }

    // * * * * *
    //  GETTER:
    // * * * * *
    @Override
	public int getCompanyID() {
        return companyID;
    }
    
    @Override
	public int getId() {
        return this.Id;
    }

    @Override
	public String getDescription() {
        return this.description;
    }

    @Override
	public Map<Integer, String> getTitleGender() {
        return this.titleGender;
    }
    
    @Override
	public boolean equals(Object o) {
        if(!getClass().isInstance(o)) {
            return false;
        }

        Title t=(Title) o;

        if(t.getCompanyID() != companyID)
            return false;

        if(t.getId() != this.Id)
            return false;

        return true;
    }

    @Override
	public int hashCode() {
	Integer i=new Integer((companyID*100)+this.Id);

        return i.hashCode();
    }
}
