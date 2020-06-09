/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.List;

public class TrackableLinkModel {

    private Integer companyID;
    private Integer customerID;
    private Integer id;
    private Integer mailingID;
    private Integer actionID;
    private String fullUrl;
    private String shortname;
    private Integer usage;
    private Boolean adminLink;
    private Boolean deleted;
    private Boolean extendByMailingExtensions;
    private String originalUrl;
    private Integer deepTracking;
    private String deepTrackingUID;
    private String deepTrackingSession;
    @SuppressWarnings("unused")
	private Integer relevance;
    private String altText;

    private List<LinkProperty> linkProperties;

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMailingID() {
        return mailingID;
    }

    public void setMailingID(Integer mailingID) {
        this.mailingID = mailingID;
    }

    public Integer getActionID() {
        return actionID;
    }

    public void setActionID(Integer actionID) {
        this.actionID = actionID;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public Integer getUsage() {
        return usage;
    }

    public void setUsage(Integer usage) {
        this.usage = usage;
    }

    public Boolean getAdminLink() {
        return adminLink;
    }

    public void setAdminLink(Boolean adminLink) {
        this.adminLink = adminLink;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getExtendByMailingExtensions() {
        return extendByMailingExtensions;
    }

    public void setExtendByMailingExtensions(Boolean extendByMailingExtensions) {
        this.extendByMailingExtensions = extendByMailingExtensions;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public Integer getDeepTracking() {
        return deepTracking;
    }

    public void setDeepTracking(Integer deepTracking) {
        this.deepTracking = deepTracking;
    }

    public String getDeepTrackingUID() {
        return deepTrackingUID;
    }

    public void setDeepTrackingUID(String deepTrackingUID) {
        this.deepTrackingUID = deepTrackingUID;
    }

    public String getDeepTrackingSession() {
        return deepTrackingSession;
    }

    public void setDeepTrackingSession(String deepTrackingSession) {
        this.deepTrackingSession = deepTrackingSession;
    }

    @Deprecated
    public Integer getRelevance() {
        return 0;
    }

    @Deprecated
    public void setRelevance(Integer relevance) {
        this.relevance = 0;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public List<LinkProperty> getLinkProperties() {
        return linkProperties;
    }

    public void setLinkProperties(List<LinkProperty> linkProperties) {
        this.linkProperties = linkProperties;
    }
}
