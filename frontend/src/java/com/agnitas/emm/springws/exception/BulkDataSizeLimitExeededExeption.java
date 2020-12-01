/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.exception;


public class BulkDataSizeLimitExeededExeption extends Exception {
	private static final long serialVersionUID = -9170958147019018324L;

	public BulkDataSizeLimitExeededExeption(String endpoint, String user, int limit, int size) {
		super(String.format("Total data size in endpoint %s requested by %s: limit is %d, requested size is %d",
				endpoint, user, limit, size));
	}
}
