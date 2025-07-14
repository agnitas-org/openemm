/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dto to serialize in JSON, contains data for recipient overview page charts
 */
public class RecipientChartDto {

    private LinkedHashMap<String, Integer> reactorType = new LinkedHashMap<>();
    private float rebuyRate;
    private String rebuyTitle;
    private String notAvailable; // TODO: EMMGUI-714: remove property if redundant
    private LinkedHashMap<String, Integer> revenue = new LinkedHashMap<>();
    private Map<String, String> favoriteOpeningDays = new LinkedHashMap<>();
    private Map<String, String> favoriteOpeningHours = new LinkedHashMap<>();
    private double ctr;
    private double pageImpressions;
    private double pageImpressionsPerVisit;
    private double pageImpressionsPerBuy;
    private double conversion;

    public double getConversion() {
        return conversion;
    }

    public void setConversion(double conversion) {
        this.conversion = conversion;
    }

    public Map<String, String> getFavoriteOpeningDays() {
        return favoriteOpeningDays;
    }

    public void setFavoriteOpeningDays(Map<String, String> favoriteOpeningDays) {
        this.favoriteOpeningDays = favoriteOpeningDays;
    }

    public Map<String, String> getFavoriteOpeningHours() {
        return favoriteOpeningHours;
    }

    public void setFavoriteOpeningHours(Map<String, String> favoriteOpeningHours) {
        this.favoriteOpeningHours = favoriteOpeningHours;
    }

    public double getCtr() {
        return ctr;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    public double getPageImpressions() {
        return pageImpressions;
    }

    public void setPageImpressions(double pageImpressions) {
        this.pageImpressions = pageImpressions;
    }

    public double getPageImpressionsPerVisit() {
        return pageImpressionsPerVisit;
    }

    public void setPageImpressionsPerVisit(double pageImpressionsPerVisit) {
        this.pageImpressionsPerVisit = pageImpressionsPerVisit;
    }

    public double getPageImpressionsPerBuy() {
        return pageImpressionsPerBuy;
    }

    public void setPageImpressionsPerBuy(double pageImpressionsPerBuy) {
        this.pageImpressionsPerBuy = pageImpressionsPerBuy;
    }

    public Map<String, Integer> getReactorType() {
        return reactorType;
    }

    public float getRebuyRate() {
        return rebuyRate;
    }

    public void setRebuyRate(float rebuyRate) {
        this.rebuyRate = rebuyRate;
    }

    public LinkedHashMap<String, Integer> getRevenue() {
        return revenue;
    }

    public String getRebuyTitle() {
        return rebuyTitle;
    }

    public void setRebuyTitle(String rebuyTitle) {
        this.rebuyTitle = rebuyTitle;
    }

    public String getNotAvailable() {
        return notAvailable;
    }

    public void setNotAvailable(String notAvailable) {
        this.notAvailable = notAvailable;
    }
}
