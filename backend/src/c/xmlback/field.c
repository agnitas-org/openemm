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
# include	<stdlib.h>
# include	<ctype.h>
# include	"xmlback.h"

field_t *
field_alloc (void) /*{{{*/
{
	field_t	*f;
	
	if (f = (field_t *) malloc (sizeof (field_t))) {
		f -> name = NULL;
		f -> lname = NULL;
		f -> ref = NULL;
		f -> uref = NULL;
		f -> rname = NULL;
		f -> type = '\0';
	}
	return f;
}/*}}}*/
field_t *
field_free (field_t *f) /*{{{*/
{
	if (f) {
		if (f -> name)
			free (f -> name);
		if (f -> lname)
			free (f -> lname);
		if (f -> ref)
			free (f -> ref);
		if (f -> uref)
			free (f -> uref);
		if (f -> rname)
			free (f -> rname);
		free (f);
	}
	return NULL;
}/*}}}*/
bool_t
field_normalize_name (field_t *f) /*{{{*/
{
	if (f -> lname)
		free (f -> lname);
	f -> lname = f -> name ? strldup (f -> name) : NULL;
	if (f -> uref)
		free (f -> uref);
	f -> uref = f -> ref ? strudup (f -> ref) : NULL;
	if (f -> ref && (! f -> uref))
		return false;
	if (f -> rname)
		free (f -> rname);
	f -> rname = f -> ref ? strlcat (f -> ref, ".", f -> lname, NULL) : NULL;
	if (f -> ref && (! f -> rname))
		return false;
	return f -> lname || (! f -> name) ? true : false;
}/*}}}*/
