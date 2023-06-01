/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Map;

public interface TagDetails {
    /**
     * Search for tagName in fullText.
     *
     * @return Value of found tagName
     */
    String findTagName();

    /**
     * Search for tagParameters in fullText.
     */
    boolean findTagParameters();

    /**
     * Getter for property endPos.
     * 
     * @return Value of property endPos.
     */
    int getEndPos();

    /**
     * Getter for property fullText.
     * 
     * @return Value of property fullText.
     */
    String getFullText();

    /**
     * Getter for property name.
     * 
     * @return Value of property name.
     */
    String getName();

    /**
     * Getter for property startPos.
     * 
     * @return Value of property startPos.
     */
    int getStartPos();

    /**
     * Getter for property tagName.
     * 
     * @return Value of property tagName.
     */
    String getTagName();

    /**
     * Getter for property tagParameters.
     * 
     * @return Value of property tagParameters.
     */
    Map<String, String> getTagParameters();

    /**
     * Setter for property tagName.
     * 
     * @param tagName New value of property tagName.
     */
    void setTagName(String tagName);

    /**
     * Setter for property tagParameters.
     * 
     * @param tagParameters New value of property tagParameters.
     */
    void setTagParameters(Map<String, String> tagParameters);

    /**
     * Setter for property startPos.
     *
     * @param startPos New value of property startPos.
     */
    void setStartPos(int startPos);

    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     */
    void setName(java.lang.String name);

    /**
     * Setter for property fullText.
     *
     * @param fullText New value of property fullText.
     */
    void setFullText(java.lang.String fullText);

    /**
     * Setter for property endPos.
     *
     * @param endPos New value of property endPos.
     */
    void setEndPos(int endPos);
}
