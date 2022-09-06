/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.dto;

import java.util.Arrays;
import java.util.List;

public class CompanySettingsDto {

    private String technicalContacts;
    private boolean hasMailTracking;
    private int recipientExpireDays;
    private int statisticsExpireDays;
    private boolean hasActivatedAccessAuthorization;
    private boolean hasExtendedSalutation;
    private int executiveAdministrator;
    private boolean hasDataExportNotify;
    private String language;
    private String timeZone;
    private boolean hasForceSending;
    private boolean hasTrackingVeto;
    private int sector;
    private int business;
    private int maxAdminMails = -1;
    private boolean hasTwoFactorAuthentication;
    private int recipientAnonymization;
    private int recipientCleanupTracking;
    private int recipientDeletion;
    private String loginlockSettingsName;
    private String passwordPolicyName;
    private int passwordExpireDays;
    private int hostauthCookieExpireDays;
    private boolean sendPasswordChangedNotification;
    private String defaultLinkExtension;
    private int linkcheckerLinktimeout;
    private int linkcheckerThreadcount;
    private int mailingUndoLimit;
    private boolean prefillCheckboxSendDuplicateCheck;
    private String fullviewFormName;
    private boolean deleteSuccessfullyImportedFiles;
	private String importAlwaysInformEmail;
	private String exportAlwaysInformEmail;
	private String bccEmail;
	private boolean anonymizeAllRecipients;
	private boolean cleanRecipientsWithoutBinding;
	private boolean recipientEmailInUseWarning;
	private boolean allowEmailWithWhitespace;
	private boolean allowEmptyEmail;
	private int expireStatistics;
	private int expireOnePixel;
	private int expireSuccess;
	private int expireRecipient;
	private int expireBounce;
	private int expireUpload;
	private boolean writeCustomerOpenOrClickField;
	private boolean trackingVetoAllowTransactionTracking;
	private List<Integer> optionsMaxAdminMails = Arrays.asList(25, 50, 100);
    private int defaultCompanyLinkTrackingMode;

	public boolean isHasMailTracking() {
        return hasMailTracking;
    }

    public void setHasMailTracking(boolean mailTracking) {
        this.hasMailTracking = mailTracking;
    }

    public int getRecipientExpireDays() {
        return recipientExpireDays;
    }

    public void setRecipientExpireDays(int recipientExpireDays) {
        this.recipientExpireDays = recipientExpireDays;
    }

    public int getStatisticsExpireDays() {
        return statisticsExpireDays;
    }

    public void setStatisticsExpireDays(int statisticsExpireDays) {
        this.statisticsExpireDays = statisticsExpireDays;
    }

    public boolean isHasActivatedAccessAuthorization() {
        return hasActivatedAccessAuthorization;
    }

    public void setHasActivatedAccessAuthorization(boolean hasActivatedAccessAuthorization) {
        this.hasActivatedAccessAuthorization = hasActivatedAccessAuthorization;
    }

    public boolean isHasExtendedSalutation() {
        return hasExtendedSalutation;
    }

    public void setHasExtendedSalutation(boolean hasExtendedSalutation) {
        this.hasExtendedSalutation = hasExtendedSalutation;
    }

    public int getExecutiveAdministrator() {
        return executiveAdministrator;
    }

    public void setExecutiveAdministrator(int executiveAdministrator) {
        this.executiveAdministrator = executiveAdministrator;
    }

    public String getTechnicalContacts() {
        return technicalContacts;
    }

    public void setTechnicalContacts(String technicalContacts) {
        this.technicalContacts = technicalContacts;
    }

    public boolean isHasDataExportNotify() {
        return hasDataExportNotify;
    }

    public void setHasDataExportNotify(boolean hasDataExportNotify) {
        this.hasDataExportNotify = hasDataExportNotify;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean isHasForceSending() {
        return hasForceSending;
    }

    public void setHasForceSending(boolean hasForceSending) {
        this.hasForceSending = hasForceSending;
    }

	public boolean isHasTrackingVeto() {
        return hasTrackingVeto;
    }

    public void setHasTrackingVeto(boolean hasTrackingVeto) {
        this.hasTrackingVeto = hasTrackingVeto;
    }

    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }

    public int getBusiness() {
        return business;
    }

    public void setBusiness(int business) {
        this.business = business;
    }

    public boolean isHasTwoFactorAuthentication() {
        return hasTwoFactorAuthentication;
    }

    public void setHasTwoFactorAuthentication(boolean hasTwoFactorAuthentication) {
        this.hasTwoFactorAuthentication = hasTwoFactorAuthentication;
    }

	public int getMaxAdminMails() {
		return maxAdminMails;
	}

	public void setMaxAdminMails(int maxAdminMails) {
		this.maxAdminMails = maxAdminMails;
	}

	public final String getLoginlockSettingsName() {
		return loginlockSettingsName;
	}

	public final void setLoginlockSettingsName(String loginlockSettingsName) {
		this.loginlockSettingsName = loginlockSettingsName;
	}
	
	public final String getPasswordPolicyName() {
		return this.passwordPolicyName;
	}
	
	public final void setPasswordPolicyName(final String name) {
		this.passwordPolicyName = name;
	}

	public final int getPasswordExpireDays() {
		return passwordExpireDays;
	}

	public final void setPasswordExpireDays(final int days) {
		this.passwordExpireDays = days;
	}

	public final int getHostauthCookieExpireDays() {
		return hostauthCookieExpireDays;
	}

	public final void setHostauthCookieExpireDays(final int hostauthCookieExpireDays) {
		this.hostauthCookieExpireDays = hostauthCookieExpireDays;
	}

	public int getRecipientAnonymization() {
		return recipientAnonymization;
	}

	public void setRecipientAnonymization(int recipientAnonymization) {
		this.recipientAnonymization = recipientAnonymization;
	}

	public int getRecipientCleanupTracking() {
		return recipientCleanupTracking;
	}

	public void setRecipientCleanupTracking(int recipientCleanupTracking) {
		this.recipientCleanupTracking = recipientCleanupTracking;
	}

	public int getRecipientDeletion() {
		return recipientDeletion;
	}

	public void setRecipientDeletion(int recipientDeletion) {
		this.recipientDeletion = recipientDeletion;
	}

    public boolean isSendPasswordChangedNotification() {
		return sendPasswordChangedNotification;
	}

	public void setSendPasswordChangedNotification(boolean sendPasswordChangedNotification) {
		this.sendPasswordChangedNotification = sendPasswordChangedNotification;
	}

	public String getDefaultLinkExtension() {
		return defaultLinkExtension;
	}

	public void setDefaultLinkExtension(String defaultLinkExtension) {
		this.defaultLinkExtension = defaultLinkExtension;
	}

	public int getLinkcheckerLinktimeout() {
		return linkcheckerLinktimeout;
	}

	public void setLinkcheckerLinktimeout(int linkcheckerLinktimeout) {
		this.linkcheckerLinktimeout = linkcheckerLinktimeout;
	}

	public int getLinkcheckerThreadcount() {
		return linkcheckerThreadcount;
	}

	public void setLinkcheckerThreadcount(int linkcheckerThreadcount) {
		this.linkcheckerThreadcount = linkcheckerThreadcount;
	}

	public int getMailingUndoLimit() {
		return mailingUndoLimit;
	}

	public void setMailingUndoLimit(int mailingUndoLimit) {
		this.mailingUndoLimit = mailingUndoLimit;
	}

	public boolean isPrefillCheckboxSendDuplicateCheck() {
		return prefillCheckboxSendDuplicateCheck;
	}

	public void setPrefillCheckboxSendDuplicateCheck(boolean prefillCheckboxSendDuplicateCheck) {
		this.prefillCheckboxSendDuplicateCheck = prefillCheckboxSendDuplicateCheck;
	}

	public String getFullviewFormName() {
		return fullviewFormName;
	}

	public void setFullviewFormName(String fullviewFormName) {
		this.fullviewFormName = fullviewFormName;
	}

	public boolean isDeleteSuccessfullyImportedFiles() {
		return deleteSuccessfullyImportedFiles;
	}

	public void setDeleteSuccessfullyImportedFiles(boolean deleteSuccessfullyImportedFiles) {
		this.deleteSuccessfullyImportedFiles = deleteSuccessfullyImportedFiles;
	}

	public String getImportAlwaysInformEmail() {
		return importAlwaysInformEmail;
	}

	public void setImportAlwaysInformEmail(String importAlwaysInformEmail) {
		this.importAlwaysInformEmail = importAlwaysInformEmail;
	}

	public String getExportAlwaysInformEmail() {
		return exportAlwaysInformEmail;
	}

	public void setExportAlwaysInformEmail(String exportAlwaysInformEmail) {
		this.exportAlwaysInformEmail = exportAlwaysInformEmail;
	}
	
	public String getBccEmail() {
		return bccEmail;
	}

	public void setBccEmail(String bccEmail) {
		this.bccEmail = bccEmail;
	}

	public boolean isAnonymizeAllRecipients() {
		return anonymizeAllRecipients;
	}

	public void setAnonymizeAllRecipients(boolean anonymizeAllRecipients) {
		this.anonymizeAllRecipients = anonymizeAllRecipients;
	}

	public boolean isCleanRecipientsWithoutBinding() {
		return cleanRecipientsWithoutBinding;
	}

	public void setCleanRecipientsWithoutBinding(boolean cleanRecipientsWithoutBinding) {
		this.cleanRecipientsWithoutBinding = cleanRecipientsWithoutBinding;
	}

	public boolean isRecipientEmailInUseWarning() {
		return recipientEmailInUseWarning;
	}

	public void setRecipientEmailInUseWarning(boolean recipientEmailInUseWarning) {
		this.recipientEmailInUseWarning = recipientEmailInUseWarning;
	}

	public boolean isAllowEmailWithWhitespace() {
		return allowEmailWithWhitespace;
	}

	public void setAllowEmailWithWhitespace(boolean allowEmailWithWhitespace) {
		this.allowEmailWithWhitespace = allowEmailWithWhitespace;
	}

	public boolean isAllowEmptyEmail() {
		return allowEmptyEmail;
	}

	public void setAllowEmptyEmail(boolean allowEmptyEmail) {
		this.allowEmptyEmail = allowEmptyEmail;
	}

	public int getExpireStatistics() {
		return expireStatistics;
	}

	public void setExpireStatistics(int expireStatistics) {
		this.expireStatistics = expireStatistics;
	}

	public int getExpireOnePixel() {
		return expireOnePixel;
	}

	public void setExpireOnePixel(int expireOnePixel) {
		this.expireOnePixel = expireOnePixel;
	}

	public int getExpireSuccess() {
		return expireSuccess;
	}

	public void setExpireSuccess(int expireSuccess) {
		this.expireSuccess = expireSuccess;
	}

	public int getExpireRecipient() {
		return expireRecipient;
	}

	public void setExpireRecipient(int expireRecipient) {
		this.expireRecipient = expireRecipient;
	}

	public int getExpireBounce() {
		return expireBounce;
	}

	public void setExpireBounce(int expireBounce) {
		this.expireBounce = expireBounce;
	}

	public int getExpireUpload() {
		return expireUpload;
	}

	public void setExpireUpload(int expireUpload) {
		this.expireUpload = expireUpload;
	}

	public boolean isWriteCustomerOpenOrClickField() {
		return writeCustomerOpenOrClickField;
	}

	public void setWriteCustomerOpenOrClickField(boolean writeCustomerOpenOrClickField) {
		this.writeCustomerOpenOrClickField = writeCustomerOpenOrClickField;
	}

	public boolean isTrackingVetoAllowTransactionTracking() {
		return trackingVetoAllowTransactionTracking;
	}

	public void setTrackingVetoAllowTransactionTracking(boolean trackingVetoAllowTransactionTracking) {
		this.trackingVetoAllowTransactionTracking = trackingVetoAllowTransactionTracking;
	}

	public List<Integer> getOptionsMaxAdminMails() {
		return optionsMaxAdminMails;
	}

	public int getDefaultCompanyLinkTrackingMode() {
		return defaultCompanyLinkTrackingMode;
	}

	public void setDefaultCompanyLinkTrackingMode(int defaultCompanyLinkTrackingMode) {
		this.defaultCompanyLinkTrackingMode = defaultCompanyLinkTrackingMode;
	}
	
}
