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
# include	"xmlback.h"

# ifndef	I
# define	I	/* */
# endif		/* I */

# ifndef	__MISC_C
# define	__MISC_C		1
I bool_t
xmlEqual (xmlBufferPtr p1, xmlBufferPtr p2) /*{{{*/
{
	int		len;
	const xmlChar	*c1, *c2;
	
	len = xmlBufferLength (p1);
	if ((len == xmlBufferLength (p2)) &&
	    (c1 = xmlBufferContent (p1)) &&
	    (c2 = xmlBufferContent (p2)) &&
	    ((! len) || (! memcmp (c1, c2, len * sizeof (xmlChar)))))
		return true;
	return false;
}/*}}}*/
I int
xmlCharLength (xmlChar ch) /*{{{*/
{
	extern int	xmlLengthtab[256];
	
	return xmlLengthtab[ch];
}/*}}}*/
I int
xmlStrictCharLength (xmlChar ch) /*{{{*/
{
	extern int	xmlStrictLengthtab[256];
	
	return xmlStrictLengthtab[ch];
}/*}}}*/
I int
xmlValidPosition (const xmlChar *str, int length) /*{{{*/
{
# define	VALID(ccc)	(((ccc) & 0xc0) == 0x80)
	int	len, n;
	
	if (((len = xmlStrictCharLength (*str)) > 0) && (length >= len))
		for (n = len; n > 1; ) {
			--n;
			if (! VALID (*(str + n))) {
				len = -1;
				break;
			}
		}
	else
		len = -1;
	return len;
# undef		VALID	
}/*}}}*/
I bool_t
xmlValid (const xmlChar *str, int length) /*{{{*/
{
	int	n;
	
	while (length > 0)
		if ((n = xmlValidPosition (str, length)) > 0) {
			str += n;
			length -= n;
		} else
			break;
	return length == 0 ? true : false;
}/*}}}*/
I char *
xml2string (xmlBufferPtr p) /*{{{*/
{
	int		len = xmlBufferLength (p);
	const xmlChar	*ptr = xmlBufferContent (p);
	char		*rc;
	
	if (rc = malloc (len + 1)) {
		if (len > 0)
			memcpy (rc, ptr, len);
		rc[len] = '\0';
	}
	return rc;
}/*}}}*/
I const char *
xml2char (const xmlChar *s) /*{{{*/
{
	return (const char *) s;
}/*}}}*/
I const xmlChar *
char2xml (const char *s) /*{{{*/
{
	return (const xmlChar *) s;
}/*}}}*/
I const char *
byte2char (const byte_t *b) /*{{{*/
{
	return (const char *) b;
}/*}}}*/
I int
xmlstrcmp (const xmlChar *s1, const char *s2) /*{{{*/
{
	return strcmp (xml2char (s1), s2);
}/*}}}*/
I int
xmlstrncmp (const xmlChar *s1, const char *s2, size_t n) /*{{{*/
{
	return strncmp (xml2char (s1), s2, n);
}/*}}}*/
I long
xml2long (xmlBufferPtr p) /*{{{*/
{
	int		len = xmlBufferLength (p);
	const xmlChar	*ptr = xmlBufferContent (p);
	char		scratch[128];
	
	if (len >= sizeof (scratch))
		len = sizeof (scratch) - 1;
	memcpy (scratch, ptr, len);
	scratch[len] = '\0';
	return strtol (scratch, NULL, 0);
}/*}}}*/
I void
entity_escape (xmlBufferPtr target, const xmlChar *source, int source_length) /*{{{*/
{
	int	clen;
					
	while (source_length > 0) {
		clen = xmlCharLength (*source);
		if (clen > 1) {
			xmlBufferAdd (target, source, clen);
		} else
			switch (*source) {
			default:
				xmlBufferAdd (target, source, clen);
				break;
			case '&':
				xmlBufferCCat (target, "&amp;");
				break;
			case '<':
				xmlBufferCCat (target, "&lt;");
				break;
			case '>':
				xmlBufferCCat (target, "&gt;");
				break;
			case '\'':
				xmlBufferCCat (target, "&apos;");
				break;
			case '"':
				xmlBufferCCat (target, "&quot;");
				break;
			}
		source += clen;
		source_length -= clen;
	}
}/*}}}*/
# endif		/* __MISC_C */
