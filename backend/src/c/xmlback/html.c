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

# define	isname(ccc)	((ccc) && (! isspace ((ccc))) && ((ccc) != '>'))

static bool_t
attr_match (html_t *h, attr_t *a, const char *name, int name_length) /*{{{*/
{
	if (a -> name.length == name_length) {
		int	n;
	
		for (n = 0; n < a -> name.length; ++n)
			if (name[n] != tolower (*(h -> chunk + a -> name.position + n)))
				break;
		if (n == name_length)
			return true;
	}
	return false;
}/*}}}*/
html_t *
html_alloc (void) /*{{{*/
{
	html_t	*h;
	
	if (h = (html_t *) malloc (sizeof (html_t))) {
		h -> chunk = NULL;
		h -> chunk_length = 0;
		h -> name.position = 0;
		h -> name.length = 0;
		h -> attr = NULL;
		h -> size = 0;
		h -> count = 0;
		h -> ev = NULL;
		h -> scratch = NULL;
		h -> matches = NULL;
		h -> msize = 0;
		h -> matched = false;
	}
	return h;
}/*}}}*/
html_t *
html_free (html_t *h) /*{{{*/
{
	if (h) {
		if (h -> attr)
			free (h -> attr);
		if (h -> ev)
			ev_free (h -> ev);
		if (h -> scratch)
			buffer_free (h -> scratch);
		if (h -> matches)
			free (h -> matches);
		free (h);
	}
	return NULL;
}/*}}}*/
bool_t
html_expression (html_t *h, blockmail_t *blockmail, const char *expression) /*{{{*/
{
	iflua_t		*ev;
	const char	*ptr;
	char		*global, *copy;
	
	if ((ptr = strstr (expression, "\n%%")) && ((*(ptr + 3) == '\n') || (*(ptr + 3) == '\r')) && (global = strdup (expression))) {
		copy = global + (ptr - expression + 1);
		*copy++ = '\0';
		++copy;
		while ((*copy == '\n') || (*copy == '\r'))
			++copy;
		expression = copy;
	} else
		global = NULL;
	if (ev = ev_html_alloc (blockmail, expression, global)) {
		if (h -> ev)
			ev_free (h -> ev);
		h -> ev = ev;
	}
	if (global)
		free (global);
	return ev ? true : false;
}/*}}}*/
static attr_t *
html_attr_request (html_t *h) /*{{{*/
{
	attr_t	*a;
	
	if (h -> count >= h -> size) {
		h -> size = h -> size ? h -> size * 2 : 16;
		if (! (h -> attr = (attr_t *) realloc (h -> attr, sizeof (attr_t) * h -> size))) {
			h -> attr = NULL;
			h -> count = 0;
			h -> size = 0;
			return NULL;
		}
	}
	a = h -> attr + h -> count++;
	a -> name.position = 0;
	a -> name.length = 0;
	a -> value.position = 0;
	a -> value.length = 0;
	return a;
}/*}}}*/
static attr_t *
html_attr_release (html_t *h) /*{{{*/
{
	if (h -> count > 0)
		h -> count--;
	return NULL;
}/*}}}*/
bool_t
html_parse (html_t *h, const xmlChar *chunk, int chunk_length) /*{{{*/
{
	const xmlChar	*ptr;
	int		n;
	int		state;
	attr_t		*cattr;
	char		quote;
	int		clen;
	char		ch;
	
	h -> chunk = chunk;
	h -> chunk_length = chunk_length;
	h -> name.position = 0;
	h -> name.length = 0;
	h -> count = 0;
	for (ptr = chunk, n = 0, state = 0, cattr = NULL, quote = '\0'; n < chunk_length; n += clen, ptr += clen) {
		clen = xmlCharLength (*ptr);
		ch = clen == 1 ? (char) *ptr : '\0';
		if (n + clen <= chunk_length) {
			if (state == 0) {
				state = 1;
				if (ch == '<')
					continue;
			}
			if ((state == 1) || (state == 4)) {
				if (isspace (ch))
					continue;
				++state;
			}
			if ((! quote) && (ch == '>')) {
				state = -1;
				continue;
			}
			if (state == 2) {
				state = 3;
				h -> name.position = n;
				h -> name.length++;
			} else if (state == 3) {
				if (isname (ch))
					h -> name.length++;
				else
					state = 4;
			} else if (state == 5) {
				if (! (cattr = html_attr_request (h)))
					break;
				cattr -> name.position = n;
				cattr -> name.length++;
				state = 6;
			} else if (state == 6) {
				if (ch == '=')
					state = 7;
				else if (isname (ch))
					cattr -> name.length++;
				else {
					cattr = html_attr_release (h);
					state = 4;
				}
			} else if (state == 7) {
				if ((ch == '"') || (ch == '\'')) {
					state = 10;
					quote = ch;
				} else if (isname (ch)) {
					state = 8;
					cattr -> value.position = n;
					cattr -> value.length++;
				} else
					state = 4;
			} else if (state == 8) {
				if (isname (ch))
					cattr -> value.length++;
				else
					state = 4;
			} else if (state == 10) {
				cattr -> value.position = n;
				if (ch != quote) {
					state = 11;
					cattr -> value.length++;
				} else {
					state = 4;
					quote = '\0';
				}
			} else if (state == 11) {
				if (ch != quote)
					cattr -> value.length++;
				else {
					state = 4;
					quote = '\0';
				}
			} else
				break;
		}
	}
	return n == chunk_length;
}/*}}}*/
const char *
html_norm (html_t *h, int start, int length, int *rlen) /*{{{*/
{
	const xmlChar	*ptr;
	
	if ((h -> scratch && buffer_size (h -> scratch, length + 1)) || (h -> scratch = buffer_alloc (length + 1))) {
		buffer_clear (h -> scratch);
		for (ptr = h -> chunk + start; length > 0; --length)
			buffer_stiffch (h -> scratch, tolower (*ptr++));
		*rlen = buffer_length (h -> scratch);
		return (const char *) buffer_content (h -> scratch);
	}
	return NULL;
}/*}}}*/
bool_t
html_match (html_t *h, receiver_t *rec) /*{{{*/
{
	if (h -> ev) {
		if (h -> msize < h -> size) {
			h -> msize = h -> size;
			if (! (h -> matches = (bool_t *) malloc (sizeof (bool_t) * h -> msize))) {
				h -> msize = 0;
				return false;
			}
		}
		if (h -> matches && h -> count)
			memset (h -> matches, 0, h -> count * sizeof (bool_t));
		h -> matched = false;
		return ev_html_evaluate (h -> ev, h, rec);
	}
	return false;
}/*}}}*/
void
html_set_pos (html_t *h, int position) /*{{{*/
{
	if ((position >= 0) && (position < h -> count) && (position < h -> msize)) {
		h -> matches[position] = true;
		h -> matched = true;
	}
}/*}}}*/
void
html_set_name (html_t *h, const char *name, int name_length) /*{{{*/
{
	int	n;
	
	for (n = 0; n < h -> count; ++n)
		if (attr_match (h, h -> attr + n, name, name_length))
			html_set_pos (h, n);
}/*}}}*/
