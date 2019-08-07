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
/*	-*- mode: c; mode: fold -*-	*/
# include	<ctype.h>
# include	"xmlback.h"

static void
parse_buffer (columns_t *c, xmlBufferPtr s) /*{{{*/
{
	const xmlChar	*ptr = xmlBufferContent (s);
	int		len = xmlBufferLength (s);
	const xmlChar	*start;
	int		length;
	int		n;
	char		*name;
	
	while (len > 0) {
		while ((len > 0) && isspace (*ptr))
			++ptr, --len;
		if (len > 0) {
			start = ptr;
			while ((len > 0) && (*ptr != ','))
				++ptr, --len;
			length = ptr - start;
			if (length > 0) {
				for (n = 0; n < c -> count; ++n)
					if ((! strncmp (c -> names[n], (const char *) start, length)) && (! c -> names[n][length]))
						break;
				if (n == c -> count) {
					c -> size += c -> size ? c -> size : 8;
					if (! (c -> names = (char **) realloc (c -> names, c -> size * sizeof (char *)))) {
						c -> names = NULL;
						c -> count = 0;
						c -> size = 0;
						break;
					}
				}
				if (name = malloc (length + 1)) {
					strncpy (name, (const char *) start, length);
					name[length] = '\0';
					c -> names[c -> count++] = name;
				}
			}
			if (len > 0)
				++ptr, --len;
		}
	}
}/*}}}*/
columns_t *
columns_parse (xmlBufferPtr s) /*{{{*/
{
	columns_t	*c = NULL;
	
	if (s && (xmlBufferLength (s) > 0) && (c = columns_alloc ()))
		parse_buffer (c, s);
	return c;
}/*}}}*/
columns_t *
columns_update (columns_t *cur, xmlBufferPtr s) /*{{{*/
{
	if (s)
		if (! cur)
			cur = columns_parse (s);
		else if (xmlBufferLength (s) > 0)
			parse_buffer (cur, s);
	return cur;
}/*}}}*/
columns_t *
columns_alloc (void) /*{{{*/
{
	columns_t	*c;
	
	if (c = (columns_t *) malloc (sizeof (columns_t))) {
		c -> names = NULL;
		c -> size = 0;
		c -> count = 0;
		c -> indexes = NULL;
	}
	return c;
}/*}}}*/
columns_t *
columns_free (columns_t *c) /*{{{*/
{
	int	n;
			
	if (c) {
		if (c -> names) {
			for (n = 0; n < c -> count; ++n)
				free (c -> names[n]);
			free (c -> names);
		}
		if (c -> indexes)
			free (c -> indexes);
		free (c);
	}
	return NULL;
}/*}}}*/

