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
# include	<ctype.h>
# include	"xmlback.h"

# define	VALIDATOR		"spf-check"
# define	CATEGORY		"spf-check"
# define	EXPIRE			(24 * 60 * 60)
# define	PASS			"pass"

typedef struct { /*{{{*/
	char	*validator;		/* external validator		*/
	fsdb_t	*fsdb;			/* caching database		*/
	cache_t	*cache;			/* cache for results		*/
	/*}}}*/
}	spf_t;

void *
spf_alloc (void) /*{{{*/
{
	spf_t	*s;
	
	if (s = (spf_t *) malloc (sizeof (spf_t))) {
		s -> validator = which (VALIDATOR);
		s -> fsdb = fsdb_alloc (NULL);
		s -> cache = cache_alloc (1024);
	}
	return s;
}/*}}}*/
void *
spf_free (void *sp) /*{{{*/
{
	spf_t	*s = (spf_t *) sp;
	
	if (s) {
		if (s -> validator)
			free (s -> validator);
		if (s -> fsdb)
			fsdb_free (s -> fsdb);
		if (s -> cache)
			cache_free (s -> cache);
		free (s);
	}
	return NULL;
}/*}}}*/
bool_t
spf_is_valid (void *sp, const char *address) /*{{{*/
{
	spf_t	*s = (spf_t *) sp;
	bool_t	rc = false;
	
	if (s && s -> validator && s -> fsdb) {
		char		key[sizeof (CATEGORY) + 256];
		char		*target;
		const char	*domain;
		int		used;
		time_t		now;
		int		round;
		bool_t		found;

		memcpy (key, CATEGORY, sizeof (CATEGORY) - 1);
		target = key + sizeof (CATEGORY) - 1;
		*target++ = ':';
		if (domain = strrchr (address, '@'))
			++domain;
		else
			domain = address;
		for (used = target - key; *domain && (used + 1 < sizeof (key)); ++used)
			*target++ = tolower (*domain++);
		*target = '\0';
		if (s -> cache) {
			centry_t	*entry = cache_find (s -> cache, (const byte_t *) key, target - key);
			
			if (entry)
				return entry -> data && (entry -> dlen == 1) && (((char *) entry -> data)[0] == '+');
		}
		time (& now);
		for (round = 0, found = false; (round < 2) && (! found); ++round) {
			fsdb_result_t	*result = fsdb_get (s -> fsdb, key);
			
			if (result) {
				if (result -> updated + EXPIRE > now) {
					if (result -> value && (result -> vlen == sizeof (PASS) - 1) && (! strncmp (result -> value, PASS, sizeof (PASS) - 1)))
						rc = true;
					found = true;
				}
				fsdb_result_free (result);
			}
			if ((! found) && (round == 0)) {
				call (s -> validator, address, NULL);
			}
		}
		if (s -> cache) {
			cache_add (s -> cache, (const byte_t *) key, target - key, (const byte_t *) (rc ? "+" : "-"), 1);
		}
	}
	return rc;
}/*}}}*/
