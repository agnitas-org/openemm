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
		u -> has_hashtags = false;
		u -> orig = NULL;
		u -> resolved = NULL;
		u -> scratch = NULL;
	}
	return u;
}/*}}}*/
url_t *
url_free (url_t *u) /*{{{*/
{
	if (u) {
		buffer_free (u -> dest);
		buffer_free (u -> orig);
		buffer_free (u -> scratch);
		free (u);
	}
	return NULL;
}/*}}}*/
static bool_t
do_match (const buffer_t *lnk, const xmlChar *check, int clen) /*{{{*/
{
	return lnk && buffer_length (lnk) == clen && (! memcmp (buffer_content (lnk), check, clen));
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
static bool_t
contains_hashtags (const byte_t *url, int len) /*{{{*/
{
	bool_t	rc;
	int	n;
	int	state;
	int	pos, count;
	
	rc = false;
	for (n = 0, state = 0, pos = 0, count = 0; (! rc) && (n < len); ++n)
		switch (state) {
		case 0:
			if (url[n] == '?')
				state = 1;
			break;
		case 1:
			if (url[n] == '=') {
				state = 3;
				pos = 0;
				count = 0;
			}
			break;
		case 2:
			if (url[n] == '#')
				state = 5;
			else if (url[n] == '&')
				state = 1;
			break;
		case 3:
			if (url[n] == '.') {
				if (pos == 0)
					state = 2;
				else {
					pos = 0;
					if (++count == 4)
						state = 4;
				}
			} else if (isalnum (url[n]))
				++count;
			else if (url[n] == '#')
				state = 5;
			else
				state = 2;
			break;
		case 4:
			if (isalnum (url[n])) {
				state = -1;
				rc = true;
			} else
				state = 2;
			break;
		case 5:
			if (url[n] == '#') {
				state = 6;
				pos = 0;
			} else
				state = 2;
			break;
		case 6:
			if (url[n] == '#') {
				if (pos == 0)
					state = 2;
				else
					state = 7;
			} else
				++pos;
			break;
		case 7:
			if (url[n] == '#') {
				state = -1;
				rc = true;
			} else {
				state = 6;
				++pos;
			}
			break;
		}
	return rc;
}/*}}}*/
void
url_set_destination (url_t *u, xmlBufferPtr dest) /*{{{*/
{
	set_link (& u -> dest, dest);
	u -> has_hashtags = contains_hashtags (buffer_content (u -> dest), buffer_length (u -> dest));
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
static buffer_t *
url_output (url_t *url, buffer_t *output) /*{{{*/
{
	if (! output) {
		if ((! url -> scratch) && (! (url -> scratch = buffer_alloc (buffer_length (url -> dest) + 256))))
			return NULL;
		buffer_clear (url -> scratch);
		output = url -> scratch;
	}
	return output;
}/*}}}*/
static const buffer_t *
url_make (url_t *url, blockmail_t *blockmail, receiver_t *rec, record_t *record, buffer_t *output) /*{{{*/
{
	char	*uid;

	if (! (output = url_output (url, output)))
		return NULL;
	if (blockmail -> auto_url_is_dynamic && (uid = create_uid (blockmail, blockmail -> uid_version, blockmail -> auto_url_prefix, rec, url, false, NULL))) {
		char	parameter_separator;

		if (blockmail -> rdir_context_links && blockmail_transcode_url_for_content (blockmail, blockmail -> auto_url, uid)) {
			buffer_stiffbuf (output, blockmail -> link_maker);
			parameter_separator = '?';
		} else {
			buffer_stiff (output, xmlBufferContent (blockmail -> auto_url), xmlBufferLength (blockmail -> auto_url));
			buffer_stiffs (output, "uid=");
			buffer_stiffs (output, uid);
			parameter_separator = '&';
		}
		free (uid);
	} else
		buffer_stiff (output, xmlBufferContent (blockmail -> auto_url), xmlBufferLength (blockmail -> auto_url));
	return output;
}/*}}}*/
typedef struct { /*{{{*/
	blockmail_t	*blockmail;
	receiver_t	*receiver;
	/*}}}*/
}	rplc_t;
static bool_t
replace_anon_hashtags (void *rp, buffer_t *output, const xchar_t *token, int tlen) /*{{{*/
{
	rplc_t	*replacer = (rplc_t *) rp;
	int	pos;
	xchar_t	*param;
	
	for (pos = 0; pos < tlen; ++pos)
		if (token[pos] == ':')
			break;
	if ((pos < tlen) && (param = malloc (tlen - pos))) {
		if (pos + 1 < tlen)
			memcpy (param, token + pos + 1, tlen - pos - 1);
		param[tlen - pos - 1] = 0;
	} else {
		param = NULL;
	}
	if ((pos == 5) && (! memcmp (token, "PUBID", 5))) {
		char	*source = NULL;
		char	*opts = NULL;
		char	*pubid;
		
		if (param) {
			source = (char *) param;
			if (opts = strchr (source, ':')) {
				*opts++ = '\0';
			}
		}
		if (pubid = create_pubid (replacer -> blockmail, replacer -> receiver, source, opts)) {
			buffer_appends (output, pubid);
			free (pubid);
		}
	}
	if (param)
		free (param);
	return true;
}/*}}}*/
const buffer_t *
url_create (url_t *url, blockmail_t *blockmail, receiver_t *rec, record_t *record, buffer_t *output) /*{{{*/
{
	bool_t	changed = false;
	
	if (! (output = url_output (url, output)))
		return NULL;
	if (blockmail -> anon) {
		if (! blockmail -> anon_preserve_links) {
			if (url -> admin_link) {
				buffer_stiffch (output, '#');
				changed = true;
			} else if (url -> has_hashtags) {
				if (purl_parsen (blockmail -> purl, buffer_content (url -> dest), buffer_length (url -> dest))) {
					const xchar_t	*rplc;
					int		rlen;
					rplc_t		replacer = { blockmail, rec };
							
					if ((rplc = purl_build (blockmail -> purl, NULL, & rlen, replace_anon_hashtags, & replacer)) && rlen)
						buffer_stiff (output, rplc, rlen);
					changed = true;
				}
			}
		}
	} else if (url_make (url, blockmail, rec, record, output))
		changed = true;
	return changed ? output : NULL;
}/*}}}*/
