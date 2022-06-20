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

head_t *
head_alloc (void) /*{{{*/
{
	head_t	*h;
	
	if (h = (head_t *) malloc (sizeof (head_t)))
		if (h -> h = buffer_alloc (256))
			h -> next = NULL;
		else {
			free (h);
			h = NULL;
		}
	return h;
}/*}}}*/
head_t *
head_free (head_t *h) /*{{{*/
{
	if (h) {
		if (h -> h)
			buffer_free (h -> h);
		free (h);
	}
	return NULL;
}/*}}}*/
void
head_add (head_t *h, const char *str, int len) /*{{{*/
{
	buffer_appendsn (h -> h, str, len);
}/*}}}*/
void
head_trim (head_t *h) /*{{{*/
{
	if ((h -> h -> length > 0) && (h -> h -> buffer[h -> h -> length - 1] == '\n')) {
		h -> h -> length--;
		if ((h -> h -> length > 0) && (h -> h -> buffer[h -> h -> length - 1] == '\r'))
			h -> h -> length--;
	}
}/*}}}*/

bool_t
flatten_header (buffer_t *target, buffer_t *header, bool_t fold) /*{{{*/
{
	bool_t		rc;
	const byte_t	*head = buffer_content (header);
	int		hlen = buffer_length (header);
	const byte_t	*ptr;
	int		pos, len;

	rc = true;
	buffer_clear (target);
	pos = 0;
	while (rc && (pos < hlen)) {
		ptr = head + pos;
		while (pos < hlen && (head[pos] != '\n'))
			++pos;
		if (pos < hlen)
			++pos;
		len = (head + pos) - ptr;
		if (isspace (*ptr)) {
			if (fold && (buffer_length (target) > 0) && (buffer_peek (target, -1) == '\n')) {
				buffer_poke (target, -1, ' ');
				while ((len > 0) && isspace (*ptr))
					++ptr, --len;
			}
			if (len > 0)
				rc = buffer_append (target, ptr, len);
		} else if (*ptr == 'H') {
			++ptr, --len;
			if (*ptr == '?') {
				++ptr, --len;
				while ((len > 0) && (*ptr != '?'))
					++ptr, --len;
				if (len > 0)
					++ptr, --len;
			}
			rc = buffer_append (target, ptr, len);
		}
	}
	if (rc)
		rc = buffer_appendch (target, '\n');
	return rc;
}/*}}}*/
