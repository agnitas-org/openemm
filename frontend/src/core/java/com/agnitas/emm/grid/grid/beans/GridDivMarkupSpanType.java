/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.grid.grid.beans;

public enum GridDivMarkupSpanType {
    /** A static text between placeholders (not a placeholder). */
    Text,

    /**
     * Either a placeholder area (when marked up as opening and closing tags)
     * or a placeholder itself (when marked up as a standalone tag or value tag).
     */
    Placeholder,

    /** TOC area (to be repeated for each TOC item). */
    TocArea,

    /** A placeholder whose value (id, name, etc) is provided by a TOC item (some div-child). */
    TocPlaceholder
}
