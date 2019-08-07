/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.beans.impl;

public enum AutoOptimizationStatus {
    NOT_STARTED(0),
    TEST_SEND(1),
    EVAL_IN_PROGRESS(2),
    FINISHED(3),
    SCHEDULED(4);

    private int code;

    AutoOptimizationStatus(int code) {
        this.code = code;
    }

    public static AutoOptimizationStatus get(int statusCode) {
        for (AutoOptimizationStatus status : values()) {
            if (status.code == statusCode) {
                return status;
            }
        }
        return null;
    }

    public int getCode() {
        return this.code;
    }
}
