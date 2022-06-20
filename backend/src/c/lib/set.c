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
# include	<ctype.h>
# include	"agn.h"

static sentry_t *
sentry_free (sentry_t *s) /*{{{*/
{
	if (s) {
		if (s -> name)
			free (s -> name);
		free (s);
	}
	return NULL;
}/*}}}*/
static sentry_t *
sentry_free_all (sentry_t *s) /*{{{*/
{
	sentry_t	*tmp;
	
	while (tmp = s) {
		s = s -> next;
		sentry_free (tmp);
	}
	return NULL;
}/*}}}*/
static sentry_t *
sentry_alloc (const char *name, int nlen, hash_t hash) /*{{{*/
{
	sentry_t	*s;
	
	if (s = (sentry_t *) malloc (sizeof (sentry_t)))
		if (s -> name = malloc (nlen + 1)) {
			if (nlen > 0)
				memcpy (s -> name, name, nlen);
			s -> name[nlen] = 0;
			s -> nlen = nlen;
			s -> hash = hash;
			s -> next = NULL;
		} else {
			free (s);
			s = NULL;
		}
	return s;
}/*}}}*/
static inline bool_t
finder (set_t *s, const char *name, int nlen, hash_t hash) /*{{{*/
{
	sentry_t	*run;
	
	for (run = s -> s[hash % s -> hsize]; run; run = run -> next)
		if (hash_smatch (run -> name, run -> nlen, run -> hash, name, nlen, hash, s -> icase))
			return true;
	return false;
}/*}}}*/
set_t *
set_alloc (bool_t icase, int aproxsize) /*{{{*/
{
	set_t	*s;
	
	if (s = (set_t *) malloc (sizeof (set_t))) {
		s -> icase = icase;
		s -> hsize = hash_size (aproxsize);
		s -> count = 0;
		if (s -> s = (sentry_t **) malloc (s -> hsize * sizeof (sentry_t))) {
			int	n;
			
			for (n = 0; n < s -> hsize; ++n)
				s -> s[n] = NULL;
		} else {
			free (s);
			s = NULL;
		}
	}
	return s;
}/*}}}*/
set_t *
set_free (set_t *s) /*{{{*/
{
	if (s) {
		if (s -> s) {
			int	n;
			
			for (n = 0; n < s -> hsize; ++n)
				sentry_free_all (s -> s[n]);
			free (s -> s);
		}
		free (s);
	}
	return NULL;
}/*}}}*/
bool_t
set_add (set_t *s, const char *name, int nlen) /*{{{*/
{
	bool_t	rc = true;
	hash_t	hash = hash_svalue (name, nlen, s -> icase);
	
	if (! finder (s, name, nlen, hash)) {
		sentry_t	*e = sentry_alloc (name, nlen, hash);
		
		if (e) {
			int	idx = hash % s -> hsize;
			
			e -> next = s -> s[idx];
			s -> s[idx] = e;
			s -> count++;
		} else
			rc = false;
	}
	return rc;
}/*}}}*/
void
set_remove (set_t *s, const char *name, int nlen) /*{{{*/
{
	hash_t		hash;
	sentry_t	*run, *prv;
	int		idx;
	
	hash = hash_svalue (name, nlen, s -> icase);
	idx = hash % s -> hsize;
	for (run = s -> s[idx], prv = NULL; run; run = run -> next)
		if (hash_smatch (run -> name, run -> nlen, run -> hash, name, nlen, hash, s -> icase))
			break;
	if (run) {
		if (prv)
			prv -> next = run -> next;
		else
			s -> s[idx] = run -> next;
		sentry_free (run);
		s -> count--;
	}
}/*}}}*/
bool_t
set_find (set_t *s, const char *name, int nlen) /*{{{*/
{
	return finder (s, name, nlen, hash_svalue (name, nlen, s -> icase));
}/*}}}*/
