/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.springws.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="mailingId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="nameOfCopy" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="descriptionOfCopy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "CopyMailingRequest")
@SuppressWarnings("all")
public class CopyMailingRequest {

    protected int mailingId;
    @XmlElement(required = true)
    protected String nameOfCopy;
    protected String descriptionOfCopy;

    /**
     * Gets the value of the mailingId property.
     * 
     */
    public int getMailingId() {
        return mailingId;
    }

    /**
     * Sets the value of the mailingId property.
     * 
     */
    public void setMailingId(int value) {
        this.mailingId = value;
    }

    /**
     * Gets the value of the nameOfCopy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameOfCopy() {
        return nameOfCopy;
    }

    /**
     * Sets the value of the nameOfCopy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameOfCopy(String value) {
        this.nameOfCopy = value;
    }

    /**
     * Gets the value of the descriptionOfCopy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescriptionOfCopy() {
        return descriptionOfCopy;
    }

    /**
     * Sets the value of the descriptionOfCopy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescriptionOfCopy(String value) {
        this.descriptionOfCopy = value;
    }

}
