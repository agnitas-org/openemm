/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import org.agnitas.beans.impl.AdminPreferencesImpl;

import com.agnitas.beans.ComAdminPreferences;

public class ComAdminPreferencesImpl extends AdminPreferencesImpl implements ComAdminPreferences {

    private static final long serialVersionUID = -8128756315435745115L;

    protected int dashboardMailingsView;
    protected int navigationLocation;
    protected int mailingSettingsView;
    protected int livePreviewPosition;
    protected int startPage;
    protected int statisticLoadType;


    @Override
    public int getNavigationLocation() {
        return navigationLocation;
    }

    @Override
    public void setNavigationLocation(int navigationLocation) {
        this.navigationLocation = navigationLocation;
    }

    @Override
    public int getMailingSettingsView() {
        return mailingSettingsView;
    }

    @Override
    public void setMailingSettingsView(int mailingSettingsView) {
        this.mailingSettingsView = mailingSettingsView;
    }

    @Override
    public int getDashboardMailingsView() {
        return dashboardMailingsView;
    }

    @Override
    public void setDashboardMailingsView(int dashboardMailingsView) {
        this.dashboardMailingsView = dashboardMailingsView;
    }

    @Override
    public int getLivePreviewPosition() {
        return this.livePreviewPosition;
    }

    @Override
    public void setLivePreviewPosition(int livePreviewPosition) {
        this.livePreviewPosition = livePreviewPosition;
    }

    @Override
    public int getStartPage() {
        return startPage;
    }

    @Override
    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    @Override
    public int getStatisticLoadType() {
        return this.statisticLoadType;
    }

    @Override
    public void setStatisticLoadType(int statisticLoadType) {
        this.statisticLoadType = statisticLoadType;
    }
}
