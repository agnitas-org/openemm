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

#define incr(xxx,lll)	do {								\
				if ((n = xmlValidPosition ((xxx), (lll))) == -1)	\
					return false;					\
				else {							\
					(xxx) += n;					\
					(lll) -= n;					\
				}							\
			} while (0)

bool_t
xmlSQLlike (const xmlChar *pattern, int plen,
	    const xmlChar *string, int slen,
	    const xmlChar *escape, int elen) /*{{{*/
{
	const xmlChar	*cur;
	int		n, sn;
	
	while ((plen > 0) && (slen > 0)) {
		cur = pattern;
		if (*cur == '_') {
			incr (pattern, plen);
			incr (string, slen);
		} else if (*cur == '%') {
			incr (pattern, plen);
			while ((plen > 0) && (*pattern == '%')) {
				cur = pattern;
				incr (pattern, plen);
			}
			if (! plen)
				return true;
			while (slen > 0) {
				if (xmlSQLlike (pattern, plen, string, slen, escape, elen))
					return true;
				incr (string, slen);
			}
		} else {
			if (escape && (plen >= elen) && (*cur == *escape) && (! memcmp (cur, escape, elen))) {
				pattern += elen;
				plen -= elen;
				cur = pattern;
			}
			incr (pattern, plen);
			if (((sn = xmlStrictCharLength (*string)) > slen) || (sn != n) || (n == 1 ? (*cur != *string) : memcmp (cur, string, n)))
				return false;
			incr (string, slen);
		}
	}
	if ((slen == 0) && (plen > 0))
		while ((plen > 0) && (*pattern == '%'))
			incr (pattern, plen);
	return plen == slen ? true : false;
}/*}}}*/
