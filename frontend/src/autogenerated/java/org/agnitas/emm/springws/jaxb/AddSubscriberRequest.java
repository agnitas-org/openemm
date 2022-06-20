/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.emm.springws.jaxb;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="doubleCheck" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="keyColumn" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="overwrite" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="parameters" type="{http://agnitas.org/ws/schemas}Map"/&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "AddSubscriberRequest")
@SuppressWarnings("all")
public class AddSubscriberRequest {

    protected boolean doubleCheck;
    @XmlElement(required = true)
    protected String keyColumn;
    protected boolean overwrite;
    @XmlElement(required = true)
    protected Map parameters;

    /**
     * Gets the value of the doubleCheck property.
     * 
     */
    public boolean isDoubleCheck() {
        return doubleCheck;
    }

    /**
     * Sets the value of the doubleCheck property.
     * 
     */
    public void setDoubleCheck(boolean value) {
        this.doubleCheck = value;
    }

    /**
     * Gets the value of the keyColumn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyColumn() {
        return keyColumn;
    }

    /**
     * Sets the value of the keyColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyColumn(String value) {
        this.keyColumn = value;
    }

    /**
     * Gets the value of the overwrite property.
     * 
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * Sets the value of the overwrite property.
     * 
     */
    public void setOverwrite(boolean value) {
        this.overwrite = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link Map }
     *     
     */
    public Map getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link Map }
     *     
     */
    public void setParameters(Map value) {
        this.parameters = value;
    }

}
