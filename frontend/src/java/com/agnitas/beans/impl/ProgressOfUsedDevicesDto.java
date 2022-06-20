/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.List;

/**
 * Data for "Progress of used devices" and "devices reacted on" charts on recipient review page
 */
public class ProgressOfUsedDevicesDto {

    /**
     * Usage by dates (data for "Progress of used devices" chart)
     */
    private List<UsedDevices> usedDevicesByPeriods;

    /**
     * Data for "devices reacted on" chart.
     */
    private UsedDevices usedDevices;
    /**
     * Localized message "Windows PC"
     */
    private String windowsTitle;
    /**
     * Localized message "Generic IPhone"
     */
    private String iosTitle;
    /**
     * Localized message "Generic Android"
     */
    private String androidTitle;
    /**
     * Localized message "Mac OS"
     */
    private String macTitle;
    /**
     * Localized message "Other"
     */
    private String otherTitle;

    /**
     * Localized message "Not Available"
     */
    private String notAvailable;

    /**
     * Date format for X axis
     */
    private String dateFormat;

    /**
     * Minimum date of X axis
     */
    private String min;

    /**
     * Maximum date of X axis
     */
    private String max;

    /**
     * Localized message "Number of reactions"
     */
    private String numberOfReactions;

    public List<UsedDevices> getUsedDevicesByPeriods() {
        return usedDevicesByPeriods;
    }

    public void setUsedDevicesByPeriods(List<UsedDevices> usedDevicesByPeriods) {
        this.usedDevicesByPeriods = usedDevicesByPeriods;
    }

    public String getWindowsTitle() {
        return windowsTitle;
    }

    public void setWindowsTitle(String windowsTitle) {
        this.windowsTitle = windowsTitle;
    }

    public String getIosTitle() {
        return iosTitle;
    }

    public void setIosTitle(String iosTitle) {
        this.iosTitle = iosTitle;
    }

    public String getAndroidTitle() {
        return androidTitle;
    }

    public void setAndroidTitle(String androidTitle) {
        this.androidTitle = androidTitle;
    }

    public String getMacTitle() {
        return macTitle;
    }

    public void setMacTitle(String macTitle) {
        this.macTitle = macTitle;
    }

    public String getOtherTitle() {
        return otherTitle;
    }

    public void setOtherTitle(String otherTitle) {
        this.otherTitle = otherTitle;
    }

    public String getNotAvailable() {
        return notAvailable;
    }

    public void setNotAvailable(String notAvailable) {
        this.notAvailable = notAvailable;
    }

    public UsedDevices getUsedDevices() {
        return usedDevices;
    }

    public void setUsedDevices(UsedDevices usedDevices) {
        this.usedDevices = usedDevices;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getNumberOfReactions() {
        return numberOfReactions;
    }

    public void setNumberOfReactions(String numberOfReactions) {
        this.numberOfReactions = numberOfReactions;
    }
}
