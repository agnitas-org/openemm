/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.stat.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.SafeString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.agnitas.beans.ComTarget;
import com.agnitas.dao.ComTargetDao;

public class DomainStatImpl extends BaseDaoImpl implements org.agnitas.stat.DomainStat {
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(DomainStatImpl.class);
    
    /** Serial version UID. */
    private static final long serialVersionUID = 4471064211444932400L;
    
	protected int listID;
	protected int companyID;
	protected int targetID;
	protected int total;
	protected int rest;
	protected int lines;
	protected int sum;
	protected int max;
	protected String csvfile = ""; // String for csv file download
	protected List<String> domains;
	protected List<Integer> subscribers;
    
    /** Holds value of property maxDomains. */
    protected int maxDomains = 20;
    
    /** CONSTRUCTOR */
    public DomainStatImpl() {
        
    }
    
    @Override
	public boolean getStatFromDB(ComTargetDao targetDao, DataSource dataSource, HttpServletRequest request) {
        boolean returnCode=true;
        String targetSQL = "";
        
        lines       = 0;
        sum         = 0;
        domains     = new LinkedList<>();
        subscribers = new LinkedList<>();
        
        Locale locale = AgnUtils.getLocale(request);
                
        csvfile += SafeString.getLocaleString("statistic.domains", locale) + "\n";
        csvfile += "\n";
        
        // 1. get target group SQL:
        if(targetID!=0) {
            ComTarget aTarget=targetDao.getTarget(this.targetID, this.companyID);
            if(aTarget.getId()!=0) {
                if(listID != 0) {
                    targetSQL = " AND (" + aTarget.getTargetSQL() + ")";
                } else {
                    targetSQL = " WHERE (" + aTarget.getTargetSQL() + ")";
                }
                csvfile += SafeString.getLocaleString("target.Target", locale) + ":;" + aTarget.getTargetName() + "\n";
                if (logger.isInfoEnabled()) {
					logger.info("getStatFromDB: target loaded " + targetID);
				}
            } else {
                csvfile += SafeString.getLocaleString("target.Target", locale) + ":;" + SafeString.getLocaleString("statistic.all_subscribers", locale) + "\n";
                if (logger.isInfoEnabled()) {
					logger.info("getStatFromDB: could not load target " + targetID);
				}
            }
        }
        
        // 2. how many total subscribers ?
        String sqlCount = "SELECT COUNT(cust.customer_id) "
                + "FROM customer_" + companyID + "_tbl cust, customer_" + companyID + "_binding_tbl bind";
        
        if(listID != 0) {
            
            sqlCount += " WHERE bind.mailinglist_id = " + listID;
            sqlCount += " AND cust.customer_id = bind.customer_id ";
            sqlCount += " AND bind.user_status =1";
            sqlCount += targetSQL;
        } else {
            if(targetID==0) {
                sqlCount += " WHERE cust.customer_id = bind.customer_id ";
                sqlCount += " AND bind.user_status =1";
                csvfile += SafeString.getLocaleString("Mailinglist", locale) + "\n";
            } else {
                sqlCount += targetSQL;
                sqlCount += " AND cust.customer_id = bind.customer_id ";
                sqlCount += " AND bind.user_status =1";
                csvfile += SafeString.getLocaleString("Mailinglist", locale) + ":;" + SafeString.getLocaleString(CommonKeys.ALL_MAILINGLISTS, locale) + "\n";
            }
        }
       
        try {
            total = selectInt(logger, sqlCount);
        } catch(Exception e) {
            logger.error("getStatFromDB: "+e);
            logger.error("SQL: "+sqlCount);
        }
        
        // 3. get the top domains:
        String sqlStmt;
        if(listID != 0) {
            sqlStmt = "SELECT COUNT(cust.customer_id) AS tmpcount, SUBSTR(email, INSTR(email, '@')) AS tmpsub from customer_" + companyID + "_tbl cust , customer_" + companyID + "_binding_tbl bind WHERE cust.customer_id = bind.customer_id AND bind.user_status =1 AND bind.MAILINGLIST_ID =" + listID + targetSQL + " group by tmpsub order by tmpcount desc LIMIT "+this.maxDomains;
        } else {
            if(targetID==0) {
                sqlStmt = "SELECT COUNT(cust.customer_id) AS tmpcount, SUBSTR(cust.email, INSTR(cust.email, '@')) AS tmpsub from customer_" + companyID + "_tbl cust , customer_" + companyID + "_binding_tbl bind WHERE cust.customer_id = bind.customer_id AND bind.user_status =1 group by tmpsub order by tmpcount desc LIMIT "+this.maxDomains;
            } else {
                sqlStmt = "SELECT COUNT(cust.customer_id) AS tmpcount, SUBSTR(cust.email, INSTR(cust.email, '@')) AS tmpsub from customer_" + companyID + "_tbl cust , customer_" + companyID + "_binding_tbl bind " + targetSQL + " AND cust.customer_id = bind.customer_id AND bind.user_status =1 group by tmpsub order by tmpcount desc LIMIT "+this.maxDomains;
            }
        }
        
        csvfile += "\n";
        csvfile += SafeString.getLocaleString("statistic.domain", locale) + "\n";
        try {
            select(logger, sqlStmt, new Object[] {}, new RowCallbackHandler() {
                @Override
				public void processRow(ResultSet rs) throws SQLException {
                    lines++;
                    domains.add(rs.getString(2));
                    subscribers.add(rs.getInt(1));
                    sum += rs.getInt(1);
                    csvfile += rs.getString(2) + ";" + rs.getString(1) + "\n";
                }
            }
            );
        } catch (Exception e) {
            logger.error("getStatFromDB(query): "+e);
            logger.error("SQL: "+sqlStmt);
        }

        rest = total - sum;
        
        csvfile += "\n";
        csvfile += SafeString.getLocaleString("statistic.Other", locale) + ":;" + rest + "\n";
        csvfile += "\n";
        csvfile += SafeString.getLocaleString("report.total", locale) + ":;" + total + "\n";
        
        return returnCode;
    }
    
    // SETTER:
    
    @Override
	public void setCompanyID(int id) {
        companyID=id;
    }
    
    @Override
	public void setTargetID(int id) {
        targetID=id;
    }
    
    @Override
	public void setListID(int id) {
        listID=id;
    }
    
    @Override
	public void setTotal(int total) {
        this.total = total;
    }
    
    @Override
	public void setRest(int rest) {
        this.rest = rest;
    }
    
    @Override
	public void setLines(int lines) {
        this.lines = lines;
    }
    
    @Override
	public void setDomains(List<String> domains) {
        this.domains = domains;
    }
    
    @Override
	public void setSubscribers(List<Integer> subscribers) {
        this.subscribers = subscribers;
    }
    
    @Override
	public void setCsvfile(String file) {
        this.csvfile = file;
    }
    
    @Override
	public void setMaxDomains(int maxDomains) {
        this.maxDomains = maxDomains;
    }
    
    // GETTER:
    
    @Override
	public int getListID() {
        return listID;
    }
    
    @Override
	public int getTargetID() {
        return targetID;
    }
    
    @Override
	public int getCompanyID() {
        return companyID;
    }
    
    @Override
	public int getTotal() {
        return total;
    }
    
    @Override
	public int getRest() {
        return rest;
    }
    
    @Override
	public int getLines() {
        return lines;
    }
    
    @Override
	public List<String> getDomains() {
        return domains;
    }
    
    @Override
	public List<Integer> getSubscribers() {
        return subscribers;
    }
    
    @Override
	public int getMaxDomains() {
        return this.maxDomains;
    }
    
    @Override
	public String getCsvfile() {
        return this.csvfile;
    }
}
