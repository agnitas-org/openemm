/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import org.agnitas.beans.EmmLayoutBase;

public class EmmLayoutBaseImpl implements EmmLayoutBase {
	private int id;
	private String baseURL;
	private String imagesURL;
	private String cssURL;
	private String jsURL;
	private String shortname;
	private ThemeType themeType;
    private int menuPosition = MENU_POSITION_DEFAULT;
    private int livepreviewPosition = LIVEPREVIEW_POSITION_RIGHT;
	
	public EmmLayoutBaseImpl(int id, String baseUrl) {
		this.id = id;
		this.baseURL = baseUrl;
		this.imagesURL = baseUrl + "/images";
		this.cssURL = baseUrl + "/styles";
		this.jsURL = baseUrl + "/js";

		this.themeType = ThemeType.STANDARD;
	}
	/* (non-Javadoc)
	 * @see org.agnitas.beans.EmmBaseLayout#getId()
	 */
	@Override
	public int getId() {
		return id;
	}
	/* (non-Javadoc)
	 * @see org.agnitas.beans.EmmBaseLayout#setId(int)
	 */
	@Override
	public void setId(int id) {
		this.id = id;
	}
	/* (non-Javadoc)
	 * @see org.agnitas.beans.EmmBaseLayout#getBaseURL()
	 */
	@Override
	public String getBaseURL() {
		return baseURL;
	}
	/* (non-Javadoc)
	 * @see org.agnitas.beans.EmmBaseLayout#setBaseURL(java.lang.String)
	 */
	@Override
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
	/* (non-Javadoc)
	 * @see org.agnitas.beans.EmmBaseLayout#getImageURL()
	 */
	@Override
	public String getImagesURL() {
		return imagesURL;
	}
	/* (non-Javadoc)
	 * @see org.agnitas.beans.EmmBaseLayout#setImageURL(java.lang.String)
	 */
	@Override
	public void setImagesURL(String imageURL) {
		this.imagesURL = imageURL;
	}
	/* (non-Javadoc)
	 * @see org.agnitas.beans.EmmBaseLayout#getCssURL()
	 */
	@Override
	public String getCssURL() {
		return cssURL;
	}
	/* (non-Javadoc)
	 * @see org.agnitas.beans.EmmBaseLayout#setCssURL(java.lang.String)
	 */
	@Override
	public void setCssURL(String cssURL) {
		this.cssURL = cssURL;
	}
	/* (non-Javadoc)
	 * @see org.agnitas.beans.EmmBaseLayout#getJsURL()
	 */
	@Override
	public String getJsURL() {
		return jsURL;
	}
	/* (non-Javadoc)
	 * @see org.agnitas.beans.EmmBaseLayout#setJsURL(java.lang.String)
	 */
	@Override
	public void setJsURL(String jsURL) {
		this.jsURL = jsURL;
	}

    @Override
	public int getMenuPosition() {
        return menuPosition;
    }

    @Override
	public void setMenuPosition(int menuPosition) {
       this.menuPosition = menuPosition;
    }

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

    @Override
	public int getLivepreviewPosition() {
        return livepreviewPosition;
    }

    @Override
	public void setLivepreviewPosition(int livepreviewPosition) {
        this.livepreviewPosition = livepreviewPosition;
    }

	@Override
	public ThemeType getThemeType() {
		return themeType;
	}

	@Override
	public void setThemeType(ThemeType themeType) {
		this.themeType = themeType;
	}
}
