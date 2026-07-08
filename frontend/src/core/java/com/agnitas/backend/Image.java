/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

/**
 * Keep track of an Image
 */
public class Image {
	private long id;
	private String name;
	private String filename;
	private String mimeType;
	private String link;
	private String description;
	private String altText;
	private int height;
	private int width;
	private boolean mediapool;

	public Image(long nId, String nName, String nFilename, String nLink) {
		this (nId, nName, nFilename, null, nLink, null, null, 0, 0, false);
	}
	public Image(long nId, String nName, String nFilename, String nLink, String nDescription) {
		this (nId, nName, nFilename, null, nLink, nDescription, null, 0, 0, false);
	}
	public Image(long id, String name, String filename, String mimeType, String link, String description, String altText, int height, int width, boolean mediapool) {
		this.id = id;
		this.name = name;
		this.filename = filename;
		if (mimeType == null) {
			int	index;
			String	typ = "jpg";

			if ((filename != null) && ((index = filename.lastIndexOf (".")) != -1) && (index + 1 < filename.length ())) {
				typ = filename.substring (index + 1);
			} else if ((name != null) && ((index = name.lastIndexOf (".")) != -1) && (index + 1 < name.length ())) {
				typ = name.substring (index + 1);
			}
			mimeType = "image/" + typ;
		}
		this.mimeType = mimeType;
		this.link = link;
		this.description = description;
		this.altText = altText;
		this.height = height;
		this.width = width;
		this.mediapool = mediapool;
	}

	public long id() {
		return id;
	}

	public String name() {
		return name;
	}

	public String filename() {
		return filename;
	}
	
	public String mimeType () {
		return mimeType;
	}

	public String link() {
		return link;
	}

	public void link(String nLink) {
		link = nLink;
	}
	
	public String description () {
		return description;
	}
	
	public String altText () {
		return altText;
	}
	
	public int height () {
		return height;
	}
	
	public int width () {
		return width;
	}
	
	public boolean mediapool () {
		return mediapool;
	}
}
