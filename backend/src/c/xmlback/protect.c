/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
# include	"xmlback.h"

protect_t *
protect_alloc (void) /*{{{*/
{
	protect_t	*p;
	
	if (p = (protect_t *) malloc (sizeof (protect_t))) {
		p -> start = -1;
		p -> end = -1;
		p -> next = NULL;
	}
	return p;
}/*}}}*/
protect_t *
protect_free (protect_t *p) /*{{{*/
{
	if (p) {
		free (p);
	}
	return NULL;
}/*}}}*/
protect_t *
protect_free_all (protect_t *p) /*{{{*/
{
	protect_t	*tmp;
	
	while (tmp = p) {
		p = p -> next;
		protect_free (tmp);
	}
	return NULL;
}/*}}}*/
