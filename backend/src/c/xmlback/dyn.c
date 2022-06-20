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
# include	<fnmatch.h>
# include	"xmlback.h"

dyn_t *
dyn_alloc (int did, int order) /*{{{*/
{
	dyn_t	*d;
	
	if (d = (dyn_t *) malloc (sizeof (dyn_t))) {
		d -> did = did;
		d -> name = NULL;
		d -> interest = NULL;
		d -> interest_index = -1;
		d -> disable_link_extension = false;
		d -> order = order;
		d -> condition = NULL;
		d -> target_id = 0;
		d -> target_index = -1;
		DO_ZERO (d, block);
		d -> sibling = NULL;
		d -> next = NULL;
	}
	return d;
}/*}}}*/
dyn_t *
dyn_free (dyn_t *d) /*{{{*/
{
	if (d) {
		if (d -> name)
			free (d -> name);
		if (d -> interest)
			free (d -> interest);
		if (d -> condition)
			xmlBufferFree (d -> condition);
		DO_FREE (d, block);
		if (d -> sibling)
			dyn_free_all (d -> sibling);
		free (d);
	}
	return NULL;
}/*}}}*/
dyn_t *
dyn_free_all (dyn_t *d) /*{{{*/
{
	dyn_t	*tmp;
	
	while (tmp = d) {
		d = d -> next;
		dyn_free (tmp);
	}
	return NULL;
}/*}}}*/
bool_t
dyn_match (const dyn_t *d, eval_t *eval, receiver_t *rec) /*{{{*/
{
	/* trivial case */
	if (! d -> condition) {
		return d -> target_index != -1 ? dataset_match (rec -> rvdata, d -> target_index) : true;
	}
	return eval_match (eval, SP_DYNAMIC, d -> did);
}/*}}}*/

bool_t
dyn_assign_interest_field (dyn_t *d, field_t **fields, int fcount) /*{{{*/
{
	int	n;
	
	for (n = 0; n < fcount; ++n)
		if ((fields[n] -> type == 'n') && (! strcasecmp (d -> interest, fields[n] -> name))) {
			d -> interest_index = n;
			break;
		}
	return n < fcount ? true : false;
}/*}}}*/
bool_t
dyn_match_selector (const dyn_t *dyn, const char *selector) /*{{{*/
{
	return (! selector) || (fnmatch (selector, dyn -> name, FNM_NOESCAPE) == 0) ? true : false;
}/*}}}*/
