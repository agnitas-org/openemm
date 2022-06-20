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
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Binding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Binding"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="customerID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="mailinglistID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="mediatype" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="userType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="remark" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="exitMailingID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Binding", propOrder = {
    "customerID",
    "mailinglistID",
    "mediatype",
    "status",
    "userType",
    "remark",
    "exitMailingID"
})
@XmlSeeAlso({
    BindingDateTimeDefault.class,
    BindingDateTimeISO.class
})
public abstract class Binding {

    protected int customerID;
    protected int mailinglistID;
    protected int mediatype;
    protected int status;
    @XmlElement(required = true)
    protected String userType;
    @XmlElement(required = true)
    protected String remark;
    protected int exitMailingID;

    /**
     * Gets the value of the customerID property.
     * 
     */
    public int getCustomerID() {
        return customerID;
    }

    /**
     * Sets the value of the customerID property.
     * 
     */
    public void setCustomerID(int value) {
        this.customerID = value;
    }

    /**
     * Gets the value of the mailinglistID property.
     * 
     */
    public int getMailinglistID() {
        return mailinglistID;
    }

    /**
     * Sets the value of the mailinglistID property.
     * 
     */
    public void setMailinglistID(int value) {
        this.mailinglistID = value;
    }

    /**
     * Gets the value of the mediatype property.
     * 
     */
    public int getMediatype() {
        return mediatype;
    }

    /**
     * Sets the value of the mediatype property.
     * 
     */
    public void setMediatype(int value) {
        this.mediatype = value;
    }

    /**
     * Gets the value of the status property.
     * 
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     */
    public void setStatus(int value) {
        this.status = value;
    }

    /**
     * Gets the value of the userType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserType() {
        return userType;
    }

    /**
     * Sets the value of the userType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserType(String value) {
        this.userType = value;
    }

    /**
     * Gets the value of the remark property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemark() {
        return remark;
    }

    /**
     * Sets the value of the remark property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemark(String value) {
        this.remark = value;
    }

    /**
     * Gets the value of the exitMailingID property.
     * 
     */
    public int getExitMailingID() {
        return exitMailingID;
    }

    /**
     * Sets the value of the exitMailingID property.
     * 
     */
    public void setExitMailingID(int value) {
        this.exitMailingID = value;
    }

}
