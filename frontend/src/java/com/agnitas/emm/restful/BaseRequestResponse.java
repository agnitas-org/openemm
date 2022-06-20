/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful;

public abstract class BaseRequestResponse {
	
	protected State responseState = State.OK;
	
	protected ErrorCode errorCode = null;
	protected Throwable error = null;

	public void setClientError(Throwable t) throws Exception {
		setClientError(t, ErrorCode.UNKNOWN);
	}
	
	public void setError(Throwable t) throws Exception {
		setError(t, ErrorCode.UNKNOWN);
	}

	public void setAuthentificationError(Throwable error, ErrorCode errorCode) throws Exception {
		responseState = State.AUTHENTIFICATION_ERROR;
		this.error = error;
		this.errorCode = errorCode;
	}

	public void setNoDataFoundError(Throwable error) throws Exception {
		responseState = State.NO_DATA_FOUND_ERROR;
		this.error = error;
	}

	public void setClientError(Throwable error, ErrorCode errorCode) throws Exception {
		responseState = State.CLIENT_ERROR;
		this.error = error;
		this.errorCode = errorCode;
	}

	public void setError(Throwable error, ErrorCode errorCode) throws Exception {
		responseState = State.ERROR;
		this.error = error;
		this.errorCode = errorCode;
	}

	public abstract String getMimeType() throws Exception;

	public abstract String getString() throws Exception;

	public void setExportedToStream(boolean exportedToStream) {
		responseState = State.EXPORTED_TO_STREAM;
	}
}
