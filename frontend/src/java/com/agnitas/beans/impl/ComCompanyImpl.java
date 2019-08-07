/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Date;

import org.agnitas.beans.impl.CompanyImpl;

import com.agnitas.beans.ComCompany;

public class ComCompanyImpl extends CompanyImpl implements ComCompany {
	private static final long serialVersionUID = -5817004759096244296L;

	private int expireStat;
	private int statAdmin;
	private int expireOnePixel;
	private int expireSuccess;
	private int expireCookie;
	private int expireRecipient;
	private int expireUpload;
	private int salutationExtended;
	private String secretKey;
	private String localeTimezone;
	private String localeLanguage;
	private int enabledUIDVersion;
	private int maxAdminMails;
	private int maxFields;
	private int exportNotifyAdmin;
	private Date creationDate;
	private boolean autoMailingReportActivated;
	private boolean forceSending = false;
	private int sector;
	private int business;
	private int parentCompanyId;
	private int expireBounce;
	private String contactTech;

	@Override
	public int getExpireStat() {
		return expireStat;
	}

	@Override
	public void setExpireStat(int expireStat) {
		this.expireStat = expireStat;
	}

	@Override
	public int getExpireOnePixel() {
		return expireOnePixel;
	}

	@Override
	public void setExpireOnePixel(int expireOnePixel) {
		this.expireOnePixel = expireOnePixel;
	}

	@Override
	public int getExpireUpload() {
		return expireUpload;
	}

	@Override
	public void setExpireUpload(int expireUpload) {
		this.expireUpload = expireUpload;
	}

	@Override
	public int getStatAdmin() {
		return statAdmin;
	}

	@Override
	public void setStatAdmin(int statAdmin) {
		this.statAdmin = statAdmin;
	}

	@Override
	public int getExpireCookie() {
		return expireCookie;
	}

	@Override
	public void setExpireCookie(int expireCookie) {
		this.expireCookie = expireCookie;
	}

	@Override
	public int getExpireRecipient() {
		return expireRecipient;
	}

	@Override
	public void setExpireRecipient(int expireRecipient) {
		this.expireRecipient = expireRecipient;
	}

	@Override
	public String getSecretKey() {
		return secretKey;
	}

	@Override
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public boolean isAutoMailingReportSendActivated() {
		return autoMailingReportActivated;
	}

	@Override
	public void setAutoMailingReportSendActivated(boolean autoMailingReportActivated) {
		this.autoMailingReportActivated = autoMailingReportActivated;
	}

	@Override
	public int getBusiness() {
		return business;
	}

	@Override
	public void setBusiness(int business) {
		this.business = business;
	}

	@Override
	public int getSector() {
		return sector;
	}

	@Override
	public void setSector(int sector) {
		this.sector = sector;
	}

	@Override
	public int getSalutationExtended() {
		return salutationExtended;
	}

	@Override
	public void setSalutationExtended(int salutationExtended) {
		this.salutationExtended = salutationExtended;
	}

	@Override
	public int getEnabledUIDVersion() {
		return enabledUIDVersion;
	}

	@Override
	public void setEnabledUIDVersion(int uidVersion) {
		this.enabledUIDVersion = uidVersion;
	}

	@Override
	public int getMaxAdminMails() {
		return maxAdminMails;
	}

	@Override
	public void setMaxAdminMails(int maxAdminMails) {
		this.maxAdminMails = maxAdminMails;
	}

	@Override
	public int getExportNotifyAdmin() {
		return exportNotifyAdmin;
	}

	@Override
	public void setExportNotifyAdmin(int exportNotifyAdmin) {
		this.exportNotifyAdmin = exportNotifyAdmin;
	}

	@Override
	public int getParentCompanyId() {
		return parentCompanyId;
	}

	@Override
	public void setParentCompanyId(int companyId) {
		this.parentCompanyId = companyId;
	}

	@Override
	public int getExpireSuccess() {
		return expireSuccess;
	}

	@Override
	public void setExpireSuccess(int maximumAgeInDays) {
		expireSuccess = maximumAgeInDays;
	}

	@Override
	public int getExpireBounce() {
		return expireBounce;
	}

	@Override
	public void setExpireBounce(int expireBounce) {
		this.expireBounce = expireBounce;
	}

	@Override
	public int getMaxFields() {
		return maxFields;
	}

	@Override
	public void setMaxFields(int maxFields) {
		this.maxFields = maxFields;
	}

	@Override
	public String getLocaleTimezone() {
		return localeTimezone;
	}

	@Override
	public void setLocaleTimezone(String localeTimezone) {
		this.localeTimezone = localeTimezone;
	}
	
	@Override
	public String getLocaleLanguage() {
		return localeLanguage;
	}

	@Override
	public void setLocaleLanguage(String localeLanguage) {
		this.localeLanguage = localeLanguage;
	}

	@Override
	public boolean isForceSending() {
		return forceSending;
	}

	@Override
	public void setForceSending(boolean forceSending) {
		this.forceSending = forceSending;
	}

	@Override
	public String getContactTech() {
		return contactTech;
	}

	@Override
	public void setContactTech(String contactTech) {
		this.contactTech = contactTech;
	}
}
