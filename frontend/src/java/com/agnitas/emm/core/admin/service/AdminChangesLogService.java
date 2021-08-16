/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service;

import java.util.List;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.emm.core.admin.form.AdminForm;
import org.agnitas.emm.core.useractivitylog.UserAction;

/**
 * Service for logging changes in {@link ComAdmin}
 */
public interface AdminChangesLogService {

    /**
     * Return Gender text representation by id
     *
     * @param id Gender ID
     * @return     Gender type text representation
     */
    static String getGenderText(int id){
        switch (id){
            case 0:
                return "Mr.";
            case 1:
                return "Mrs.";
            case 2:
                return "Unknown";
            case 3:
                return "Miss";
            case 4:
                return "Practice";
            case 5:
                return "Company";
            default:
                return "Unknown gender";
        }
    }

    /**
     * Return Statistic-Summary load type text representation by id
     *
     * @param type Statistic-Summary load type id
     * @return Statistic-Summary load type text representation
     */
    static String getStatisticLoadType(int type) {

        switch (type) {
            case 0:
                return "Load only after click on tab";
            case 1:
                return "Load immediately";
            default:
                return "Unknown type";
        }
    }

    /**
     * Return mailing content view type text representation by id
     *
     * @param type Mailing content view type id
     * @return mailing content view type text representation
     */
    static String getMailingContentViewName(int type) {

        switch (type) {
            case 0:
                return "HTML-Code";
            case 1:
                return "HTML-Editor";
            default:
                return "Unknown type";
        }
    }

    /**
     * Return dashboard mailings view type text representation by id
     *
     * @param type Dashboard mailings view type id
     * @return Dashboard mailings view type text representation
     */
    static String getDashboardMailingsView(int type) {

        switch (type) {
            case 0:
                return "List";
            case 1:
                return "Preview";
            default:
                return "Unknown type";
        }
    }

    /**
     * Return mailing settings view type text representation by id
     *
     * @param type Mailing settings view type id
     * @return Mailing settings view type text representation
     */
    static String getMailingSettingsViewName(int type) {

        switch (type) {
            case 0:
                return "expanded";
            case 1:
                return "collapsed";
            default:
                return "unknown type";
        }
    }

    /**
     * Return mailing content live preview position text representation by positionId
     *
     * @param position Mailing settings view type id
     * @return Mailing settings view type text representation
     */
    static String getMailingLivePreviewPosition(int position) {
        switch (position) {
            case 0:
                return "right";
            case 1:
                return "bottom";
            case 2:
                return "deactivated";
            default:
                return "unknown position";
        }
    }

    List<UserAction> getChangesAsUserActions(AdminForm newAdminData, ComAdmin oldAdmin, ComAdminPreferences oldAdminPreferences);

}
