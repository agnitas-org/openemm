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
