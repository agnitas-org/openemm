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

blockspec_t *
blockspec_alloc (void) /*{{{*/
{
	blockspec_t	*b;
	
	if (b = (blockspec_t *) malloc (sizeof (blockspec_t))) {
		b -> nr = -1;
		b -> block = NULL;
		b -> prefix = fix_alloc ();
		DO_ZERO (b, postfix);
		b -> linelength = 0;
		b -> opl = Add_None;
		b -> clearance = false;
		if (! b -> prefix)
			b = blockspec_free (b);
	}
	return b;
}/*}}}*/
blockspec_t *
blockspec_free (blockspec_t *b) /*{{{*/
{
	if (b) {
		if (b -> prefix)
			fix_free (b -> prefix);
		DO_FREE (b, postfix);
		free (b);
	}
	return NULL;
}/*}}}*/
