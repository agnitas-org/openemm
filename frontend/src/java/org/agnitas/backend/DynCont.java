/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

/**
 * Holds all information about one dynamic content block
 */
public class DynCont {
	/**
	 * constant for always matching
	 */
	public static final long MATCH_NEVER = -1;
	/**
	 * constant for never matching
	 */
	public static final long MATCH_ALWAYS = 0;

	/**
	 * Unique content ID
	 */
	public long id = 0;
	/**
	 * ID for the target condiition
	 */
	public long targetID = 0;
	/**
	 * order to describe importance of this part
	 */
	public long order = 0;
	/**
	 * textual content
	 */
	public BlockData text = null;
	/**
	 * HTML content
	 */
	public BlockData html = null;
	/**
	 * the condition
	 */
	public String condition = null;

	/**
	 * Constructor
	 *
	 * @param dynContId  the unique ID
	 * @param dynTarget  the optional target id
	 * @param dynOrder   the order value
	 * @param dynContent the content of the block
	 */
	public DynCont(long dynContId, long dynTarget, long dynOrder, String dynContent) {
		id = dynContId;
		targetID = dynTarget;
		order = dynOrder;
		text = new BlockData(StringOps.removeHTMLTagsAndEntities(dynContent), null, null, BlockData.TEXT, 0, 0, "text/plain", true, true, false, false, false, false);
		html = new BlockData(dynContent, null, null, BlockData.HTML, 0, 0, "text/html", true, true, false, false, false, false);
		condition = null;
	}

	public DynCont() {
		condition = null;
	}
}
