/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import org.apache.log4j.Logger;

import com.agnitas.beans.ComTarget;

/**
 * Implementation of {@link ComTarget} interface.
 */
public class ComTargetImpl extends TargetLightImpl implements ComTarget {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComTargetImpl.class);

	/** Fragment of SQL WHERE clause built from the target group. */
    protected String targetSQL;

    /** Flag, if target group is used for admin and test delivery. */
	private boolean adminTestDelivery;
	
	/** EQL code representing the target group. */
	private String eql;

    /** Creates new Target */
    public ComTargetImpl() {
    }

    public ComTargetImpl(int id, String name) {
        setId(id);
        setTargetName(name);
    }

    @Override
	public void setTargetSQL(String sql) {
    	
    	// TODO: The following code block ("if") is for debugging (see AGNEMM-787)
    	if (targetSQL != null) {
    		// Check only, when new SQL statement is longer than the old one 
    		if (sql.length() > targetSQL.length()) {
    			String tmp = targetSQL;
    			int parCount = 0;
    			
    			// Add "(" and ")" to the old statement until its length is greater or equals to the new one
    			while (tmp.length() < sql.length()) {
    				tmp = "(" + tmp + ")";
    				parCount++;
    			}
    			
    			// When both statement are equal by content then we got a problem!
    			if (tmp.equals( sql)) {
    				try {
    					throw new RuntimeException( "POSSIBLE PROBLEM WITH PARENTHESIS DETECTED - " + parCount + " new parenthesis levels added");
    				} catch (RuntimeException e) {
    					logger.error( "possible error with parenthesis detected", e);
    					logger.error( "target ID: " + id);
    					logger.error( "company ID: " + companyID);
    					logger.error( "target name: " + targetName);
    					logger.error( "old SQL: " + targetSQL);
    					logger.error( "new SQL: " + sql);
    				}
    			}
    		}
    	}
    	
        targetSQL=sql;
    }

    @Override
	public String getTargetSQL() {
    	/*
    	 * Outer parenthesis has been removed here.
    	 * Outer parenthesis is already added in TargetRepresentationImpl.generateSQL().
    	 * Adding parenthesis here may cause problem when loading and saving Target
    	 * without generating SQL from TargetRepresentation.
    	 * 
    	 * See JIRA-787 for more informations.
    	 */
        return targetSQL; 
    }

	@Override
	public boolean isAdminTestDelivery() {
		return this.adminTestDelivery;
	}

	@Override
	public void setAdminTestDelivery( boolean adminTestDelivery) {
		this.adminTestDelivery = adminTestDelivery;
	}

	@Override
	public void setEQL(String eql) {
		this.eql = eql;
	}
	
	@Override
	public String getEQL() {
		return this.eql;
	}

}
