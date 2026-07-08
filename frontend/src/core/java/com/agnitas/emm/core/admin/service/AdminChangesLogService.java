/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service;

import java.util.List;

import com.agnitas.emm.core.useractivitylog.bean.UserAction;

import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.form.AdminForm;

/**
 * Service for logging changes in {@link Admin}
 */
public interface AdminChangesLogService {

    /**
     * Return Gender text representation by id
     *
     * @param id Gender ID
     * @return     Gender type text representation
     */
    static String getGenderText(int id){
        return switch (id) {
            case 0 -> "Mr.";
            case 1 -> "Mrs.";
            case 2 -> "Unknown";
            case 3 -> "Miss";
            case 4 -> "Practice";
            case 5 -> "Company";
            default -> "Unknown gender";
        };
    }

    /**
     * Return Statistic-Summary load type text representation by id
     *
     * @param type Statistic-Summary load type id
     * @return Statistic-Summary load type text representation
     */
    static String getStatisticLoadType(int type) {
        return switch (type) {
            case 0 -> "Load only after click on tab";
            case 1 -> "Load immediately";
            default -> "Unknown type";
        };
    }

    /**
     * Return mailing content view type text representation by id
     *
     * @param type Mailing content view type id
     * @return mailing content view type text representation
     */
    static String getMailingContentViewName(int type) {
        return switch (type) {
            case 0 -> "HTML-Code";
            case 1 -> "HTML-Editor";
            default -> "Unknown type";
        };
    }

    /**
     * Return dashboard mailings view type text representation by id
     *
     * @param type Dashboard mailings view type id
     * @return Dashboard mailings view type text representation
     */
    static String getDashboardMailingsView(int type) {
        return switch (type) {
            case 0 -> "List";
            case 1 -> "Preview";
            default -> "Unknown type";
        };
    }

    /**
     * Return mailing settings view type text representation by id
     *
     * @param type Mailing settings view type id
     * @return Mailing settings view type text representation
     */
    static String getMailingSettingsViewName(int type) {
        return switch (type) {
            case 0 -> "expanded";
            case 1 -> "collapsed";
            default -> "unknown type";
        };
    }

    /**
     * Return mailing content live preview position text representation by positionId
     *
     * @param position Mailing settings view type id
     * @return Mailing settings view type text representation
     */
    static String getMailingLivePreviewPosition(int position) {
        return switch (position) {
            case 0 -> "right";
            case 1 -> "bottom";
            case 2 -> "deactivated";
            default -> "unknown position";
        };
    }

    List<UserAction> getChangesAsUserActions(AdminForm newAdminData, Admin oldAdmin, AdminPreferences oldAdminPreferences);

}
