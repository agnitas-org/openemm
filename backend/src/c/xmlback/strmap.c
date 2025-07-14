/********************************************************************************************************************************************************************************************************************************************************************
 *                                                                                                                                                                                                                                                                  *
 *                                                                                                                                                                                                                                                                  *
 *        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   *
 *                                                                                                                                                                                                                                                                  *
 *        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    *
 *        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           *
 *        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            *
 *                                                                                                                                                                                                                                                                  *
 ********************************************************************************************************************************************************************************************************************************************************************/
# include	<string.h>
# include	<regex.h>
# include	"xmlback.h"

typedef struct { /*{{{*/
	const xmlChar	*data;
	int		dlen;
	/*}}}*/
}	loc_t;
static loc_t
locate (map_t **maps, int mcount, const xmlChar *pattern, int len) /*{{{*/
{
	loc_t	rc;
	
	if ((len > 1) && ((pattern[0] == '"') || (pattern[0] == '\'')) && (pattern[len - 1] == pattern[0])) {
		rc.data = pattern + 1;
		rc.dlen = len - 2;
	} else {
		int	n;
		gnode_t	*g;
		
		for (n = 0, g = NULL; (! g) && (n < mcount); ++n)
			g = map_gfind (maps[n], pattern, len);
		if (g) {
			rc.data = g -> data;
			rc.dlen = g -> dlen;
		} else {
			rc.data = NULL;
			rc.dlen = 0;
		}
	}
	return rc;
}/*}}}*/
static bool_t
loc_match (loc_t *compare, loc_t *value, regex_t *rcompare) /*{{{*/
{
	if (compare -> data) {
		if (value -> data) {
			if (rcompare) {
				bool_t	rc = false;
				char	*scratch;
				
				if (scratch = malloc (value -> dlen + 1)) {
					if (value -> dlen)
						memcpy (scratch, value -> data, value -> dlen);
					scratch[value -> dlen] = '\0';
					if (regexec (rcompare, scratch, 0, NULL, 0) == 0)
						rc = true;
					free (scratch);
				}
				return rc;
			} else if ((compare -> dlen == value -> dlen) && ((compare -> dlen == 0) || (! memcmp (compare -> data, value -> data, compare -> dlen))))
				return true;
		}
		return false;
	} else
		return value -> data != NULL;
}/*}}}*/
static loc_t
evaluate (map_t **maps, int mcount, const xmlChar *name, int len) /*{{{*/
{
	loc_t		rc;
	const xmlChar	*temp;
	int		tlen;
	for (temp = name, tlen = len; tlen > 0; ++temp, --tlen)
		if ((*temp == ':') || (*temp == '?') || (*temp == '='))
			break;
	if (tlen > 0) {
		loc_t		compare;
		regex_t		*rcompare;
		xmlChar		what = *temp;

		compare.data = NULL;
		compare.dlen = 0;
		rcompare = NULL;
		rc = locate (maps, mcount, name, len - tlen);
		++temp;
		--tlen;
		if (what == '=') {
			compare.data = temp;
			compare.dlen = 0;
			while (tlen > 0) {
				if ((*temp == ':') || (*temp == '?'))
					break;
				--tlen;
				++temp;
				compare.dlen++;
			}
			if ((compare.dlen > 1) && (compare.data[0] == '~')) {
				char	*scratch;
				
				compare.dlen--;
				compare.data++;
				if (scratch = malloc (compare.dlen + 1)) {
					if (rcompare = malloc (sizeof (regex_t))) {
						memcpy (scratch, compare.data, compare.dlen);
						scratch[compare.dlen] = '\0';
						if (regcomp (rcompare, scratch, REG_EXTENDED | REG_NOSUB | REG_ICASE)) {
							free (rcompare);
							rcompare = NULL;
						}
					}
					free (scratch);
				}
			}
			if (tlen > 0) {
				what = *temp;
				++temp;
				--tlen;
			}
		}
		if (tlen > 0) {
			if (what == ':') {
				if (! loc_match (& compare, & rc, rcompare))
					rc = locate (maps, mcount, temp, tlen);
			} else if (what == '?') {
				if (tlen > 0) {
					const xmlChar	*branch;
					int		blen;
					xmlChar		quote;
			
					branch = temp;
					blen = 0;
					if ((*temp == '"') || (*temp == '\''))
						quote = *temp;
					else
						quote = '\0';
					while ((tlen > 0) && (quote || (*temp != ':'))) {
						++temp;
						--tlen;
						if (quote && (tlen > 1) && (*temp == quote) && (*(temp + 1) == ':'))
							quote = '\0';
						++blen;
					}
					if (tlen > 0) {
						++temp;
						--tlen;
					}
					if (loc_match (& compare, & rc, rcompare)) {
						if (blen > 0)
							rc = locate (maps, mcount, branch, blen);
					} else {
						if (tlen > 0)
							rc = locate (maps, mcount, temp, tlen);
					}
				}
			}
		}
		if (rcompare) {
			regfree (rcompare);
			free (rcompare);
		}
	} else
		rc = locate (maps, mcount, name, len);
	return rc;
}/*}}}*/
xmlBufferPtr
string_maps (const xmlBufferPtr src, map_t **maps, int mcount) /*{{{*/
{
	xmlBufferPtr	rc;
	const xmlChar	*sptr;
	
	rc = NULL;
	if (src && (sptr = xmlBufferContent (src))) {
		int	slen;
		int	n;
		
		slen = xmlBufferLength (src);
		if (rc = xmlBufferCreateSize (slen + 512)) {
			while (slen > 0)
				if ((slen > 3) && (*sptr == '%') && (*(sptr + 1) == '(')) {
					const xmlChar	*name;
					int		len;
					
					sptr += 2;
					slen -= 2;
					name = sptr;
					len = 0;
					while ((slen > 0) && (*sptr != ')')) {
						n = xmlCharLength (*sptr);
						sptr += n;
						slen -= n;
						len += n;
					}
					if ((slen > 0) && (*sptr == ')')) {
						loc_t	l;
						
						--slen;
						++sptr;
						l = evaluate (maps, mcount, name, len);
						if (l.dlen)
							xmlBufferAdd (rc, l.data, l.dlen);
					}
				} else {
					n = xmlCharLength (*sptr);
					xmlBufferAdd (rc, sptr, n);
					sptr += n;
					slen -= n;
				}
		}
	}
	return rc;
}/*}}}*/
xmlBufferPtr
string_mapv (const xmlBufferPtr src, va_list par) /*{{{*/
{
	map_t		**maps;
	int		msize, mcount;
	map_t		*temp;
	xmlBufferPtr	rc;
	
	maps = NULL;
	msize = 0;
	mcount = 0;
	while (temp = va_arg (par, map_t *)) {
		if (mcount >= msize) {
			msize += 8;
			if (! (maps = realloc (maps, msize * sizeof (map_t *)))) {
				mcount = 0;
				break;
			}
		}
		maps[mcount++] = temp;
	}
	rc = string_maps (src, maps, mcount);
	if (maps)
		free (maps);
	return rc;
}/*}}}*/
xmlBufferPtr
string_mapn (const xmlBufferPtr src, ...) /*{{{*/
{
	va_list		par;
	xmlBufferPtr	rc;

	va_start (par, src);
	rc = string_mapv (src, par);
	va_end (par);
	return rc;
}/*}}}*/
xmlBufferPtr
string_map (const xmlBufferPtr src, map_t *local, map_t *global) /*{{{*/
{
	xmlBufferPtr	rc;
	if (local && global)
		rc = string_mapn (src, local, global, NULL);
	else if (local)
		rc = string_mapn (src, local, NULL);
	else if (global)
		rc = string_mapn (src, global, NULL);
	else
		rc = string_mapn (src, NULL);
	return rc;
}/*}}}*/
map_t *
string_map_setup (void) /*{{{*/
{
	return map_alloc (MAP_Generic, 0);
}/*}}}*/
gnode_t *
string_map_addss (map_t *map, const char *key, const char *data) /*{{{*/
{
	return map_gadd (map, (const byte_t *) key, strlen (key), (const byte_t *) data, strlen (data));
}/*}}}*/
gnode_t *
string_map_addsb (map_t *map, const char *key, xmlBufferPtr data) /*{{{*/
{
	return map_gadd (map, (const byte_t *) key, strlen (key), (const byte_t *) xmlBufferContent (data), xmlBufferLength (data));
}/*}}}*/
gnode_t *
string_map_addsbuf (map_t *map, const char *key, const buffer_t *data) /*{{{*/
{
	return map_gadd (map, (const byte_t *) key, strlen (key), buffer_content (data), buffer_length (data));
}/*}}}*/
gnode_t *
string_map_addsi (map_t *map, const char *key, long data) /*{{{*/
{
	char	scratch[128];
	int	n;
	
	n = snprintf (scratch, sizeof (scratch) - 1, "%ld", data);
	return n == -1 ? NULL : map_gadd (map, (const byte_t *) key, strlen (key), (const byte_t *) scratch, n);
}/*}}}*/
void
string_map_done (map_t *map) /*{{{*/
{
	map_free (map);
}/*}}}*/
