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

links_t *
links_alloc (void) /*{{{*/
{
	links_t	*l;
	
	if (l = (links_t *) malloc (sizeof (links_t))) {
		l -> l = NULL;
		l -> seen = NULL;
		l -> lcnt = 0;
		l -> lsiz = 0;
	}
	return l;
}/*}}}*/
links_t *
links_free (links_t *l) /*{{{*/
{
	if (l) {
		if (l -> l) {
			int	n;
			
			for (n = 0; n < l -> lcnt; ++n)
				if (l -> l[n])
					free (l -> l[n]);
			free (l -> l);
		}
		if (l -> seen)
			free (l -> seen);
		free (l);
	}
	return NULL;
}/*}}}*/
bool_t
links_expand (links_t *l) /*{{{*/
{
	if (l -> lcnt >= l -> lsiz) {
		int	nsiz;
		char	**temp;
		
		nsiz = l -> lsiz ? l -> lsiz * 2 : 32;
		if (temp = (char **) realloc (l -> l, nsiz * sizeof (char *))) {
			l -> l = temp;
			l -> seen = (bool_t *) realloc (l -> seen, nsiz * sizeof (bool_t));
			l -> lsiz = nsiz;
		}
	}
	return l -> lcnt < l -> lsiz ? true : false;
}/*}}}*/
bool_t
links_nadd (links_t *l, const char *lnk, int llen) /*{{{*/
{
	bool_t	st;
	int	n, len;
	
	st = false;
	for (n = 0; n < l -> lcnt; ++n)
		if (((len = strlen (l -> l[n])) == llen) && (! memcmp (l -> l[n], lnk, len)))
			break;
	if (n < l -> lcnt)
		st = true;
	else if (links_expand (l) && (l -> l[l -> lcnt] = malloc (llen + 1))) {
		memcpy (l -> l[l -> lcnt], lnk, llen);
		l -> l[l -> lcnt][llen] = '\0';
		if (l -> seen)
			l -> seen[l -> lcnt] = false;
		l -> lcnt++;
		st = true;
	}
	return st;
}/*}}}*/

