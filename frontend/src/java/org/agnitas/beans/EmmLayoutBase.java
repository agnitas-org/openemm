/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

public interface EmmLayoutBase {
    public static final int MENU_POSITION_LEFT = 0;
    public static final int MENU_POSITION_TOP = 1;
    public static final int MENU_POSITION_DEFAULT = MENU_POSITION_LEFT;

    public static final int LIVEPREVIEW_POSITION_RIGHT = 0;
    public static final int LIVEPREVIEW_POSITION_BOTTOM = 1;
    public static final int LIVEPREVIEW_POSITION_DEACTIVATE = 2;

	/**
	 * @return the id
	 */
	public int getId();

	/**
	 * @param id the id to set
	 */
	public void setId(int id);

	/**
	 * @return the baseURL
	 */
	public String getBaseURL();

	/**
	 * @param baseURL the baseURL to set
	 */
	public void setBaseURL(String baseURL);

	/**
	 * @return the imagesURL
	 */
	public String getImagesURL();

	/**
	 * @param imagesURL the imagesURL to set
	 */
	public void setImagesURL(String imagesURL);

	/**
	 * @return the cssURL
	 */
	public String getCssURL();

	/**
	 * @param cssURL the cssURL to set
	 */
	public void setCssURL(String cssURL);

	/**
	 * @return the jsURL
	 */
	public String getJsURL();

	/**
	 * @param jsURL the jsURL to set
	 */
	public void setJsURL(String jsURL);

	/**
	 * @return the menuPosition
	 */
	public int getMenuPosition();

	/**
	 * @param menuPosition the menuPosition to set
	 */
	public void setMenuPosition(int menuPosition);

    /**
	 * @return the livepreviewPosition
	 */
    public int getLivepreviewPosition();

    /**
	 * @param livepreviewPosition the livepreviewPosition to set
	 */
    public void setLivepreviewPosition(int livepreviewPosition);

}
