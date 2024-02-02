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
# include	"xmlback.h"


url_t *
url_alloc (void) /*{{{*/
{
	url_t	*u;
	
	if (u = (url_t *) malloc (sizeof (url_t))) {
		u -> url_id = 0;
		u -> dest = NULL;
		u -> usage = 0;
		u -> admin_link = false;
		u -> orig = NULL;
		u -> resolved = NULL;
	}
	return u;
}/*}}}*/
url_t *
url_free (url_t *u) /*{{{*/
{
	if (u) {
		buffer_free (u -> dest);
		buffer_free (u -> orig);
		free (u);
	}
	return NULL;
}/*}}}*/
static bool_t
do_match (buffer_t *lnk, const xmlChar *check, int clen) /*{{{*/
{
	return lnk && lnk -> length == clen && (! memcmp (lnk -> buffer, check, clen));
}/*}}}*/
bool_t
url_match (url_t *u, const xmlChar *check, int clen) /*{{{*/
{
	return do_match (u -> dest, check, clen);
}/*}}}*/
static void
set_link (buffer_t **lnk, xmlBufferPtr url) /*{{{*/
{
	if (! url)
		*lnk = buffer_free (*lnk);
	else if (*lnk || (*lnk = buffer_alloc (xmlBufferLength (url) + 2)))
		buffer_set (*lnk, xmlBufferContent (url), xmlBufferLength (url));
}/*}}}*/
void
url_set_destination (url_t *u, xmlBufferPtr dest) /*{{{*/
{
	set_link (& u -> dest, dest);
}/*}}}*/
bool_t
url_match_original (url_t *u, const xmlChar *check, int clen) /*{{{*/
{
	return do_match (u -> orig, check, clen);
}/*}}}*/
void
url_set_original (url_t *u, xmlBufferPtr orig) /*{{{*/
{
	set_link (& u -> orig, orig);
}/*}}}*/
