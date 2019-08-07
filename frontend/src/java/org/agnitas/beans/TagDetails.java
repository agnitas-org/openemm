/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
    public String findTagName();

    /**
     * Search for tagParameters in fullText.
     */
    public boolean findTagParameters();

    /**
     * Getter for property endPos.
     * 
     * @return Value of property endPos.
     */
    public int getEndPos();

    /**
     * Getter for property fullText.
     * 
     * @return Value of property fullText.
     */
    public String getFullText();

    /**
     * Getter for property name.
     * 
     * @return Value of property name.
     */
    public String getName();

    /**
     * Getter for property startPos.
     * 
     * @return Value of property startPos.
     */
    public int getStartPos();

    /**
     * Getter for property tagName.
     * 
     * @return Value of property tagName.
     */
    public String getTagName();

    /**
     * Getter for property tagParameters.
     * 
     * @return Value of property tagParameters.
     */
    public Map<String, String> getTagParameters();

    /**
     * Setter for property tagName.
     * 
     * @param tagName New value of property tagName.
     */
    public void setTagName(String tagName);

    /**
     * Setter for property tagParameters.
     * 
     * @param tagParameters New value of property tagParameters.
     */
    public void setTagParameters(Map<String, String> tagParameters);

    /**
     * Setter for property startPos.
     *
     * @param startPos New value of property startPos.
     */
    public void setStartPos(int startPos);

    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     */
    public void setName(java.lang.String name);

    /**
     * Setter for property fullText.
     *
     * @param fullText New value of property fullText.
     */
    public void setFullText(java.lang.String fullText);

    /**
     * Setter for property endPos.
     *
     * @param endPos New value of property endPos.
     */
    public void setEndPos(int endPos);
}
