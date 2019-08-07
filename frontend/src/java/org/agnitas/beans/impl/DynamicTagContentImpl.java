/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.util.Objects;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang.StringUtils;

public class DynamicTagContentImpl implements DynamicTagContent {

    protected int mailingID;
    protected int companyID;
    protected int dynNameID;
    protected int id;
    protected String dynName;
    protected int dynOrder;
    protected int targetID;
    protected String dynContent;
    
    public static int WITHOUT_CLOB_CONTENT=0;
    public static int WITH_CLOB_CONTENT=1;
    
    /** Creates new DynamicTagContent */
    public DynamicTagContentImpl() {
    }
    
    @Override
    public void setDynNameID(int id) {
        dynNameID=id;
    }

    @Override
    public void setId(int id) {
        this.id=id;
    }

    @Override
    public void setDynName(String name) {
        dynName=name;
    }

    @Override
    public void setDynContent(String content) {
        dynContent=content;
    }
    
    @Override
    public void setCompanyID( @VelocityCheck int id) {
        companyID=id;
    }

    @Override
    public void setMailingID(int id) {
        mailingID=id;
    }

    @Override
    public void setDynOrder(int id) {
        dynOrder=id;
    }

    @Override
    public void setTargetID(int tid) {
        targetID=tid;
    }
    
    @Override
    public int getDynOrder() {
        return dynOrder;
    }
    
    @Override
    public int getDynNameID() {
        return dynNameID;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getDynName() {
        return dynName;
    }
    
    @Override
    public String getDynContent() {
    	if(dynContent == null) {
            dynContent = "";
        }
    	
        return dynContent;
    }
    
    @Override
    public int getTargetID() {
        return targetID;
    }

    @Override
    public final boolean equals(final Object obj) {
    	if (obj instanceof DynamicTagContent) {
            DynamicTagContent other = (DynamicTagContent) obj;
            return StringUtils.equals(getDynContent(), other.getDynContent());
    	}

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dynContent);
    }

    /**
     * Getter for property mailingID.
     * @return Value of property mailingID.
     */
    @Override
    public int getMailingID() {
        return this.mailingID;
    }

    /**
     * Getter for property companyID.
     * @return Value of property companyID.
     */
    @Override
    public int getCompanyID() {
        return this.companyID;
    }


    @Override
    public DynamicTagContent clone() {
        DynamicTagContentImpl dynamicTagContent = new DynamicTagContentImpl();

        dynamicTagContent.setTargetID(targetID);
        dynamicTagContent.setMailingID(mailingID);
        dynamicTagContent.setDynOrder(dynOrder);
        dynamicTagContent.setDynNameID(dynNameID);
        dynamicTagContent.setDynContent(dynContent);
        dynamicTagContent.setCompanyID(companyID);
        dynamicTagContent.setId(id);

        return dynamicTagContent;
    }
}
