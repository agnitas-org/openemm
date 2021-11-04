/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.stat;

import java.io.Serializable;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.dao.ComTargetDao;

public interface DomainStat extends Serializable {
    
     /**
     * Getter for property companyID.
     * 
     * @return Value of property companyID.
     */
    int getCompanyID();
    
    /**
     * Getter for property csvfile.
     * 
     * @return Value of property csvfile.
     */
    String getCsvfile();

     /**
     * Getter for property domains.
     * 
     * @return Value of property domains.
     */
    List<String> getDomains();

     /**
     * Getter for property lines.
     * 
     * @return Value of property lines.
     */
    int getLines();

     /**
     * Getter for property listID.
     * 
     * @return Value of property listID.
     */
    int getListID();

     /**
     * Getter for property maxDomains.
     * 
     * @return Value of property maxDomains.
     */
    int getMaxDomains();

     /**
     * Getter for property rest.
     * 
     * @return Value of property rest.
     */
    int getRest();

     /**
     * Getter for property stat from database.
     * 
     * @return Value of property stat from database.
     */
    boolean getStatFromDB(ComTargetDao targetDao, DataSource dataSource, HttpServletRequest request);

     /**
     * Getter for property subscribers.
     * 
     * @return Value of property subscribers.
     */
    List<Integer> getSubscribers();

     /**
     * Getter for property targetID.
     * 
     * @return Value of property targetID.
     */
    int getTargetID();

     /**
     * Getter for property total.
     * 
     * @return Value of property total.
     */
    int getTotal();
    
    /**
     * Setter for property companyID.
     * 
     * @param id New value of property companyID.
     */
    void setCompanyID(@VelocityCheck int id);

    /**
     * Setter for property csvfile.
     * 
     * @param file New value of property csvfile.
     */
    void setCsvfile(String file);

    /**
     * Setter for property domains.
     * 
     * @param domains New value of property domains.
     */
    void setDomains(List<String> domains);

    /**
     * Setter for property lines.
     * 
     * @param lines New value of property lines.
     */
    void setLines(int lines);

    /**
     * Setter for property listID.
     * 
     * @param id New value of property listID.
     */
    void setListID(int id);

    /**
     * Setter for property maxDomains.
     * 
     * @param maxDomains New value of property maxDomains.
     */
    void setMaxDomains(int maxDomains);

    /**
     * Setter for property rest.
     * 
     * @param rest New value of property rest.
     */
    void setRest(int rest);

    /**
     * Setter for property subscribers.
     * 
     * @param subscribers New value of property subscribers.
     */
    void setSubscribers(List<Integer> subscribers);

    /**
     * Setter for property targetID.
     * 
     * @param id New value of property targetID.
     */
    void setTargetID(int id);

    /**
     * Setter for property total.
     * 
     * @param total New value of property total.
     */
    void setTotal(int total);
    
}
