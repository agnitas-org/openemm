/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.binding.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.recipient.service.RecipientType;

public class BindingUtils {

    public static String getUserRemarkForStatusByAdmin(Admin admin, int newUserStatus) {
        if (!UserStatus.existsWithId(newUserStatus)) {
            return "Unknown status " + newUserStatus + " by ADMIN (" + admin.getUsername() + ")";
        }

        return getUserRemarkForStatusByAdmin(admin, UserStatus.getByCode(newUserStatus));
	}

	public static String getUserRemarkForStatusByAdmin(Admin admin, UserStatus userStatus) {
        if (userStatus == null) {
            return "Unknown status by ADMIN (" + admin.getUsername() + ")";
        }

        switch (userStatus) {
            case Active:
                return "Opt-In by ADMIN (" + admin.getUsername() + ")";
            case Bounce:
                return "Bounced by ADMIN (" + admin.getUsername() + ")";
            case AdminOut:
                return "Opt-Out by ADMIN (" + admin.getUsername() + ")";
            case UserOut:
                return "User-Opt-Out by ADMIN (" + admin.getUsername() + ")";
            case WaitForConfirm:
                return "Wait-For-Confirm by ADMIN (" + admin.getUsername() + ")";
            case Blacklisted:
                return "Blacklist by ADMIN (" + admin.getUsername() + ")";
            case Suspend:
                return "Suspend by ADMIN (" + admin.getUsername() + ")";
            default:
                return "Unknown status by ADMIN (" + admin.getUsername() + ")";
        }
	}

	public static String getRecipientTypeTitleByLetter(String letter){
        RecipientType type = RecipientType.getRecipientTypeByLetter(letter);
        switch (type){
            case ALL_RECIPIENTS:
                return "All";
            case ADMIN_RECIPIENT:
                return "Administrator";
            case TEST_RECIPIENT:
                return "Test recipient";
            case TEST_VIP_RECIPIENT:
                return "Test VIP";
            case NORMAL_RECIPIENT:
                return "Normal recipient";
            case NORMAL_VIP_RECIPIENT:
                return "Normal VIP recipient";
            default:
                return "not set";
        }
    }
}
