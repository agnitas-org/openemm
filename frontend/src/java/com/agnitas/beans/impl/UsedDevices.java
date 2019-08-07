/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Mailing open+click by devices
 */
@JsonIgnoreProperties({"endOfPeriod"})
public class UsedDevices {

    /**
     * End of counted period as string
     */
    private String date;

    /**
     * Number of opened/clicked mailing from Windows PC
     */
    private int windows;

    /**
     * Number of opened/clicked mailing from windows IPhone
     */
    private int ios;

    /**
     * Number of opened/clicked mailing from windows Android
     */
    private int android;

    /**
     * Number of opened/clicked mailing from windows Mac OS
     */
    private int mac;

    /**
     * Number of opened/clicked mailing from other devices
     */
    private int other;

    /**
     * End of counted period
     */
    private Date endOfPeriod;

    public UsedDevices() {
    }

    public UsedDevices(String date, int windows, int ios, int android, int mac, int other, Date endOfPeriod) {
        this.date = date;
        this.windows = windows;
        this.ios = ios;
        this.android = android;
        this.mac = mac;
        this.other = other;
        this.endOfPeriod = endOfPeriod;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getWindows() {
        return windows;
    }

    public void setWindows(int windows) {
        this.windows = windows;
    }

    public int getIos() {
        return ios;
    }

    public void setIos(int ios) {
        this.ios = ios;
    }

    public int getAndroid() {
        return android;
    }

    public void setAndroid(int android) {
        this.android = android;
    }

    public int getMac() {
        return mac;
    }

    public void setMac(int mac) {
        this.mac = mac;
    }

    public int getOther() {
        return other;
    }

    public void setOther(int other) {
        this.other = other;
    }

    public Date getEndOfPeriod() {
        return endOfPeriod;
    }

    public void setEndOfPeriod(Date endOfPeriod) {
        this.endOfPeriod = endOfPeriod;
    }
}
