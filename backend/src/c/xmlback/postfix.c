/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
/*	-*- mode: c; mode: fold -*-	*/
# include	<stdlib.h>
# include	"xmlback.h"

postfix_t *
postfix_alloc (void) /*{{{*/
{
	postfix_t	*p;
	
	if (p = (postfix_t *) malloc (sizeof (postfix_t)))
		if (p -> c = fix_alloc ()) {
			p -> pid = NULL;
			p -> after = -1;
			p -> ref = NULL;
			p -> stack = NULL;
		} else {
			free (p);
			p = NULL;
		}
	return p;
}/*}}}*/
postfix_t *
postfix_free (postfix_t *p) /*{{{*/
{
	if (p) {
		if (p -> c)
			fix_free (p -> c);
		if (p -> pid)
			free (p -> pid);
		free (p);
	}
	return NULL;
}/*}}}*/
