/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.password;

/**
 * Possible results of password expiration check.
 */
public enum PasswordState {
    /** Password is valid. */
    VALID,

    /** It's a one-time password that must be changed immediately upon log in */
    ONE_TIME,

    /** Password is valid, but will expire in the near future. */
    EXPIRING,

    /** Password is expired, has to be changed on next login. */
    EXPIRED,

    /** Password is expired and cannot be used to log in anymore. */
    EXPIRED_LOCKED
}
