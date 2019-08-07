/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.List;

import javax.servlet.jsp.JspException;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetRepresentation;
import org.agnitas.target.impl.TargetNodeDate;
import org.agnitas.target.impl.TargetNodeNumeric;
import org.agnitas.target.impl.TargetNodeString;
import org.agnitas.target.impl.TargetRepresentationImpl;
import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.agnitas.beans.ComTarget;

import bsh.Interpreter;

/**
 * Implementation of {@link ComTarget} interface.
 */
public class ComTargetImpl extends TargetLightImpl implements ComTarget {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComTargetImpl.class);

	/** Fragment of SQL WHERE clause built from the target group. */
    protected String targetSQL;

    /** Serialized form of target group. (Will be replaced by EQL) */
    protected TargetRepresentation targetStructure;

    /** Flag, if target group is used for admin and test delivery. */
	private boolean adminTestDelivery;
	
	private boolean simpleStructure;
	
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

    /** Getter for property targetStructure.
     * @return Value of property targetStructure.
     */
    @Override
	public TargetRepresentation getTargetStructure() {
        return this.targetStructure;
    }

    /** Setter for property targetStructure.
     * @param targetStructure New value of property targetStructure.
     */
    @Override
	public void setTargetStructure(TargetRepresentation targetStructure) {
        if (targetStructure.getClass().getName().equals("com.agnitas.query.TargetRepresentation")) {
            TargetRepresentationImpl newrep = new TargetRepresentationImpl();
            List<TargetNode> nodes = targetStructure.getAllNodes();
            
            for (int n = 0; n < nodes.size (); ++n) {
                TargetNode  tmp = nodes.get(n);
                String      prim = tmp.getPrimaryField();

                if (prim != null) {
                    tmp.setPrimaryField(prim.toLowerCase());
                }
                
                String      tname = tmp.getClass().getName();
                TargetNode  newtarget = null;
                
                if (tname.equals ("com.agnitas.query.TargetNodeNumeric")) {
                    newtarget = new TargetNodeNumeric ();
                } else if (tname.equals ("com.agnitas.query.TargetNodeString")) {
                    newtarget = new TargetNodeString ();
                } else if (tname.equals ("com.agnitas.query.TargetNodeDate")) {
                    newtarget = TargetNodeDate.withDefaultDateFormat(ConfigService.isOracleDB());
                }
                if (newtarget != null) {
                    newtarget.setOpenBracketBefore (tmp.isOpenBracketBefore ());
                    newtarget.setCloseBracketAfter (tmp.isCloseBracketAfter ());
                    newtarget.setChainOperator (tmp.getChainOperator ());
                    newtarget.setPrimaryOperator (tmp.getPrimaryOperator ());
                    newtarget.setPrimaryField (tmp.getPrimaryField ());
                    newtarget.setPrimaryFieldType (tmp.getPrimaryFieldType ());
                    newtarget.setPrimaryValue (tmp.getPrimaryValue ());
                    
                    tmp = newtarget;
                }
                newrep.addNode (tmp);
            }
            targetStructure = newrep;
        }
        this.targetStructure = targetStructure;
    }

    @Override
	public boolean isCustomerInGroup(Interpreter interpreter) {
        try {
            return (Boolean) interpreter.eval(String.format("return (%s)", targetStructure.generateBsh()));
        } catch (Exception e) {
            logger.error("isCustomerInGroup: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
	public boolean isCustomerInGroup(int customerID, ApplicationContext con) throws JspException {
		Interpreter aBsh = AgnUtils.getBshInterpreter(companyID, customerID, con);
		if (aBsh == null) {
			return false;
		}

        return this.isCustomerInGroup(aBsh);
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
	
	@Override
	public void setSimpleStructured(boolean simpleStructured) {
		this.simpleStructure = simpleStructured;
	}

	@Override
	public boolean isSimpleStructured() {
		return this.simpleStructure;
	}
}
