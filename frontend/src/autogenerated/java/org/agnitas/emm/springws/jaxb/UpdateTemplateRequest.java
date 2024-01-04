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
 *         &lt;element name="templateID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="shortname" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="mailinglistID" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="targetIDList"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="targetID" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="matchTargetGroups" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="mailingType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="subject" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="senderName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="senderAddress" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="replyToName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="replyToAddress" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="charset" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="linefeed" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="format" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="onePixel" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
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
@XmlRootElement(name = "UpdateTemplateRequest")
@SuppressWarnings("all")
public class UpdateTemplateRequest {

    protected int templateID;
    @XmlElement(required = true)
    protected String shortname;
    protected String description;
    protected int mailinglistID;
    @XmlElement(required = true)
    protected UpdateTemplateRequest.TargetIDList targetIDList;
    @XmlElement(required = true)
    protected String matchTargetGroups;
    @XmlElement(required = true)
    protected String mailingType;
    @XmlElement(required = true)
    protected String subject;
    protected String senderName;
    @XmlElement(required = true)
    protected String senderAddress;
    protected String replyToName;
    @XmlElement(required = true)
    protected String replyToAddress;
    @XmlElement(required = true)
    protected String charset;
    protected int linefeed;
    @XmlElement(required = true)
    protected String format;
    @XmlElement(required = true)
    protected String onePixel;

    /**
     * Gets the value of the templateID property.
     * 
     */
    public int getTemplateID() {
        return templateID;
    }

    /**
     * Sets the value of the templateID property.
     * 
     */
    public void setTemplateID(int value) {
        this.templateID = value;
    }

    /**
     * Gets the value of the shortname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShortname() {
        return shortname;
    }

    /**
     * Sets the value of the shortname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortname(String value) {
        this.shortname = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
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
     * Gets the value of the targetIDList property.
     * 
     * @return
     *     possible object is
     *     {@link UpdateTemplateRequest.TargetIDList }
     *     
     */
    public UpdateTemplateRequest.TargetIDList getTargetIDList() {
        return targetIDList;
    }

    /**
     * Sets the value of the targetIDList property.
     * 
     * @param value
     *     allowed object is
     *     {@link UpdateTemplateRequest.TargetIDList }
     *     
     */
    public void setTargetIDList(UpdateTemplateRequest.TargetIDList value) {
        this.targetIDList = value;
    }

    /**
     * Gets the value of the matchTargetGroups property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMatchTargetGroups() {
        return matchTargetGroups;
    }

    /**
     * Sets the value of the matchTargetGroups property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMatchTargetGroups(String value) {
        this.matchTargetGroups = value;
    }

    /**
     * Gets the value of the mailingType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMailingType() {
        return mailingType;
    }

    /**
     * Sets the value of the mailingType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMailingType(String value) {
        this.mailingType = value;
    }

    /**
     * Gets the value of the subject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the value of the subject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubject(String value) {
        this.subject = value;
    }

    /**
     * Gets the value of the senderName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Sets the value of the senderName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderName(String value) {
        this.senderName = value;
    }

    /**
     * Gets the value of the senderAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSenderAddress() {
        return senderAddress;
    }

    /**
     * Sets the value of the senderAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSenderAddress(String value) {
        this.senderAddress = value;
    }

    /**
     * Gets the value of the replyToName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplyToName() {
        return replyToName;
    }

    /**
     * Sets the value of the replyToName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplyToName(String value) {
        this.replyToName = value;
    }

    /**
     * Gets the value of the replyToAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplyToAddress() {
        return replyToAddress;
    }

    /**
     * Sets the value of the replyToAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplyToAddress(String value) {
        this.replyToAddress = value;
    }

    /**
     * Gets the value of the charset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Sets the value of the charset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharset(String value) {
        this.charset = value;
    }

    /**
     * Gets the value of the linefeed property.
     * 
     */
    public int getLinefeed() {
        return linefeed;
    }

    /**
     * Sets the value of the linefeed property.
     * 
     */
    public void setLinefeed(int value) {
        this.linefeed = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormat(String value) {
        this.format = value;
    }

    /**
     * Gets the value of the onePixel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOnePixel() {
        return onePixel;
    }

    /**
     * Sets the value of the onePixel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOnePixel(String value) {
        this.onePixel = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="targetID" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "targetID"
    })
    public static class TargetIDList {

        @XmlElement(type = Integer.class)
        protected List<Integer> targetID;

        /**
         * Gets the value of the targetID property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the Jakarta XML Binding object.
         * This is why there is not a <CODE>set</CODE> method for the targetID property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getTargetID().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Integer }
         * 
         * 
         */
        public List<Integer> getTargetID() {
            if (targetID == null) {
                targetID = new ArrayList<Integer>();
            }
            return this.targetID;
        }

    }

}
