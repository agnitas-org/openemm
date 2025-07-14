/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.dto;

import com.agnitas.web.mvc.Popups;

public class DataResponseDto<D> extends BooleanResponseDto {

    private D data;

    public DataResponseDto(D data, Popups popups) {
        this(data, popups, !popups.hasAlertPopups());
    }

    public DataResponseDto(D data, Popups popups, boolean success) {
        super(popups, success);
        this.data = data;
    }


    public DataResponseDto(Popups popups, boolean success) {
        super(popups, success);
    }

    public DataResponseDto(D data, boolean success) {
        super(success);
        this.data = data;
    }

    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }
}
