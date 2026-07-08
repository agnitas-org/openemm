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

    private BindingUtils() {

    }

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

        return switch (userStatus) {
            case Active -> "Opt-In by ADMIN (" + admin.getUsername() + ")";
            case Bounce -> "Bounced by ADMIN (" + admin.getUsername() + ")";
            case AdminOut -> "Opt-Out by ADMIN (" + admin.getUsername() + ")";
            case UserOut -> "User-Opt-Out by ADMIN (" + admin.getUsername() + ")";
            case WaitForConfirm -> "Wait-For-Confirm by ADMIN (" + admin.getUsername() + ")";
            case Blacklisted -> "Blacklist by ADMIN (" + admin.getUsername() + ")";
            case Suspend -> "Suspend by ADMIN (" + admin.getUsername() + ")";
        };
	}

	public static String getRecipientTypeTitleByLetter(String letter){
        return switch (RecipientType.getRecipientTypeByLetter(letter)) {
            case ALL_RECIPIENTS -> "All";
            case ADMIN_RECIPIENT -> "Administrator";
            case TEST_RECIPIENT -> "Test recipient";
            case TEST_VIP_RECIPIENT -> "Test VIP";
            case NORMAL_RECIPIENT -> "Normal recipient";
            case NORMAL_VIP_RECIPIENT -> "Normal VIP recipient";
        };
    }
}
