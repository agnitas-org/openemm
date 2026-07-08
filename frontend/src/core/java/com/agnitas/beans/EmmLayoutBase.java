/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

public interface EmmLayoutBase {
    int MENU_POSITION_LEFT = 0;
    int MENU_POSITION_TOP = 1;
    int MENU_POSITION_DEFAULT = MENU_POSITION_LEFT;

    int LIVEPREVIEW_POSITION_RIGHT = 0;
    int LIVEPREVIEW_POSITION_BOTTOM = 1;
    int LIVEPREVIEW_POSITION_DEACTIVATE = 2;

    enum ThemeType {
    	LIGHT(0, "light"),
		DARK(1, "dark"),
		LIGHT_CONTRAST(2, "light-contrast"),
		DARK_CONTRAST(3, "dark-contrast");

    	private final int code;
		private final String name;

		ThemeType(int code, String name) {
    		this.code = code;
            this.name = name;
        }

		public int getCode() {
    		return this.code;
		}

		public String getName() {
			return name;
		}

		public static ThemeType valueOf(final int code) {
    		for(ThemeType type : values()) {
    			if(type.getCode() == code) {
    				return type;
				}
			}
			throw new IllegalArgumentException("Unsupported code of EmmLayoutBase type!");
		}
	}

	/**
	 * @return the id
	 */
	int getId();

	/**
	 * @param id the id to set
	 */
	void setId(int id);

	/**
	 * @return the baseURL
	 */
	String getBaseURL();

	/**
	 * @param baseURL the baseURL to set
	 */
	void setBaseURL(String baseURL);

	/**
	 * @return the imagesURL
	 */
	String getImagesURL();

	/**
	 * @param imagesURL the imagesURL to set
	 */
	void setImagesURL(String imagesURL);

	/**
	 * @return the cssURL
	 */
	String getCssURL();

	/**
	 * @param cssURL the cssURL to set
	 */
	void setCssURL(String cssURL);

	/**
	 * @return the jsURL
	 */
	String getJsURL();

	/**
	 * @param jsURL the jsURL to set
	 */
	void setJsURL(String jsURL);

	/**
	 * @return the menuPosition
	 */
	int getMenuPosition();

	/**
	 * @param menuPosition the menuPosition to set
	 */
	void setMenuPosition(int menuPosition);

    /**
	 * @return the livepreviewPosition
	 */
	int getLivepreviewPosition();

    /**
	 * @param livepreviewPosition the livepreviewPosition to set
	 */
	void setLivepreviewPosition(int livepreviewPosition);

	/**
	 * @return the themeType
	 */
	ThemeType getThemeType();

	/**
	 * @param themeType the themeType to set
	 */
	void setThemeType(ThemeType themeType);
}
