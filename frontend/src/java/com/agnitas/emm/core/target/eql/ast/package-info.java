/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/**
 * This package contains classes representing nodes in the syntax tree.
 *
 * The nodes can be considered as instructions of a virtual machine. The syntax tree
 * will be used by the code generators when generating code for the target language.
 * 
 * The syntax tree can be broken into three levels:
 * <ol>
 *   <li><i>boolean level</i>: This is the top-most level and contains the boolean operators like AND.</li>
 *   <li><i>relational level</i>: In this level, relational operator (like &lt;) can be found. It acts as a bridge between boolean and arithmetic expressions.</li>
 *   <li><i>arithmetic (expressional) level</i>: In this level, arithmetic expressions (like +, -) are located.</li>
 *   <li><i>atomic level</i>: Here are constants, names, strings, etc. located.</li>
 * </ol>
 *
 */
package com.agnitas.emm.core.target.eql.ast;


