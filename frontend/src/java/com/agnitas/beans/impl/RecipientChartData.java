/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

/**
 * Data for charts on recipient overview page
 */
public class RecipientChartData {

    /**
     * Version without Automation Package (no permission for mailing history):
     * recipients without reaction during the last 90 days, but have status active since 90 days or longer
     * (creationdate < sysdate-90 & (lastopendate < sysdate-90 or lastopendate is null)
     * & (lastclickdate < sysdate-90 or lastclickdate is null) & lastsenddate >= sysdate-90)
     */
    private int sleeper;
    /**
     * Version without Automation Package (no permission for mailing history):
     * All recipients, who had opened or clicked during the last 90 days, unimportant how long they have status active
     */
    private int opportunity;
    /**
     * Version without Automation Package (no permission for mailing history):
     *  recipient is less then 90 days active and did not react (lastopendate is null & lastclickdate is null)
     *  OR last senddate is greater then 90 days (lastsenddate < sysdate-90 or lastsenddate is null)
     */
    private int lead;

    /**
     *  number of all buyings / number of all buyers
     */
    private float rebuyRate;

    /**
     * Poor Dog: no value or 0
     */
    private int noRevenue;
    /**
     * Question mark: < 150 €
     */
    private int lowRevenue;
    /**
     * Rising Stars: < 500 €
     */
    private int averageRevenue;
    /**
     * Cash Cows: >= 500 €
     */
    private int highRevenue;

    private String favouriteOpeningDays;
    private String favouriteOpeningTime;
    private double ctr;
    private double pi;
    private double piPerVisit;
    private double piPerBuyings;
    private double conversion;

    public double getConversion() {
        return conversion;
    }

    public void setConversion(double conversion) {
        this.conversion = conversion;
    }

    public String getFavoriteOpeningDays() {
        return favouriteOpeningDays;
    }

    public void setFavoriteOpeningDays(String favouriteOpeningDays) {
        this.favouriteOpeningDays = favouriteOpeningDays;
    }

    public String getFavoriteOpeningTime() {
        return favouriteOpeningTime;
    }

    public void setFavoriteOpeningTime(String favouriteOpeningTime) {
        this.favouriteOpeningTime = favouriteOpeningTime;
    }

    public double getCtr() {
        return ctr;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    public double getPi() {
        return pi;
    }

    public void setPi(double pi) {
        this.pi = pi;
    }

    public double getPiPerVisit() {
        return piPerVisit;
    }

    public void setPiPerVisit(double piPerVisit) {
        this.piPerVisit = piPerVisit;
    }

    public double getPiPerBuyings() {
        return piPerBuyings;
    }

    public void setPiPerBuyings(double piPerBuyings) {
        this.piPerBuyings = piPerBuyings;
    }

    public int getSleeper() {
        return sleeper;
    }

    public void setSleeper(int sleeper) {
        this.sleeper = sleeper;
    }

    public int getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(int opportunity) {
        this.opportunity = opportunity;
    }

    public int getLead() {
        return lead;
    }

    public void setLead(int lead) {
        this.lead = lead;
    }

    public float getRebuyRate() {
        return rebuyRate;
    }

    public void setRebuyRate(float rebuyRate) {
        this.rebuyRate = rebuyRate;
    }

    public int getNoRevenue() {
        return noRevenue;
    }

    public void setNoRevenue(int noRevenue) {
        this.noRevenue = noRevenue;
    }

    public int getLowRevenue() {
        return lowRevenue;
    }

    public void setLowRevenue(int lowRevenue) {
        this.lowRevenue = lowRevenue;
    }

    public int getAverageRevenue() {
        return averageRevenue;
    }

    public void setAverageRevenue(int averageRevenue) {
        this.averageRevenue = averageRevenue;
    }

    public int getHighRevenue() {
        return highRevenue;
    }

    public void setHighRevenue(int highRevenue) {
        this.highRevenue = highRevenue;
    }
}
