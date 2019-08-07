/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class ActionOperationActivateDoubleOptInParameters extends AbstractActionOperationParameters {
	
	/** Default media type. */
	public static final MediaTypes DEFAULT_MEDIA_TYPE = MediaTypes.EMAIL;
	
    private boolean forAllLists;
    private MediaTypes mediaType;

	public ActionOperationActivateDoubleOptInParameters() {
		super(ActionOperationType.ACTIVATE_DOUBLE_OPT_IN);
		
		this.mediaType = DEFAULT_MEDIA_TYPE;
	}

	public boolean isForAllLists() {
		return forAllLists;
	}

	public void setForAllLists(final boolean forAllLists) {
		this.forAllLists = forAllLists;
	}
	
	/**
	 * Set media type for DOI confirmation. If given media type is <code>null</code>
	 * the default media type ({@link #DEFAULT_MEDIA_TYPE} is set.
	 * 
	 * @param mediatype media type for DOI confirmation
	 */
	public void setMediaType(final MediaTypes mediatype) {
		this.mediaType = mediatype != null ? mediatype : DEFAULT_MEDIA_TYPE;
		
		assert this.mediaType != null;
	}
	
	/**
	 * Returns the media type for DOI confirmation.
	 * 
	 * @return media type for DOI confirmation
	 */
	public MediaTypes getMediaType() {
		assert this.mediaType != null;
		
		return this.mediaType;
	}
	
	
	/**
	 * Set media type by code. If code is unknown, {@link MediaTypes#EMAIL} is set.
	 * 
	 * @param mediaType code of media type
	 */
	public void setMediaTypeCode(final int mediaType) {
		setMediaType(MediaTypes.getMediaTypeForCode(mediaType));
	}

	/**
	 * Returns the type code of the media type for DOI confirmation.
	 * 
	 * @return media type code for DOI confirmation
	 */
	
	public int getMediaTypeCode() {
		return getMediaType().getMediaCode();
	}
	
}
