/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.form;

public final class AdminPreferences {
    private int startPage;
    private int mailingContentView;
    private int dashboardMailingsView;
    private int navigationLocation;
    private int mailingSettingsView;
    private int livePreviewPosition;
    private int statisticLoadType;

    public AdminPreferences() {
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getMailingContentView() {
        return mailingContentView;
    }

    public void setMailingContentView(int mailingContentView) {
        this.mailingContentView = mailingContentView;
    }

    public int getDashboardMailingsView() {
        return dashboardMailingsView;
    }

    public void setDashboardMailingsView(int dashboardMailingsView) {
        this.dashboardMailingsView = dashboardMailingsView;
    }

    public int getNavigationLocation() {
        return navigationLocation;
    }

    public void setNavigationLocation(int navigationLocation) {
        this.navigationLocation = navigationLocation;
    }

    public int getMailingSettingsView() {
        return mailingSettingsView;
    }

    public void setMailingSettingsView(int mailingSettingsView) {
        this.mailingSettingsView = mailingSettingsView;
    }

    public int getLivePreviewPosition() {
        return livePreviewPosition;
    }

    public void setLivePreviewPosition(int livePreviewPosition) {
        this.livePreviewPosition = livePreviewPosition;
    }

    public int getStatisticLoadType() {
        return statisticLoadType;
    }

    public void setStatisticLoadType(int statisticLoadType) {
        this.statisticLoadType = statisticLoadType;
    }
}
