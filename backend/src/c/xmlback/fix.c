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
# include	"xmlback.h"

fix_t *
fix_alloc (void) /*{{{*/
{
	fix_t	*f;
	
	if (f = (fix_t *) malloc (sizeof (fix_t))) {
		f -> cont = xmlBufferCreate ();
		f -> acont = xmlBufferCreate ();
		f -> dyn = false;
		f -> adyn = false;
		if (! (f -> cont && f -> acont))
			f = fix_free (f);
	}
	return f;
}/*}}}*/
fix_t *
fix_free (fix_t *f) /*{{{*/
{
	if (f) {
		if (f -> cont)
			xmlBufferFree (f -> cont);
		if (f -> acont)
			xmlBufferFree (f -> acont);
		free (f);
	}
	return NULL;
}/*}}}*/
static inline bool_t
scan_for_dyn (xmlBufferPtr buf) /*{{{*/
{
	if (buf) {
		const xmlChar	*ptr = xmlBufferContent (buf);
		int		len = xmlBufferLength (buf);
		int		n;
		
		while (len > 3) {
			n = xmlCharLength (*ptr);
			if ((n == 1) && (*ptr == '%') && (*(ptr + 1) == '('))
				return true;
			len -= n;
			ptr += n;
		}
	}
	return false;
}/*}}}*/
void
fix_scan_for_dynamic_content (fix_t *f) /*{{{*/
{
	f -> dyn = scan_for_dyn (f -> cont);
	f -> adyn = scan_for_dyn (f -> acont);
}/*}}}*/
