/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import org.agnitas.beans.AdminPreferences;

public interface ComAdminPreferences extends AdminPreferences {

    public static final int DASHBOARD_MAILINGS_LIST = 0;
    public static final int DASHBOARD_MAILINGS_PREVIEW = 1;

    public static final int NAVIGATION_LEFT = 0;
    public static final int NAVIGATION_TOP = 1;

    public static final int MAILING_SETTINGS_EXPANDED = 0;
    public static final int MAILING_SETTINGS_COLLAPSED = 1;

    public static final int LIVE_PREVIEW_RIGHT = 0;
    public static final int LIVE_PREVIEW_BOTTOM = 1;
    public static final int LIVE_PREVIEW_DEACTIVATE = 2;

    public static final int START_PAGE_DASHBOARD = 0;
    public static final int START_PAGE_CALENDAR = 1;

    public static final int STATISTIC_LOADTYPE_ON_CLICK = 0;
    public static final int STATISTIC_LOADTYPE_IMMEDIATELY = 1;

    /**
     * Getter for the preferred dashboard mailing view type
     */
    public int getDashboardMailingsView();

    /**
     * Setter for the preferred dashboard mailing view type
     */
    public void setDashboardMailingsView(int dashboardMailingsView);

    /**
     * Getter for the preferred navigation location (left(0) or top(1))
     */
    public int getNavigationLocation();

    /**
     * Setter for the preferred navigation location (left(0) or top(1))
     */
    public void setNavigationLocation(int navigationLocation);

    /**
     * Getter for the preferred mailing settings view type (expanded(0) or collapsed(1))
     */
    public int getMailingSettingsView();

    /**
     * Setter for the preferred mailing settings view type (expanded(0) or collapsed(1))
     */
    public void setMailingSettingsView(int mailingSettingsView);

    /**
     * Getter for the preferred position of the mailing content live preview (right(0)/bottom(1)/deactivated(2))
     */
    public int getLivePreviewPosition();

    /**
     * Setter for the preferred position of the mailing content live preview (right(0)/bottom(1)/deactivated(2))
     */
    public void setLivePreviewPosition(int livePreviewPosition);

    /**
     * Getter for the preferred start page (dashboard(0)/calender(1))
     */
    public int getStartPage();

    /**
     * Setter for the preferred start page (dashboard(0)/calender(1))
     */
    public void setStartPage(int startPage);

    /**
     * Getter for the preferred Statistic-Summary load type (immediately(0)/on click(1))
     */
    public int getStatisticLoadType();

    /**
     * Setter for the preferred Statistic-Summary load type (immediately(0)/on click(1))
     */
    public void setStatisticLoadType(int statisticLoadType);

}
