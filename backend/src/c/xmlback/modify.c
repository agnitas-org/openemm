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
# include	<ctype.h>
# include	"xmlback.h"

# define	ST_INITIAL		0
# define	ST_PLAIN_START_FOUND	(-1)
# define	ST_QUOTED_START_FOUND	(-2)
# define	ST_END_FOUND		(-3)

static bool_t
transcode_url_for_content (blockmail_t *blockmail, xmlBufferPtr url, const char *uid) /*{{{*/
{
	bool_t		rc = false;
	const xmlChar	*ptr = xmlBufferContent (url);
	int		size = xmlBufferLength (url);
	int		state = 0;
	xmlChar		ch;
	
	buffer_clear (blockmail -> link_maker);
	while (size > 0) {
		ch = *ptr++;
		--size;
		if (state < 3) {
			if (ch == '/')
				++state;
		} else if (state == 3) {
			if (isalpha (ch)) {
				buffer_appendch (blockmail -> link_maker, ch);
				buffer_appendch (blockmail -> link_maker, '/');
				buffer_appends (blockmail -> link_maker, uid);
				buffer_appendch (blockmail -> link_maker, '/');
				rc = true;
			}
			++state;
		} else if ((ch == '?') || (ch == '&'))
			break;
		buffer_appendch (blockmail -> link_maker, ch);
	}
	return rc;
}/*}}}*/
static void
mkautourl (blockmail_t *blockmail, receiver_t *rec, block_t *block, url_t *url, record_t *record) /*{{{*/
{
	char	*uid;

	if (blockmail -> auto_url_is_dynamic && (uid = create_uid (blockmail, blockmail -> uid_version, blockmail -> auto_url_prefix, rec, url -> url_id))) {
		char	parameter_separator[2];

		parameter_separator[1] = '\0';
		if (blockmail -> rdir_content_links && transcode_url_for_content (blockmail, blockmail -> auto_url, uid)) {
			xmlBufferAdd (block -> out, buffer_content (blockmail -> link_maker), buffer_length (blockmail -> link_maker));
			parameter_separator[0] = '?';
		} else {
			xmlBufferAdd (block -> out, xmlBufferContent (blockmail -> auto_url), xmlBufferLength (blockmail -> auto_url));
			xmlBufferCCat (block -> out, "uid=");
			xmlBufferCCat (block -> out, uid);
			parameter_separator[0] = '&';
		}
		free (uid);
		if (record -> ids) {
			var_t		*temp;
			bool_t		first;
			const char	*ref;
			
			encrypt_build_reset (rec -> encrypt);
			for (temp = record -> ids, first = true; temp; temp = temp -> next) {
				if (first)
					first = false;
				else
					encrypt_build_add (rec -> encrypt, ",", 1);
				encrypt_build_add (rec -> encrypt, temp -> var, strlen (temp -> var));
				encrypt_build_add (rec -> encrypt, "=", 1);
				encrypt_build_add (rec -> encrypt, temp -> val, strlen (temp -> val));
			}
			if (ref = encrypt_build_do (rec -> encrypt, rec, 0)) {
				xmlBufferCCat (block -> out, parameter_separator);
				xmlBufferCCat (block -> out, "ref=");
				xmlBufferCCat (block -> out, ref);
				parameter_separator[0] = '&';
			}
		}
	} else
		xmlBufferAdd (block -> out, xmlBufferContent (blockmail -> auto_url), xmlBufferLength (blockmail -> auto_url));
}/*}}}*/
static const char *
mkonepixellogurl (blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	char	*uid;
	
	if (blockmail -> onepixel_url && (uid = create_uid (blockmail, blockmail -> uid_version, NULL, rec, 0))) {
		if ((! blockmail -> rdir_content_links) || (! transcode_url_for_content (blockmail, blockmail -> onepixel_url, uid))) {
			buffer_set (blockmail -> link_maker, xmlBufferContent (blockmail -> onepixel_url), xmlBufferLength (blockmail -> onepixel_url));
			buffer_appends (blockmail -> link_maker, "uid=");
			buffer_appends (blockmail -> link_maker, uid);
			if (blockmail -> gui)
				buffer_appends (blockmail -> link_maker, "&nocount=1");
		}
		free (uid);
		return buffer_length (blockmail -> link_maker) ? buffer_string (blockmail -> link_maker) : NULL;
	}
	return NULL;
}/*}}}*/
	
static bool_t
url_is_personal (const xmlChar *url, int len) /*{{{*/
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
static bool_t
replace_url (blockmail_t *blockmail, receiver_t *rec, block_t *block, record_t *record, const xmlChar *url, int url_length, int mask) /*{{{*/
{
	bool_t	changed;
	int	m;
	url_t	*match;
	int	first_orig = -1;

	changed = false;
	for (m = 0, match = NULL; m < blockmail -> url_count; ++m) {
		if (url_match (blockmail -> url[m], url, url_length)) {
			match = blockmail -> url[m];
			if (blockmail -> url[m] -> usage & mask)
				break;
		}
		if ((first_orig == -1) && blockmail -> url[m] -> orig) {
			first_orig = m;
		}
	}
	if ((match == NULL) && (first_orig != -1)) {
		for (m = first_orig; m < blockmail -> url_count; ++m) {
			if (url_match_original (blockmail -> url[m], url, url_length)) {
				match = blockmail -> url[m];
				if (blockmail -> url[m] -> usage & mask)
					break;
			}
		}
	}
	if (blockmail -> anon) {
		if (! blockmail -> anon_preserve_links) {
			if ((m == blockmail -> url_count) || blockmail -> url[m] -> admin_link) {
				xmlBufferAdd (block -> out, (const xmlChar *) "#", 1);
				changed = true;
			} else if (url_is_personal (url, url_length)) {
				if (purl_parsen (blockmail -> purl, url, url_length)) {
					const xchar_t	*rplc;
					int		rlen;
					rplc_t		replacer = { blockmail, rec };
							
					if ((rplc = purl_build (blockmail -> purl, NULL, & rlen, replace_anon_hashtags, & replacer)) && rlen)
						xmlBufferAdd (block -> out, (const xmlChar *) rplc, rlen);
					changed = true;
				}
			}
		}
	} else if (m < blockmail -> url_count) {
		mkautourl (blockmail, rec, block, blockmail -> url[m], record);
		changed = true;
	} else if (match && match -> resolved) {
		const xmlChar	*resolved_url = NULL;
		int		resolved_url_length = -1;
		buffer_t	*resolve;
				
		if (resolve = link_resolve_get (match -> resolved, blockmail, block, match, rec, record)) {
			resolved_url = resolve -> buffer;
			resolved_url_length = resolve -> length;
		}
		if (blockmail -> tracker) {
			if (! resolved_url) {
				resolved_url = url;
				resolved_url_length = url_length;
			}
			tracker_fill (blockmail -> tracker, blockmail, & resolved_url, & resolved_url_length);
		}
		if (resolved_url) {
			if (resolved_url_length > 0)
				xmlBufferAdd (block -> out, resolved_url, resolved_url_length);
			changed = true;
		}
	}
	return changed;
}/*}}}*/
static bool_t
replace_url_html (blockmail_t *blockmail, receiver_t *rec, block_t *block, record_t *record, const xmlChar *chunk, int chunk_length, int mask) /*{{{*/
{
	if (html_parse (blockmail -> html, chunk, chunk_length) && html_match (blockmail -> html, rec)) {
		int	lstore = 0;
		int	n;
		
		for (n = 0; n < blockmail -> html -> count; ++n)
			if (blockmail -> html -> matches[n]) {
				if (lstore < blockmail -> html -> attr[n].value.position) {
					xmlBufferAdd (block -> out, blockmail -> html -> chunk + lstore, blockmail -> html -> attr[n].value.position - lstore);
					lstore = blockmail -> html -> attr[n].value.position;
				}
				if (replace_url (blockmail, rec, block, record, blockmail -> html -> chunk + blockmail -> html -> attr[n].value.position, blockmail -> html -> attr[n].value.length, mask))
					lstore = blockmail -> html -> attr[n].value.position + blockmail -> html -> attr[n].value.length;
			}
		if (lstore < blockmail -> html -> chunk_length)
			xmlBufferAdd (block -> out, blockmail -> html -> chunk + lstore, blockmail -> html -> chunk_length - lstore);
		return true;
	}
	return false;
}/*}}}*/
static bool_t
modify_urls_enhanced (blockmail_t *blockmail, receiver_t *rec, block_t *block, protect_t *protect, bool_t ishtml, record_t *record) /*{{{*/
{
	int		n;
	int		len;
	const xmlChar	*cont;
	int		lstore;
	int		state;
	char		ch, quote;
	int		start, end;
	int		mask;
	int		clen;
	bool_t		changed;
	
	xmlBufferEmpty (block -> out);
	len = xmlBufferLength (block -> in);
	cont = xmlBufferContent (block -> in);
	lstore = 0;
	state = ST_INITIAL;
	quote = '\0';
	if (ishtml) {
		mask = (1 << 1);
	} else {
		mask = (1 << 0);
	}
	start = -1;
	end = -1;
	changed = false;
# define	CHK(ccc)	do { if ((ccc) == ch) ++state; else state = ST_INITIAL; } while (0)
# define	CCHK(ccc)	do { if ((ccc) == tolower (ch)) ++state; else state = ST_INITIAL; } while (0)
	for (n = 0; n < len; ) {
		clen = xmlCharLength (cont[n]);
# if	0
		if (protect) {
			if (n >= protect -> start) {
				if (n < protect -> end)
					n += clen;
				else 
					protect = protect -> next;
				state = ST_INITIAL;
				continue;
			}
		}
# else
		if (protect) {
			while (protect && (n >= protect -> end))
				protect = protect -> next;
			rec -> disable_link_extension = protect && n >= protect -> start;
		}
# endif		
		if (ishtml) {
			ch = clen == 1 ? (char) cont[n] : '\0';
			switch (state) {
			case ST_INITIAL:
				if (ch == '<') {
					start = n + 1;
					state = 1;
				}
				break;
			case 1:
				if (ch == '>')
					state = ST_INITIAL;
				else if (ch == '/')
					state = 2;
				else if (ch == '!')
					state = 3;
				else
					state = 10;
				break;
			case 2:
				if (ch == '>')
					state = ST_INITIAL;
				break;
			case 3:
				if (ch == '>')
					state = ST_INITIAL;
				else if (ch == '-')
					state = 4;
				else
					state = 10;
				break;
			case 4:
				if (ch == '>')
					state = ST_INITIAL;
				else if (ch == '-')
					state = 5;
				else
					state = 10;
				break;
			case 5:
				if (ch == '-')
					state = 6;
				break;
			case 6:
				if (ch == '-')
					state = 7;
				else
					state = 5;
				break;
			case 7:
				if (ch == '>')
					state = ST_INITIAL;
				else if (state != '-')
					state = 5;
				break;
			case 10:
				if ((ch == '"') || (ch == '\'')) {
					quote = ch;
					state = 11;
				} else if (ch == '>') {
					end = n;
					state = ST_INITIAL;
				}
				break;
			case 11:
				if (ch == quote) {
					quote = '\0';
					state = 10;
				}
				break;
			}
		} else {
			if ((clen > 1) || isascii ((char) cont[n])) {
				ch = clen == 1 ? (char) cont[n] : '\0';
				switch (state) {
				case ST_INITIAL:
					if (tolower (ch) == 'h') {
						state = 1;
						start = n;
					}
					break;
				/* plain: http:// and https:// */
				case 1:		CCHK ('t');	break;
				case 2:		CCHK ('t');	break;
				case 3:		CCHK ('p');	break;
				case 4:
					++state;
					if (tolower (ch) == 's')
						break;
					/* Fall through . . . */
				case 5:		CHK (':');	break;
				case 6:		CHK ('/');	break;
				case 7:
					if (ch == '/')
						++state;
					else
						state = ST_INITIAL;
					break;
				case 8:
					if (isspace (ch) || (ch == '>'))
						end = n;
					break;
				}
			} else if (start != -1)
				end = n;
		}
		if (end != -1) {
			if (lstore < start) {
				xmlBufferAdd (block -> out, cont + lstore, start - lstore);
				lstore = start;
			}
			if ((ishtml ? replace_url_html : replace_url) (blockmail, rec, block, record, cont + start, end - start, mask)) {
				changed = true;
				lstore = end;
			}
			start = -1;
			end = -1;
			state = ST_INITIAL;
		}
		n += clen;
	}
# undef		CHK
# undef		CCHK
	if ((! ishtml) && (start != -1) && (start < n)) {
		end = len;
		if (lstore < start) {
			xmlBufferAdd (block -> out, cont + lstore, start - lstore);
			lstore = start;
		}
		if (replace_url (blockmail, rec, block, record, cont + start, end - start, mask)) {
			changed = true;
			lstore = end;
		}
	}
	if (changed) {
		if (lstore < len)
			xmlBufferAdd (block -> out, cont + lstore, len - lstore);
		block_swap_inout (block);
	}
	rec -> disable_link_extension = false;
	return true;
}/*}}}*/
static bool_t
modify_urls_classic (blockmail_t *blockmail, receiver_t *rec, block_t *block, protect_t *protect, bool_t ishtml, record_t *record) /*{{{*/
{
	int		n;
	int		len;
	const xmlChar	*cont;
	int		lstore;
	int		state;
	char		ch, quote;
	int		start, end;
	int		mask;
	int		clen;
	bool_t		changed;
	purl_t		*scratch;
	
	scratch = NULL;
	xmlBufferEmpty (block -> out);
	len = xmlBufferLength (block -> in);
	cont = xmlBufferContent (block -> in);
	lstore = 0;
	state = ST_INITIAL;
	quote = '\0';
	if (ishtml) {
		mask = 2;
	} else {
		mask = 1;
	}
	start = -1;
	end = -1;
	changed = false;
	for (n = 0; n <= len; ) {
		if (n < len) {
			clen = xmlCharLength (cont[n]);
			if (protect) {
				if (n >= protect -> start) {
					if (n < protect -> end)
						n += clen;
					else 
						protect = protect -> next;
					continue;
				}
			}
			if ((clen > 1) || isascii ((char) cont[n])) {
				ch = clen == 1 ? (char) cont[n] : '\0';
				switch (state) {
				case ST_INITIAL:
					if (ishtml) {
						if (ch == '<') {
							state = blockmail -> relaxed_url_resolver ? 101 : 100;
						}
					} else if (strchr ("hm", tolower (ch))) {
						if (tolower (ch) == 'h') {
							state = 1;
						} else {
							state = 31;
						}
						start = n;
					}
					break;
# define	CHK(ccc)	do { if ((ccc) == ch) ++state; else state = ST_INITIAL; } while (0)
# define	CCHK(ccc)	do { if ((ccc) == tolower (ch)) ++state; else state = ST_INITIAL; } while (0)
					
				/* plain: http:// and https:// */
				case 1:		CCHK ('t');	break;
				case 2:		CCHK ('t');	break;
				case 3:		CCHK ('p');	break;
				case 4:
					++state;
					if (tolower (ch) == 's')
						break;
					/* Fall through . . . */
				case 5:		CHK (':');	break;
				case 6:		CHK ('/');	break;
				case 7:
					if (ch == '/')
						state = ST_PLAIN_START_FOUND;
					else
						state = ST_INITIAL;
					break;
				/* plain: mailto: */
				case 31:	CCHK ('a');	break;
				case 32:	CCHK ('i');	break;
				case 33:	CCHK ('l');	break;
				case 34:	CCHK ('t');	break;
				case 35:	CCHK ('o');	break;
				case 36:	CHK (':');	break;
				case 37:
					state = ST_PLAIN_START_FOUND;
					break;
				/* HTML */
				case 100:
					if ((tolower (ch) == 'a') || (tolower (ch) == 'v'))
						++state;
					else
						state = ST_INITIAL;
					break;
# define	HCHK(ccc)	do { if ((ccc) == tolower (ch)) ++state; else if ('>' == ch) state = ST_INITIAL; else state = 101; } while (0)
				case 101:	HCHK ('h');	break;
				case 102:	HCHK ('r');	break;
				case 103:	HCHK ('e');	break;
				case 104:	HCHK ('f');	break;
# undef		HCHK						
				case 105:	CHK ('=');	break;
				case 106:
					if ((ch == '"') || (ch == '\'')) {
						quote = ch;
						state = ST_QUOTED_START_FOUND;
						start = n + 1;
					} else {
						state = ST_PLAIN_START_FOUND;
						start = n;
					}
					break;
				case ST_PLAIN_START_FOUND:
					if (isspace (ch) || (ch == '>')) {
						end = n;
						state = ST_END_FOUND;
					}
					break;
				case ST_QUOTED_START_FOUND:
					if (isspace (ch) || (ch == quote)) {
						end = n;
						state = ST_END_FOUND;
					}
					break;
				default:
					log_out (blockmail -> lg, LV_ERROR, "modify_urls: invalid state %d at position %d", state, n);
					state = ST_INITIAL;
					break;
				}
# undef		CHK
# undef		CCHK					
			} else {
				if (state == ST_PLAIN_START_FOUND) {
					end = n;
					state = ST_END_FOUND;
				} else
					state = ST_INITIAL;
			}
			n += clen;
		} else {
			if (state == ST_PLAIN_START_FOUND) {
				end = n;
				state = ST_END_FOUND;
			}
			++n;
		}
		if (state == ST_END_FOUND) {
			int	ulen, m;
			url_t	*match;
			int	first_orig = -1;

			ulen = end - start;
			for (m = 0, match = NULL; m < blockmail -> url_count; ++m) {
				if (url_match (blockmail -> url[m], cont + start, ulen)) {
					match = blockmail -> url[m];
					if (blockmail -> url[m] -> usage & mask)
						break;
				}
				if ((first_orig == -1) && blockmail -> url[m] -> orig) {
					first_orig = m;
				}
			}
			if (lstore < start) {
				xmlBufferAdd (block -> out, cont + lstore, start - lstore);
				lstore = start;
			}
			if ((match == NULL) && (first_orig != -1)) {
				for (m = first_orig; m < blockmail -> url_count; ++m) {
					if (url_match_original (blockmail -> url[m], cont + start, ulen)) {
						match = blockmail -> url[m];
						if (blockmail -> url[m] -> usage & mask)
							break;
					}
				}
			}
			if (blockmail -> anon) {
				if (! blockmail -> anon_preserve_links) {
					if ((m == blockmail -> url_count) || blockmail -> url[m] -> admin_link) {
						xmlBufferAdd (block -> out, (const xmlChar *) "#", 1);
						lstore = end;
						changed = true;
					} else if (url_is_personal (cont + start, ulen)) {
						if (! scratch)
							scratch = purl_alloc (NULL);
						if (scratch && purl_parsen (scratch, cont + start, ulen)) {
							const xchar_t	*rplc;
							int		rlen;
							rplc_t		replacer = { blockmail, rec };
							
							if ((rplc = purl_build (scratch, NULL, & rlen, replace_anon_hashtags, & replacer)) && rlen)
								xmlBufferAdd (block -> out, (const xmlChar *) rplc, rlen);
							lstore = end;
							changed = true;
						}
					}
				}
			} else if (m < blockmail -> url_count) {
				mkautourl (blockmail, rec, block, blockmail -> url[m], record);
				lstore = end;
				changed = true;
			} else if (match && match -> resolved) {
				const xmlChar	*url = NULL;
				int		ulength = -1;
				buffer_t	*resolve;
				
				if (resolve = link_resolve_get (match -> resolved, blockmail, block, match, rec, record)) {
					url = resolve -> buffer;
					ulength = resolve -> length;
				}
				if (blockmail -> tracker) {
					if (! url) {
						url = cont + start;
						ulength = ulen;
					}
					tracker_fill (blockmail -> tracker, blockmail, & url, & ulength);
				}
				if (url) {
					if (ulength > 0)
						xmlBufferAdd (block -> out, url, ulength);
					lstore = end;
					changed = true;
				}
			}
			state = ST_INITIAL;
		}
	}
	if (changed) {
		if (lstore < len)
			xmlBufferAdd (block -> out, cont + lstore, len - lstore);
		block_swap_inout (block);
	}
	if (scratch)
		purl_free (scratch);
	return true;
}/*}}}*/
bool_t
modify_urls (blockmail_t *blockmail, receiver_t *rec, block_t *block, protect_t *protect, bool_t ishtml, record_t *record) /*{{{*/
{
	return (blockmail -> enhanced_url_resolver ? modify_urls_enhanced : modify_urls_classic) (blockmail, rec, block, protect, ishtml, record);
}/*}}}*/
static bool_t
collect_links (blockmail_t *blockmail, block_t *block, links_t *links) /*{{{*/
{
	bool_t		rc;
	int		n, clen;
	int		len;
	const xmlChar	*cont;
	int		start;
	
	rc = true;
	len = xmlBufferLength (block -> in);
	cont = xmlBufferContent (block -> in);
	for (n = 0; rc && (n < len); ) {
		clen = xmlCharLength (cont[n]);
		if ((clen == 1) && (cont[n] == 'h')) {
			start = n;
			++n;
			if ((n + 3 < len) && (cont[n] == 't') && (cont[n + 1] == 't') && (cont[n + 2] == 'p')) {
				n += 3;
				if ((n + 1 < len) && (cont[n] == 's'))
					++n;
				if ((n + 3 < len) && (cont[n] == ':') && (cont[n + 1] == '/') && (cont[n + 2] == '/')) {
					n += 3;
					while ((n < len) && (xmlCharLength (cont[n]) == 1) &&
					       (cont[n] != '"') && (cont[n] != '<') && (cont[n] != '>') &&
					       (! isspace (cont[n])))
						++n;
					rc = links_nadd (links, (const char *) (cont + start), n - start);
				}
			}
		} else
			n += clen;
	}
	return rc;
}/*}}}*/
static int
find_top (const xmlChar *cont, int len) /*{{{*/
{
	int		pos;
	int		state;
	int		n;
	int		clen;
	unsigned char	ch;

	for (pos = -1, state = 0, n = 0; (n < len) && (pos == -1); ) {
		clen = xmlCharLength (cont[n]);
		if (clen > 1)
			state = 0;
		else {
			ch = cont[n];

			switch (state) {
			case 0:
				if (ch == '<')
					state = 1;
				break;
			case 1:
				if ((ch == 'b') || (ch == 'B'))
					state = 2;
				else if (ch == '>')
					state = 0;
				else if (! isspace (ch))
					state = 100;
				break;
			case 2:
				if ((ch == 'o') || (ch == 'O'))
					state = 3;
				else if (ch == '>')
					state = 0;
				else
					state = 100;
				break;
			case 3:
				if ((ch == 'd') || (ch == 'D'))
					state = 4;
				else if (ch == '>')
					state = 0;
				else
					state = 100;
				break;
			case 4:
				if ((ch == 'y') || (ch == 'Y'))
					state = 5;
				else if (ch == '>')
					state = 0;
				else
					state = 100;
				break;
			case 5:
				if (ch == '>') {
					pos = n + clen;
					state = 0;
				} else if (isspace (ch))
					state = 6;
				else
					state = 100;
				break;
			case 6:
				if (ch == '>') {
					pos = n + clen;
					state = 0;
				}
# ifdef		STRICT				
				else if (ch == '"')
					state = 7;
				break;
			case 7:
				if (ch == '"')
					state = 6;
# endif		/* STRICT */
				break;
			case 100:
				if (ch == '>')
					state = 0;
				break;
			}
		}
		n += clen;
	}
	return pos;
}/*}}}*/
static int
find_bottom (const xmlChar *cont, int len) /*{{{*/
{
	int	pos;
	int	last;
	int	m;
	int	bclen;
	
	for (pos = -1, last = len, m = len - 1; (m >= 0) && (pos == -1); ) {
		bclen = xmlCharLength (cont[m]);
		if ((bclen == 1) && (cont[m] == '<')) {
			int		n;
			int		state;
			int		clen;
			unsigned char	ch;

			for (n = m + bclen, state = 1; (n < last) && (state > 0) && (state != 99); ) {
				clen = xmlCharLength (cont[n]);
				if (clen != 1)
					state = 0;
				else {
					ch = cont[n];
					switch (state) {
					case 1:
						if (ch == '/')
							state = 2;
						else if (! isspace (ch))
							state = 0;
						break;
					case 2:
						if ((ch == 'b') || (ch == 'B'))
							state = 3;
						else if (! isspace (ch))
							state = 0;
						break;
					case 3:
						if ((ch == 'o') || (ch == 'O'))
							state = 4;
						else
							state = 0;
						break;
					case 4:
						if ((ch == 'd') || (ch == 'D'))
							state = 5;
						else
							state = 0;
						break;
					case 5:
						if ((ch == 'y') || (ch == 'Y'))
							state = 6;
						else
							state = 0;
						break;
					case 6:
						if ((ch == '>') || isspace (ch))
							state = 99;
						else
							state = 0;
						break;
					}
				}
				n += clen;
			}
			if (state == 99)
				pos = m;
		} else if ((bclen == 1) && (cont[m] == '>'))
			last = m + bclen;
		m -= bclen;
	}
	return pos;
}/*}}}*/
static bool_t
update_html (blockmail_t *blockmail, receiver_t *rec, block_t *block, blockspec_t *bspec, block_t *preheader, block_t *clearance) /*{{{*/
{
	const char	*opx;
	opl_t		opl;
	bool_t		clr;

	opx = NULL;
	if ((! blockmail -> anon) && bspec) {
		opl = bspec -> opl;
		if (opl != OPL_None)
			if (! (opx = mkonepixellogurl (blockmail, rec)))
				return false;
		clr = blockmail -> status_field == 'T' && bspec -> clearance && clearance && xmlBufferLength (clearance -> in) > 0;
	} else {
		opl = OPL_None;
		clr = false;
	}
	if ((opl != OPL_None) || preheader || clr) {
		int		state;
		int		position;
		int		length;
		const xmlChar	*content;

		for (state = 0; state < 2; ++state) {
			position = -1;
			length = xmlBufferLength (block -> in);
			content = xmlBufferContent (block -> in);
			switch (state) {
			case 0:
				if ((opl == OPL_Top) || preheader) {
					position = find_top (content, length);
					if (position == -1)
						position = 0;
				}
				break;
			case 1:
				if ((opl == OPL_Bottom) || clr) {
					position = find_bottom (content, length);
					if (position == -1)
						position = length;
				}
				break;
			}
			if (position != -1) {
				xmlBufferEmpty (block -> out);
				if (position > 0)
					xmlBufferAdd (block -> out, content, position);
				if ((state == 0) && preheader) {
					xmlBufferAdd (block -> out, xmlBufferContent (preheader -> in), xmlBufferLength (preheader -> in));
				}
				if (((state == 0) && (opl == OPL_Top)) ||
				    ((state == 1) && (opl == OPL_Bottom))) {
					if (blockmail -> onepix_template) {
						map_t		*local = string_map_setup ();
						xmlBufferPtr	temp;
				
						string_map_addss (local, "link", opx);
						if (temp = string_mapn (blockmail -> onepix_template, local, rec -> smap, blockmail -> smap, NULL)) {
							xmlBufferAdd (block -> out, xmlBufferContent (temp), xmlBufferLength (temp));
							xmlBufferFree (temp);
						}
					} else {
						const xmlChar	lprefix[] = "<img src=\"";
						const xmlChar	lpostfix[] = "\" alt=\"\" border=\"0\" height=\"1\" width=\"1\"/>";
			
						xmlBufferAdd (block -> out, lprefix, sizeof (lprefix) - 1);
						xmlBufferCCat (block -> out, opx);
						xmlBufferAdd (block -> out, lpostfix, sizeof (lpostfix) - 1);
					}
				}
				if ((state == 1) && clr) {
					xmlBufferAdd (block -> out, xmlBufferContent (clearance -> in), xmlBufferLength (clearance -> in));
				}
				if (position < length)
					xmlBufferAdd (block -> out, content + position, length - position);
				block_swap_inout (block);
			}
		}
	}
	return true;
}/*}}}*/
static bool_t
convert_entities (blockmail_t *blockmail, block_t *block) /*{{{*/
{
	xmlBufferEmpty (block -> out);
	entity_replace (block -> in, block -> out, false);
	block_swap_inout (block);
	return true;
}/*}}}*/
static bool_t
add_vip_block (blockmail_t *blockmail, block_t *block) /*{{{*/
{
	bool_t		rc;
	int             len;
	int             pos;
	const xmlChar   *cont;
	
	rc = true;
	len = xmlBufferLength (block -> in);
	cont = xmlBufferContent (block -> in);
	pos = find_top (cont, len);
	if (pos == -1)
		pos = 0;
	xmlBufferEmpty (block -> out);
	if (pos > 0)
		xmlBufferAdd (block -> out, cont, pos);
	xmlBufferAdd (block -> out, xmlBufferContent (blockmail -> vip), xmlBufferLength (blockmail -> vip));
	if (pos < len)
		xmlBufferAdd (block -> out, cont + pos, len - pos);
	block_swap_inout (block);
	return rc;
}/*}}}*/
static
# ifdef		__OPTIMIZE__
inline
# endif		/* __OPTIMIZE__ */
bool_t
islink (const xmlChar *str, int len) /*{{{*/
{
	int	n, state, clen;
	
	for (n = 0, state = 1; (n < len) && state; ) {
		clen = xmlCharLength (str[n]);
		if (clen != 1)
			return false;
		switch (state) {
		default: /* should NEVER happen */
			return false;
		case 1:	/* check for http:// https:// and mailto: */
			if ((str[n] == 'h') || (str[n] == 'H'))
				++state;
			else if ((str[n] == 'm') || (str[n] == 'M'))
				state = 100;
			else
				return false;
			break;
		case 2:
			if ((str[n] == 't') || (str[n] == 'T'))
				++state;
			else
				return false;
			break;
		case 3:
			if ((str[n] == 't') || (str[n] == 'T'))
				++state;
			else
				return false;
			break;
		case 4:
			if ((str[n] == 'p') || (str[n] == 'P'))
				++state;
			else
				return false;
			break;
		case 5:
			if ((str[n] == 's') || (str[n] == 'S'))
				++state;
			else if (str[n] == ':')
				state += 2;
			else
				return false;
			break;
		case 6:
			if (str[n] == ':')
				++state;
			else
				return false;
			break;
		case 7:
			if (str[n] == '/')
				++state;
			else
				return false;
			break;
		case 8:
			if (str[n] == '/')
				state = 0;
			else
				return false;
			break;
		case 100:
			if ((str[n] == 'a') || (str[n] == 'A'))
				++state;
			else
				return false;
			break;
		case 101:
			if ((str[n] == 'i') || (str[n] == 'I'))
				++state;
			else
				return false;
			break;
		case 102:
			if ((str[n] == 'l') || (str[n] == 'L'))
				++state;
			else
				return false;
			break;
		case 103:
			if ((str[n] == 't') || (str[n] == 'T'))
				++state;
			else
				return false;
			break;
		case 104:
			if ((str[n] == 'o') || (str[n] == 'O'))
				++state;
			else
				return false;
			break;
		case 105:
			if (str[n] == ':')
				state = 0;
			else
				return false;
			break;
		}
		n += clen;
	}
	return ! state;
}/*}}}*/
static bool_t
modify_linelength (blockmail_t *blockmail, block_t *block, blockspec_t *bspec) /*{{{*/
{
# define	DOIT_NONE	(0)
# define	DOIT_RESET	(1 << 0)
# define	DOIT_NEWLINE	(1 << 1)
# define	DOIT_IGNORE	(1 << 2)
# define	DOIT_SKIP	(1 << 3)	
	int		n;
	int		len;
	const xmlChar	*cont;
	int		spos, slen;
	int		space, dash;
	int		spchr;
	int		inspace, spacecount;
	int		llen, wordstart;
	int		doit;
	int		skipcount;
	bool_t		changed;

	xmlBufferEmpty (block -> out);
	len = xmlBufferLength (block -> in);
	cont = xmlBufferContent (block -> in);
	spos = 0;
	space = -1;
	dash = -1;
	spchr = -1;
	inspace = 0;
	spacecount = 0;
	llen = 0;
	wordstart = 0;
	doit = DOIT_NONE;
	skipcount = 0;
	changed = false;
	for (n = 0; n < len; ) {
		if ((cont[n] == '\r') || (cont[n] == '\n')) {
			doit = DOIT_RESET;
			if (inspace && (spchr + inspace == llen)) {
				n = space;
				doit |= DOIT_NEWLINE | DOIT_IGNORE | DOIT_SKIP;
				skipcount = inspace + 1;
			}
		} else {
			if ((cont[n] == ' ') || (cont[n] == '\t')) {
				if (! inspace++) {
					space = n;
					spchr = llen;
				}
				spacecount = inspace;
			} else {
				if (inspace) {
					inspace = 0;
					wordstart = n;
				}
				if ((cont[n] == '-') && (llen > 2) &&
				    ((spchr == -1) || (llen - spchr > 2)) &&
				    (! islink (cont + wordstart, n - wordstart))) {
					dash = n;
				}
			}
			if (++llen >= bspec -> linelength)
				if ((space != -1) || (dash != -1)) {
					if (space > dash) {
						if (! inspace) {
							n = space;
							doit = DOIT_RESET | DOIT_NEWLINE | DOIT_IGNORE | DOIT_SKIP;
							skipcount = spacecount;
						}
					} else {
						n = dash;
						doit = DOIT_RESET | DOIT_NEWLINE;
					}
				}
		}
		if (! (doit & DOIT_IGNORE))
			n += xmlCharLength (cont[n]);
		if (doit) {
			slen = n - spos;
			if (slen > 0)
				xmlBufferAdd (block -> out, cont + spos, slen);
			if (doit & DOIT_NEWLINE) {
				xmlBufferAdd (block -> out, bspec -> linesep, bspec -> seplength);
				changed = true;
			}
			if (doit & DOIT_SKIP) {
				while ((skipcount-- > 0) && (n < len))
					n += xmlCharLength (cont[n]);
				changed = true;
			}
			spos = n;
			space = -1;
			dash = -1;
			spchr = -1;
			inspace = 0;
			spacecount = 0;
 			llen = 0;
			wordstart = n;
			doit = DOIT_NONE;
		}
	}
	if (changed) {
		if (spos < len)
			xmlBufferAdd (block -> out, cont + spos, len - spos);
		block_swap_inout (block);
	}
	return true;
# undef		DOIT_NONE
# undef		DOIT_RESET
# undef		DOIT_NEWLINE
# undef		DOIT_IGNORE
# undef		DOIT_SKIP
}/*}}}*/
bool_t
modify_output (blockmail_t *blockmail, receiver_t *rec, block_t *block, blockspec_t *bspec, block_t *preheader, block_t *clearance, links_t *links) /*{{{*/
{
	bool_t	rc;
	
	rc = true;
	if (rc &&
	    (block -> tid == TID_EMail_HTML) &&
	    links) {
		rc = collect_links (blockmail, block, links);
	}
	if (rc &&
	    (block -> tid == TID_EMail_HTML)) {
		rc = update_html (blockmail, rec, block, bspec, preheader, clearance);
	}
	if (rc &&
	    (block -> tid == TID_EMail_HTML) &&
	    blockmail -> convert_to_entities) {
		rc = convert_entities (blockmail, block);
	}
	if (rc &&
	    blockmail -> vip &&
	    (block -> tid == TID_EMail_HTML) &&
	    islower (rec -> user_type) &&
	    (tolower (blockmail -> status_field) == rec -> user_type)) {
		rc = add_vip_block (blockmail, block);
	}
	if (rc &&
	    (block -> tid == TID_EMail_Text) &&
	    bspec &&
	    (bspec -> linelength > 0) &&
	    bspec -> linesep) {
		rc = modify_linelength (blockmail, block, bspec);
	}
	return rc;
}/*}}}*/
