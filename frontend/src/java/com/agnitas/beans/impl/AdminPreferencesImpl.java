/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import com.agnitas.beans.AdminPreferences;

public class AdminPreferencesImpl implements AdminPreferences {

    protected int dashboardMailingsView;
    protected int mailingSettingsView;
    protected int livePreviewPosition;
    protected int statisticLoadType;
    protected int adminId;
    protected int mailingContentView;

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
    public int getStatisticLoadType() {
        return this.statisticLoadType;
    }

    @Override
    public void setStatisticLoadType(int statisticLoadType) {
        this.statisticLoadType = statisticLoadType;
    }
    
    @Override
    public int getAdminID() {
        return this.adminId;
    }

    @Override
    public void setAdminID(int adminID) {
        this.adminId = adminID;
    }

    @Override
    public int getMailingContentView() {
        return this.mailingContentView;
    }

    @Override
    public void setMailingContentView(int mailingContentView) {
        this.mailingContentView = mailingContentView;
    }
}
