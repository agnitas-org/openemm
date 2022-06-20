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
# include	<stdlib.h>
# include	"xmlback.h"

dcache_t *
dcache_alloc (const char *name, const dyn_t *dyn) /*{{{*/
{
	dcache_t	*d;
	
	if (d = (dcache_t *) malloc (sizeof (dcache_t))) {
		d -> name = name;
		d -> dyn = dyn;
		d -> next = NULL;
	}
	return d;
}/*}}}*/
dcache_t *
dcache_free (dcache_t *d) /*{{{*/
{
	if (d)
		free (d);
	return NULL;
}/*}}}*/
dcache_t *
dcache_free_all (dcache_t *d) /*{{{*/
{
	dcache_t	*tmp;
	
	while (tmp = d) {
		d = d -> next;
		dcache_free (tmp);
	}
	return NULL;
}/*}}}*/
