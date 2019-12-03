/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.emm.springws.jaxb;

import java.util.ArrayList;
import java.util.List;
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
 *       &lt;sequence>
 *         &lt;element name="TrackableLink" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;all>
 *                   &lt;element name="urlID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                   &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="shortname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="altText" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="originalUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/all>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "trackableLink"
})
@XmlRootElement(name = "ListTrackableLinksResponse")
@SuppressWarnings("all")
public class ListTrackableLinksResponse {

    @XmlElement(name = "TrackableLink")
    protected List<ListTrackableLinksResponse.TrackableLink> trackableLink;

    /**
     * Gets the value of the trackableLink property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trackableLink property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrackableLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ListTrackableLinksResponse.TrackableLink }
     * 
     * 
     */
    public List<ListTrackableLinksResponse.TrackableLink> getTrackableLink() {
        if (trackableLink == null) {
            trackableLink = new ArrayList<ListTrackableLinksResponse.TrackableLink>();
        }
        return this.trackableLink;
    }


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
     *         &lt;element name="urlID" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="shortname" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="altText" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="originalUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    public static class TrackableLink {

        protected int urlID;
        @XmlElement(required = true)
        protected String url;
        @XmlElement(required = true)
        protected String shortname;
        @XmlElement(required = true)
        protected String altText;
        @XmlElement(required = true)
        protected String originalUrl;

        /**
         * Gets the value of the urlID property.
         * 
         */
        public int getUrlID() {
            return urlID;
        }

        /**
         * Sets the value of the urlID property.
         * 
         */
        public void setUrlID(int value) {
            this.urlID = value;
        }

        /**
         * Gets the value of the url property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUrl() {
            return url;
        }

        /**
         * Sets the value of the url property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUrl(String value) {
            this.url = value;
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
         * Gets the value of the altText property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getAltText() {
            return altText;
        }

        /**
         * Sets the value of the altText property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setAltText(String value) {
            this.altText = value;
        }

        /**
         * Gets the value of the originalUrl property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOriginalUrl() {
            return originalUrl;
        }

        /**
         * Sets the value of the originalUrl property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOriginalUrl(String value) {
            this.originalUrl = value;
        }

    }

}
