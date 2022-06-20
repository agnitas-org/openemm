/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

/**
 * container class for start/end values of found tags in a parsed block
 */
public class TagPos {
	/**
	 * its start position
	 */
	private int start;
	/**
	 * its end position
	 */
	private int end;
	/**
	 * the name of the tag
	 */
	private String tagname;
	/**
	 * the tag with stripped of [ .. ]
	 */
	private String tagid;
	/**
	 * if this is a simple tag
	 */
	private boolean simpleTag;
	/**
	 * the content if this is dynamic
	 */
	private BlockData content;

	/**
	 * Constructor
	 *
	 * @param start   start position of tag
	 * @param end     end position of tag
	 * @param tagname the full tagname
	 */
	public TagPos(int start, int end, String tagname) {
		this.start = start;
		this.tagname = tagname;
		this.end = end;
		tagid = null;
		simpleTag = false;
		content = null;

		checkTagname();
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getTagid() {
		return tagid;
	}

	public String getTagname() {
		return tagname;
	}

	public BlockData getContent() {
		return content;
	}

	public void setContent(BlockData bd) {
		content = bd;
	}

	public boolean isSimpleTag() {
		return simpleTag;
	}

	/**
	 * Modifiy position of tag in content due to external
	 * modification of the content
	 *
	 * @param offset the value to modify the position of the tag
	 */
	public void relocateBy(int offset) {
		start += offset;
		end += offset;
	}

	/**
	 * Checks if this is the agnDYN tag
	 *
	 * @return true, it this is the case
	 */
	public boolean isDynamic() {
		return tagid.equals(EMMTag.TAG_INTERNALS[EMMTag.TI_DYN]) || isGridPH();
	}

	private boolean isGridPH() {
		return tagid.equals(EMMTag.TAG_INTERNALS[EMMTag.TI_GRIDPH]);
	}

	/**
	 * Checks if this is the agnDVALUE tag
	 *
	 * @return true, it this is the case
	 */
	public boolean isDynamicValue() {
		return tagid.equals(EMMTag.TAG_INTERNALS[EMMTag.TI_DYNVALUE]) || (isDynamic() && simpleTag);
	}

	/**
	 * extract the tagid from the tagname
	 */
	private void checkTagname() {
		int len = tagname.length();

		if (tagname.endsWith("/]")) {
			simpleTag = true;
		}

		int n;

		for (n = 1; n < len - 1; ++n) {
			char c = tagname.charAt(n);
			if (c == ' ' || c == '/' || c == ']') {
				break;
			}
		}
		tagid = tagname.substring(1, n);
		if (isGridPH()) {
			// Always standalone.
			simpleTag = true;
		}
	}
}
