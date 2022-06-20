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

counter_t *
counter_alloc (const char *mediatype, int subtype) /*{{{*/
{
	counter_t	*c;
	
	if (c = (counter_t *) malloc (sizeof (counter_t))) {
		c -> mediatype = strdup (mediatype);
		c -> subtype = subtype;
		c -> unitcount = 0;
		c -> unitskip = 0;
		c -> chunkcount = 0;
		c -> bytecount = 0;
		c -> bccunitcount = 0;
		c -> bccbytecount = 0;
		c -> next = NULL;
		if (! c -> mediatype)
			c = counter_free (c);
	}
	return c;
}/*}}}*/
counter_t *
counter_free (counter_t *c) /*{{{*/
{
	if (c) {
		if (c -> mediatype)
			free (c -> mediatype);
		free (c);
	}
	return NULL;
}/*}}}*/
counter_t *
counter_free_all (counter_t *c) /*{{{*/
{
	counter_t	*tmp;
	
	while (tmp = c) {
		c = c -> next;
		counter_free (tmp);
	}
	return NULL;
}/*}}}*/
