/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                              *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.                                                                                                       *
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

static const char
in_cache (spf_t *spf, const char *address, char **domain, int *dlength) /*{{{*/
{
	*domain = NULL;
	*dlength = 0;
	if (spf -> cache) {
		const char	*domain_part;
		int		domain_length;
		char		*pattern;
		
		if (domain_part = strrchr (address, '@'))
			++domain_part;
		else
			domain_part = address;
		domain_length = strlen (domain_part);
		if (pattern = malloc (domain_length + 1)) {
			const char	*source;
			char		*target;
			centry_t	*entry;
			
			for (source = domain_part, target = pattern; *source; )
				*target++ = tolower (*source++);
			*target = '\0';
			if (entry = cache_find (spf -> cache, (const byte_t *) pattern, domain_length)) {
				free (pattern);
				return entry -> data && (entry -> dlen == 1) ? entry -> data[0] : '-';
			}
			*domain = pattern;
			*dlength = domain_length;
		}
	}
	return '\0';
}/*}}}*/
static void
to_cache (spf_t *spf, char *domain, int dlength, bool_t pass) /*{{{*/
{
	if (domain) {
		if (spf -> cache)
			cache_add (spf -> cache, (const byte_t *) domain, dlength, (const byte_t *) (pass ? "+" : "-"), 1);
		free (domain);
	}
}/*}}}*/

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
static bool_t
legacy_spf_is_valid (spf_t *spf, const char *address) /*{{{*/
{
	bool_t	rc = false;
	
	if (spf && spf -> validator && spf -> fsdb) {
		char		key[sizeof (CATEGORY) + 256];
		time_t		now;
		int		round;
		bool_t		found;

		time (& now);
		for (round = 0, found = false; (round < 2) && (! found); ++round) {
			fsdb_result_t	*result = fsdb_get (spf -> fsdb, key);
			
			if (result) {
				if (result -> updated + EXPIRE > now) {
					if (result -> value && (result -> vlen == sizeof (PASS) - 1) && (! strncmp (result -> value, PASS, sizeof (PASS) - 1)))
						rc = true;
					found = true;
				}
				fsdb_result_free (result);
			}
			if ((! found) && (round == 0)) {
				call (spf -> validator, address, NULL);
			}
		}
	}
	return rc;
}/*}}}*/
bool_t
spf_is_valid (blockmail_t *blockmail, const char *address) /*{{{*/
{
	spf_t		*spf = (spf_t *) blockmail -> spf;
	bool_t		rc = false;
	bool_t		success = false;
	char		*domain;
	int		dlength;
	char		cached;
	hparam_t	*request;

	if (cached = in_cache (spf, address, & domain, & dlength))
		return cached == '+' ? true : false;
	if (request = hparam_alloc ()) {
		hparam_t	*answer;
		const char	*response;
		
		hparam_add_string (request, "address", -1, address);
		if (answer = helper_call (blockmail, "spfcheck", request)) {
			if (hparam_get_boolean (answer, "status") && (response = hparam_get_string (answer, "response"))) {
				success = true;
				if (! strcmp (response, PASS))
					rc = true;
			}
			hparam_free (answer);
		}
		hparam_free (request);
	}
	if (! success)
		rc = legacy_spf_is_valid (spf, address);
	if (spf -> cache && domain)
		to_cache (spf, domain, dlength, rc);
	return rc;
}/*}}}*/
