/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.post;

/** State of delivery (0=planned, 1=started, 2=finished */
public enum TriggerdialogDeliveryStatus {
	Planned(0),
	Started(1),
	Finished(2),
	Error(3),
	Canceled(4);
	
	private int code;
	
	TriggerdialogDeliveryStatus(int code) {
		this.code = code;
	}
	
	public static TriggerdialogDeliveryStatus getTriggerdialogDeliveryStatusByCode(int code) {
		for (TriggerdialogDeliveryStatus status : TriggerdialogDeliveryStatus.values()) {
			if (status.getCode() == code) {
				return status;
			}
		}
		throw new RuntimeException("Invalid code for TriggerdialogDeliveryStatus: " + code);
	}

	public int getCode() {
		return code;
	}
}
