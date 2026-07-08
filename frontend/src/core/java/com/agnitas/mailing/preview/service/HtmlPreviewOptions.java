/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.preview.service;

public final class HtmlPreviewOptions {

	private boolean removeHtmlTags;
	private int lineFeed;
	
	private HtmlPreviewOptions() {
		this.removeHtmlTags = false;
		this.lineFeed = 0;
	}
	
	public static final HtmlPreviewOptions createDefault() {
		return new HtmlPreviewOptions();
	}
	
	public final HtmlPreviewOptions withRemoveHtmlTags(final boolean remove) {
		this.removeHtmlTags = remove;
		
		return this;
	}
	
	public final boolean isRemoveHtmlTags() {
		return this.removeHtmlTags;
	}
	
	public final HtmlPreviewOptions withLineFeed(final int feed) {
		this.lineFeed = Math.max(0, feed);
		
		return this;
	}
	
	public final int getLineFeed() {
		return this.lineFeed;
	}
	
}
