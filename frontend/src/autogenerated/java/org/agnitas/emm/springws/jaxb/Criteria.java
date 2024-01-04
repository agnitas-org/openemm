/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.emm.springws.jaxb;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Criteria complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Criteria"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="equals" type="{http://agnitas.org/ws/schemas}Equals" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="matchAll" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Criteria", propOrder = {
    "equals",
    "matchAll"
})
@SuppressWarnings("all")
public class Criteria {

    protected List<Equals> equals;
    protected boolean matchAll;

    /**
     * Gets the value of the equals property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the equals property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEquals().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Equals }
     * 
     * 
     */
    public List<Equals> getEquals() {
        if (equals == null) {
            equals = new ArrayList<Equals>();
        }
        return this.equals;
    }

    /**
     * Gets the value of the matchAll property.
     * 
     */
    public boolean isMatchAll() {
        return matchAll;
    }

    /**
     * Sets the value of the matchAll property.
     * 
     */
    public void setMatchAll(boolean value) {
        this.matchAll = value;
    }

}
