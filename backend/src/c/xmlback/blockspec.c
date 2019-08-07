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
# include	"xmlback.h"

blockspec_t *
blockspec_alloc (void) /*{{{*/
{
	blockspec_t	*b;
	
	if (b = (blockspec_t *) malloc (sizeof (blockspec_t))) {
		b -> nr = -1;
		b -> block = NULL;
		b -> prefix = fix_alloc ();
		DO_ZERO (b, postfix);
		b -> linelength = 0;
		b -> linesep = NULL;
		b -> seplength = 0;
		b -> opl = OPL_None;
		if (! b -> prefix)
			b = blockspec_free (b);
	}
	return b;
}/*}}}*/
blockspec_t *
blockspec_free (blockspec_t *b) /*{{{*/
{
	if (b) {
		if (b -> prefix)
			fix_free (b -> prefix);
		DO_FREE (b, postfix);
		if (b -> linesep)
			free (b -> linesep);
		free (b);
	}
	return NULL;
}/*}}}*/
bool_t
blockspec_set_lineseparator (blockspec_t *b, const xmlChar *sep, int slen) /*{{{*/
{
	bool_t	rc;
	
	rc = true;
	if (! sep) {
		if (b -> linesep) {
			free (b -> linesep);
			b -> linesep = NULL;
		}
		b -> seplength = 0;
	} else if (b -> linesep = (b -> linesep ? realloc (b -> linesep, slen) : malloc (slen))) {
		memcpy (b -> linesep, sep, slen);
		b -> seplength = slen;
	} else
		rc = false;
	return rc;
}/*}}}*/
bool_t
blockspec_find_lineseparator (blockspec_t *b) /*{{{*/
{
	const xmlChar	*sep;
	int		slen;
	int		len;
	const xmlChar	*cont;
	int		n;

	sep = NULL;
	slen = 0;
	if (b -> block && b -> block -> content) {
		len = xmlBufferLength (b -> block -> content);
		cont = xmlBufferContent (b -> block -> content);
		for (n = 0; n < len; )
			if ((cont[n] == '\r') || (cont[n] == '\n')) {
				sep = cont + n;
				slen = 1;
				++n;
				if (n < len)
					if (sep[0] == '\r') {
						if (cont[n] == '\n')
							slen++;
					} else if (sep[0] == '\n') {
						if (cont[n] == '\r')
							slen++;
					}
				break;
			} else
				n += xmlCharLength (cont[n]);
		if (! sep) {
			sep = char2xml ("\r\n");
			slen = 2;
		}
	}
	return blockspec_set_lineseparator (b, sep, slen);
}/*}}}*/
