/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.impl.AdminPreferencesImpl;
import com.agnitas.dao.AdminPreferencesDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.util.preferences.PreferenceItem;
import org.springframework.dao.DataAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPreferencesDaoImpl extends BaseDaoImpl implements AdminPreferencesDao {

    //table name
    protected static final String TABLE = "admin_pref_tbl";

    //table columns
    protected static final String FIELD_ADMIN_ID = "admin_id";
    protected static final String FIELD_PREFERENCE = "pref";
    protected static final String FIELD_VALUE = "val";

    //preferences names
    protected static final String PREFERENCE_CONTENTBLOCKS = "mailing.contentblocks";    //Contentblocks
    private static final String PREFERENCE_DASHBOARD_MAILING = "dashboard.mailing";      //Mailings in Dashboard
    private static final String PREFERENCE_MAILING_LIVE_PREVIEW = "mailing.livepreview"; //Live Preview
    private static final String PREFERENCE_MAILING_SETTINGS = "mailing.settings";        //Mailing settings
    private static final String PREFERENCE_STATISTIC_LOADTYPE = "statistic.loadtype";    //Statistic-Summary

    //SQL requests
    private static final String DELETE_PREFERENCES_BY_ADMIN_ID = "DELETE FROM " + TABLE + " WHERE " + FIELD_ADMIN_ID + " = ?";
    private static final String INSERT = "INSERT INTO " + TABLE + " ( " + FIELD_ADMIN_ID + " , " + FIELD_PREFERENCE + " , " + FIELD_VALUE +  " ) VALUES (?, ?, ?)";
    private static final String SELECT_ALL_BY_ADMIN_ID = "SELECT " + FIELD_PREFERENCE + " , " + FIELD_VALUE + " FROM " + TABLE + " WHERE " + FIELD_ADMIN_ID + " = ?";

    @Override
    public AdminPreferences getAdminPreferences(int adminId) {
    	// !!! adminId == 0 for new created user - it is legal argunemt
        try {
            AdminPreferences adminPreferences = new AdminPreferencesImpl();
            adminPreferences.setAdminID(adminId);

            List<Map<String, Object>> resultList = select(SELECT_ALL_BY_ADMIN_ID, adminId);

            if (!resultList.isEmpty()) {

                Map<String, String> adminPreferencesMap = new HashMap<>();
                for (Map<String, Object> resultRow : resultList) {
                    adminPreferencesMap.put(resultRow.get(FIELD_PREFERENCE).toString(), resultRow.get(FIELD_VALUE).toString());
                }

                int prefDashboardMailing = adminPreferencesMap.get(PREFERENCE_DASHBOARD_MAILING) != null ?
                        Integer.parseInt(adminPreferencesMap.get(PREFERENCE_DASHBOARD_MAILING)) : PreferenceItem.DASHBOARD_MAILING.getDefaultValue();
                int prefMailingLivePreview = adminPreferencesMap.get(PREFERENCE_MAILING_LIVE_PREVIEW) != null ?
                        Integer.parseInt(adminPreferencesMap.get(PREFERENCE_MAILING_LIVE_PREVIEW)) : PreferenceItem.MAILING_LIVE_PREVIEW.getDefaultValue();
                int prefMailingSettings = adminPreferencesMap.get(PREFERENCE_MAILING_SETTINGS) != null ?
                        Integer.parseInt(adminPreferencesMap.get(PREFERENCE_MAILING_SETTINGS)) : PreferenceItem.MAILING_SETTINGS.getDefaultValue();
                int prefContentBlocks = adminPreferencesMap.get(PREFERENCE_CONTENTBLOCKS) != null ?
                        Integer.parseInt(adminPreferencesMap.get(PREFERENCE_CONTENTBLOCKS)) : PreferenceItem.CONTENTBLOCKS.getDefaultValue();
                int statisticLoadType = adminPreferencesMap.get(PREFERENCE_STATISTIC_LOADTYPE) != null ?
                        Integer.parseInt(adminPreferencesMap.get(PREFERENCE_STATISTIC_LOADTYPE)) : PreferenceItem.STATISTIC_LOADTYPE.getDefaultValue();

                adminPreferences.setDashboardMailingsView(prefDashboardMailing);
                adminPreferences.setLivePreviewPosition(prefMailingLivePreview);
                adminPreferences.setMailingSettingsView(prefMailingSettings);
                adminPreferences.setMailingContentView(prefContentBlocks);
                adminPreferences.setStatisticLoadType(statisticLoadType);

                return adminPreferences;
            } else {
                logger.debug("User preferences not found for user id = {}", adminId);
                //set and return default preferences
                adminPreferences.setDashboardMailingsView(PreferenceItem.DASHBOARD_MAILING.getDefaultValue());
                adminPreferences.setLivePreviewPosition(PreferenceItem.MAILING_LIVE_PREVIEW.getDefaultValue());
                adminPreferences.setMailingSettingsView(PreferenceItem.MAILING_SETTINGS.getDefaultValue());
                adminPreferences.setMailingContentView(PreferenceItem.CONTENTBLOCKS.getDefaultValue());
                adminPreferences.setStatisticLoadType(PreferenceItem.STATISTIC_LOADTYPE.getDefaultValue());

                return adminPreferences;
            }
        } catch (DataAccessException | NumberFormatException e) {
            logger.error("Error reading admin preferences", e);
            return null;
        }
    }

    @Override
    public int save(AdminPreferences adminPreferences) {
        if (adminPreferences == null) {
            return 0;
        }
        
        int deletedPrefs = delete(adminPreferences.getAdminID());
        if (deletedPrefs > 0 && logger.isDebugEnabled()) {
        	logger.debug("Removed " + deletedPrefs + " old preferences of admin id = " + adminPreferences.getAdminID());
        }

        return updatePreferences(adminPreferences);
    }

    @Override
	@DaoUpdateReturnValueCheck
    public int delete(int adminId) {
        return update(DELETE_PREFERENCES_BY_ADMIN_ID, adminId);
    }

	@DaoUpdateReturnValueCheck
    private int updatePreferences(AdminPreferences adminPreferences){
        int adminId = adminPreferences.getAdminID();
        update(DELETE_PREFERENCES_BY_ADMIN_ID, adminId);
        
        int touchedLines = 0;

        touchedLines += update(INSERT, adminId, PREFERENCE_DASHBOARD_MAILING, adminPreferences.getDashboardMailingsView());
        touchedLines += update(INSERT, adminId, PREFERENCE_MAILING_LIVE_PREVIEW, adminPreferences.getLivePreviewPosition());
        touchedLines += update(INSERT, adminId, PREFERENCE_CONTENTBLOCKS, adminPreferences.getMailingContentView());
        touchedLines += update(INSERT, adminId, PREFERENCE_MAILING_SETTINGS, adminPreferences.getMailingSettingsView());
        touchedLines += update(INSERT, adminId, PREFERENCE_STATISTIC_LOADTYPE, adminPreferences.getStatisticLoadType());
        
        return touchedLines;
    }
}
