/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.stat;

public interface URLStatEntry extends Comparable<URLStatEntry> {
    /**
     * Getter for property clicks.
     *
     * @return Value of property clicks.
     */
    int getClicks();

    /**
     * Getter for property clicksNetto.
     *
     * @return Value of property clicksNetto.
     */
    int getClicksNetto();

    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    String getShortname();

    /**
     * Getter for property url.
     *
     * @return Value of property url.
     */
    String getUrl();

    /**
     * Getter for property urlID.
     *
     * @return Value of property urlID.
     */
    int getUrlID();

     /**
     * Setter for property clicks.
     *
     * @param clicks New value of property clicks.
     */
    void setClicks(int clicks);

    /**
     * Setter for property clicksNetto.
     *
     * @param clicksNetto New value of property clicksNetto.
     */
    void setClicksNetto(int clicksNetto);

    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    void setShortname(String shortname);

    /**
     * Setter for property url.
     *
     * @param url New value of property url.
     */
    void setUrl(String url);

    /**
     * Setter for property urlID.
     *
     * @param urlID New value of property urlID.
     */
    void setUrlID(int urlID);
    
}
