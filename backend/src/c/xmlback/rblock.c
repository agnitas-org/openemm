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
# include	<stdlib.h>
# include	<string.h>
# include	"xmlback.h"

rblock_t *
rblock_alloc (tid_t tid, const char *bname, xmlBufferPtr content) /*{{{*/
{
	rblock_t	*r;
	
	if (r = (rblock_t *) malloc (sizeof (rblock_t))) {
		r -> tid = tid;
		r -> bname = NULL;
		r -> content = NULL;
		r -> next = NULL;
		if ((bname && (! rblock_set_name (r, bname))) ||
		    (content && (! rblock_set_content (r, content))))
			r = rblock_free (r);
	}
	return r;
}/*}}}*/
rblock_t *
rblock_free (rblock_t *r) /*{{{*/
{
	if (r) {
		if (r -> bname)
			free (r -> bname);
		if (r -> content)
			xmlBufferFree (r -> content);
		free (r);
	}
	return NULL;
}/*}}}*/
rblock_t *
rblock_free_all (rblock_t *r) /*{{{*/
{
	rblock_t	*tmp;
	
	while (tmp = r) {
		r = r -> next;
		rblock_free (tmp);
	}
	return NULL;
}/*}}}*/
bool_t
rblock_set_name (rblock_t *r, const char *bname) /*{{{*/
{
	if (r -> bname)
		free (r -> bname);
	r -> bname = bname ? strdup (bname) : NULL;
	return ((! bname) || r -> bname) ? true : false;
}/*}}}*/
static inline bool_t
copy (rblock_t *r, const byte_t *content, int length) /*{{{*/
{
	if (r -> content)
		xmlBufferEmpty (r -> content);
	else
		r -> content = xmlBufferCreate ();
	if (r -> content) {
		xmlBufferAdd (r -> content, content, length);
		if (xmlBufferLength (r -> content) != length) {
			xmlBufferFree (r -> content);
			r -> content = NULL;
		}
	}
	return r -> content ? true : false;
}/*}}}*/
bool_t
rblock_set_content (rblock_t *r, xmlBufferPtr content) /*{{{*/
{
	return copy (r, xmlBufferContent (content), xmlBufferLength (content));
}/*}}}*/
bool_t
rblock_retrieve_content (rblock_t *r, buffer_t *content) /*{{{*/
{
	return copy (r, content -> buffer, content -> length);
}/*}}}*/
