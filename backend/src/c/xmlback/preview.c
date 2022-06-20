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
# include	<string.h>
# include	"xmlback.h"

typedef struct { /*{{{*/
	char	*path;
	/*}}}*/
}	preview_t;
static preview_t *
preview_alloc (void) /*{{{*/
{
	preview_t	*p;
	
	if (p = (preview_t *) malloc (sizeof (preview_t))) {
		p -> path = NULL;
	}
	return p;
}/*}}}*/
static preview_t *
preview_free (preview_t *p) /*{{{*/
{
	if (p) {
		if (p -> path)
			free (p -> path);
		free (p);
	}
	return NULL;
}/*}}}*/
static bool_t
preview_set_output_path (preview_t *p, const char *path) /*{{{*/
{
	if (p -> path)
		free (p -> path);
	p -> path = path ? strdup (path) : NULL;
	return ((! path) || p -> path) ? true : false;
}/*}}}*/

void *
preview_oinit (blockmail_t *blockmail, var_t *opts) /*{{{*/
{
	preview_t	*pv;

	if (pv = preview_alloc ()) {
		var_t		*tmp;
		const char	*path;
		
		path = NULL;
		for (tmp = opts; tmp; tmp = tmp -> next)
			if ((! tmp -> var) || var_partial_imatch (tmp, "path"))
				path = tmp -> val;
		if ((! path) || (! preview_set_output_path (pv, path)))
			pv = preview_free (pv);
	}
	return pv;
}/*}}}*/
bool_t
preview_odeinit (void *data, blockmail_t *blockmail, bool_t success) /*{{{*/
{
	preview_t	*pv = (preview_t *) data;
	
	preview_free (pv);
	return true;
}/*}}}*/

static bool_t
make_pure_header (buffer_t *dest, const byte_t *content, long length, bool_t usecrlf) /*{{{*/
{
	bool_t	st = true;
	long	n = 0;
	long	start, end;
	
	while (st && (n < length)) {
		start = n;
		while ((n  < length) && (content[n] != '\r') && (content[n] != '\n'))
			++n;
		end = n;
		if (n < length && (content[n] == '\r'))
			++n;
		if (n < length && (content[n] == '\n'))
			++n;
		if ((start < end) && (content[start] == 'H')) {
			++start;
			if ((start + 2 < end) && (content[start] == '?')) {
				++start;
				while ((start < end) && (content[start] != '?'))
					++start;
				if ((start < end) && (content[start] == '?'))
					++start;
			}
			if (start < end)
				if ((! buffer_stiff (dest, content + start, end - start)) ||
				    (! (usecrlf ? buffer_stiffcrlf (dest) : buffer_stiffnl (dest))))
					st = false;
		}
	}
	return st;
}/*}}}*/
static bool_t
replace_crlf_by_nl (buffer_t *dest, const byte_t *content, long length) /*{{{*/
{
	bool_t	st = true;
	long	n = 0;
	long	start = 0;
					
	while (st && (n <= length))
		if ((n == length) || ((n + 1 < length) && (content[n] == '\r') && (content[n + 1] == '\n'))) {
			if (start < n)
				if (! buffer_stiff (dest, content + start, n - start))
					st = false;
			++n;
			start = n;
		} else
			++n;
	return st;
}/*}}}*/
bool_t
preview_owrite (void *data, blockmail_t *blockmail, receiver_t *rec) /*{{{*/
{
	preview_t	*pv = (preview_t *) data;
	bool_t		st = false;
	xmlDocPtr	doc;
	
	if (doc = xmlNewDoc (char2xml ("1.0"))) {
		xmlNodePtr	root;
		rblock_t	*run;
		buffer_t	*scratch;
		
		if (root = xmlNewNode (NULL, char2xml ("preview"))) {
			xmlDocSetRootElement (doc, root);
			st = true;
			scratch = buffer_alloc (65536);
			for (run = blockmail -> rblocks; st && run; run = run -> next)
				if (run -> bname && run -> content) {
					const char	*encode = NULL;
					const byte_t	*content = xmlBufferContent (run -> content);
					long		length = xmlBufferLength (run -> content);

					if (run -> tid == TID_Unspec) {
						if (scratch) {
							buffer_clear (scratch);
							st = encode_base64 (run -> content, scratch);
							content = scratch -> buffer;
							length = scratch -> length;
							encode = "base64";
						} else
							st = false;
					} else if ((run -> tid == TID_EMail_Head) || (! blockmail -> usecrlf)) {
						if (scratch) {
							buffer_clear (scratch);
							if (run -> tid == TID_EMail_Head)
								st = make_pure_header (scratch, content, length, blockmail -> usecrlf);
							else
								st = replace_crlf_by_nl (scratch, content, length);
							content = scratch -> buffer;
							length = scratch -> length;
						} else
							st = false;
					}
					if (st) {
						xmlNodePtr	node, text;
						
						if ((node = xmlNewNode (NULL, char2xml ("content"))) &&
						    xmlNewProp (node, char2xml ("name"), char2xml (run -> bname)) &&
						    ((! encode) || xmlNewProp (node, char2xml ("encode"), char2xml (encode)))) {
							xmlAddChild (root, node);
							if (text = xmlNewTextLen (content, length)) {
								xmlAddChild (node, text);
							} else
								st = false;
						} else
							st = false;
					}
				}
			if (scratch)
				buffer_free (scratch);
		}
		if (st && pv -> path) {
			if (xmlSaveFile (pv -> path, doc) == -1)
				st = false;
		} else
			st = false;
		xmlFreeDoc (doc);
	}
	return st;
}/*}}}*/
