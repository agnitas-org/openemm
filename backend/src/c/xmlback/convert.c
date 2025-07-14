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
# include	<stdlib.h>
# include	"xmlback.h"

bool_t
convert_character_set (blockmail_t *blockmail, block_t *block) /*{{{*/
{
	const buffer_t	*temp;
	
	if (block -> convert && xmlBufferLength (block -> in) && (temp = convert_encode (block -> convert, xmlBufferContent (block -> in), xmlBufferLength (block -> in)))) {
		xmlBufferEmpty (block -> out);
		xmlBufferAdd (block -> out, buffer_content (temp), buffer_length (temp));
	} else
		block_swap_inout (block);
	return true;
}/*}}}*/
