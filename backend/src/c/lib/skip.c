/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
/** @file skip.c
 * Skip over whitespaces.
 */
# include	<ctype.h>
# include	"agn.h"

/** Skip over whitespaces.
 * Forwards the pointer up to the next whitespace, overwrite
 * it with nul byte and move pointer up to next non whitespace
 * character
 * @param str the string to use
 * @return the new pointer position
 */
char *
skip (char *str) /*{{{*/
{
	while (*str && (! isspace ((int) ((unsigned char) *str))))
		++str;
	if (*str) {
		*str++ = '\0';
		while (isspace ((int) ((unsigned char) *str)))
			++str;
	}
	return str;
}/*}}}*/
