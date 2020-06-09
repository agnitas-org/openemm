/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import java.io.Serializable;

import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;

public interface Popups extends Serializable {
    /**
     * Add a new alert popup.
     *
     * @param popup a popup content representation.
     * @return self.
     */
    Popups alert(Message popup);

    /**
     * Add a new translatable alert popup.
     *
     * @param code a key of the translatable message.
     * @param arguments an arguments for the translatable message.
     * @return self.
     */
    Popups alert(String code, Object ...arguments);

    /**
     * Add an exact (non-translatable) alert popup.
     *
     * @param text an exact message.
     * @return self.
     */
    Popups exactAlert(String text);

    /**
     * Add a new warning popup.
     *
     * @param popup a popup content representation.
     * @return self.
     */
    Popups warning(Message popup);

    /**
     * Add a new translatable warning popup.
     *
     * @param code a key of the translatable message.
     * @param arguments an arguments for the translatable message.
     * @return self.
     */
    Popups warning(String code, Object ...arguments);

    /**
     * Add an exact (non-translatable) warning popup.
     *
     * @param text an exact message.
     * @return self.
     */
    Popups exactWarning(String text);

    /**
     * Add a new success (information) popup.
     *
     * @param popup a popup content representation.
     * @return self.
     */
    Popups success(Message popup);

    /**
     * Add a new translatable success (information) popup.
     *
     * @param code a key of the translatable message.
     * @param arguments an arguments for the translatable message.
     * @return self.
     */
    Popups success(String code, Object ...arguments);

    /**
     * Add an exact (non-translatable) success (information) popup.
     *
     * @param text an exact message.
     * @return self.
     */
    Popups exactSuccess(String text);


    Popups addPopups(ServiceResult<?> serviceResult);

    /**
     * Add a new form field error popup.
     *
     * @param field a field name.
     * @param popup a popup content representation.
     * @return self.
     */
    Popups field(String field, Message popup);

    /**
     * Add a new translatable form field error popup.
     *
     * @param field a field name.
     * @param code a key of the translatable message.
     * @param arguments an arguments for the translatable message.
     * @return self.
     */
    Popups field(String field, String code, Object... arguments);

    /**
     * Add an exact (non-translatable) form field error popup.
     *
     * @param field a field name.
     * @param text an exact message.
     * @return self.
     */
    Popups exactField(String field, String text);

    /**
     * Get a count of added popups.
     *
     * @return popups count.
     */
    int size();

    /**
     * Check if nothing is added.
     *
     * @return {@code true} if there are no popups added.
     */
    boolean isEmpty();

    /**
     * Check if at least one alert popup is added.
     * @return {@code true} if number of alerts > 0.
     */
    boolean hasAlertPopups();

    /**
     * Check if at least one warning popup is added.
     * @return {@code true} if number of warnings > 0.
     */
    boolean hasWarningPopups();

    /**
     * Check if at least one success popup is added.
     * @return {@code true} if number of success popups > 0.
     */
    boolean hasSuccessPopups();

    /**
     * Check if at least one form field error popup is added.
     * @return {@code true} if number of form field error popups > 0.
     */
    boolean hasFieldPopups();

    /**
     * Check if at least one form field error popup is added for field name {@code field}.
     * @return {@code true} if number of form field error popups for field name {@code field} > 0.
     */
    boolean hasFieldPopups(String field);

    /**
     * Clear all the added popups.
     */
    void clear();
}
