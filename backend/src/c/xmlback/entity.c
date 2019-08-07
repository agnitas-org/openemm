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
# include	"xmlback.h"

# include	"entity-mapping.h"

# define	mapsize		(sizeof (entity_map) / sizeof (entity_map[0]))

static inline const xmlChar *
entity_mapping (unsigned long coded, int *len) /*{{{*/
{
	int	low, high, diff, cur;
	
	for (low = 0, high = mapsize; low <= high; ) {
		cur = (low + high) >> 1;
		diff = entity_map[cur].code - coded;
		if (diff == 0) {
			*len = entity_map[cur].len;
			return (const xmlChar *) entity_map[cur].entity;
		} else if (diff > 0)
			high = cur - 1;
		else
			low = cur + 1;
	}
	return NULL;
}/*}}}*/
void
entity_replace (xmlBufferPtr in, xmlBufferPtr out, bool_t all) /*{{{*/
{
	const xmlChar	*content;
	const xmlChar	*ptr;
	int		len;
	int		pos;
	int		clen;
	int		start;
	unsigned long	coded;
	int		n;
	char		scratch[36];
	const xmlChar	*rplc;
	int		rlen;

	for (ptr = content = xmlBufferContent (in), len = xmlBufferLength (in), pos = 0, start = 0; pos <= len; ) {
		if (pos < len) {
			clen = xmlCharLength (*ptr);
			if (pos + clen > len)
				clen = 0;
		} else
			clen = 0;
		if (all || (clen != 1)) {
			if (pos > start)
				xmlBufferAdd (out, content + start, pos - start);
			if (clen > 0) {
				coded = 0;
				for (n = 0, coded = 0; n < clen; ++n)
					coded = (coded << 8) | *((unsigned char *) ptr + n);
				if (! (rplc = entity_mapping (coded, & rlen))) {
					if (clen > 1) {
						rlen = snprintf (scratch, sizeof (scratch), "&#%lu;", coded);
						rplc = (xmlChar *) scratch;
					} else {
						rlen = 1;
						rplc = ptr;
					}
				}
				xmlBufferAdd (out, rplc, rlen);
				start = pos + clen;
			} else
				clen = 1;
		}
		ptr += clen;
		pos += clen;
	}
}/*}}}*/
