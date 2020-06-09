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
/** @file var.c
 * Handling for variable/value pairs.
 * This modules adds support for linked list of variable/value
 * pairs, memory handling and matching
 */
# include	<stdlib.h>
# include	<string.h>
# include	"agn.h"

/** Allocate a pair.
 * An instance for var_t is allocated and a copy of parameter
 * var and val is made (if not NULL) for the member variables
 * @param var the name of the variable
 * @param val the value
 * @return the new instance on success, NULL otherwise
 */
var_t *
var_alloc (const char *var, const char *val) /*{{{*/
{
	var_t	*v;
	
	if (v = (var_t *) malloc (sizeof (var_t))) {
		v -> var = NULL;
		v -> val = NULL;
		v -> next = NULL;
		if ((var && (! (v -> var = strdup (var)))) ||
		    (val && (! (v -> val = strdup (val)))))
			v = var_free (v);
	}
	return v;
}/*}}}*/
/** Frees a pair.
 * Returns the used memory of the instance to the system.
 * @param v the instance to use
 * @return NULL
 */
var_t *
var_free (var_t *v) /*{{{*/
{
	if (v) {
		if (v -> var)
			free (v -> var);
		if (v -> val)
			free (v -> val);
		free (v);
	}
	return NULL;
}/*}}}*/
/** Free the linked list.
 * Returns the used memory of the instance and all linked instances
 * to the system.
 * @param v the instance to start from
 * @return NULL
 * @see var_free
 */
var_t *
var_free_all (var_t *v) /*{{{*/
{
	var_t	*tmp;
	
	while (tmp = v) {
		v = v -> next;
		var_free (tmp);
	}
	return NULL;
}/*}}}*/
/** Set the variable name.
 * Sets/replaces the name of the variable.
 * @param v the instance to use
 * @param var the new name of the variable
 * @return true on success, false otherwise
 */
bool_t
var_variable (var_t *v, const char *var) /*{{{*/
{
	if (v -> var)
		free (v -> var);
	v -> var = var ? strdup (var) : NULL;
	return (! var) || v -> var ? true : false;
}/*}}}*/
/** Set the value.
 * Sets/replaces the value.
 * @param v the instance to use
 * @param val the new value
 * @return true on success, false otherwise
 */
bool_t
var_value (var_t *v, const char *val) /*{{{*/
{
	if (v -> val)
		free (v -> val);
	v -> val = val ? strdup (val) : NULL;
	return (! val) || v -> val ? true : false;
}/*}}}*/
static
# ifdef		__OPTIMIZE__
inline
# endif		/* __OPTIMIZE__ */
bool_t
do_match (var_t *v, const char *var, int (*func) (const char *, const char *)) /*{{{*/
{
	if (((! v -> var) && (! var)) ||
	    (v -> var && var && (! (*func) (v -> var, var))))
		return true;
	return false;
}/*}}}*/
/** Compare instance to variable.
 * Comapres the instance variable name to the given variable name
 * if the match
 * @param v the instance
 * @param var the variable name
 * @return true if they are equal, false otherwise
 */
bool_t
var_match (var_t *v, const char *var) /*{{{*/
{
	return do_match (v, var, strcmp);
}/*}}}*/
/** Compare instance to variable case insensitive.
 * Like <i>var_match</i> but ignores case
 * @param v the instance
 * @param var the variable name
 * @return true if they are equal ignoring case, false otherwise
 * @see var_match
 */
bool_t
var_imatch (var_t *v, const char *var) /*{{{*/
{
	return do_match (v, var, strcasecmp);
}/*}}}*/
static
# ifdef		__OPTIMIZE__
inline
# endif		/* __OPTIMIZE__ */
bool_t
do_part_match (var_t *v, const char *var, int (*func) (const char *, const char *, size_t)) /*{{{*/
{
	if ((! v -> var) && (! var))
		return true;
	if (v -> var && var) {
		int	v1 = strlen (v -> var),
			v2 = strlen (var);
		
		if ((v1 <= v2) && (! (*func) (v -> var, var, v1)))
			return true;
	}
	return false;
}/*}}}*/
/** Compare instance partial to variable.
 * Checks the instance variable name if it starts with the given
 * variable name prefix.
 * @param v the instance
 * @param var the variable name prefix
 * @return true if the instance variable name starts with var, false otherwise
 */
bool_t
var_partial_match (var_t *v, const char *var) /*{{{*/
{
	return do_part_match (v, var, strncmp);
}/*}}}*/
/** Compare instance partial to variable case insensitive.
 * Like <i>var_partial_match</i> but ignores case
 * @param v the instance
 * @param var the variable name prefix
 * @return true if the instance variable name starts with var ignoring case, false otherwise
 * @see var_partial_match
 */
bool_t
var_partial_imatch (var_t *v, const char *var) /*{{{*/
{
	return do_part_match (v, var, strncasecmp);
}/*}}}*/
static
# ifdef		__OPTIMIZE__
inline
# endif		/* __OPTIMIZE__ */
var_t *
do_find (var_t *v, const char *var, bool_t (*match) (var_t *, const char *)) /*{{{*/
{
	for (; v; v = v -> next)
		if ((*match) (v, var))
			break;
	return v;
}/*}}}*/
var_t *
var_find (var_t *v, const char *var) /*{{{*/
{
	return do_find (v, var, var_match);
}/*}}}*/
var_t *
var_ifind (var_t *v, const char *var) /*{{{*/
{
	return do_find (v, var, var_imatch);
}/*}}}*/
var_t *
var_partial_find (var_t *v, const char *var) /*{{{*/
{
	return do_find (v, var, var_partial_match);
}/*}}}*/
var_t *
var_partial_ifind (var_t *v, const char *var) /*{{{*/
{
	return do_find (v, var, var_partial_imatch);
}/*}}}*/
