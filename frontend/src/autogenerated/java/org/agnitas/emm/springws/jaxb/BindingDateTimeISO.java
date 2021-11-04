/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.emm.springws.jaxb;

import java.util.Date;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for BindingDateTimeISO complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BindingDateTimeISO"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://agnitas.org/ws/schemas}Binding"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="changeDate" type="{http://agnitas.org/ws/schemas}dateTimeISO"/&gt;
 *         &lt;element name="creationDate" type="{http://agnitas.org/ws/schemas}dateTimeISO"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BindingDateTimeISO", propOrder = {
    "changeDate",
    "creationDate"
})
@SuppressWarnings("all")
public class BindingDateTimeISO
    extends Binding
{

    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(DateISOAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected Date changeDate;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(DateISOAdapter.class)
    @XmlSchemaType(name = "dateTime")
    protected Date creationDate;

    /**
     * Gets the value of the changeDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getChangeDate() {
        return changeDate;
    }

    /**
     * Sets the value of the changeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeDate(Date value) {
        this.changeDate = value;
    }

    /**
     * Gets the value of the creationDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the value of the creationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreationDate(Date value) {
        this.creationDate = value;
    }

}
