/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.dto;


public class CompanySettingsDto {

    private String technicalContacts;
    private boolean hasMailTracking;
    private int recipientExpireDays;
    private int statisticsExpireDays;
    private boolean hasActivatedAccessAuthorization;
    private boolean hasExtendedSalutation;
    private int executiveAdministrator;
    private boolean hasDataExportNotify;
    private int maxFailedLoginAttempts;
    private int blockIpTime;
    private String language;
    private String timeZone;
    private boolean hasForceSending;
    private boolean hasRecipientsCleanup;
    private boolean hasTrackingVeto;
    private int sector;
    private int business;
    private boolean hasTwoFactorAuthentication;

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

    public int getMaxFailedLoginAttempts() {
        return maxFailedLoginAttempts;
    }

    public void setMaxFailedLoginAttempts(int maxFailedLoginAttempts) {
        this.maxFailedLoginAttempts = maxFailedLoginAttempts;
    }

    public int getBlockIpTime() {
        return blockIpTime;
    }

    public void setBlockIpTime(int blockIpTime) {
        this.blockIpTime = blockIpTime;
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

    public boolean isHasRecipientsCleanup() {
        return hasRecipientsCleanup;
    }

    public void setHasRecipientsCleanup(boolean hasRecipientsCleanup) {
        this.hasRecipientsCleanup = hasRecipientsCleanup;
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
}
