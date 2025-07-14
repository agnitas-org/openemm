/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.springws.jaxb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


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
 *         &lt;element name="filter" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;all&gt;
 *                   &lt;element name="sentBefore" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;all&gt;
 *                             &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *                             &lt;element name="inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *                           &lt;/all&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="sentAfter" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;all&gt;
 *                             &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *                             &lt;element name="inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *                           &lt;/all&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="mailingStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *                   &lt;element name="mailingStatusList" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="mailingStatus" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/all&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
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
@XmlRootElement(name = "ListMailingsRequest")
@SuppressWarnings("all")
public class ListMailingsRequest {

    protected ListMailingsRequest.Filter filter;

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link ListMailingsRequest.Filter }
     *     
     */
    public ListMailingsRequest.Filter getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListMailingsRequest.Filter }
     *     
     */
    public void setFilter(ListMailingsRequest.Filter value) {
        this.filter = value;
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
     *       &lt;all&gt;
     *         &lt;element name="sentBefore" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;all&gt;
     *                   &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
     *                   &lt;element name="inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
     *                 &lt;/all&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="sentAfter" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;all&gt;
     *                   &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
     *                   &lt;element name="inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
     *                 &lt;/all&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="mailingStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
     *         &lt;element name="mailingStatusList" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="mailingStatus" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
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
    public static class Filter {

        protected ListMailingsRequest.Filter.SentBefore sentBefore;
        protected ListMailingsRequest.Filter.SentAfter sentAfter;
        protected String mailingStatus;
        protected ListMailingsRequest.Filter.MailingStatusList mailingStatusList;

        /**
         * Gets the value of the sentBefore property.
         * 
         * @return
         *     possible object is
         *     {@link ListMailingsRequest.Filter.SentBefore }
         *     
         */
        public ListMailingsRequest.Filter.SentBefore getSentBefore() {
            return sentBefore;
        }

        /**
         * Sets the value of the sentBefore property.
         * 
         * @param value
         *     allowed object is
         *     {@link ListMailingsRequest.Filter.SentBefore }
         *     
         */
        public void setSentBefore(ListMailingsRequest.Filter.SentBefore value) {
            this.sentBefore = value;
        }

        /**
         * Gets the value of the sentAfter property.
         * 
         * @return
         *     possible object is
         *     {@link ListMailingsRequest.Filter.SentAfter }
         *     
         */
        public ListMailingsRequest.Filter.SentAfter getSentAfter() {
            return sentAfter;
        }

        /**
         * Sets the value of the sentAfter property.
         * 
         * @param value
         *     allowed object is
         *     {@link ListMailingsRequest.Filter.SentAfter }
         *     
         */
        public void setSentAfter(ListMailingsRequest.Filter.SentAfter value) {
            this.sentAfter = value;
        }

        /**
         * Gets the value of the mailingStatus property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMailingStatus() {
            return mailingStatus;
        }

        /**
         * Sets the value of the mailingStatus property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMailingStatus(String value) {
            this.mailingStatus = value;
        }

        /**
         * Gets the value of the mailingStatusList property.
         * 
         * @return
         *     possible object is
         *     {@link ListMailingsRequest.Filter.MailingStatusList }
         *     
         */
        public ListMailingsRequest.Filter.MailingStatusList getMailingStatusList() {
            return mailingStatusList;
        }

        /**
         * Sets the value of the mailingStatusList property.
         * 
         * @param value
         *     allowed object is
         *     {@link ListMailingsRequest.Filter.MailingStatusList }
         *     
         */
        public void setMailingStatusList(ListMailingsRequest.Filter.MailingStatusList value) {
            this.mailingStatusList = value;
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
         *         &lt;element name="mailingStatus" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
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
            "mailingStatus"
        })
        public static class MailingStatusList {

            @XmlElement(required = true)
            protected List<String> mailingStatus;

            /**
             * Gets the value of the mailingStatus property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the Jakarta XML Binding object.
             * This is why there is not a <CODE>set</CODE> method for the mailingStatus property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getMailingStatus().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             * 
             * 
             */
            public List<String> getMailingStatus() {
                if (mailingStatus == null) {
                    mailingStatus = new ArrayList<String>();
                }
                return this.mailingStatus;
            }

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
         *       &lt;all&gt;
         *         &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
         *         &lt;element name="inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
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
        public static class SentAfter {

            @XmlElement(required = true, type = String.class)
            @XmlJavaTypeAdapter(DateAdapter.class)
            @XmlSchemaType(name = "dateTime")
            protected Date timestamp;
            protected Boolean inclusive;

            /**
             * Gets the value of the timestamp property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public Date getTimestamp() {
                return timestamp;
            }

            /**
             * Sets the value of the timestamp property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTimestamp(Date value) {
                this.timestamp = value;
            }

            /**
             * Gets the value of the inclusive property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean isInclusive() {
                return inclusive;
            }

            /**
             * Sets the value of the inclusive property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setInclusive(Boolean value) {
                this.inclusive = value;
            }

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
         *       &lt;all&gt;
         *         &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
         *         &lt;element name="inclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
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
        public static class SentBefore {

            @XmlElement(required = true, type = String.class)
            @XmlJavaTypeAdapter(DateAdapter.class)
            @XmlSchemaType(name = "dateTime")
            protected Date timestamp;
            protected Boolean inclusive;

            /**
             * Gets the value of the timestamp property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public Date getTimestamp() {
                return timestamp;
            }

            /**
             * Sets the value of the timestamp property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setTimestamp(Date value) {
                this.timestamp = value;
            }

            /**
             * Gets the value of the inclusive property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean isInclusive() {
                return inclusive;
            }

            /**
             * Sets the value of the inclusive property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setInclusive(Boolean value) {
                this.inclusive = value;
            }

        }

    }

}
